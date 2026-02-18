package com.yuedong.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuedong.entity.CheckinRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface CheckinRecordMapper extends BaseMapper<CheckinRecord> {

    IPage<CheckinRecord> selectPageWithDetail(Page<CheckinRecord> page,
                                               @Param("userId") Long userId,
                                               @Param("sportTypeId") Long sportTypeId,
                                               @Param("startDate") String startDate,
                                               @Param("endDate") String endDate);

    IPage<CheckinRecord> selectAdminPageWithDetail(Page<CheckinRecord> page,
                                                    @Param("userId") Long userId,
                                                    @Param("sportTypeId") Long sportTypeId,
                                                    @Param("startDate") String startDate,
                                                    @Param("endDate") String endDate);

    @Select("SELECT checkin_date FROM checkin_record WHERE user_id = #{userId} AND checkin_date >= #{startDate} AND checkin_date <= #{endDate}")
    List<LocalDate> selectCheckinDates(@Param("userId") Long userId,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    List<Map<String, Object>> selectWeeklyDuration(@Param("userId") Long userId,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    List<Map<String, Object>> selectSportDistribution(@Param("userId") Long userId);

    List<Map<String, Object>> selectRecentTrend(@Param("userId") Long userId,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    List<Map<String, Object>> selectRanking(@Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);
}
