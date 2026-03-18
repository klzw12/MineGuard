-- =====================================================
-- MineGuard 行程域数据库设计
-- 模块: trip-service
-- 版本: 1.1
-- 日期: 2026-03-15
-- 说明: 行程域相关表结构，包含行程、路线、轨迹、调度计划和通知等
-- =====================================================

-- =====================================================
-- 1. 行程表
-- =====================================================
CREATE TABLE IF NOT EXISTS `trip` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `trip_no` VARCHAR(50) NOT NULL COMMENT '行程编号',
    `vehicle_id` BIGINT NOT NULL COMMENT '车辆ID',
    `driver_id` BIGINT NOT NULL COMMENT '司机ID',
    `route_id` BIGINT NOT NULL COMMENT '路线ID',
    `start_location` VARCHAR(255) COMMENT '起始位置',
    `end_location` VARCHAR(255) COMMENT '结束位置',
    `estimated_start_time` DATETIME COMMENT '预计开始时间',
    `estimated_end_time` DATETIME COMMENT '预计结束时间',
    `actual_start_time` DATETIME COMMENT '实际开始时间',
    `actual_end_time` DATETIME COMMENT '实际结束时间',
    `status` INT DEFAULT 0 COMMENT '状态：0-待开始，1-已接单，2-进行中，3-已完成，0/1-已取消',
    `trip_type` INT DEFAULT 0 COMMENT '行程类型',
    `estimated_mileage` DECIMAL(10,2) COMMENT '预计里程',
    `actual_mileage` DECIMAL(10,2) COMMENT '实际里程',
    `estimated_duration` INT COMMENT '预计时长（分钟）',
    `actual_duration` INT COMMENT '实际时长（分钟）',
    `fuel_consumption` DOUBLE COMMENT '燃油消耗',
    `average_speed` DOUBLE COMMENT '平均速度',
    `cancellation_reason` VARCHAR(255) COMMENT '取消原因',
    `start_longitude` DOUBLE COMMENT '起始经度',
    `start_latitude` DOUBLE COMMENT '起始纬度',
    `end_longitude` DOUBLE COMMENT '结束经度',
    `end_latitude` DOUBLE COMMENT '结束纬度',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    UNIQUE KEY `uk_trip_no` (`trip_no`),
    INDEX `idx_vehicle_id` (`vehicle_id`),
    INDEX `idx_driver_id` (`driver_id`),
    INDEX `idx_route_id` (`route_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行程表';

-- =====================================================
-- 2. 路线表
-- =====================================================
CREATE TABLE IF NOT EXISTS `route` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `route_name` VARCHAR(100) NOT NULL COMMENT '路线名称',
    `start_location` VARCHAR(255) NOT NULL COMMENT '起始位置',
    `end_location` VARCHAR(255) NOT NULL COMMENT '结束位置',
    `start_longitude` DOUBLE NOT NULL COMMENT '起始经度',
    `start_latitude` DOUBLE NOT NULL COMMENT '起始纬度',
    `end_longitude` DOUBLE NOT NULL COMMENT '结束经度',
    `end_latitude` DOUBLE NOT NULL COMMENT '结束纬度',
    `distance` DECIMAL(10,2) COMMENT '距离',
    `estimated_duration` INT COMMENT '预计时长（分钟）',
    `waypoints` TEXT COMMENT '途经点',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    INDEX `idx_route_name` (`route_name`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='路线表';

-- =====================================================
-- 3. 行程轨迹表
-- =====================================================
CREATE TABLE IF NOT EXISTS `trip_track` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `trip_id` BIGINT NOT NULL COMMENT '行程ID',
    `vehicle_id` BIGINT NOT NULL COMMENT '车辆ID',
    `longitude` DOUBLE NOT NULL COMMENT '经度',
    `latitude` DOUBLE NOT NULL COMMENT '纬度',
    `speed` DECIMAL(10,2) COMMENT '速度',
    `direction` DECIMAL(10,2) COMMENT '方向',
    `altitude` DECIMAL(10,2) COMMENT '海拔',
    `record_time` DATETIME NOT NULL COMMENT '记录时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_trip_id` (`trip_id`),
    INDEX `idx_vehicle_id` (`vehicle_id`),
    INDEX `idx_record_time` (`record_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行程轨迹表';

-- =====================================================
-- 4. 调度计划表
-- =====================================================
CREATE TABLE IF NOT EXISTS `dispatch_plan` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `plan_no` VARCHAR(50) NOT NULL COMMENT '计划编号',
    `plan_date` DATE NOT NULL COMMENT '计划日期',
    `vehicle_id` BIGINT NOT NULL COMMENT '车辆ID',
    `driver_id` BIGINT NOT NULL COMMENT '司机ID',
    `route_id` BIGINT NOT NULL COMMENT '路线ID',
    `planned_trips` INT DEFAULT 1 COMMENT '计划行程次数',
    `planned_cargo_weight` DECIMAL(10,2) COMMENT '计划 cargo 重量',
    `start_time_slot` VARCHAR(50) COMMENT '开始时间段',
    `status` INT DEFAULT 0 COMMENT '状态',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    UNIQUE KEY `uk_plan_no` (`plan_no`),
    INDEX `idx_vehicle_id` (`vehicle_id`),
    INDEX `idx_driver_id` (`driver_id`),
    INDEX `idx_route_id` (`route_id`),
    INDEX `idx_plan_date` (`plan_date`),
    INDEX `idx_status` (`status`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调度计划表';

-- =====================================================
-- 5. 行程通知表
-- =====================================================
CREATE TABLE IF NOT EXISTS `trip_notification` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `trip_id` BIGINT NOT NULL COMMENT '行程ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `notification_type` VARCHAR(50) COMMENT '通知类型',
    `notification_content` TEXT COMMENT '通知内容',
    `status` VARCHAR(20) DEFAULT 'unread' COMMENT '状态：unread-未读，read-已读',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `read_time` DATETIME COMMENT '读取时间',
    INDEX `idx_trip_id` (`trip_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行程通知表';

-- =====================================================
-- 初始化数据
-- =====================================================

-- 初始化路线数据
INSERT INTO `route` (`id`, `route_name`, `start_location`, `end_location`, `start_longitude`, `start_latitude`, `end_longitude`, `end_latitude`, `distance`, `estimated_duration`) VALUES
(1, '矿区A到矿区B', '矿区A', '矿区B', 116.397428, 39.90923, 116.417428, 39.91923, 5.5, 15),
(2, '矿区B到仓库', '矿区B', '仓库', 116.417428, 39.91923, 116.437428, 39.92923, 8.2, 20);

-- 注意：行程数据应通过系统的调度服务或管理界面创建
-- 避免在数据库文件中直接插入行程数据