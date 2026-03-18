-- =====================================================
-- MineGuard 调度域数据库设计
-- 模块: dispatch-service
-- 版本: 1.1
-- 日期: 2026-03-15
-- 说明: 调度域相关表结构，包含调度规则和调度任务等
-- =====================================================

-- =====================================================
-- 1. 调度规则表
-- =====================================================
CREATE TABLE IF NOT EXISTS `dispatch_rule` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `rule_code` VARCHAR(50) NOT NULL COMMENT '规则编码',
    `rule_type` INT NOT NULL COMMENT '规则类型',
    `rule_content` TEXT COMMENT '规则内容',
    `status` INT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `description` TEXT COMMENT '描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    UNIQUE KEY `uk_rule_code` (`rule_code`),
    INDEX `idx_rule_name` (`rule_name`),
    INDEX `idx_rule_type` (`rule_type`),
    INDEX `idx_status` (`status`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调度规则表';

-- =====================================================
-- 2. 调度任务表
-- =====================================================
CREATE TABLE IF NOT EXISTS `dispatch_task` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `task_no` VARCHAR(50) NOT NULL COMMENT '任务编号',
    `task_type` INT NOT NULL COMMENT '任务类型',
    `status` INT DEFAULT 0 COMMENT '状态：0-待处理，1-处理中，2-已完成，3-已取消',
    `vehicle_id` BIGINT COMMENT '车辆ID',
    `driver_id` BIGINT COMMENT '司机ID',
    `start_location` VARCHAR(255) COMMENT '起始位置',
    `end_location` VARCHAR(255) COMMENT '结束位置',
    `scheduled_time` DATETIME COMMENT '计划时间',
    `actual_start_time` DATETIME COMMENT '实际开始时间',
    `actual_end_time` DATETIME COMMENT '实际结束时间',
    `priority` VARCHAR(20) DEFAULT 'normal' COMMENT '优先级：low-低，normal-正常，high-高',
    `description` TEXT COMMENT '描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_task_no` (`task_no`),
    INDEX `idx_task_type` (`task_type`),
    INDEX `idx_status` (`status`),
    INDEX `idx_vehicle_id` (`vehicle_id`),
    INDEX `idx_driver_id` (`driver_id`),
    INDEX `idx_scheduled_time` (`scheduled_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调度任务表';

-- =====================================================
-- 初始化数据
-- =====================================================

-- 初始化调度规则数据
INSERT INTO `dispatch_rule` (`id`, `rule_name`, `rule_code`, `rule_type`, `rule_content`, `status`, `description`) VALUES
(1, '默认调度规则', 'DEFAULT_RULE', 1, '默认调度规则内容', 1, '默认调度规则'),
(2, '紧急调度规则', 'EMERGENCY_RULE', 2, '紧急调度规则内容', 1, '紧急调度规则');

-- 注意：调度任务数据应通过系统的调度服务自动生成
-- 避免在数据库文件中直接插入调度任务数据