-- 创建数据库
CREATE DATABASE IF NOT EXISTS short_url DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE short_url;

-- 短链表
CREATE TABLE IF NOT EXISTS short_url (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    short_code VARCHAR(10) NOT NULL COMMENT '短码',
    original_url VARCHAR(2048) NOT NULL COMMENT '原始URL',
    expire_time DATETIME DEFAULT NULL COMMENT '过期时间',
    password VARCHAR(64) DEFAULT NULL COMMENT '访问密码(Bcrypt加密)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    expire_flag TINYINT DEFAULT 0 COMMENT '过期标志: 0-有效 1-已过期',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    UNIQUE KEY uk_short_code (short_code),
    INDEX idx_expire_time (expire_time),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='短链表';

-- 访问统计表
CREATE TABLE IF NOT EXISTS url_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    short_code VARCHAR(10) NOT NULL COMMENT '短码',
    access_date DATE NOT NULL COMMENT '访问日期',
    pv INT DEFAULT 0 COMMENT '点击次数',
    uv INT DEFAULT 0 COMMENT '独立访客数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_code_date (short_code, access_date),
    INDEX idx_short_code (short_code),
    INDEX idx_access_date (access_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访问统计表';

-- 初始化Redis计数器key
-- 后续可通过 SET short_url:id_generator 10000 初始化