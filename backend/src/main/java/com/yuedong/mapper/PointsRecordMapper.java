package com.yuedong.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuedong.entity.PointsRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PointsRecordMapper extends BaseMapper<PointsRecord> {

    IPage<PointsRecord> selectPageWithUser(Page<PointsRecord> page,
                                            @Param("userId") Long userId,
                                            @Param("type") String type);
}
