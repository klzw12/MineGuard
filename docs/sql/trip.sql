-- =====================================================
-- MineGuard 行程域数据库设计
-- 模块: trip-service
-- 版本: 1.3
-- 日期: 2026-03-22
-- 说明: 行程域相关表结构，包含行程等
-- 注意: 路线模板在dispatch模块的route_template表，实际导航使用高德地图API
--       行程通知使用user模块的user_notification表，通过business_id和business_type关联
-- =====================================================

-- =====================================================
-- 1. 行程表
-- 说明: 行程是司机接单后创建的实际运输记录
-- 起点终点坐标用于高德地图导航
-- =====================================================
CREATE TABLE IF NOT EXISTS `trip` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `trip_no` VARCHAR(50) NOT NULL COMMENT '行程编号',
    `dispatch_task_id` BIGINT COMMENT '调度任务ID(调度任务接单后创建)',
    `vehicle_id` BIGINT NOT NULL COMMENT '车辆ID',
    `driver_id` BIGINT NOT NULL COMMENT '司机ID',
    `start_location` VARCHAR(255) COMMENT '起始位置名称',
    `start_longitude` DOUBLE COMMENT '起始经度',
    `start_latitude` DOUBLE COMMENT '起始纬度',
    `end_location` VARCHAR(255) COMMENT '结束位置名称',
    `end_longitude` DOUBLE COMMENT '结束经度',
    `end_latitude` DOUBLE COMMENT '结束纬度',
    `estimated_start_time` DATETIME COMMENT '预计开始时间',
    `estimated_end_time` DATETIME COMMENT '预计结束时间',
    `actual_start_time` DATETIME COMMENT '实际开始时间',
    `actual_end_time` DATETIME COMMENT '实际结束时间',
    `status` INT DEFAULT 0 COMMENT '状态：0-待开始，1-已接单，2-进行中，3-已完成，4-已取消',
    `trip_type` INT DEFAULT 0 COMMENT '行程类型',
    `estimated_mileage` DECIMAL(10,2) COMMENT '预计里程',
    `actual_mileage` DECIMAL(10,2) COMMENT '实际里程',
    `estimated_duration` INT COMMENT '预计时长（分钟）',
    `actual_duration` INT COMMENT '实际时长（分钟）',
    `fuel_consumption` DOUBLE COMMENT '燃油消耗',
    `average_speed` DOUBLE COMMENT '平均速度',
    `cargo_weight` DECIMAL(10,2) COMMENT '货物重量(吨)',
    `cancellation_reason` VARCHAR(255) COMMENT '取消原因',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    UNIQUE KEY `uk_trip_no` (`trip_no`),
    INDEX `idx_dispatch_task_id` (`dispatch_task_id`),
    INDEX `idx_vehicle_id` (`vehicle_id`),
    INDEX `idx_driver_id` (`driver_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行程表';

-- =====================================================
-- 初始化数据
-- =====================================================

-- 注意：行程数据应通过系统的调度服务或管理界面创建
-- 避免在数据库文件中直接插入行程数据
