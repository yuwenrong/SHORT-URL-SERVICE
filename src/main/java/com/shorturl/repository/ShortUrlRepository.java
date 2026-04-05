package com.shorturl.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shorturl.entity.ShortUrl;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ShortUrlRepository extends BaseMapper<ShortUrl> {

    @Select("SELECT * FROM short_url WHERE short_code = #{shortCode} AND deleted = 0 LIMIT 1")
    ShortUrl findByShortCode(@Param("shortCode") String shortCode);

    @Select("SELECT * FROM short_url WHERE deleted = 0 ORDER BY create_time DESC LIMIT #{offset}, #{limit}")
    List<ShortUrl> findAll(@Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM short_url WHERE deleted = 0")
    long count();

    @Select("SELECT * FROM short_url WHERE expire_time < #{now} AND expire_flag = 0 AND deleted = 0")
    List<ShortUrl> findExpiredUrls(@Param("now") LocalDateTime now);
}