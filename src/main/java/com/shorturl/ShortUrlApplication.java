package com.shorturl;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@MapperScan("com.shorturl.repository")
@EnableCaching
public class ShortUrlApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShortUrlApplication.class, args);
    }
}