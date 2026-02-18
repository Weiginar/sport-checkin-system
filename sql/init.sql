-- 悦动打卡 数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS yuedong DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE yuedong;

-- 表1：用户表
DROP TABLE IF EXISTS user_achievement;
DROP TABLE IF EXISTS points_record;
DROP TABLE IF EXISTS checkin_record;
DROP TABLE IF EXISTS achievement;
DROP TABLE IF EXISTS sport_type;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码(BCrypt)',
  `nickname` VARCHAR(50) DEFAULT '' COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT '' COMMENT '头像URL',
  `phone` VARCHAR(20) DEFAULT '' COMMENT '手机号',
  `gender` TINYINT DEFAULT 0 COMMENT '性别:0未知1男2女',
  `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色:USER/ADMIN',
  `points` INT NOT NULL DEFAULT 0 COMMENT '当前积分',
  `total_checkins` INT NOT NULL DEFAULT 0 COMMENT '累计打卡次数',
  `consecutive_days` INT NOT NULL DEFAULT 0 COMMENT '当前连续打卡天数',
  `max_consecutive` INT NOT NULL DEFAULT 0 COMMENT '最长连续打卡天数',
  `total_duration` INT NOT NULL DEFAULT 0 COMMENT '累计运动时长(分钟)',
  `total_calories` DECIMAL(10,1) NOT NULL DEFAULT 0.0 COMMENT '累计消耗卡路里',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0禁用1正常',
  `last_checkin_date` DATE DEFAULT NULL COMMENT '最后打卡日期',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role` (`role`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- 表2：运动项目表
CREATE TABLE `sport_type` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(50) NOT NULL COMMENT '运动名称',
  `icon` VARCHAR(50) DEFAULT '' COMMENT '图标标识',
  `calories_per_hour` DECIMAL(6,1) NOT NULL DEFAULT 0.0 COMMENT '每小时消耗卡路里',
  `description` VARCHAR(200) DEFAULT '' COMMENT '运动描述',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0停用1启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  KEY `idx_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='运动项目表';

-- 表3：打卡记录表
CREATE TABLE `checkin_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `sport_type_id` BIGINT NOT NULL COMMENT '运动项目ID',
  `checkin_date` DATE NOT NULL COMMENT '打卡日期',
  `duration` INT NOT NULL DEFAULT 0 COMMENT '运动时长(分钟)',
  `distance` DECIMAL(8,2) DEFAULT 0.00 COMMENT '运动距离(公里)',
  `calories` DECIMAL(8,1) NOT NULL DEFAULT 0.0 COMMENT '消耗卡路里',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '打卡备注',
  `points_earned` INT NOT NULL DEFAULT 0 COMMENT '获得积分',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `checkin_date`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_checkin_date` (`checkin_date`),
  KEY `idx_sport_type_id` (`sport_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='打卡记录表';

-- 表4：积分记录表
CREATE TABLE `points_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `points` INT NOT NULL COMMENT '积分变动值',
  `type` VARCHAR(30) NOT NULL COMMENT '类型:CHECKIN/CONSECUTIVE/ACHIEVEMENT',
  `description` VARCHAR(200) DEFAULT '' COMMENT '积分变动描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='积分记录表';

-- 表5：成就定义表
CREATE TABLE `achievement` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(50) NOT NULL COMMENT '成就名称',
  `icon` VARCHAR(50) DEFAULT '' COMMENT '成就图标',
  `description` VARCHAR(200) DEFAULT '' COMMENT '成就描述',
  `condition_type` VARCHAR(30) NOT NULL COMMENT '条件类型:TOTAL_CHECKINS/CONSECUTIVE/TOTAL_DURATION',
  `condition_value` INT NOT NULL COMMENT '条件阈值',
  `reward_points` INT NOT NULL DEFAULT 0 COMMENT '奖励积分',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='成就定义表';

-- 表6：用户成就表
CREATE TABLE `user_achievement` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `achievement_id` BIGINT NOT NULL COMMENT '成就ID',
  `unlock_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '解锁时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_achievement` (`user_id`, `achievement_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户成就表';

-- 插入默认管理员 (密码admin123的BCrypt加密值)
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`) VALUES
('admin', '$2a$10$/b3aTV358XPTHpHOaf.ETOzLxexIpuoEn.m5ixSnoHLPguvWHdgzW', '系统管理员', 'ADMIN', 1);

-- 插入预设运动项目
INSERT INTO `sport_type` (`name`, `icon`, `calories_per_hour`, `description`, `sort_order`) VALUES
('跑步', 'running', 600.0, '户外或跑步机跑步', 1),
('健走', 'walking', 300.0, '快走或散步', 2),
('骑行', 'cycling', 500.0, '自行车或动感单车', 3),
('游泳', 'swimming', 700.0, '各种泳姿', 4),
('瑜伽', 'yoga', 250.0, '瑜伽练习', 5),
('篮球', 'basketball', 550.0, '篮球运动', 6),
('羽毛球', 'badminton', 450.0, '羽毛球运动', 7),
('健身', 'fitness', 500.0, '器械或自重训练', 8);

-- 插入预设成就徽章
INSERT INTO `achievement` (`name`, `icon`, `description`, `condition_type`, `condition_value`, `reward_points`, `sort_order`) VALUES
('初次打卡', 'badge1', '完成第一次运动打卡', 'TOTAL_CHECKINS', 1, 10, 1),
('运动新手', 'badge2', '累计打卡达到7次', 'TOTAL_CHECKINS', 7, 30, 2),
('运动达人', 'badge3', '累计打卡达到30次', 'TOTAL_CHECKINS', 30, 100, 3),
('运动健将', 'badge4', '累计打卡达到100次', 'TOTAL_CHECKINS', 100, 300, 4),
('坚持一周', 'badge5', '连续打卡7天', 'CONSECUTIVE', 7, 50, 5),
('坚持半月', 'badge6', '连续打卡15天', 'CONSECUTIVE', 15, 120, 6),
('坚持一月', 'badge7', '连续打卡30天', 'CONSECUTIVE', 30, 300, 7),
('运动入门', 'badge8', '累计运动时长达到10小时', 'TOTAL_DURATION', 600, 50, 8),
('运动进阶', 'badge9', '累计运动时长达到50小时', 'TOTAL_DURATION', 3000, 150, 9),
('运动大师', 'badge10', '累计运动时长达到100小时', 'TOTAL_DURATION', 6000, 500, 10);
