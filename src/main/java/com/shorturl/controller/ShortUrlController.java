package com.shorturl.controller;

import com.shorturl.dto.CreateShortUrlRequest;
import com.shorturl.dto.ShortUrlResponse;
import com.shorturl.dto.StatsResponse;
import com.shorturl.entity.ShortUrl;
import com.shorturl.service.ShortUrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/short")
@RequiredArgsConstructor
public class ShortUrlController {

    private final ShortUrlService shortUrlService;

    /**
     * 创建短链
     */
    @PostMapping
    public ResponseEntity<ShortUrlResponse> createShortUrl(@Valid @RequestBody CreateShortUrlRequest request) {
        ShortUrlResponse response = shortUrlService.createShortUrl(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取短链信息
     */
    @GetMapping("/{code}")
    public ResponseEntity<ShortUrlResponse> getShortUrl(@PathVariable String code) {
        ShortUrl shortUrl = shortUrlService.getByCode(code);
        if (shortUrl == null) {
            return ResponseEntity.notFound().build();
        }

        ShortUrlResponse response = ShortUrlResponse.builder()
                .shortCode(shortUrl.getShortCode())
                .shortUrl(shortUrl.getOriginalUrl())
                .originalUrl(shortUrl.getOriginalUrl())
                .expireTime(shortUrl.getExpireTime())
                .hasPassword(shortUrl.getPassword() != null)
                .createTime(shortUrl.getCreateTime())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 获取访问统计
     */
    @GetMapping("/{code}/stats")
    public ResponseEntity<StatsResponse> getStats(@PathVariable String code) {
        StatsResponse stats = shortUrlService.getStats(code);
        return ResponseEntity.ok(stats);
    }

    /**
     * 分页获取短链列表（管理后台）
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<ShortUrl> list = shortUrlService.list(page, size);
        long total = shortUrlService.count();

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);

        return ResponseEntity.ok(result);
    }
}