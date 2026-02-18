package com.yuedong.service;

import com.yuedong.entity.PointsRecord;
import com.yuedong.mapper.PointsRecordMapper;
import com.yuedong.common.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PointsService {

    @Autowired
    private PointsRecordMapper pointsRecordMapper;

    public void addRecord(Long userId, int points, String type, String description) {
        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setPoints(points);
        record.setType(type);
        record.setDescription(description);
        pointsRecordMapper.insert(record);
    }

    public Result<?> getUserPointsList(Long userId, int pageNum, int pageSize) {
        Page<PointsRecord> page = new Page<>(pageNum, pageSize);
        IPage<PointsRecord> result = pointsRecordMapper.selectPageWithUser(page, userId, null);

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    // 管理员：积分记录列表
    public Result<?> adminPointsList(int pageNum, int pageSize, Long userId, String type) {
        Page<PointsRecord> page = new Page<>(pageNum, pageSize);
        IPage<PointsRecord> result = pointsRecordMapper.selectPageWithUser(page, userId, type);

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }
}
