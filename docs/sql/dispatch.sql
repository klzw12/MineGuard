-- =====================================================
-- MineGuard 调度域数据库设计
-- 模块: dispatch-service
-- 版本: 2.1
-- 日期: 2026-03-22
-- 说明: 调度域相关表结构，包含路线模板、调度计划和调度任务等
-- =====================================================

-- =====================================================
-- 1. 路线模板表
-- 说明: 存储常用的运输路线模板，方便快速选择
-- 实际导航使用高德地图API动态规划，此表仅为快捷选择用途
-- =====================================================
CREATE TABLE IF NOT EXISTS `route_template` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `route_name` VARCHAR(100) NOT NULL COMMENT '路线名称',
    `start_location` VARCHAR(255) NOT NULL COMMENT '起始位置名称',
    `end_location` VARCHAR(255) NOT NULL COMMENT '结束位置名称',
    `start_longitude` DOUBLE NOT NULL COMMENT '起始经度',
    `start_latitude` DOUBLE NOT NULL COMMENT '起始纬度',
    `end_longitude` DOUBLE NOT NULL COMMENT '结束经度',
    `end_latitude` DOUBLE NOT NULL COMMENT '结束纬度',
    `distance` DECIMAL(10,2) COMMENT '参考距离(公里)',
    `estimated_duration` INT COMMENT '参考时长(分钟)',
    `status` INT DEFAULT 1 COMMENT '状态：1-启用，2-禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    INDEX `idx_route_name` (`route_name`),
    INDEX `idx_status` (`status`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='路线模板表';

-- =====================================================
-- 2. 调度计划表
-- 说明: 调度计划是宏观计划，按日/周/月制定车辆运输计划
-- 指定起点和终点坐标，实际导航使用高德地图API
-- 一个计划可生成多个调度任务（如计划跑3趟则生成3个任务）
-- =====================================================
CREATE TABLE IF NOT EXISTS `dispatch_plan` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `plan_no` VARCHAR(50) NOT NULL COMMENT '计划编号',
    `plan_name` VARCHAR(100) COMMENT '计划名称',
    `plan_date` DATE NOT NULL COMMENT '计划日期',
    `plan_type` INT DEFAULT 1 COMMENT '计划类型：1-运输计划，2-维修计划，3-巡检计划',
    `route_id` BIGINT COMMENT '路线模板ID',
    `start_location` VARCHAR(255) COMMENT '起始位置名称',
    `start_longitude` DOUBLE COMMENT '起始经度',
    `start_latitude` DOUBLE COMMENT '起始纬度',
    `end_location` VARCHAR(255) COMMENT '结束位置名称',
    `end_longitude` DOUBLE COMMENT '结束经度',
    `end_latitude` DOUBLE COMMENT '结束纬度',
    `planned_trips` INT DEFAULT 1 COMMENT '计划行程次数',
    `completed_trips` INT DEFAULT 0 COMMENT '已完成行程次数',
    `planned_cargo_weight` DECIMAL(10,2) COMMENT '计划货物重量(吨)',
    `start_time_slot` VARCHAR(50) COMMENT '开始时间段',
    `end_time_slot` VARCHAR(50) COMMENT '结束时间段',
    `status` INT DEFAULT 0 COMMENT '状态：0-待分配，1-已分配，2-执行中，3-已完成，4-已取消',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    UNIQUE KEY `uk_plan_no` (`plan_no`),
    INDEX `idx_plan_date` (`plan_date`),
    INDEX `idx_plan_type` (`plan_type`),
    INDEX `idx_route_id` (`route_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调度计划表';

-- =====================================================
-- 3. 运输任务表 (dispatch_task_transport)
-- 说明: 司机执行的运输任务，包含货物信息
-- =====================================================
CREATE TABLE IF NOT EXISTS `dispatch_task_transport` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `task_no` VARCHAR(50) NOT NULL COMMENT '任务编号',
    `plan_id` BIGINT COMMENT '调度计划ID',
    `trip_id` BIGINT COMMENT '行程ID(司机接单后关联)',
    `route_id` BIGINT COMMENT '路线模板ID',
    `task_sequence` INT DEFAULT 1 COMMENT '任务序号(第几趟)',
    `vehicle_id` BIGINT COMMENT '分配车辆ID',
    `executor_id` BIGINT NOT NULL COMMENT '执行司机ID',
    `start_location` VARCHAR(255) COMMENT '起始位置名称',
    `start_longitude` DOUBLE COMMENT '起始经度',
    `start_latitude` DOUBLE COMMENT '起始纬度',
    `end_location` VARCHAR(255) COMMENT '结束位置名称',
    `end_longitude` DOUBLE COMMENT '结束经度',
    `end_latitude` DOUBLE COMMENT '结束纬度',
    `cargo_weight` DECIMAL(10,2) COMMENT '货物重量(吨)',
    `estimated_commission_amount` DECIMAL(10,2) COMMENT '预计提成金额（调度时设定）',
    `cargo_type` VARCHAR(50) COMMENT '货物类型',
    `scheduled_start_time` DATETIME COMMENT '计划开始时间',
    `scheduled_end_time` DATETIME COMMENT '计划结束时间',
    `actual_start_time` DATETIME COMMENT '实际开始时间',
    `actual_end_time` DATETIME COMMENT '实际结束时间',
    `status` INT DEFAULT 0 COMMENT '状态：0-待接单，1-已接单，2-执行中，3-已完成，4-已取消',
    `priority` VARCHAR(20) DEFAULT 'normal' COMMENT '优先级：low-低，normal-正常，high-高，urgent-紧急',
    `push_time` DATETIME COMMENT '推送时间',
    `accept_time` DATETIME COMMENT '接单时间',
    `description` TEXT COMMENT '描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    UNIQUE KEY `uk_task_no` (`task_no`),
    INDEX `idx_plan_id` (`plan_id`),
    INDEX `idx_trip_id` (`trip_id`),
    INDEX `idx_route_id` (`route_id`),
    INDEX `idx_vehicle_id` (`vehicle_id`),
    INDEX `idx_executor_id` (`executor_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_scheduled_start_time` (`scheduled_start_time`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运输任务表';

-- =====================================================
-- 4. 维修任务表 (dispatch_task_maintenance)
-- 说明: 维修员执行的维修任务，包含故障信息
-- =====================================================
CREATE TABLE IF NOT EXISTS `dispatch_task_maintenance` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `task_no` VARCHAR(50) NOT NULL COMMENT '任务编号',
    `plan_id` BIGINT COMMENT '调度计划ID',
    `vehicle_id` BIGINT NOT NULL COMMENT '待维修车辆ID',
    `repairman_vehicle_id` BIGINT COMMENT '维修员专用车辆ID',
    `executor_id` BIGINT NOT NULL COMMENT '执行维修员ID',
    `fault_type` INT COMMENT '故障类型：1-发动机故障，2-制动系统，3-轮胎问题，4-电气系统，5-液压系统，6-其他',
    `fault_level` INT DEFAULT 1 COMMENT '故障级别：1-轻微，2-一般，3-严重，4-紧急',
    `fault_description` TEXT COMMENT '故障描述',
    `fault_location` VARCHAR(255) COMMENT '故障发生位置',
    `fault_longitude` DOUBLE COMMENT '故障发生经度',
    `fault_latitude` DOUBLE COMMENT '故障发生纬度',
    `repair_location` VARCHAR(255) COMMENT '维修地点',
    `repair_longitude` DOUBLE COMMENT '维修地点经度',
    `repair_latitude` DOUBLE COMMENT '维修地点纬度',
    `scheduled_start_time` DATETIME COMMENT '计划开始时间',
    `scheduled_end_time` DATETIME COMMENT '计划结束时间',
    `actual_start_time` DATETIME COMMENT '实际开始时间',
    `actual_end_time` DATETIME COMMENT '实际结束时间',
    `repair_result` TEXT COMMENT '维修结果',
    `repair_cost` DECIMAL(10,2) COMMENT '维修费用',
    `status` INT DEFAULT 0 COMMENT '状态：0-待接单，1-已接单，2-执行中，3-已完成，4-已取消',
    `priority` VARCHAR(20) DEFAULT 'normal' COMMENT '优先级：low-低，normal-正常，high-高，urgent-紧急',
    `push_time` DATETIME COMMENT '推送时间',
    `accept_time` DATETIME COMMENT '接单时间',
    `description` TEXT COMMENT '描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    UNIQUE KEY `uk_task_no` (`task_no`),
    INDEX `idx_plan_id` (`plan_id`),
    INDEX `idx_vehicle_id` (`vehicle_id`),
    INDEX `idx_repairman_vehicle_id` (`repairman_vehicle_id`),
    INDEX `idx_executor_id` (`executor_id`),
    INDEX `idx_fault_type` (`fault_type`),
    INDEX `idx_fault_level` (`fault_level`),
    INDEX `idx_status` (`status`),
    INDEX `idx_scheduled_start_time` (`scheduled_start_time`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修任务表';

-- =====================================================
-- 5. 巡检任务表 (dispatch_task_inspection)
-- 说明: 安全员执行的巡检任务，包含巡检信息
-- =====================================================
CREATE TABLE IF NOT EXISTS `dispatch_task_inspection` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `task_no` VARCHAR(50) NOT NULL COMMENT '任务编号',
    `plan_id` BIGINT COMMENT '调度计划ID',
    `vehicle_id` BIGINT COMMENT '巡检车辆ID(可选，车辆巡检时填写)',
    `executor_id` BIGINT NOT NULL COMMENT '执行安全员ID',
    `inspection_type` INT COMMENT '巡检类型：1-车辆巡检，2-路线巡检，3-矿区巡检，4-设备巡检',
    `inspection_area` VARCHAR(255) COMMENT '巡检区域',
    `inspection_points` TEXT COMMENT '巡检点位(JSON数组)',
    `start_location` VARCHAR(255) COMMENT '起始位置',
    `start_longitude` DOUBLE COMMENT '起始经度',
    `start_latitude` DOUBLE COMMENT '起始纬度',
    `end_location` VARCHAR(255) COMMENT '结束位置',
    `end_longitude` DOUBLE COMMENT '结束经度',
    `end_latitude` DOUBLE COMMENT '结束纬度',
    `scheduled_start_time` DATETIME COMMENT '计划开始时间',
    `scheduled_end_time` DATETIME COMMENT '计划结束时间',
    `actual_start_time` DATETIME COMMENT '实际开始时间',
    `actual_end_time` DATETIME COMMENT '实际结束时间',
    `inspection_result` TEXT COMMENT '巡检结果',
    `issues_found` TEXT COMMENT '发现问题(JSON数组)',
    `status` INT DEFAULT 0 COMMENT '状态：0-待接单，1-已接单，2-执行中，3-已完成，4-已取消',
    `priority` VARCHAR(20) DEFAULT 'normal' COMMENT '优先级：low-低，normal-正常，high-高，urgent-紧急',
    `push_time` DATETIME COMMENT '推送时间',
    `accept_time` DATETIME COMMENT '接单时间',
    `description` TEXT COMMENT '描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    UNIQUE KEY `uk_task_no` (`task_no`),
    INDEX `idx_plan_id` (`plan_id`),
    INDEX `idx_vehicle_id` (`vehicle_id`),
    INDEX `idx_executor_id` (`executor_id`),
    INDEX `idx_inspection_type` (`inspection_type`),
    INDEX `idx_status` (`status`),
    INDEX `idx_scheduled_start_time` (`scheduled_start_time`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡检任务表';

-- 初始化路线模板数据
INSERT INTO `route_template` (`id`, `route_name`, `start_location`, `end_location`, `start_longitude`, `start_latitude`, `end_longitude`, `end_latitude`, `distance`, `estimated_duration`, `status`) VALUES
(1, '矿区A到矿区B', '矿区A', '矿区B', 116.397428, 39.90923, 116.417428, 39.91923, 5.5, 15, 1),
(2, '矿区B到仓库', '矿区B', '仓库', 116.417428, 39.91923, 116.437428, 39.92923, 8.2, 20, 1);
