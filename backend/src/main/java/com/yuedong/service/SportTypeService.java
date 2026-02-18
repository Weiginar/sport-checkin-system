package com.yuedong.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuedong.common.Result;
import com.yuedong.entity.SportType;
import com.yuedong.mapper.SportTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SportTypeService {

    @Autowired
    private SportTypeMapper sportTypeMapper;

    public Result<?> listEnabled() {
        LambdaQueryWrapper<SportType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SportType::getStatus, 1);
        wrapper.orderByAsc(SportType::getSortOrder);
        List<SportType> list = sportTypeMapper.selectList(wrapper);
        return Result.success(list);
    }

    public SportType getById(Long id) {
        return sportTypeMapper.selectById(id);
    }

    // 管理员：分页列表
    public Result<?> adminList(int pageNum, int pageSize) {
        Page<SportType> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SportType> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SportType::getSortOrder);
        IPage<SportType> result = sportTypeMapper.selectPage(page, wrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    // 管理员：新增
    public Result<?> add(String name, String icon, BigDecimal caloriesPerHour, String description, Integer sortOrder, Integer status) {
        LambdaQueryWrapper<SportType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SportType::getName, name);
        if (sportTypeMapper.selectOne(wrapper) != null) {
            return Result.error("运动项目名称已存在");
        }
        SportType sportType = new SportType();
        sportType.setName(name);
        sportType.setIcon(icon != null ? icon : "");
        sportType.setCaloriesPerHour(caloriesPerHour);
        sportType.setDescription(description != null ? description : "");
        sportType.setSortOrder(sortOrder != null ? sortOrder : 0);
        sportType.setStatus(status != null ? status : 1);
        sportTypeMapper.insert(sportType);
        return Result.success("新增成功", sportType);
    }

    // 管理员：编辑
    public Result<?> update(Long id, String name, String icon, BigDecimal caloriesPerHour, String description, Integer sortOrder, Integer status) {
        SportType sportType = sportTypeMapper.selectById(id);
        if (sportType == null) {
            return Result.error(404, "运动项目不存在");
        }
        if (name != null) {
            LambdaQueryWrapper<SportType> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SportType::getName, name).ne(SportType::getId, id);
            if (sportTypeMapper.selectOne(wrapper) != null) {
                return Result.error("运动项目名称已存在");
            }
            sportType.setName(name);
        }
        if (icon != null) sportType.setIcon(icon);
        if (caloriesPerHour != null) sportType.setCaloriesPerHour(caloriesPerHour);
        if (description != null) sportType.setDescription(description);
        if (sortOrder != null) sportType.setSortOrder(sortOrder);
        if (status != null) sportType.setStatus(status);
        sportTypeMapper.updateById(sportType);
        return Result.success("更新成功", sportType);
    }

    // 管理员：删除
    public Result<?> delete(Long id) {
        SportType sportType = sportTypeMapper.selectById(id);
        if (sportType == null) {
            return Result.error(404, "运动项目不存在");
        }
        sportTypeMapper.deleteById(id);
        return Result.success("删除成功", null);
    }
}
