-- 行程服务模块数据库表结构

-- 路线表
CREATE TABLE `route` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '路线ID',
  `route_name` varchar(100) NOT NULL COMMENT '路线名称',
  `start_location` varchar(255) DEFAULT NULL COMMENT '起点位置',
  `end_location` varchar(255) DEFAULT NULL COMMENT '终点位置',
  `estimated_duration` int(11) DEFAULT NULL COMMENT '预计时长(分钟)',
  `distance` double DEFAULT NULL COMMENT '距离(公里)',
  `description` varchar(500) DEFAULT NULL COMMENT '路线描述',
  `status` int(1) DEFAULT 1 COMMENT '状态(1-启用, 2-禁用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_route_name` (`route_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='路线表';

-- 行程表
CREATE TABLE `trip` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '行程ID',
  `trip_no` varchar(50) NOT NULL COMMENT '行程编号',
  `vehicle_id` bigint(20) NOT NULL COMMENT '车辆ID',
  `driver_id` bigint(20) NOT NULL COMMENT '司机ID',
  `route_id` bigint(20) DEFAULT NULL COMMENT '路线ID',
  `start_longitude` double DEFAULT NULL COMMENT '起点经度',
  `start_latitude` double DEFAULT NULL COMMENT '起点纬度',
  `end_longitude` double DEFAULT NULL COMMENT '终点经度',
  `end_latitude` double DEFAULT NULL COMMENT '终点纬度',
  `start_location` varchar(255) DEFAULT NULL COMMENT '起点位置',
  `end_location` varchar(255) DEFAULT NULL COMMENT '终点位置',
  `estimated_start_time` datetime DEFAULT NULL COMMENT '预计开始时间',
  `estimated_end_time` datetime DEFAULT NULL COMMENT '预计结束时间',
  `actual_start_time` datetime DEFAULT NULL COMMENT '实际开始时间',
  `actual_end_time` datetime DEFAULT NULL COMMENT '实际结束时间',
  `trip_type` varchar(20) DEFAULT NULL COMMENT '行程类型',
  `estimated_mileage` double DEFAULT NULL COMMENT '预计里程(公里)',
  `actual_mileage` double DEFAULT NULL COMMENT '实际里程(公里)',
  `estimated_duration` int(11) DEFAULT NULL COMMENT '预计时长(分钟)',
  `actual_duration` int(11) DEFAULT NULL COMMENT '实际时长(分钟)',
  `fuel_consumption` double DEFAULT NULL COMMENT '燃油消耗(升)',
  `average_speed` double DEFAULT NULL COMMENT '平均速度(km/h)',
  `cancellation_reason` varchar(500) DEFAULT NULL COMMENT '取消原因',
  `status` int(1) DEFAULT 0 COMMENT '状态(0-待开始, 1-已接单, 2-进行中, 3-已完成, 4-已取消)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_trip_no` (`trip_no`),
  KEY `idx_vehicle_id` (`vehicle_id`),
  KEY `idx_driver_id` (`driver_id`),
  KEY `idx_route_id` (`route_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='行程表';

-- 行程轨迹表
CREATE TABLE `trip_track` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '轨迹ID',
  `trip_id` bigint(20) NOT NULL COMMENT '行程ID',
  `longitude` double NOT NULL COMMENT '经度',
  `latitude` double NOT NULL COMMENT '纬度',
  `speed` double DEFAULT NULL COMMENT '速度(km/h)',
  `direction` double DEFAULT NULL COMMENT '方向(度)',
  `altitude` double DEFAULT NULL COMMENT ' altitude(米)',
  `record_time` bigint(20) NOT NULL COMMENT '记录时间戳',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_trip_id` (`trip_id`),
  KEY `idx_record_time` (`record_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='行程轨迹表';

-- 行程预警表
CREATE TABLE `trip_alert` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '预警ID',
  `trip_id` bigint(20) NOT NULL COMMENT '行程ID',
  `alert_type` varchar(50) NOT NULL COMMENT '预警类型',
  `alert_level` varchar(20) NOT NULL COMMENT '预警级别(low, medium, high)',
  `alert_message` varchar(500) NOT NULL COMMENT '预警消息',
  `longitude` double DEFAULT NULL COMMENT '预警位置经度',
  `latitude` double DEFAULT NULL COMMENT '预警位置纬度',
  `status` varchar(20) DEFAULT 'unhandled' COMMENT '状态(unhandled, handled)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `handler_id` bigint(20) DEFAULT NULL COMMENT '处理人ID',
  `handle_result` varchar(500) DEFAULT NULL COMMENT '处理结果',
  PRIMARY KEY (`id`),
  KEY `idx_trip_id` (`trip_id`),
  KEY `idx_alert_level` (`alert_level`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='行程预警表';

-- 行程通知表
CREATE TABLE `trip_notification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `trip_id` bigint(20) NOT NULL COMMENT '行程ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `notification_type` varchar(50) NOT NULL COMMENT '通知类型',
  `notification_content` varchar(500) NOT NULL COMMENT '通知内容',
  `status` varchar(20) DEFAULT 'unread' COMMENT '状态(unread, read)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  PRIMARY KEY (`id`),
  KEY `idx_trip_id` (`trip_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='行程通知表';
