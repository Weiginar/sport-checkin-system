package com.yuedong.controller;

import com.yuedong.common.Result;
import com.yuedong.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<?> register(@RequestBody Map<String, String> params) {
        return userService.register(
                params.get("username"),
                params.get("password"),
                params.get("nickname")
        );
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> params) {
        return userService.login(params.get("username"), params.get("password"));
    }

    @GetMapping("/profile")
    public Result<?> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.getProfile(userId);
    }

    @PutMapping("/profile")
    public Result<?> updateProfile(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        String nickname = params.get("nickname") != null ? params.get("nickname").toString() : null;
        String avatar = params.get("avatar") != null ? params.get("avatar").toString() : null;
        String phone = params.get("phone") != null ? params.get("phone").toString() : null;
        Integer gender = params.get("gender") != null ? Integer.parseInt(params.get("gender").toString()) : null;
        return userService.updateProfile(userId, nickname, avatar, phone, gender);
    }
}
