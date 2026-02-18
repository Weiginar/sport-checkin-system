package com.yuedong.controller;

import com.yuedong.common.Result;
import com.yuedong.service.CheckinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/checkin")
public class CheckinController {

    @Autowired
    private CheckinService checkinService;

    @PostMapping("/do")
    public Result<?> doCheckin(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        Long sportTypeId = Long.parseLong(params.get("sportTypeId").toString());
        Integer duration = Integer.parseInt(params.get("duration").toString());
        Double distance = params.get("distance") != null ? Double.parseDouble(params.get("distance").toString()) : null;
        Double calories = params.get("calories") != null ? Double.parseDouble(params.get("calories").toString()) : null;
        String remark = params.get("remark") != null ? params.get("remark").toString() : null;
        return checkinService.doCheckin(userId, sportTypeId, duration, distance, calories, remark);
    }

    @GetMapping("/today")
    public Result<?> today(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return checkinService.getTodayStatus(userId);
    }

    @GetMapping("/history")
    public Result<?> history(HttpServletRequest request,
                             @RequestParam(defaultValue = "1") int pageNum,
                             @RequestParam(defaultValue = "10") int pageSize,
                             @RequestParam(required = false) String startDate,
                             @RequestParam(required = false) String endDate,
                             @RequestParam(required = false) Long sportTypeId) {
        Long userId = (Long) request.getAttribute("userId");
        return checkinService.getHistory(userId, pageNum, pageSize, startDate, endDate, sportTypeId);
    }

    @GetMapping("/calendar")
    public Result<?> calendar(HttpServletRequest request,
                              @RequestParam int year,
                              @RequestParam int month) {
        Long userId = (Long) request.getAttribute("userId");
        return checkinService.getCalendar(userId, year, month);
    }
}
