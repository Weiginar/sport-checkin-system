package com.yuedong.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuedong.common.Constants;
import com.yuedong.common.Result;
import com.yuedong.entity.Achievement;
import com.yuedong.entity.User;
import com.yuedong.entity.UserAchievement;
import com.yuedong.mapper.AchievementMapper;
import com.yuedong.mapper.UserAchievementMapper;
import com.yuedong.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AchievementService {

    @Autowired
    private AchievementMapper achievementMapper;
    @Autowired
    private UserAchievementMapper userAchievementMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PointsService pointsService;

    public List<Map<String, Object>> checkAndUnlock(Long userId, User user) {
        List<Map<String, Object>> newAchievements = new ArrayList<>();

        // 获取所有成就
        List<Achievement> allAchievements = achievementMapper.selectList(
                new LambdaQueryWrapper<Achievement>().orderByAsc(Achievement::getSortOrder));

        // 获取用户已解锁的成就ID
        LambdaQueryWrapper<UserAchievement> uaWrapper = new LambdaQueryWrapper<>();
        uaWrapper.eq(UserAchievement::getUserId, userId);
        List<UserAchievement> unlockedList = userAchievementMapper.selectList(uaWrapper);
        Set<Long> unlockedIds = new HashSet<>();
        for (UserAchievement ua : unlockedList) {
            unlockedIds.add(ua.getAchievementId());
        }

        for (Achievement achievement : allAchievements) {
            if (unlockedIds.contains(achievement.getId())) {
                continue;
            }

            boolean conditionMet = false;
            switch (achievement.getConditionType()) {
                case Constants.CONDITION_TOTAL_CHECKINS:
                    conditionMet = user.getTotalCheckins() >= achievement.getConditionValue();
                    break;
                case Constants.CONDITION_CONSECUTIVE:
                    conditionMet = user.getConsecutiveDays() >= achievement.getConditionValue();
                    break;
                case Constants.CONDITION_TOTAL_DURATION:
                    conditionMet = user.getTotalDuration() >= achievement.getConditionValue();
                    break;
            }

            if (conditionMet) {
                // 解锁成就
                UserAchievement ua = new UserAchievement();
                ua.setUserId(userId);
                ua.setAchievementId(achievement.getId());
                userAchievementMapper.insert(ua);

                // 发放成就积分
                if (achievement.getRewardPoints() > 0) {
                    pointsService.addRecord(userId, achievement.getRewardPoints(),
                            Constants.POINTS_TYPE_ACHIEVEMENT, "解锁成就：" + achievement.getName());
                    user.setPoints(user.getPoints() + achievement.getRewardPoints());
                    userMapper.updateById(user);
                }

                Map<String, Object> achievementMap = new HashMap<>();
                achievementMap.put("name", achievement.getName());
                achievementMap.put("icon", achievement.getIcon());
                achievementMap.put("rewardPoints", achievement.getRewardPoints());
                newAchievements.add(achievementMap);
            }
        }

        return newAchievements;
    }

    public Result<?> getAchievementList(Long userId) {
        List<Achievement> allAchievements = achievementMapper.selectList(
                new LambdaQueryWrapper<Achievement>().orderByAsc(Achievement::getSortOrder));

        LambdaQueryWrapper<UserAchievement> uaWrapper = new LambdaQueryWrapper<>();
        uaWrapper.eq(UserAchievement::getUserId, userId);
        List<UserAchievement> unlockedList = userAchievementMapper.selectList(uaWrapper);
        Map<Long, UserAchievement> unlockedMap = new HashMap<>();
        for (UserAchievement ua : unlockedList) {
            unlockedMap.put(ua.getAchievementId(), ua);
        }

        for (Achievement achievement : allAchievements) {
            UserAchievement ua = unlockedMap.get(achievement.getId());
            if (ua != null) {
                achievement.setUnlocked(true);
                achievement.setUnlockTime(ua.getUnlockTime());
            } else {
                achievement.setUnlocked(false);
                achievement.setUnlockTime(null);
            }
        }

        return Result.success(allAchievements);
    }
}
