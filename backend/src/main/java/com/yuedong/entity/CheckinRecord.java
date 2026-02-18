package com.yuedong.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("checkin_record")
public class CheckinRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long sportTypeId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkinDate;
    private Integer duration;
    private BigDecimal distance;
    private BigDecimal calories;
    private String remark;
    private Integer pointsEarned;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String sportTypeName;
    @TableField(exist = false)
    private String sportTypeIcon;
    @TableField(exist = false)
    private String username;
    @TableField(exist = false)
    private String nickname;
}
