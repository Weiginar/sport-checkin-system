package com.yuedong.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface StatsMapper {
    int countTodayCheckins();
    int countActiveUsers();
    int countTodayNewUsers();
    double avgDailyCheckins();
    List<Map<String, Object>> dailyCheckinTrend(@Param("days") int days);
    List<Map<String, Object>> sportDistributionAll(@Param("days") int days);
    List<Map<String, Object>> userGrowthTrend(@Param("days") int days);
}
