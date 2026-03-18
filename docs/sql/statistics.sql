-- =====================================================
-- MineGuard 统计域数据库设计
-- 模块: statistics-service
-- 版本: 1.1
-- 日期: 2026-03-15
-- 说明: 统计域相关表结构，包含成本统计和行程统计等
-- =====================================================

-- =====================================================
-- 1. 成本统计表
-- =====================================================
CREATE TABLE IF NOT EXISTS `cost_statistics` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `statistics_date` DATE NOT NULL COMMENT '统计日期',
    `fuel_cost` DECIMAL(10,2) DEFAULT 0 COMMENT '燃油成本',
    `maintenance_cost` DECIMAL(10,2) DEFAULT 0 COMMENT '维修成本',
    `labor_cost` DECIMAL(10,2) DEFAULT 0 COMMENT '人工成本',
    `other_cost` DECIMAL(10,2) DEFAULT 0 COMMENT '其他成本',
    `total_cost` DECIMAL(10,2) DEFAULT 0 COMMENT '总成本',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_statistics_date` (`statistics_date`),
    INDEX `idx_statistics_date` (`statistics_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成本统计表';

-- =====================================================
-- 2. 行程统计表
-- =====================================================
CREATE TABLE IF NOT EXISTS `trip_statistics` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `statistics_date` DATE NOT NULL COMMENT '统计日期',
    `trip_count` INT DEFAULT 0 COMMENT '行程数量',
    `total_distance` DECIMAL(10,2) DEFAULT 0 COMMENT '总距离',
    `total_duration` DECIMAL(10,2) DEFAULT 0 COMMENT '总时长',
    `completed_trip_count` INT DEFAULT 0 COMMENT '已完成行程数量',
    `cancelled_trip_count` INT DEFAULT 0 COMMENT '已取消行程数量',
    `average_speed` DECIMAL(10,2) DEFAULT 0 COMMENT '平均速度',
    `fuel_consumption` DECIMAL(10,2) DEFAULT 0 COMMENT '燃油消耗',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_statistics_date` (`statistics_date`),
    INDEX `idx_statistics_date` (`statistics_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行程统计表';

-- =====================================================
-- 初始化数据
-- =====================================================

-- 注意：统计数据应通过系统的统计服务自动生成
-- 避免在数据库文件中直接插入统计数据