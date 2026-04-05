package com.shorturl.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shorturl.entity.UrlStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface UrlStatsRepository extends BaseMapper<UrlStats> {

    @Select("SELECT * FROM url_stats WHERE short_code = #{shortCode} AND access_date = #{accessDate} LIMIT 1")
    UrlStats findByCodeAndDate(@Param("shortCode") String shortCode, @Param("accessDate") LocalDate accessDate);

    @Select("SELECT * FROM url_stats WHERE short_code = #{shortCode} ORDER BY access_date DESC LIMIT #{limit}")
    List<UrlStats> findByShortCode(@Param("shortCode") String shortCode, @Param("limit") int limit);

    @Update("UPDATE url_stats SET pv = pv + 1 WHERE short_code = #{shortCode} AND access_date = #{accessDate}")
    int incrementPv(@Param("shortCode") String shortCode, @Param("accessDate") LocalDate accessDate);

    @Update("UPDATE url_stats SET uv = uv + 1 WHERE short_code = #{shortCode} AND access_date = #{accessDate}")
    int incrementUv(@Param("shortCode") String shortCode, @Param("accessDate") LocalDate accessDate);
}