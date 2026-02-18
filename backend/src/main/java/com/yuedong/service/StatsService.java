package com.yuedong.service;

import com.yuedong.common.Result;
import com.yuedong.entity.User;
import com.yuedong.mapper.CheckinRecordMapper;
import com.yuedong.mapper.StatsMapper;
import com.yuedong.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class StatsService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CheckinRecordMapper checkinRecordMapper;
    @Autowired
    private StatsMapper statsMapper;

    public Result<?> getPersonalStats(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(6);
        LocalDate monthAgo = today.minusDays(29);

        // 近7天每日运动时长
        List<Map<String, Object>> weeklyDuration = checkinRecordMapper.selectWeeklyDuration(userId, weekAgo, today);

        // 运动类型分布
        List<Map<String, Object>> sportDistribution = checkinRecordMapper.selectSportDistribution(userId);

        // 近30天趋势
        List<Map<String, Object>> recentTrend = checkinRecordMapper.selectRecentTrend(userId, monthAgo, today);

        Map<String, Object> data = new HashMap<>();
        data.put("totalCheckins", user.getTotalCheckins());
        data.put("consecutiveDays", user.getConsecutiveDays());
        data.put("maxConsecutive", user.getMaxConsecutive());
        data.put("totalDuration", user.getTotalDuration());
        data.put("totalCalories", user.getTotalCalories());
        data.put("points", user.getPoints());
        data.put("weeklyDuration", weeklyDuration);
        data.put("sportDistribution", sportDistribution);
        data.put("recentTrend", recentTrend);
        return Result.success(data);
    }

    public Result<?> getRanking(Long userId, String type) {
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        if ("month".equals(type)) {
            startDate = today.with(TemporalAdjusters.firstDayOfMonth());
        } else {
            startDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        }

        List<Map<String, Object>> rankList = checkinRecordMapper.selectRanking(startDate, today);

        // 添加排名序号
        int rank = 1;
        Map<String, Object> myRank = null;
        for (Map<String, Object> item : rankList) {
            item.put("rank", rank);
            Object uid = item.get("userId");
            if (uid != null && Long.parseLong(uid.toString()) == userId) {
                myRank = new HashMap<>();
                myRank.put("rank", rank);
                myRank.put("checkinCount", item.get("checkinCount"));
            }
            rank++;
        }

        if (myRank == null) {
            myRank = new HashMap<>();
            myRank.put("rank", 0);
            myRank.put("checkinCount", 0);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("rankList", rankList);
        data.put("myRank", myRank);
        return Result.success(data);
    }

    // 管理员：系统总览
    public Result<?> getOverview() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", userMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>().eq(User::getRole, "USER")));
        data.put("todayCheckins", statsMapper.countTodayCheckins());
        data.put("totalCheckins", userMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>().eq(User::getRole, "USER")).stream().mapToInt(u -> u.getTotalCheckins() != null ? u.getTotalCheckins() : 0).sum());
        data.put("activeUsers", statsMapper.countActiveUsers());
        data.put("todayNewUsers", statsMapper.countTodayNewUsers());
        data.put("avgDailyCheckins", statsMapper.avgDailyCheckins());
        return Result.success(data);
    }

    // 管理员：趋势分析
    public Result<?> getTrend(int days) {
        if (days <= 0 || days > 90) days = 30;
        Map<String, Object> data = new HashMap<>();
        data.put("dailyCheckins", statsMapper.dailyCheckinTrend(days));
        data.put("sportDistribution", statsMapper.sportDistributionAll(days));
        data.put("userGrowth", statsMapper.userGrowthTrend(days));
        return Result.success(data);
    }
}
