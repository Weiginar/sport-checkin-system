package com.yuedong.controller;

import com.yuedong.common.Result;
import com.yuedong.service.AchievementService;
import com.yuedong.service.PointsService;
import com.yuedong.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class StatsController {

    @Autowired
    private StatsService statsService;
    @Autowired
    private PointsService pointsService;
    @Autowired
    private AchievementService achievementService;

    @GetMapping("/stats/personal")
    public Result<?> personalStats(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return statsService.getPersonalStats(userId);
    }

    @GetMapping("/stats/ranking")
    public Result<?> ranking(HttpServletRequest request,
                             @RequestParam(defaultValue = "week") String type) {
        Long userId = (Long) request.getAttribute("userId");
        return statsService.getRanking(userId, type);
    }

    @GetMapping("/points/list")
    public Result<?> pointsList(HttpServletRequest request,
                                @RequestParam(defaultValue = "1") int pageNum,
                                @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = (Long) request.getAttribute("userId");
        return pointsService.getUserPointsList(userId, pageNum, pageSize);
    }

    @GetMapping("/achievement/list")
    public Result<?> achievementList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return achievementService.getAchievementList(userId);
    }
}
