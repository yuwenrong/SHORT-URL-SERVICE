package com.shorturl.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("short_url")
public class ShortUrl {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String shortCode;

    private String originalUrl;

    private LocalDateTime expireTime;

    private String password;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;

    /**
     * 过期标志: 0-有效 1-已过期
     */
    private Integer expireFlag;
}