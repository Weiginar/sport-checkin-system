package com.yuedong.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuedong.common.Constants;
import com.yuedong.common.Result;
import com.yuedong.entity.User;
import com.yuedong.mapper.UserMapper;
import com.yuedong.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    public Result<?> register(String username, String password, String nickname) {
        // 校验用户名格式
        if (username == null || !username.matches("^[a-zA-Z0-9]{4,20}$")) {
            return Result.error("用户名需为4-20位字母或数字");
        }
        if (password == null || password.length() < 6 || password.length() > 20) {
            return Result.error("密码需为6-20位");
        }
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        if (userMapper.selectOne(wrapper) != null) {
            return Result.error("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname != null && !nickname.isEmpty() ? nickname : username);
        user.setRole(Constants.ROLE_USER);
        user.setPoints(0);
        user.setTotalCheckins(0);
        user.setConsecutiveDays(0);
        user.setMaxConsecutive(0);
        user.setTotalDuration(0);
        user.setTotalCalories(java.math.BigDecimal.ZERO);
        user.setStatus(1);
        userMapper.insert(user);

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("role", user.getRole());
        return Result.success("注册成功", data);
    }

    public Result<?> login(String username, String password) {
        if (username == null || password == null) {
            return Result.error("用户名和密码不能为空");
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            return Result.error("用户名不存在");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Result.error("密码错误");
        }
        if (user.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("role", user.getRole());
        userInfo.put("points", user.getPoints());
        userInfo.put("totalCheckins", user.getTotalCheckins());
        userInfo.put("consecutiveDays", user.getConsecutiveDays());
        data.put("user", userInfo);

        return Result.success("登录成功", data);
    }

    public Result<?> getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.success(user);
    }

    public Result<?> updateProfile(Long userId, String nickname, String avatar, String phone, Integer gender) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        if (nickname != null) user.setNickname(nickname);
        if (avatar != null) user.setAvatar(avatar);
        if (phone != null) user.setPhone(phone);
        if (gender != null) user.setGender(gender);
        userMapper.updateById(user);
        return Result.success("更新成功", user);
    }

    // 管理员：用户列表
    public Result<?> adminUserList(int pageNum, int pageSize, String keyword, Integer status) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, Constants.ROLE_USER);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword).or().like(User::getNickname, keyword));
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);
        IPage<User> result = userMapper.selectPage(page, wrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    // 管理员：修改用户状态
    public Result<?> updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
        return Result.success("操作成功", null);
    }

    // 获取总用户数
    public long countUsers() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, Constants.ROLE_USER);
        return userMapper.selectCount(wrapper);
    }

    // 获取总打卡次数
    public int totalCheckins() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, Constants.ROLE_USER);
        wrapper.select(User::getTotalCheckins);
        return userMapper.selectList(wrapper).stream()
                .mapToInt(u -> u.getTotalCheckins() != null ? u.getTotalCheckins() : 0).sum();
    }
}
