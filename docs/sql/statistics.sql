-- 行程统计表
CREATE TABLE IF NOT EXISTS `trip_statistics` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `statistics_date` DATE NOT NULL COMMENT '统计日期',
    `trip_count` INT NOT NULL DEFAULT 0 COMMENT '行程数量',
    `total_distance` DECIMAL(12,2) DEFAULT 0 COMMENT '总行驶距离(公里)',
    `total_duration` DECIMAL(12,2) DEFAULT 0 COMMENT '总行驶时长(小时)',
    `completed_trip_count` INT DEFAULT 0 COMMENT '完成行程数',
    `cancelled_trip_count` INT DEFAULT 0 COMMENT '取消行程数',
    `average_speed` DECIMAL(8,2) DEFAULT 0 COMMENT '平均速度(km/h)',
    `fuel_consumption` DECIMAL(12,2) DEFAULT 0 COMMENT '燃油消耗(升)',
    `cargo_weight` DECIMAL(12,2) DEFAULT 0 COMMENT '货物运输量(吨)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_statistics_date` (`statistics_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行程统计表';

-- 成本统计表
CREATE TABLE IF NOT EXISTS `cost_statistics` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `statistics_date` DATE NOT NULL COMMENT '统计日期',
    `fuel_cost` DECIMAL(12,2) DEFAULT 0 COMMENT '燃油成本',
    `maintenance_cost` DECIMAL(12,2) DEFAULT 0 COMMENT '维修成本',
    `labor_cost` DECIMAL(12,2) DEFAULT 0 COMMENT '人工成本',
    `insurance_cost` DECIMAL(12,2) DEFAULT 0 COMMENT '保险成本',
    `depreciation_cost` DECIMAL(12,2) DEFAULT 0 COMMENT '折旧成本',
    `management_cost` DECIMAL(12,2) DEFAULT 0 COMMENT '管理成本',
    `other_cost` DECIMAL(12,2) DEFAULT 0 COMMENT '其他成本',
    `total_cost` DECIMAL(12,2) NOT NULL COMMENT '总成本',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_statistics_date` (`statistics_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成本统计表';

-- 车辆统计表
CREATE TABLE IF NOT EXISTS `vehicle_statistics` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `vehicle_id` BIGINT NOT NULL COMMENT '车辆ID',
    `statistics_date` DATE NOT NULL COMMENT '统计日期',
    `trip_count` INT DEFAULT 0 COMMENT '行程数量',
    `total_distance` DECIMAL(12,2) DEFAULT 0 COMMENT '总行驶距离(公里)',
    `total_duration` DECIMAL(12,2) DEFAULT 0 COMMENT '总行驶时长(小时)',
    `cargo_weight` DECIMAL(12,2) DEFAULT 0 COMMENT '货物运输量(吨)',
    `fuel_consumption` DECIMAL(12,2) DEFAULT 0 COMMENT '燃油消耗(升)',
    `fuel_cost` DECIMAL(12,2) DEFAULT 0 COMMENT '燃油成本',
    `maintenance_count` INT DEFAULT 0 COMMENT '维修次数',
    `maintenance_cost` DECIMAL(12,2) DEFAULT 0 COMMENT '维修成本',
    `warning_count` INT DEFAULT 0 COMMENT '预警次数',
    `violation_count` INT DEFAULT 0 COMMENT '违规次数',
    `idle_duration` DECIMAL(12,2) DEFAULT 0 COMMENT '怠速时长(小时)',
    `idle_distance` DECIMAL(12,2) DEFAULT 0 COMMENT '空驶距离(公里)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_vehicle_date` (`vehicle_id`, `statistics_date`),
    KEY `idx_statistics_date` (`statistics_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆统计表';

-- 司机统计表
CREATE TABLE IF NOT EXISTS `driver_statistics` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `statistics_date` DATE NOT NULL COMMENT '统计日期',
    `attendance_days` INT DEFAULT 0 COMMENT '出勤天数',
    `attendance_hours` DECIMAL(12,2) DEFAULT 0 COMMENT '出勤时长(小时)',
    `trip_count` INT DEFAULT 0 COMMENT '行程数量',
    `total_distance` DECIMAL(12,2) DEFAULT 0 COMMENT '总行驶距离(公里)',
    `cargo_weight` DECIMAL(12,2) DEFAULT 0 COMMENT '货物运输量(吨)',
    `late_count` INT DEFAULT 0 COMMENT '迟到次数',
    `early_leave_count` INT DEFAULT 0 COMMENT '早退次数',
    `warning_count` INT DEFAULT 0 COMMENT '预警次数',
    `violation_count` INT DEFAULT 0 COMMENT '违规次数',
    `over_speed_count` INT DEFAULT 0 COMMENT '超速次数',
    `route_deviation_count` INT DEFAULT 0 COMMENT '路线偏离次数',
    `performance_score` DECIMAL(5,2) DEFAULT 100 COMMENT '绩效评分',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `statistics_date`),
    KEY `idx_statistics_date` (`statistics_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='司机统计表';

-- 运输统计表
CREATE TABLE IF NOT EXISTS `transport_statistics` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `statistics_date` DATE NOT NULL COMMENT '统计日期',
    `total_cargo_weight` DECIMAL(12,2) DEFAULT 0 COMMENT '总货物运输量(吨)',
    `total_trips` INT DEFAULT 0 COMMENT '总行程数',
    `total_vehicles` INT DEFAULT 0 COMMENT '参与车辆数',
    `total_drivers` INT DEFAULT 0 COMMENT '参与司机数',
    `avg_cargo_per_trip` DECIMAL(12,2) DEFAULT 0 COMMENT '平均每趟货运量(吨)',
    `avg_trips_per_vehicle` DECIMAL(8,2) DEFAULT 0 COMMENT '平均每车行程数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_statistics_date` (`statistics_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运输统计表';

-- 故障统计表
CREATE TABLE IF NOT EXISTS `fault_statistics` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `statistics_date` DATE NOT NULL COMMENT '统计日期',
    `vehicle_id` BIGINT COMMENT '车辆ID（为空表示总体统计）',
    `fault_count` INT DEFAULT 0 COMMENT '故障总数',
    `minor_fault_count` INT DEFAULT 0 COMMENT '轻微故障数',
    `major_fault_count` INT DEFAULT 0 COMMENT '一般故障数',
    `critical_fault_count` INT DEFAULT 0 COMMENT '严重故障数',
    `total_repair_cost` DECIMAL(12,2) DEFAULT 0 COMMENT '总维修费用',
    `avg_repair_time` DECIMAL(8,2) DEFAULT 0 COMMENT '平均维修时长(小时)',
    `top_fault_type` VARCHAR(50) COMMENT '主要故障类型',
    `top_fault_count` INT DEFAULT 0 COMMENT '主要故障数量',
    `repaired_count` INT DEFAULT 0 COMMENT '已修复数量',
    `pending_count` INT DEFAULT 0 COMMENT '待修复数量',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_statistics_date` (`statistics_date`),
    KEY `idx_vehicle_id` (`vehicle_id`),
    UNIQUE KEY `uk_vehicle_date` (`vehicle_id`, `statistics_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='故障统计表';
