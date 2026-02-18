package com.yuedong.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    @JsonIgnore
    private String password;
    private String nickname;
    private String avatar;
    private String phone;
    private Integer gender;
    private String role;
    private Integer points;
    private Integer totalCheckins;
    private Integer consecutiveDays;
    private Integer maxConsecutive;
    private Integer totalDuration;
    private BigDecimal totalCalories;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastCheckinDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
