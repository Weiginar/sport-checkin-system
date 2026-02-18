package com.yuedong.controller;

import com.yuedong.common.Constants;
import com.yuedong.common.Result;
import com.yuedong.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private SportTypeService sportTypeService;
    @Autowired
    private CheckinService checkinService;
    @Autowired
    private PointsService pointsService;
    @Autowired
    private StatsService statsService;

    // 权限校验
    private Result<?> checkAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!Constants.ROLE_ADMIN.equals(role)) {
            return Result.error(403, "无管理员权限");
        }
        return null;
    }

    // ===== 用户管理 =====
    @GetMapping("/user/list")
    public Result<?> userList(HttpServletRequest request,
                              @RequestParam(defaultValue = "1") int pageNum,
                              @RequestParam(defaultValue = "10") int pageSize,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) Integer status) {
        Result<?> check = checkAdmin(request);
        if (check != null) return check;
        return userService.adminUserList(pageNum, pageSize, keyword, status);
    }

    @PutMapping("/user/status")
    public Result<?> updateUserStatus(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Result<?> check = checkAdmin(request);
        if (check != null) return check;
        Long userId = Long.parseLong(params.get("userId").toString());
        Integer status = Integer.parseInt(params.get("status").toString());
        return userService.updateUserStatus(userId, status);
    }

    // ===== 运动项目管理 =====
    @GetMapping("/sport/list")
    public Result<?> sportList(HttpServletRequest request,
                               @RequestParam(defaultValue = "1") int pageNum,
                               @RequestParam(defaultValue = "10") int pageSize) {
        Result<?> check = checkAdmin(request);
        if (check != null) return check;
        return sportTypeService.adminList(pageNum, pageSize);
    }

    @PostMapping("/sport/add")
    public Result<?> sportAdd(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Result<?> check = checkAdmin(request);
        if (check != null) return check;
        return sportTypeService.add(
                params.get("name").toString(),
                params.get("icon") != null ? params.get("icon").toString() : null,
                new BigDecimal(params.get("caloriesPerHour").toString()),
                params.get("description") != null ? params.get("description").toString() : null,
                params.get("sortOrder") != null ? Integer.parseInt(params.get("sortOrder").toString()) : null,
                params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null
        );
    }

    @PutMapping("/sport/update")
    public Result<?> sportUpdate(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Result<?> check = checkAdmin(request);
        if (check != null) return check;
        return sportTypeService.update(
                Long.parseLong(params.get("id").toString()),
                params.get("name") != null ? params.get("name").toString() : null,
                params.get("icon") != null ? params.get("icon").toString() : null,
                params.get("caloriesPerHour") != null ? new BigDecimal(params.get("caloriesPerHour").toString()) : null,
                params.get("description") != null ? params.get("description").toString() : null,
                params.get("sortOrder") != null ? Integer.parseInt(params.get("sortOrder").toString()) : null,
                params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null
        );
    }

    @DeleteMapping("/sport/delete/{id}")
    public Result<?> sportDelete(HttpServletRequest request, @PathVariable Long id) {
        Result<?> check = checkAdmin(request);
        if (check != null) return check;
        return sportTypeService.delete(id);
    }

    // ===== 打卡记录管理 =====
    @GetMapping("/checkin/list")
    public Result<?> checkinList(HttpServletRequest request,
                                 @RequestParam(defaultValue = "1") int pageNum,
                                 @RequestParam(defaultValue = "10") int pageSize,
                                 @RequestParam(required = false) Long userId,
                                 @RequestParam(required = false) Long sportTypeId,
                                 @RequestParam(required = false) String startDate,
                                 @RequestParam(required = false) String endDate) {
        Result<?> check = checkAdmin(request);
        if (check != null) return check;
        return checkinService.adminCheckinList(pageNum, pageSize, userId, sportTypeId, startDate, endDate);
    }

    @DeleteMapping("/checkin/delete/{id}")
    public Result<?> checkinDelete(HttpServletRequest request, @PathVariable Long id) {
        Result<?> check = checkAdmin(request);
        if (check != null) return check;
        return checkinService.adminDeleteCheckin(id);
    }

    // ===== 积分记录管理 =====
    @GetMapping("/points/list")
    public Result<?> pointsList(HttpServletRequest request,
                                @RequestParam(defaultValue = "1") int pageNum,
                                @RequestParam(defaultValue = "10") int pageSize,
                                @RequestParam(required = false) Long userId,
                                @RequestParam(required = false) String type) {
        Result<?> check = checkAdmin(request);
        if (check != null) return check;
        return pointsService.adminPointsList(pageNum, pageSize, userId, type);
    }

    // ===== 系统统计 =====
    @GetMapping("/stats/overview")
    public Result<?> statsOverview(HttpServletRequest request) {
        Result<?> check = checkAdmin(request);
        if (check != null) return check;
        return statsService.getOverview();
    }

    @GetMapping("/stats/trend")
    public Result<?> statsTrend(HttpServletRequest request,
                                @RequestParam(defaultValue = "30") int days) {
        Result<?> check = checkAdmin(request);
        if (check != null) return check;
        return statsService.getTrend(days);
    }
}
