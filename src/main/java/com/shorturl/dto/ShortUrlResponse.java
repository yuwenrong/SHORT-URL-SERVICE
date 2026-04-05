package com.shorturl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrlResponse {

    private String shortCode;

    private String shortUrl;

    private String originalUrl;

    private LocalDateTime expireTime;

    private Boolean hasPassword;

    private LocalDateTime createTime;
}