-- =====================================================
-- MineGuard 车辆域数据库设计
-- 模块: vehicle-service
-- 版本: 1.1
-- 日期: 2026-03-17
-- 说明: 车辆域相关表结构，包含车辆、故障、保险、维护、加油、状态等
-- =====================================================

-- =====================================================
-- 1. 车辆表
-- 说明：车辆属于矿山，通过调度动态分配给司机
-- =====================================================
CREATE TABLE IF NOT EXISTS `vehicle` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `vehicle_no` VARCHAR(20) NOT NULL COMMENT '车牌号',
    `vehicle_type` INTEGER COMMENT '车辆类型',
    `brand` VARCHAR(50) COMMENT '品牌',
    `model` VARCHAR(50) COMMENT '型号',
    `status` INTEGER DEFAULT 0 COMMENT '状态：0-空闲，1-运行中，2-维护中，3-故障，4-报废',
    `fuel_level` INTEGER COMMENT '油量百分比(0-100)',
    `score` INTEGER DEFAULT 0 COMMENT '车辆评分(0-100)',
    `photo_url` VARCHAR(255) COMMENT '车辆照片URL',
    `license_front_url` VARCHAR(255) COMMENT '行驶证正面URL',
    `license_back_url` VARCHAR(255) COMMENT '行驶证背面URL',
    `owner` VARCHAR(50) COMMENT '所有人',
    `address` VARCHAR(255) COMMENT '住址',
    `brand_model` VARCHAR(100) COMMENT '品牌型号',
    `vehicle_model` VARCHAR(100) COMMENT '车辆型号',
    `engine_number` VARCHAR(50) COMMENT '发动机号',
    `vin` VARCHAR(50) COMMENT '车架号',
    `use_nature` VARCHAR(50) COMMENT '使用性质',
    `register_date` DATE COMMENT '注册日期',
    `issue_date` DATE COMMENT '发证日期',
    `seating_capacity` INTEGER COMMENT '核定载人数',
    `total_mass` VARCHAR(20) COMMENT '总质量',
    `curb_weight` VARCHAR(20) COMMENT '整备质量',
    `rated_load` VARCHAR(20) COMMENT '核定载质量',
    `dimensions` VARCHAR(100) COMMENT '外廓尺寸',
    `inspection_record` VARCHAR(500) COMMENT '年检记录',
    `depreciation_rate` DECIMAL(10,4) DEFAULT 0.0500 COMMENT '折旧系数（元/公里），默认0.05元/公里',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    UNIQUE KEY `uk_vehicle_no` (`vehicle_no`),
    INDEX `idx_status` (`status`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆表';

-- =====================================================
-- 2. 车辆故障表
-- =====================================================
CREATE TABLE IF NOT EXISTS `vehicle_fault` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `vehicle_id` BIGINT NOT NULL COMMENT '车辆ID',
    `fault_type` VARCHAR(50) COMMENT '故障类型',
    `fault_description` VARCHAR(500) COMMENT '故障描述',
    `fault_date` DATETIME COMMENT '故障日期',
    `severity` INTEGER COMMENT '严重程度：1-轻微，2-中等，3-严重，4-紧急',
    `status` INTEGER DEFAULT 0 COMMENT '状态：0-待处理，1-处理中，2-已解决，3-已关闭',
    `repairman_id` BIGINT COMMENT '维修员ID',
    `repair_date` DATETIME COMMENT '维修日期',
    `repair_cost` DECIMAL(10,2) COMMENT '维修费用',
    `repair_content` VARCHAR(500) COMMENT '维修内容',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    INDEX `idx_vehicle_id` (`vehicle_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_severity` (`severity`),
    INDEX `idx_repairman_id` (`repairman_id`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆故障表';

-- =====================================================
-- 3. 车辆保险表
-- =====================================================
CREATE TABLE IF NOT EXISTS `vehicle_insurance` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `vehicle_id` BIGINT NOT NULL COMMENT '车辆ID',
    `insurance_company` VARCHAR(100) COMMENT '保险公司',
    `insurance_number` VARCHAR(100) COMMENT '保险单号',
    `insurance_type` INTEGER COMMENT '保险类型：1-交强险，2-商业险，3-第三者责任险，4-车辆损失险，5-盗抢险',
    `insurance_amount` DECIMAL(10,2) COMMENT '保险金额',
    `start_date` DATE COMMENT '开始日期',
    `expiry_date` DATE COMMENT '到期日期',
    `status` INTEGER DEFAULT 1 COMMENT '状态：0-未投保，1-已投保，2-已过期，3-理赔中',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    INDEX `idx_vehicle_id` (`vehicle_id`),
    INDEX `idx_insurance_type` (`insurance_type`),
    INDEX `idx_status` (`status`),
    INDEX `idx_expiry_date` (`expiry_date`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆保险表';

-- =====================================================
-- 4. 车辆维护表
-- =====================================================
CREATE TABLE IF NOT EXISTS `vehicle_maintenance` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `vehicle_id` BIGINT NOT NULL COMMENT '车辆ID',
    `maintenance_type` INTEGER COMMENT '维护类型：1-常规保养，2-小修，3-中修，4-大修，5-年检',
    `maintenance_date` DATETIME COMMENT '维护日期',
    `maintenance_shop` VARCHAR(100) COMMENT '维修厂',
    `maintenance_cost` DECIMAL(10,2) COMMENT '维护费用',
    `maintenance_content` VARCHAR(500) COMMENT '维护内容',
    `maintenance_result` VARCHAR(500) COMMENT '维护结果',
    `mileage` INTEGER COMMENT '行驶里程',
    `next_maintenance_date` DATE COMMENT '下次维护日期',
    `next_maintenance_mileage` INTEGER COMMENT '下次维护里程',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    INDEX `idx_vehicle_id` (`vehicle_id`),
    INDEX `idx_maintenance_type` (`maintenance_type`),
    INDEX `idx_maintenance_date` (`maintenance_date`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆维护表';

-- =====================================================
-- 5. 车辆加油表
-- =====================================================
CREATE TABLE IF NOT EXISTS `vehicle_refueling` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `vehicle_id` BIGINT NOT NULL COMMENT '车辆ID',
    `driver_id` BIGINT COMMENT '司机ID',
    `refueling_date` DATETIME COMMENT '加油日期',
    `refueling_station` VARCHAR(100) COMMENT '加油站',
    `fuel_type` VARCHAR(20) COMMENT '燃油类型',
    `refueling_amount` DECIMAL(10,2) COMMENT '加油量（升）',
    `unit_price` DECIMAL(5,2) COMMENT '单价',
    `total_cost` DECIMAL(10,2) COMMENT '总费用',
    `current_mileage` INTEGER COMMENT '当前里程',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    INDEX `idx_vehicle_id` (`vehicle_id`),
    INDEX `idx_driver_id` (`driver_id`),
    INDEX `idx_refueling_date` (`refueling_date`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆加油表';

-- =====================================================
-- 6. 车辆状态表
-- =====================================================
CREATE TABLE IF NOT EXISTS `vehicle_status` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `vehicle_id` BIGINT NOT NULL COMMENT '车辆ID',
    `trip_id` BIGINT COMMENT '行程ID',
    `status` INTEGER COMMENT '状态：0-空闲，1-运行中，2-维护中，3-故障，4-报废',
    `status_time` DATETIME COMMENT '状态时间',
    `longitude` DOUBLE COMMENT '经度',
    `latitude` DOUBLE COMMENT '纬度',
    `speed` DOUBLE COMMENT '速度(km/h)',
    `direction` DOUBLE COMMENT '方向(度)',
    `altitude` DOUBLE COMMENT '海拔',
    `mileage` DOUBLE COMMENT '里程(km)',
    `fuel_level` INTEGER COMMENT '油量百分比(0-100)',
    `report_time` DATETIME COMMENT '上报时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    INDEX `idx_vehicle_id` (`vehicle_id`),
    INDEX `idx_trip_id` (`trip_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_status_time` (`status_time`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆状态表';

-- =====================================================
