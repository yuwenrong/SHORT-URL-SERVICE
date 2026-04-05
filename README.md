# 短链服务

高性能、易扩展的短链接生成服务，支持访问统计、密码保护、有效期管理。

## 技术栈

- Spring Boot 3.2
- MySQL 8.0
- Redis
- Caffeine (本地缓存)
- MyBatis Plus

## 快速开始

### 1. 初始化数据库

```sql
-- 执行 src/main/resources/schema.sql
```

### 2. 配置

修改 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/short_url
    username: root
    password: root

  data:
    redis:
      host: localhost
      port: 6379
```

### 3. 启动

```bash
mvn spring-boot:run
```

服务启动后访问 `http://localhost:8080`

---

## 短链生成规则

使用 **自增ID + 62进制编码** 规则，按需生成。

### 字符集

```
0-9 a-z A-Z (共62个字符)
```

### 编码示例

| 自增ID | 62进制编码 | 短码 |
|--------|-----------|------|
| 1 | 1 | 1 |
| 62 | 10 | 10 |
| 100 | 1C | 1C |
| 10000 | 2Bi | 2Bi |

### 生成流程

```
用户请求 → Redis自增ID → 62进制编码 → 补齐长度 → 存入MySQL → 返回短链
```

6位短码可容纳约 **568亿** 个唯一链接。

---

## 使用场景

| 场景 | 说明 |
|------|------|
| 营销推广 | 短信、邮件中发送短链，缩短URL长度 |
| 社交分享 | 微博、微信限制字数，短链更简洁 |
| 二维码 | 减少二维码数据量，提高识别率 |
| 电商推广 | 追踪推广效果，分析转化率 |
| 文档/简历 | 长链接不易记忆，短链更美观 |
| APP Deep Link | 简化Scheme或Universal Link |

---

## API接口

### 创建短链

```http
POST /api/short
Content-Type: application/json

{
    "originalUrl": "https://example.com/very/long/url",
    "expireTime": "2025-12-31T23:59:59",
    "password": "123456",
    "customCode": "mycode"
}
```

**参数说明**

| 参数 | 必填 | 说明 |
|------|------|------|
| originalUrl | 是 | 原始URL |
| expireTime | 否 | 过期时间，默认365天 |
| password | 否 | 访问密码 |
| customCode | 否 | 自定义短码 |

**响应**

```json
{
    "shortCode": "abc123",
    "shortUrl": "http://localhost:8080/abc123",
    "originalUrl": "https://example.com/very/long/url",
    "expireTime": "2025-12-31T23:59:59",
    "hasPassword": true,
    "createTime": "2024-01-01T10:00:00"
}
```

### 获取短链信息

```http
GET /api/short/{code}
```

### 获取访问统计

```http
GET /api/short/{code}/stats
```

### 短链列表（管理后台）

```http
GET /api/short/list?page=1&size=10
```

### 跳转访问

```http
GET /{code}
```

**带密码访问**

```http
GET /{code}?password=123456
```

---

## 缓存设计

### 本地缓存 (Caffeine)

- 容量：1000条
- 过期时间：60秒
- 用途：热点短链数据缓存

### Redis缓存

- **ID生成器**：`short_url:id_generator` 自增计数器
- **UV去重**：`short_url:uv:{code}:{date}` Set结构

### 缓存流程

```
请求 → Caffeine(60s) → Redis → MySQL
            ↑_______________|
          缓存未命中回源
```

---

## 项目结构

```
src/main/java/com/shorturl/
├── ShortUrlApplication.java      # 启动类
├── controller/
│   ├── ShortUrlController.java   # 短链API
│   └── RedirectController.java   # 跳转服务
├── service/
│   └── ShortUrlService.java      # 核心业务
├── repository/
│   ├── ShortUrlRepository.java
│   └── UrlStatsRepository.java
├── entity/
│   ├── ShortUrl.java
│   └── UrlStats.java
├── dto/
│   ├── CreateShortUrlRequest.java
│   ├── ShortUrlResponse.java
│   └── StatsResponse.java
├── config/
│   ├── RedisConfig.java
│   ├── CacheConfig.java
│   ├── GlobalExceptionHandler.java
│   └── SchedulerConfig.java
└── util/
    └── ShortCodeUtil.java        # 短码生成工具
```

---

## 配置说明

```yaml
short-url:
  domain: http://localhost:8080   # 短链域名
  code-length: 6                  # 短码长度
  default-expire-days: 365        # 默认有效期(天)
```