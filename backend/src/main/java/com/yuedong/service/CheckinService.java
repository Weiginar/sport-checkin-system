package com.yuedong.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuedong.common.Constants;
import com.yuedong.common.Result;
import com.yuedong.entity.*;
import com.yuedong.mapper.CheckinRecordMapper;
import com.yuedong.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CheckinService {

    @Autowired
    private CheckinRecordMapper checkinRecordMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SportTypeService sportTypeService;
    @Autowired
    private PointsService pointsService;
    @Autowired
    private AchievementService achievementService;

    @Transactional
    public Result<?> doCheckin(Long userId, Long sportTypeId, Integer duration, Double distance, Double calories, String remark) {
        LocalDate today = LocalDate.now();

        // 1. 校验今日是否已打卡
        LambdaQueryWrapper<CheckinRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckinRecord::getUserId, userId).eq(CheckinRecord::getCheckinDate, today);
        if (checkinRecordMapper.selectOne(wrapper) != null) {
            return Result.error("今日已打卡，请勿重复打卡");
        }

        // 2. 校验运动项目
        SportType sportType = sportTypeService.getById(sportTypeId);
        if (sportType == null || sportType.getStatus() != 1) {
            return Result.error("运动项目不存在或已停用");
        }

        // 3. 校验时长
        if (duration == null || duration < 1 || duration > 600) {
            return Result.error("运动时长需在1-600分钟之间");
        }

        // 4. 自动计算卡路里
        BigDecimal caloriesBd;
        if (calories != null && calories > 0) {
            caloriesBd = BigDecimal.valueOf(calories);
        } else {
            caloriesBd = sportType.getCaloriesPerHour()
                    .multiply(BigDecimal.valueOf(duration))
                    .divide(BigDecimal.valueOf(60), 1, RoundingMode.HALF_UP);
        }

        // 5. 插入打卡记录
        CheckinRecord record = new CheckinRecord();
        record.setUserId(userId);
        record.setSportTypeId(sportTypeId);
        record.setCheckinDate(today);
        record.setDuration(duration);
        record.setDistance(distance != null ? BigDecimal.valueOf(distance) : BigDecimal.ZERO);
        record.setCalories(caloriesBd);
        record.setRemark(remark != null ? remark : "");

        // 6. 更新用户统计数据
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setTotalCheckins(user.getTotalCheckins() + 1);
        user.setTotalDuration(user.getTotalDuration() + duration);
        user.setTotalCalories(user.getTotalCalories().add(caloriesBd));

        // 计算连续打卡天数
        LocalDate lastCheckin = user.getLastCheckinDate();
        if (lastCheckin != null && lastCheckin.equals(today.minusDays(1))) {
            user.setConsecutiveDays(user.getConsecutiveDays() + 1);
        } else {
            user.setConsecutiveDays(1);
        }
        if (user.getConsecutiveDays() > user.getMaxConsecutive()) {
            user.setMaxConsecutive(user.getConsecutiveDays());
        }
        user.setLastCheckinDate(today);

        // 7. 发放基础积分
        int totalPointsEarned = Constants.BASE_CHECKIN_POINTS;
        pointsService.addRecord(userId, Constants.BASE_CHECKIN_POINTS, Constants.POINTS_TYPE_CHECKIN, "每日打卡奖励");

        // 8. 检查连续打卡奖励
        int consecutiveDays = user.getConsecutiveDays();
        if (consecutiveDays == 3) {
            pointsService.addRecord(userId, 5, Constants.POINTS_TYPE_CONSECUTIVE, "连续打卡3天奖励");
            totalPointsEarned += 5;
        } else if (consecutiveDays == 7) {
            pointsService.addRecord(userId, 20, Constants.POINTS_TYPE_CONSECUTIVE, "连续打卡7天奖励");
            totalPointsEarned += 20;
        } else if (consecutiveDays == 30) {
            pointsService.addRecord(userId, 100, Constants.POINTS_TYPE_CONSECUTIVE, "连续打卡30天奖励");
            totalPointsEarned += 100;
        }

        record.setPointsEarned(totalPointsEarned);
        checkinRecordMapper.insert(record);

        user.setPoints(user.getPoints() + totalPointsEarned);
        userMapper.updateById(user);

        // 9. 检查成就解锁
        List<Map<String, Object>> newAchievements = achievementService.checkAndUnlock(userId, user);

        // 10. 返回结果
        Map<String, Object> data = new HashMap<>();
        data.put("checkinId", record.getId());
        data.put("pointsEarned", totalPointsEarned);
        data.put("consecutiveDays", user.getConsecutiveDays());
        data.put("newAchievements", newAchievements);
        return Result.success("打卡成功", data);
    }

    public Result<?> getTodayStatus(Long userId) {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<CheckinRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckinRecord::getUserId, userId).eq(CheckinRecord::getCheckinDate, today);
        CheckinRecord record = checkinRecordMapper.selectOne(wrapper);

        Map<String, Object> data = new HashMap<>();
        if (record != null) {
            SportType sportType = sportTypeService.getById(record.getSportTypeId());
            data.put("checkedIn", true);
            Map<String, Object> recordMap = new HashMap<>();
            recordMap.put("id", record.getId());
            recordMap.put("sportTypeName", sportType != null ? sportType.getName() : "");
            recordMap.put("duration", record.getDuration());
            recordMap.put("distance", record.getDistance());
            recordMap.put("calories", record.getCalories());
            recordMap.put("remark", record.getRemark());
            recordMap.put("pointsEarned", record.getPointsEarned());
            recordMap.put("createTime", record.getCreateTime());
            data.put("record", recordMap);
        } else {
            data.put("checkedIn", false);
            data.put("record", null);
        }
        return Result.success(data);
    }

    public Result<?> getHistory(Long userId, int pageNum, int pageSize, String startDate, String endDate, Long sportTypeId) {
        Page<CheckinRecord> page = new Page<>(pageNum, pageSize);
        IPage<CheckinRecord> result = checkinRecordMapper.selectPageWithDetail(page, userId, sportTypeId, startDate, endDate);

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    public Result<?> getCalendar(Long userId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        List<LocalDate> dates = checkinRecordMapper.selectCheckinDates(userId, startDate, endDate);

        List<String> dateStrings = new ArrayList<>();
        for (LocalDate d : dates) {
            dateStrings.add(d.toString());
        }

        Map<String, Object> data = new HashMap<>();
        data.put("year", year);
        data.put("month", month);
        data.put("checkinDates", dateStrings);
        data.put("totalDays", dateStrings.size());
        return Result.success(data);
    }

    // 管理员：打卡记录列表
    public Result<?> adminCheckinList(int pageNum, int pageSize, Long userId, Long sportTypeId, String startDate, String endDate) {
        Page<CheckinRecord> page = new Page<>(pageNum, pageSize);
        IPage<CheckinRecord> result = checkinRecordMapper.selectAdminPageWithDetail(page, userId, sportTypeId, startDate, endDate);

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    // 管理员：删除打卡记录
    @Transactional
    public Result<?> adminDeleteCheckin(Long id) {
        CheckinRecord record = checkinRecordMapper.selectById(id);
        if (record == null) {
            return Result.error(404, "打卡记录不存在");
        }
        checkinRecordMapper.deleteById(id);
        return Result.success("删除成功", null);
    }
}
