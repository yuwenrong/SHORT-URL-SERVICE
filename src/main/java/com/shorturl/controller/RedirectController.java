package com.shorturl.controller;

import com.shorturl.entity.ShortUrl;
import com.shorturl.service.ShortUrlService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final ShortUrlService shortUrlService;

    /**
     * 短链跳转
     * 支持密码保护
     */
    @GetMapping("/{code}")
    public ResponseEntity<?> redirect(
            @PathVariable String code,
            @RequestParam(required = false) String password,
            HttpServletRequest request) {

        ShortUrl shortUrl = shortUrlService.getByCode(code);

        if (shortUrl == null) {
            return ResponseEntity.notFound().build();
        }

        // 检查是否过期
        if (shortUrl.getExpireTime() != null && shortUrl.getExpireTime().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.GONE)
                    .body(Map.of("error", "链接已过期"));
        }

        // 检查密码
        if (!shortUrlService.verifyPassword(shortUrl, password)) {
            Map<String, Object> result = new HashMap<>();
            result.put("requirePassword", true);
            result.put("message", "请输入访问密码");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }

        // 生成访客ID（用于UV统计）
        String visitorId = getOrCreateVisitorId(request);

        // 记录访问统计
        shortUrlService.recordAccess(code, visitorParams(request));

        // 返回原始URL
        return ResponseEntity.ok(Map.of(
                "originalUrl", shortUrl.getOriginalUrl(),
                "redirect", true
        ));
    }

    /**
     * 验证密码后跳转
     */
    @PostMapping("/{code}/verify")
    public ResponseEntity<?> verifyPassword(
            @PathVariable String code,
            @RequestBody Map<String, String> body) {

        String password = body.get("password");
        ShortUrl shortUrl = shortUrlService.getByCode(code);

        if (shortUrl == null) {
            return ResponseEntity.notFound().build();
        }

        if (!shortUrlService.verifyPassword(shortUrl, password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "密码错误"));
        }

        // 记录访问统计
        shortUrlService.recordAccess(code, null);

        return ResponseEntity.ok(Map.of("originalUrl", shortUrl.getOriginalUrl()));
    }

    private String getOrCreateVisitorId(HttpServletRequest request) {
        String visitorId = (String) request.getSession(true).getAttribute("visitor_id");
        if (visitorId == null) {
            visitorId = UUID.randomUUID().toString();
            request.getSession(true).setAttribute("visitor_id", visitorId);
        }
        return visitorId;
    }

    private String visitorParams(HttpServletRequest request) {
        // 简单实现：使用IP + UserAgent作为访客标识
        String ip = getClientIp(request);
        String ua = request.getHeader("User-Agent");
        return ip + "_" + (ua != null ? ua.hashCode() : "");
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}