-- =====================================================
-- MineGuard 成本域数据库设计
-- 模块: cost-service
-- 版本: 1.1
-- 日期: 2026-03-15
-- 说明: 成本域相关表结构，包含成本明细等
-- =====================================================

-- =====================================================
-- 1. 成本明细表
-- =====================================================
CREATE TABLE IF NOT EXISTS `cost_detail` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `cost_no` VARCHAR(50) NOT NULL COMMENT '成本编号',
    `cost_type` INT NOT NULL COMMENT '成本类型',
    `cost_name` VARCHAR(100) NOT NULL COMMENT '成本名称',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '金额',
    `vehicle_id` BIGINT COMMENT '车辆ID',
    `trip_id` BIGINT COMMENT '行程ID',
    `description` TEXT COMMENT '描述',
    `payment_method` VARCHAR(50) COMMENT '支付方式',
    `cost_date` DATETIME NOT NULL COMMENT '成本日期',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_cost_no` (`cost_no`),
    INDEX `idx_cost_type` (`cost_type`),
    INDEX `idx_vehicle_id` (`vehicle_id`),
    INDEX `idx_trip_id` (`trip_id`),
    INDEX `idx_cost_date` (`cost_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成本明细表';

-- =====================================================
-- 初始化数据
-- =====================================================

-- 注意：成本数据应通过系统的成本管理界面或API创建
-- 避免在数据库文件中直接插入成本数据