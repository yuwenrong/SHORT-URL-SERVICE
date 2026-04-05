package com.shorturl.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateShortUrlRequest {

    @NotBlank(message = "原始URL不能为空")
    @Pattern(regexp = "^https?://.*", message = "URL格式不正确")
    private String originalUrl;

    /**
     * 过期时间（可选）
     */
    private LocalDateTime expireTime;

    /**
     * 访问密码（可选）
     */
    private String password;

    /**
     * 自定义短码（可选）
     */
    private String customCode;
}