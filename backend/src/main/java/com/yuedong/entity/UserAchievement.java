package com.yuedong.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_achievement")
public class UserAchievement {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long achievementId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime unlockTime;
}
