package com.shorturl.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("url_stats")
public class UrlStats {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String shortCode;

    private LocalDate accessDate;

    private Integer pv;

    private Integer uv;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}