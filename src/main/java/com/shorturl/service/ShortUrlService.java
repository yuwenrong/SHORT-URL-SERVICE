package com.shorturl.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.bcrypt.BCrypt;
import com.shorturl.dto.CreateShortUrlRequest;
import com.shorturl.dto.ShortUrlResponse;
import com.shorturl.dto.StatsResponse;
import com.shorturl.entity.ShortUrl;
import com.shorturl.entity.UrlStats;
import com.shorturl.repository.ShortUrlRepository;
import com.shorturl.repository.UrlStatsRepository;
import com.shorturl.util.ShortCodeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShortUrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final UrlStatsRepository urlStatsRepository;
    private final ShortCodeUtil shortCodeUtil;
    private final StringRedisTemplate redisTemplate;

    @Value("${short-url.domain}")
    private String domain;

    @Value("${short-url.code-length:6}")
    private int codeLength;

    @Value("${short-url.default-expire-days:365}")
    private int defaultExpireDays;

    private static final String ID_GENERATOR_KEY = "short_url:id_generator";

    /**
     * 创建短链
     */
    @Transactional(rollbackFor = Exception.class)
    public ShortUrlResponse createShortUrl(CreateShortUrlRequest request) {
        String shortCode;

        // 如果提供了自定义短码，直接使用
        if (StrUtil.isNotBlank(request.getCustomCode())) {
            shortCode = request.getCustomCode().toLowerCase();
            // 检查是否已存在
            ShortUrl existing = shortUrlRepository.findByShortCode(shortCode);
            if (existing != null) {
                throw new RuntimeException("短码已存在: " + shortCode);
            }
        } else {
            // 使用Redis自增生成ID
            Long id = redisTemplate.opsForValue().increment(ID_GENERATOR_KEY);
            if (id == null) {
                // 初始化
                redisTemplate.opsForValue().set(ID_GENERATOR_KEY, "10000");
                id = redisTemplate.opsForValue().increment(ID_GENERATOR_KEY);
            }
            shortCode = shortCodeUtil.encodeWithPadding(id, codeLength);
        }

        // 处理密码
        String encodedPassword = null;
        if (StrUtil.isNotBlank(request.getPassword())) {
            encodedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt());
        }

        // 处理过期时间
        LocalDateTime expireTime = request.getExpireTime();
        if (expireTime == null) {
            expireTime = LocalDateTime.now().plusDays(defaultExpireDays);
        }

        // 保存短链
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setShortCode(shortCode);
        shortUrl.setOriginalUrl(request.getOriginalUrl());
        shortUrl.setExpireTime(expireTime);
        shortUrl.setPassword(encodedPassword);
        shortUrl.setExpireFlag(0);
        shortUrl.setCreateTime(LocalDateTime.now());

        shortUrlRepository.insert(shortUrl);

        return buildResponse(shortUrl);
    }

    /**
     * 根据短码获取短链信息
     */
    @Cacheable(value = "shortUrl", key = "#shortCode", unless = "#result == null")
    public ShortUrl getByCode(String shortCode) {
        return shortUrlRepository.findByShortCode(shortCode.toLowerCase());
    }

    /**
     * 验证密码
     */
    public boolean verifyPassword(ShortUrl shortUrl, String password) {
        if (shortUrl.getPassword() == null) {
            return true;
        }
        if (password == null) {
            return false;
        }
        return BCrypt.checkpw(password, shortUrl.getPassword());
    }

    /**
     * 记录访问统计
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordAccess(String shortCode, String visitorId) {
        LocalDate today = LocalDate.now();

        // 查找今天的统计记录
        UrlStats stats = urlStatsRepository.findByCodeAndDate(shortCode, today);

        if (stats == null) {
            // 新建记录
            stats = new UrlStats();
            stats.setShortCode(shortCode);
            stats.setAccessDate(today);
            stats.setPv(1);
            stats.setUv(1);
            urlStatsRepository.insert(stats);
        } else {
            // 更新PV
            urlStatsRepository.incrementPv(shortCode, today);
            // 更新UV（简单去重，实际生产可用Redis Set）
            if (visitorId != null) {
                String uvKey = "short_url:uv:" + shortCode + ":" + today;
                Boolean isNew = redisTemplate.opsForSet().add(uvKey, visitorId) > 0;
                if (Boolean.TRUE.equals(isNew)) {
                    urlStatsRepository.incrementUv(shortCode, today);
                }
            }
        }
    }

    /**
     * 获取访问统计
     */
    public StatsResponse getStats(String shortCode) {
        List<UrlStats> dailyStats = urlStatsRepository.findByShortCode(shortCode, 30);

        int totalPv = dailyStats.stream().mapToInt(UrlStats::getPv).sum();
        int totalUv = dailyStats.stream().mapToInt(UrlStats::getUv).sum();

        List<StatsResponse.DailyStats> statsList = dailyStats.stream()
                .map(s -> StatsResponse.DailyStats.builder()
                        .date(s.getAccessDate())
                        .pv(s.getPv())
                        .uv(s.getUv())
                        .build())
                .collect(Collectors.toList());

        return StatsResponse.builder()
                .shortCode(shortCode)
                .totalPv(totalPv)
                .totalUv(totalUv)
                .dailyStats(statsList)
                .build();
    }

    /**
     * 分页获取短链列表
     */
    public List<ShortUrl> list(int page, int size) {
        int offset = (page - 1) * size;
        return shortUrlRepository.findAll(offset, size);
    }

    /**
     * 获取短链总数
     */
    public long count() {
        return shortUrlRepository.count();
    }

    /**
     * 清理过期短链
     */
    @Transactional(rollbackFor = Exception.class)
    public void cleanExpiredUrls() {
        List<ShortUrl> expiredUrls = shortUrlRepository.findExpiredUrls(LocalDateTime.now());
        for (ShortUrl url : expiredUrls) {
            url.setExpireFlag(1);
            shortUrlRepository.updateById(url);
        }
        log.info("清理了 {} 条过期短链", expiredUrls.size());
    }

    private ShortUrlResponse buildResponse(ShortUrl shortUrl) {
        return ShortUrlResponse.builder()
                .shortCode(shortUrl.getShortCode())
                .shortUrl(domain + "/" + shortUrl.getShortCode())
                .originalUrl(shortUrl.getOriginalUrl())
                .expireTime(shortUrl.getExpireTime())
                .hasPassword(shortUrl.getPassword() != null)
                .createTime(shortUrl.getCreateTime())
                .build();
    }
}