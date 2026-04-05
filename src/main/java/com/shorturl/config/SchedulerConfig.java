package com.shorturl.config;

import com.shorturl.service.ShortUrlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulerConfig {

    private final ShortUrlService shortUrlService;

    /**
     * 每天凌晨2点清理过期短链
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredUrls() {
        log.info("开始清理过期短链...");
        try {
            shortUrlService.cleanExpiredUrls();
        } catch (Exception e) {
            log.error("清理过期短链失败", e);
        }
    }
}