-- =====================================================
-- MineGuard 预警域数据库设计
-- 模块: warning-service
-- 版本: 1.2
-- 日期: 2026-03-21
-- 说明: 预警域相关表结构，包含预警记录和预警规则等
-- =====================================================

-- =====================================================
-- 1. 预警记录表
-- 说明: 存储系统自动检测或人工上报的预警记录
-- =====================================================
CREATE TABLE IF NOT EXISTS `warning_record` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `warning_no` VARCHAR(50) NOT NULL COMMENT '预警编号',
    `rule_id` BIGINT COMMENT '规则ID',
    `warning_type` INT COMMENT '预警类型：1-车辆故障，2-路线偏离，3-长时间停留，4-危险区域，5-速度异常，6-异常行为，7-疲劳驾驶',
    `warning_level` INT COMMENT '预警级别：1-低危，2-中危，3-高危',
    `vehicle_id` BIGINT COMMENT '车辆ID',
    `driver_id` BIGINT COMMENT '司机ID',
    `trip_id` BIGINT COMMENT '行程ID',
    `longitude` DOUBLE COMMENT '经度',
    `latitude` DOUBLE COMMENT '纬度',
    `speed` DECIMAL(10,2) COMMENT '速度',
    `warning_content` TEXT COMMENT '预警内容',
    `warning_time` DATETIME NOT NULL COMMENT '预警时间',
    `status` INT DEFAULT 0 COMMENT '状态：0-待处理，1-处理中，2-已解决，3-已忽略',
    `handler_id` BIGINT COMMENT '处理人ID',
    `handle_time` DATETIME COMMENT '处理时间',
    `handle_result` TEXT COMMENT '处理结果',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    UNIQUE KEY `uk_warning_no` (`warning_no`),
    INDEX `idx_vehicle_id` (`vehicle_id`),
    INDEX `idx_driver_id` (`driver_id`),
    INDEX `idx_trip_id` (`trip_id`),
    INDEX `idx_warning_type` (`warning_type`),
    INDEX `idx_warning_level` (`warning_level`),
    INDEX `idx_status` (`status`),
    INDEX `idx_warning_time` (`warning_time`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警记录表';

-- =====================================================
-- 2. 预警规则表
-- 说明: 存储预警规则配置，定义触发预警的条件和阈值
-- =====================================================
CREATE TABLE IF NOT EXISTS `warning_rule` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `rule_code` VARCHAR(50) NOT NULL COMMENT '规则编码',
    `warning_type` INT NOT NULL COMMENT '预警类型：1-车辆故障，2-路线偏离，3-长时间停留，4-危险区域，5-速度异常，6-异常行为，7-疲劳驾驶',
    `warning_level` INT NOT NULL COMMENT '预警级别：1-低危，2-中危，3-高危',
    `threshold_value` VARCHAR(100) COMMENT '阈值',
    `push_roles` VARCHAR(255) COMMENT '推送目标角色',
    `rule_config` TEXT COMMENT '规则配置JSON',
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
    INDEX `idx_warning_type` (`warning_type`),
    INDEX `idx_warning_level` (`warning_level`),
    INDEX `idx_status` (`status`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警规则表';

-- =====================================================
-- 初始化数据
-- =====================================================

-- 初始化预警规则数据
INSERT INTO `warning_rule` (`id`, `rule_name`, `rule_code`, `warning_type`, `warning_level`, `threshold_value`, `status`, `description`) VALUES
(1, '车辆故障预警', 'VEHICLE_FAULT', 1, 1, '故障代码', 1, '车辆故障预警规则，检测车辆上报的故障信息'),
(2, '路线偏离预警', 'ROUTE_DEVIATION', 2, 2, '50米', 1, '路线偏离预警规则，检测车辆是否偏离规划路线'),
(3, '危险地带预警', 'DANGER_ZONE', 3, 3, '危险区域', 1, '危险地带预警规则，检测车辆是否进入危险区域'),
(4, '速度异常预警', 'SPEED_ABNORMAL', 4, 2, '超过限速20%', 1, '速度异常预警规则，检测车辆是否超速行驶'),
(5, '盗卸行为预警', 'THEFT_BEHAVIOR', 5, 3, '超声波检测', 1, '盗卸行为预警规则，检测车辆是否有盗卸矿石行为'),
(6, '疲劳驾驶预警', 'FATIGUE_DRIVING', 6, 2, '连续驾驶4小时', 1, '疲劳驾驶预警规则，检测司机是否疲劳驾驶');

-- 注意：预警记录数据应通过系统的预警服务自动生成
-- 避免在数据库文件中直接插入预警记录数据
