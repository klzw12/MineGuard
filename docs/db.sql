-- =====================================================
-- MineGuard 数据库设计
-- 版本: 1.1
-- 日期: 2026-03-05
-- 说明: 按业务域划分表结构，使用UUID作为主键
-- =====================================================

-- =====================================================
-- 1. 用户域 (user-service)
-- =====================================================
CREATE DATABASE IF NOT EXISTS `MineGuard` ;

USE `MineGuard`;
-- 1.1 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '主键UUID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(50) COMMENT '真实姓名',
    `phone` VARCHAR(20) COMMENT '手机号',
    `email` VARCHAR(100) COMMENT '邮箱',
    `avatar_url` VARCHAR(255) COMMENT '头像URL',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `user_type` TINYINT DEFAULT 1 COMMENT '用户类型：1-管理员，2-司机，3-安全员，4-维修员',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    INDEX `idx_status` (`status`),
    INDEX `idx_user_type` (`user_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 1.2 角色表
CREATE TABLE IF NOT EXISTS `role` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '主键UUID',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `description` VARCHAR(255) COMMENT '描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_role_code` (`role_code`),
    INDEX `idx_role_name` (`role_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 1.3 用户角色关联表
CREATE TABLE IF NOT EXISTS `user_role` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '主键UUID',
    `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID',
    `role_id` VARCHAR(36) NOT NULL COMMENT '角色ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 1.4 权限表
CREATE TABLE IF NOT EXISTS `permission` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '主键UUID',
    `permission_name` VARCHAR(50) NOT NULL COMMENT '权限名称',
    `permission_code` VARCHAR(100) NOT NULL COMMENT '权限编码',
    `parent_id` VARCHAR(36) DEFAULT '' COMMENT '父权限ID',
    `permission_type` TINYINT DEFAULT 1 COMMENT '权限类型：1-菜单，2-按钮，3-接口',
    `path` VARCHAR(255) COMMENT '路径',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_permission_type` (`permission_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 1.5 角色权限关联表
CREATE TABLE IF NOT EXISTS `role_permission` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '主键UUID',
    `role_id` VARCHAR(36) NOT NULL COMMENT '角色ID',
    `permission_id` VARCHAR(36) NOT NULL COMMENT '权限ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    INDEX `idx_role_id` (`role_id`),
    INDEX `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 1.6 司机信息表
CREATE TABLE IF NOT EXISTS `driver` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '主键UUID',
    `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID',
    `driver_name` VARCHAR(50) NOT NULL COMMENT '司机姓名',
    `gender` TINYINT COMMENT '性别：1-男，2-女',
    `age` INT COMMENT '年龄',
    `id_card` VARCHAR(18) NOT NULL COMMENT '身份证号',
    `id_card_front_url` VARCHAR(255) COMMENT '身份证正面URL',
    `id_card_back_url` VARCHAR(255) COMMENT '身份证背面URL',
    `driving_license_url` VARCHAR(255) COMMENT '驾驶证URL',
    `driving_years` INT COMMENT '驾龄（年）',
    `license_type` VARCHAR(10) COMMENT '准驾车型',
    `belonging_team` VARCHAR(100) COMMENT '隶属班组',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-离职，1-在职',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    UNIQUE KEY `uk_id_card` (`id_card`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_belonging_team` (`belonging_team`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='司机信息表';

-- 1.7 司机出勤记录表
CREATE TABLE IF NOT EXISTS `driver_attendance` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '主键UUID',
    `driver_id` VARCHAR(36) NOT NULL COMMENT '司机ID',
    `attendance_date` DATE NOT NULL COMMENT '出勤日期',
    `check_in_time` DATETIME COMMENT '上班时间',
    `check_out_time` DATETIME COMMENT '下班时间',
    `status` TINYINT DEFAULT 1 COMMENT '出勤状态：1-正常，2-迟到，3-早退，4-缺勤',
    `late_minutes` INT DEFAULT 0 COMMENT '迟到分钟数',
    `early_leave_minutes` INT DEFAULT 0 COMMENT '早退分钟数',
    `remark` VARCHAR(255) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_driver_id` (`driver_id`),
    INDEX `idx_attendance_date` (`attendance_date`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='司机出勤记录表';

-- 1.8 安全员信息表
CREATE TABLE IF NOT EXISTS `safety_officer` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '主键UUID',
    `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID',
    `officer_name` VARCHAR(50) NOT NULL COMMENT '安全员姓名',
    `gender` TINYINT COMMENT '性别：1-男，2-女',
    `id_card` VARCHAR(18) NOT NULL COMMENT '身份证号',
    `id_card_front_url` VARCHAR(255) COMMENT '身份证正面URL',
    `id_card_back_url` VARCHAR(255) COMMENT '身份证背面URL',
    `emergency_cert_url` VARCHAR(255) COMMENT '应急救援证URL',
    `cert_number` VARCHAR(50) COMMENT '证书编号',
    `training_project` VARCHAR(100) COMMENT '培训项目',
    `valid_until` DATE COMMENT '有效期至',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-离职，1-在职',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_id_card` (`id_card`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安全员信息表';

-- 1.9 维修员信息表
CREATE TABLE IF NOT EXISTS `repairman` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '主键UUID',
    `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID',
    `repairman_name` VARCHAR(50) NOT NULL COMMENT '维修员姓名',
    `gender` TINYINT COMMENT '性别：1-男，2-女',
    `id_card` VARCHAR(18) NOT NULL COMMENT '身份证号',
    `id_card_front_url` VARCHAR(255) COMMENT '身份证正面URL',
    `id_card_back_url` VARCHAR(255) COMMENT '身份证背面URL',
    `repair_cert_url` VARCHAR(255) COMMENT '维修资格证URL',
    `cert_number` VARCHAR(50) COMMENT '证书编号',
    `repair_level` VARCHAR(20) COMMENT '维修等级：初级/中级/高级',
    `repair_type` VARCHAR(50) COMMENT '维修类别',
    `valid_until` DATE COMMENT '有效期至',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-离职，1-在职',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_id_card` (`id_card`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修员信息表';


-- =====================================================
-- 2. 车辆域 (vehicle-service)
-- =====================================================

-- 2.1 车型表
CREATE TABLE IF NOT EXISTS `vehicle_model` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '主键UUID',
    `model_name` VARCHAR(100) NOT NULL COMMENT '车型名称',
    `model_code` VARCHAR(50) COMMENT '车型编码',
    `brand` VARCHAR(50) COMMENT '品牌',
    `tonnage` DECIMAL(10,2) COMMENT '吨位（吨）',
    `load_capacity` DECIMAL(10,2) COMMENT '载重量（吨）',
    `standard_fuel_consumption` DECIMAL(10,2) COMMENT '标准油耗（升/百公里）',
    `maintenance_interval` INT COMMENT '保养时限（天）',
    `vehicle_type` TINYINT DEFAULT 1 COMMENT '车辆类型：1-普通车辆，2-救援车，3-维修车',
    `description` VARCHAR(500) COMMENT '描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    UNIQUE KEY `uk_model_code` (`model_code`),
    INDEX `idx_model_name` (`model_name`),
    INDEX `idx_vehicle_type` (`vehicle_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车型表';

-- 2.2 车辆表
CREATE TABLE IF NOT EXISTS `vehicle` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '主键UUID',
    `vehicle_model_id` VARCHAR(36) COMMENT '车型ID',
    `license_plate` VARCHAR(20) NOT NULL COMMENT '车牌号',
    `vehicle_identification_number` VARCHAR(50) COMMENT '车辆识别代号（VIN）',
    `engine_number` VARCHAR(50) COMMENT '发动机号',
    `owner` VARCHAR(100) DEFAULT 'MineGuard Org' COMMENT '所有人',
    `usage_type` VARCHAR(50) COMMENT '使用性质',
    `register_date` DATE COMMENT '注册日期',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-空闲，2-忙碌，3-维护，4-故障',
    `fuel_level` DECIMAL(5,2) COMMENT '当前油量（百分比）',
    `current_driver_id` VARCHAR(36) COMMENT '当前司机ID',
    `last_maintenance_date` DATE COMMENT '上次保养日期',
    `next_maintenance_date` DATE COMMENT '下次保养日期',
    `total_mileage` DECIMAL(10,2) DEFAULT 0 COMMENT '总里程（公里）',
    `photo_url` VARCHAR(255) COMMENT '车辆照片URL',
    `vehicle_license_front_url` VARCHAR(255) COMMENT '行驶证正面URL',
    `vehicle_license_back_url` VARCHAR(255) COMMENT '行驶证背面URL',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    UNIQUE KEY `uk_license_plate` (`license_plate`),
    INDEX `idx_vehicle_model_id` (`vehicle_model_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_current_driver_id` (`current_driver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆表';

-- 2.3 车辆保险表
CREATE TABLE IF NOT EXISTS `vehicle_insurance` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '主键UUID',
    `vehicle_id` VARCHAR(36) NOT NULL COMMENT '车辆ID',
    `insurance_company` VARCHAR(100) COMMENT '保险公司',
    `insurance_number` VARCHAR(100) COMMENT '保险单号',
    `insurance_type` TINYINT DEFAULT 1 COMMENT '保险类型：1-交强险，2-商业险',
    `insurance_amount` DECIMAL(12,2) COMMENT '保险金额',
    `start_date` DATE COMMENT '保险开始日期',
    `expiry_date` DATE COMMENT '保险到期日',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-有效，2-过期',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_vehicle_id` (`vehicle_id`),
    INDEX `idx_expiry_date` (`expiry_date`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆保险表';

-- 2.4 车辆保养记录表
CREATE TABLE IF NOT EXISTS `vehicle_maintenance` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '主键UUID',
    `vehicle_id` VARCHAR(36) NOT NULL COMMENT '车辆ID',
    `maintenance_type` TINYINT DEFAULT 1 COMMENT '保养类型：1-常规保养，2-大修，3-故障维修',
    `maintenance_date` DATE NOT NULL COMMENT '保养日期',
    `maintenance_content` VARCHAR(500) COMMENT '保养内容',
    `maintenance_cost` DECIMAL(10,2) COMMENT '保养费用',
    `repairman_id` VARCHAR(36) COMMENT '维修员ID',
    `next_maintenance_date` DATE COMMENT '下次保养日期',
    `mileage` DECIMAL(10,2) COMMENT '保养时里程',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_vehicle_id` (`vehicle_id`),
    INDEX `idx_maintenance_date` (`maintenance_date`),
    INDEX `idx_repairman_id` (`repairman_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆保养记录表';

-- 2.5 车辆故障记录表
CREATE TABLE IF NOT EXISTS `vehicle_fault` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '主键UUID',
    `vehicle_id` VARCHAR(36) NOT NULL COMMENT '车辆ID',
    `fault_type` VARCHAR(50) COMMENT '故障类型',
    `fault_description` VARCHAR(500) COMMENT '故障描述',
    `fault_date` DATETIME NOT NULL COMMENT '故障发生时间',
    `severity` TINYINT DEFAULT 1 COMMENT '严重程度：1-轻微，2-一般，3-严重',
    `status` TINYINT DEFAULT 1 COMMENT '处理状态：1-未处理，2-处理中，3-已处理',
    `repairman_id` VARCHAR(36) COMMENT '处理维修员ID',
    `repair_date` DATETIME COMMENT '修复时间',
    `repair_cost` DECIMAL(10,2) COMMENT '维修费用',
    `repair_content` VARCHAR(500) COMMENT '维修内容',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_vehicle_id` (`vehicle_id`),
    INDEX `idx_fault_date` (`fault_date`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆故障记录表';

-- 2.6 车辆加油记录表
CREATE TABLE IF NOT EXISTS `vehicle_refueling` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '主键UUID',
    `vehicle_id` VARCHAR(36) NOT NULL COMMENT '车辆ID',
    `driver_id` VARCHAR(36) COMMENT '司机ID',
    `refueling_date` DATETIME NOT NULL COMMENT '加油时间',
    `fuel_type` VARCHAR(20) COMMENT '燃油类型',
    `fuel_amount` DECIMAL(10,2) COMMENT '加油量（升）',
    `fuel_price` DECIMAL(10,2) COMMENT '油价（元/升）',
    `total_cost` DECIMAL(10,2) COMMENT '总金额',
    `mileage` DECIMAL(10,2) COMMENT '加油时里程',
    `gas_station` VARCHAR(100) COMMENT '加油站',
    `remark` VARCHAR(255) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_vehicle_id` (`vehicle_id`),
    INDEX `idx_refueling_date` (`refueling_date`),
    INDEX `idx_driver_id` (`driver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆加油记录表';


-- =====================================================
-- 3. 行程域 (trip-service)
-- =====================================================

-- 3.1 路线表
CREATE TABLE IF NOT EXISTS `route` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '主键UUID',
    `route_name` VARCHAR(100) NOT NULL COMMENT '路线名称',
    `start_point` VARCHAR(255) NOT NULL COMMENT '起点',
    `end_point` VARCHAR(255) NOT NULL COMMENT '终点',
    `start_longitude` DOUBLE COMMENT '起点经度',
    `start_latitude` DOUBLE COMMENT '起点纬度',
    `end_longitude` DOUBLE COMMENT '终点经度',
    `end_latitude` DOUBLE COMMENT '终点纬度',
    `distance` DECIMAL(10,2) COMMENT '路线距离（公里）',
    `estimated_time` INT COMMENT '预计时间（分钟）',
    `route_type` TINYINT DEFAULT 1 COMMENT '路线类型：1-常规路线，2-备用路线',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用，2-禁用',
    `waypoints` JSON COMMENT '途经点（JSON格式）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    INDEX `idx_route_name` (`route_name`),
    INDEX `idx_start_point` (`start_point`),
    INDEX `idx_end_point` (`end_point`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='路线表';

-- 3.2 行程表
CREATE TABLE IF NOT EXISTS `trip` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '主键UUID',
    `trip_no` VARCHAR(50) NOT NULL COMMENT '行程编号',
    `vehicle_id` VARCHAR(36) NOT NULL COMMENT '车辆ID',
    `driver_id` VARCHAR(36) NOT NULL COMMENT '司机ID',
    `route_id` VARCHAR(36) COMMENT '路线ID',
    `start_time` DATETIME COMMENT '开始时间',
    `end_time` DATETIME COMMENT '结束时间',
    `start_mileage` DECIMAL(10,2) COMMENT '开始里程',
    `end_mileage` DECIMAL(10,2) COMMENT '结束里程',
    `cargo_weight` DECIMAL(10,2) COMMENT '载货重量（吨）',
    `status` TINYINT DEFAULT 1 COMMENT '行程状态：1-待开始，2-进行中，3-已完成，4-已取消',
    `start_longitude` DOUBLE COMMENT '起点经度',
    `start_latitude` DOUBLE COMMENT '起点纬度',
    `end_longitude` DOUBLE COMMENT '终点经度',
    `end_latitude` DOUBLE COMMENT '终点纬度',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_trip_no` (`trip_no`),
    INDEX `idx_vehicle_id` (`vehicle_id`),
    INDEX `idx_driver_id` (`driver_id`),
    INDEX `idx_route_id` (`route_id`),
    INDEX `idx_start_time` (`start_time`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行程表';

-- 3.3 行程轨迹表（按时间分区）
CREATE TABLE IF NOT EXISTS `trip_track` (
    `id` VARCHAR(36) NOT NULL COMMENT '主键UUID',
    `trip_id` VARCHAR(36) NOT NULL COMMENT '行程ID',
    `vehicle_id` VARCHAR(36) NOT NULL COMMENT '车辆ID',
    `longitude` DOUBLE NOT NULL COMMENT '经度',
    `latitude` DOUBLE NOT NULL COMMENT '纬度',
    `speed` DECIMAL(5,2) COMMENT '速度（km/h）',
    `direction` DECIMAL(5,2) COMMENT '方向（度）',
    `altitude` DECIMAL(8,2) COMMENT '海拔（米）',
    `record_time` DATETIME NOT NULL COMMENT '记录时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`, `record_time`),
    INDEX `idx_trip_id` (`trip_id`),
    INDEX `idx_vehicle_id` (`vehicle_id`),
    INDEX `idx_record_time` (`record_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行程轨迹表'
PARTITION BY RANGE (YEAR(record_time)) (
    PARTITION p2024 VALUES LESS THAN (2025),
    PARTITION p2025 VALUES LESS THAN (2026),
    PARTITION p2026 VALUES LESS THAN (2027),
    PARTITION p2027 VALUES LESS THAN (2028),
    PARTITION p2028 VALUES LESS THAN (2029),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);

-- 3.4 调度计划表
CREATE TABLE IF NOT EXISTS `dispatch_plan` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '主键UUID',
    `plan_no` VARCHAR(50) NOT NULL COMMENT '计划编号',
    `plan_date` DATE NOT NULL COMMENT '计划日期',
    `vehicle_id` VARCHAR(36) NOT NULL COMMENT '车辆ID',
    `driver_id` VARCHAR(36) NOT NULL COMMENT '司机ID',
    `route_id` VARCHAR(36) COMMENT '路线ID',
    `planned_trips` INT DEFAULT 1 COMMENT '计划运输次数',
    `planned_cargo_weight` DECIMAL(10,2) COMMENT '计划运输吨位',
    `start_time_slot` VARCHAR(20) COMMENT '发车时段',
    `status` TINYINT DEFAULT 1 COMMENT '计划状态：1-待执行，2-执行中，3-已完成，4-已取消',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_plan_no` (`plan_no`),
    INDEX `idx_plan_date` (`plan_date`),
    INDEX `idx_vehicle_id` (`vehicle_id`),
    INDEX `idx_driver_id` (`driver_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调度计划表';


-- =====================================================
-- 4. 预警域 (warning-service)
-- =====================================================

-- 4.1 预警规则表
CREATE TABLE IF NOT EXISTS `warning_rule` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '主键UUID',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `rule_code` VARCHAR(50) NOT NULL COMMENT '规则编码',
    `warning_type` TINYINT NOT NULL COMMENT '预警类型：1-超速，2-偏离路线，3-盗卸，4-疲劳驾驶，5-停留超时',
    `warning_level` TINYINT DEFAULT 1 COMMENT '预警级别：1-提示，2-警告，3-严重',
    `threshold_value` VARCHAR(100) COMMENT '阈值（JSON格式）',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用，2-禁用',
    `description` VARCHAR(500) COMMENT '描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_rule_code` (`rule_code`),
    INDEX `idx_warning_type` (`warning_type`),
    INDEX `idx_warning_level` (`warning_level`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警规则表';

-- 4.2 预警记录表（按时间分区）
CREATE TABLE IF NOT EXISTS `warning_record` (
    `id` VARCHAR(36) NOT NULL COMMENT '主键UUID',
    `warning_no` VARCHAR(50) NOT NULL COMMENT '预警编号',
    `rule_id` VARCHAR(36) COMMENT '规则ID',
    `warning_type` TINYINT NOT NULL COMMENT '预警类型：1-超速，2-偏离路线，3-盗卸，4-疲劳驾驶，5-停留超时',
    `warning_level` TINYINT DEFAULT 1 COMMENT '预警级别：1-提示，2-警告，3-严重',
    `vehicle_id` VARCHAR(36) NOT NULL COMMENT '车辆ID',
    `driver_id` VARCHAR(36) COMMENT '司机ID',
    `trip_id` VARCHAR(36) COMMENT '行程ID',
    `longitude` DOUBLE COMMENT '经度',
    `latitude` DOUBLE COMMENT '纬度',
    `speed` DECIMAL(5,2) COMMENT '速度（km/h）',
    `warning_content` VARCHAR(500) COMMENT '预警内容',
    `warning_time` DATETIME NOT NULL COMMENT '预警时间',
    `status` TINYINT DEFAULT 1 COMMENT '处理状态：1-未处理，2-处理中，3-已处理',
    `handler_id` VARCHAR(36) COMMENT '处理人ID',
    `handle_time` DATETIME COMMENT '处理时间',
    `handle_result` VARCHAR(500) COMMENT '处理结果',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`, `warning_time`),
    UNIQUE KEY `uk_warning_no` (`warning_no`, `warning_time`),
    INDEX `idx_rule_id` (`rule_id`),
    INDEX `idx_warning_type` (`warning_type`),
    INDEX `idx_vehicle_id` (`vehicle_id`),
    INDEX `idx_driver_id` (`driver_id`),
    INDEX `idx_warning_time` (`warning_time`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警记录表'
PARTITION BY RANGE (YEAR(warning_time)) (
    PARTITION p2024 VALUES LESS THAN (2025),
    PARTITION p2025 VALUES LESS THAN (2026),
    PARTITION p2026 VALUES LESS THAN (2027),
    PARTITION p2027 VALUES LESS THAN (2028),
    PARTITION p2028 VALUES LESS THAN (2029),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);


-- =====================================================
-- 5. 统计域 (statistics-service)
-- =====================================================

-- 5.1 车辆统计表
CREATE TABLE IF NOT EXISTS `vehicle_statistics` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '主键UUID',
    `vehicle_id` VARCHAR(36) NOT NULL COMMENT '车辆ID',
    `stat_date` DATE NOT NULL COMMENT '统计日期',
    `trip_count` INT DEFAULT 0 COMMENT '出车次数',
    `total_mileage` DECIMAL(10,2) DEFAULT 0 COMMENT '行驶里程（公里）',
    `total_cargo_weight` DECIMAL(10,2) DEFAULT 0 COMMENT '运输吨位',
    `total_fuel_consumption` DECIMAL(10,2) DEFAULT 0 COMMENT '油耗（升）',
    `empty_trip_rate` DECIMAL(5,2) DEFAULT 0 COMMENT '空驶率（%）',
    `fuel_efficiency` DECIMAL(10,2) DEFAULT 0 COMMENT '燃油效率（吨/升）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_vehicle_stat_date` (`vehicle_id`, `stat_date`),
    INDEX `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆统计表';

-- 5.2 司机统计表
CREATE TABLE IF NOT EXISTS `driver_statistics` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '主键UUID',
    `driver_id` VARCHAR(36) NOT NULL COMMENT '司机ID',
    `stat_date` DATE NOT NULL COMMENT '统计日期',
    `attendance_count` INT DEFAULT 0 COMMENT '出勤次数',
    `attendance_hours` DECIMAL(5,2) DEFAULT 0 COMMENT '出勤时长（小时）',
    `trip_count` INT DEFAULT 0 COMMENT '出车次数',
    `total_mileage` DECIMAL(10,2) DEFAULT 0 COMMENT '行驶里程（公里）',
    `violation_count` INT DEFAULT 0 COMMENT '违章次数',
    `warning_count` INT DEFAULT 0 COMMENT '预警次数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_driver_stat_date` (`driver_id`, `stat_date`),
    INDEX `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='司机统计表';

-- 5.3 日报表
CREATE TABLE IF NOT EXISTS `daily_report` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '主键UUID',
    `report_date` DATE NOT NULL COMMENT '报表日期',
    `total_vehicles` INT DEFAULT 0 COMMENT '车辆总数',
    `active_vehicles` INT DEFAULT 0 COMMENT '活跃车辆数',
    `total_drivers` INT DEFAULT 0 COMMENT '司机总数',
    `active_drivers` INT DEFAULT 0 COMMENT '活跃司机数',
    `total_trips` INT DEFAULT 0 COMMENT '总行程数',
    `total_cargo_weight` DECIMAL(12,2) DEFAULT 0 COMMENT '总运输吨位',
    `total_mileage` DECIMAL(12,2) DEFAULT 0 COMMENT '总行驶里程',
    `total_fuel_consumption` DECIMAL(12,2) DEFAULT 0 COMMENT '总油耗',
    `total_warnings` INT DEFAULT 0 COMMENT '总预警数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_report_date` (`report_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日报表';


-- =====================================================
-- 6. 成本域 (cost-service)
-- =====================================================

-- 6.1 成本记录表
CREATE TABLE IF NOT EXISTS `cost_record` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '主键UUID',
    `cost_no` VARCHAR(50) NOT NULL COMMENT '成本编号',
    `cost_type` TINYINT NOT NULL COMMENT '成本类型：1-燃油费，2-维修费，3-保险费，4-人工费，5-其他',
    `vehicle_id` VARCHAR(36) COMMENT '车辆ID',
    `driver_id` VARCHAR(36) COMMENT '司机ID',
    `amount` DECIMAL(12,2) NOT NULL COMMENT '金额',
    `cost_date` DATE NOT NULL COMMENT '成本日期',
    `description` VARCHAR(500) COMMENT '描述',
    `related_trip_id` VARCHAR(36) COMMENT '关联行程ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_cost_no` (`cost_no`),
    INDEX `idx_cost_type` (`cost_type`),
    INDEX `idx_vehicle_id` (`vehicle_id`),
    INDEX `idx_driver_id` (`driver_id`),
    INDEX `idx_cost_date` (`cost_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成本记录表';

-- 6.2 预算表
CREATE TABLE IF NOT EXISTS `budget` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '主键UUID',
    `budget_no` VARCHAR(50) NOT NULL COMMENT '预算编号',
    `budget_year` INT NOT NULL COMMENT '预算年度',
    `budget_month` INT COMMENT '预算月份（为空表示年度预算）',
    `total_budget` DECIMAL(12,2) NOT NULL COMMENT '总预算',
    `fuel_budget` DECIMAL(12,2) DEFAULT 0 COMMENT '燃油预算',
    `maintenance_budget` DECIMAL(12,2) DEFAULT 0 COMMENT '维修预算',
    `insurance_budget` DECIMAL(12,2) DEFAULT 0 COMMENT '保险预算',
    `labor_budget` DECIMAL(12,2) DEFAULT 0 COMMENT '人工预算',
    `other_budget` DECIMAL(12,2) DEFAULT 0 COMMENT '其他预算',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-有效，2-已执行',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_budget_no` (`budget_no`),
    INDEX `idx_budget_year` (`budget_year`),
    INDEX `idx_budget_month` (`budget_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预算表';

-- 6.3 预算执行表
CREATE TABLE IF NOT EXISTS `budget_execution` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '主键UUID',
    `budget_id` VARCHAR(36) NOT NULL COMMENT '预算ID',
    `execution_month` INT NOT NULL COMMENT '执行月份',
    `actual_cost` DECIMAL(12,2) DEFAULT 0 COMMENT '实际支出',
    `execution_rate` DECIMAL(5,2) DEFAULT 0 COMMENT '执行率（%）',
    `variance` DECIMAL(12,2) DEFAULT 0 COMMENT '差异',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_budget_month` (`budget_id`, `execution_month`),
    INDEX `idx_execution_month` (`execution_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预算执行表';


-- =====================================================
-- 7. 文件存储域 (file-service)
-- =====================================================

-- 7.1 文件记录表
CREATE TABLE IF NOT EXISTS `file_record` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '主键UUID',
    `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
    `original_name` VARCHAR(255) COMMENT '原始文件名',
    `file_type` VARCHAR(50) COMMENT '文件类型',
    `file_size` BIGINT COMMENT '文件大小（字节）',
    `file_url` VARCHAR(500) NOT NULL COMMENT '文件URL',
    `bucket_name` VARCHAR(50) NOT NULL COMMENT '存储桶名称',
    `object_key` VARCHAR(255) NOT NULL COMMENT '对象键',
    `business_type` TINYINT COMMENT '业务类型：1-用户头像，2-身份证，3-驾驶证，4-行驶证，5-车辆照片，6-沟通图片，7-其他',
    `business_id` VARCHAR(36) COMMENT '业务ID',
    `uploader_id` VARCHAR(36) COMMENT '上传人ID',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-有效，2-已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_bucket_name` (`bucket_name`),
    INDEX `idx_business_type` (`business_type`),
    INDEX `idx_business_id` (`business_id`),
    INDEX `idx_uploader_id` (`uploader_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件记录表';


-- =====================================================
-- 8. 系统配置域
-- =====================================================

-- 8.1 系统参数表
CREATE TABLE IF NOT EXISTS `system_config` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '主键UUID',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` VARCHAR(500) COMMENT '配置值',
    `config_type` VARCHAR(50) COMMENT '配置类型',
    `description` VARCHAR(255) COMMENT '描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统参数表';

-- 8.2 操作日志表（按时间分区）
CREATE TABLE IF NOT EXISTS `operation_log` (
    `id` VARCHAR(36) NOT NULL COMMENT '主键UUID',
    `user_id` VARCHAR(36) COMMENT '用户ID',
    `username` VARCHAR(50) COMMENT '用户名',
    `operation_type` VARCHAR(50) COMMENT '操作类型',
    `operation_desc` VARCHAR(255) COMMENT '操作描述',
    `request_method` VARCHAR(10) COMMENT '请求方法',
    `request_url` VARCHAR(255) COMMENT '请求URL',
    `request_params` TEXT COMMENT '请求参数',
    `response_data` TEXT COMMENT '响应数据',
    `ip_address` VARCHAR(50) COMMENT 'IP地址',
    `user_agent` VARCHAR(500) COMMENT '用户代理',
    `execution_time` INT COMMENT '执行时间（毫秒）',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-成功，2-失败',
    `error_msg` VARCHAR(500) COMMENT '错误信息',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`, `create_time`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_operation_type` (`operation_type`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表'
PARTITION BY RANGE (YEAR(create_time)) (
    PARTITION p2024 VALUES LESS THAN (2025),
    PARTITION p2025 VALUES LESS THAN (2026),
    PARTITION p2026 VALUES LESS THAN (2027),
    PARTITION p2027 VALUES LESS THAN (2028),
    PARTITION p2028 VALUES LESS THAN (2029),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);


-- =====================================================
-- 初始化数据
-- =====================================================


-- 初始化预警规则数据
INSERT INTO `warning_rule` (`id`, `rule_name`, `rule_code`, `warning_type`, `warning_level`, `threshold_value`, `description`) VALUES
(UUID(), '超速预警', 'SPEED_WARNING', 1, 2, '{"maxSpeed": 80}', '车辆速度超过80km/h时触发'),
(UUID(), '偏离路线预警', 'ROUTE_DEVIATION', 2, 2, '{"maxDeviation": 100}', '车辆偏离路线100米以上时触发'),
(UUID(), '盗卸预警', 'THEFT_WARNING', 3, 3, '{"unloadDetection": true}', '检测到异常卸载时触发'),
(UUID(), '疲劳驾驶预警', 'FATIGUE_WARNING', 4, 2, '{"maxDrivingHours": 4}', '连续驾驶超过4小时时触发'),
(UUID(), '停留超时预警', 'STOP_TIMEOUT', 5, 1, '{"maxStopMinutes": 30}', '停留超过30分钟时触发');

-- 初始化系统参数
INSERT INTO `system_config` (`id`, `config_key`, `config_value`, `config_type`, `description`) VALUES
(UUID(), 'system.name', 'MineGuard', 'string', '系统名称'),
(UUID(), 'system.version', '1.0.0', 'string', '系统版本'),
(UUID(), 'vehicle.max_speed', '80', 'int', '车辆最大限速（km/h）'),
(UUID(), 'vehicle.fatigue_hours', '4', 'int', '疲劳驾驶阈值（小时）'),
(UUID(), 'vehicle.stop_timeout', '30', 'int', '停留超时阈值（分钟）'),
(UUID(), 'map.default_zoom', '12', 'int', '地图默认缩放级别');

-- 注意：管理员用户初始化
-- 请通过系统的初始化脚本或管理界面创建管理员用户
-- 避免在数据库文件中直接存储密码哈希值
-- 推荐使用系统提供的密码设置功能，确保与系统加密逻辑一致

-- 系统启动后，可通过以下方式初始化管理员：
-- 1. 运行系统提供的初始化脚本
-- 2. 通过管理界面的用户管理功能创建管理员
-- 3. 使用系统API创建管理员账号
