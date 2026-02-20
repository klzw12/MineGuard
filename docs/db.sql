-- 数据库优化方案

-- 1. 车辆表优化
ALTER TABLE car ADD COLUMN insurance_company VARCHAR(100) COMMENT '保险公司';
ALTER TABLE car ADD COLUMN insurance_number VARCHAR(100) COMMENT '保险单号';
ALTER TABLE car ADD COLUMN insurance_expiry DATE COMMENT '保险到期日';

-- 2. 优化索引
ALTER TABLE car ADD INDEX idx_car_number (car_number);
ALTER TABLE car ADD INDEX idx_vehicle_type (vehicle_type);
ALTER TABLE car ADD INDEX idx_status (status);
ALTER TABLE driver ADD INDEX idx_driver_name (driver_name);
ALTER TABLE driver ADD INDEX idx_license_number (license_number);
ALTER TABLE trip_record ADD INDEX idx_car_id (car_id);
ALTER TABLE trip_record ADD INDEX idx_driver_id (driver_id);
ALTER TABLE trip_record ADD INDEX idx_start_time (start_time);
ALTER TABLE trip_record ADD INDEX idx_end_time (end_time);
ALTER TABLE warning_record ADD INDEX idx_car_id (car_id);
ALTER TABLE warning_record ADD INDEX idx_warning_time (warning_time);
ALTER TABLE warning_record ADD INDEX idx_warning_type (warning_type);

-- 3. 车辆实时状态表（解决频繁更新问题）
CREATE TABLE vehicle_realtime_status (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    car_id BIGINT NOT NULL COMMENT '车辆ID',
    longitude DOUBLE COMMENT '经度',
    latitude DOUBLE COMMENT '纬度',
    speed DOUBLE COMMENT '速度',
    status VARCHAR(20) COMMENT '状态',
    fuel_level DOUBLE COMMENT '油量',
    last_update_time DATETIME NOT NULL COMMENT '最后更新时间',
    FOREIGN KEY (car_id) REFERENCES car(id) ON DELETE CASCADE
) COMMENT '车辆实时状态表';

-- 为车辆实时状态表添加索引
ALTER TABLE vehicle_realtime_status ADD INDEX idx_car_id (car_id);
ALTER TABLE vehicle_realtime_status ADD INDEX idx_last_update_time (last_update_time);

-- 4. 分区表设计
-- 为行程记录表添加分区
ALTER TABLE trip_record PARTITION BY RANGE (YEAR(start_time)) (
    PARTITION p2024 VALUES LESS THAN (2025),
    PARTITION p2025 VALUES LESS THAN (2026),
    PARTITION p2026 VALUES LESS THAN (2027),
    PARTITION p2027 VALUES LESS THAN (2028),
    PARTITION p2028 VALUES LESS THAN (2029),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);

-- 为预警记录表添加分区
ALTER TABLE warning_record PARTITION BY RANGE (YEAR(warning_time)) (
    PARTITION p2024 VALUES LESS THAN (2025),
    PARTITION p2025 VALUES LESS THAN (2026),
    PARTITION p2026 VALUES LESS THAN (2027),
    PARTITION p2027 VALUES LESS THAN (2028),
    PARTITION p2028 VALUES LESS THAN (2029),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);

-- 5. 车辆统计数据表优化
ALTER TABLE vehicle_statistics ADD COLUMN empty_trip_rate DOUBLE COMMENT '空驶率';

-- 6. 成本分析表优化
ALTER TABLE cost_analysis ADD COLUMN accident_cost DECIMAL(10,2) COMMENT '事故成本';
ALTER TABLE cost_analysis ADD COLUMN maintenance_cost DECIMAL(10,2) COMMENT '维修成本';

-- 7. 司机绩效表添加索引
ALTER TABLE driver_performance ADD INDEX idx_driver_id (driver_id);
ALTER TABLE driver_performance ADD INDEX idx_period (period);

-- 8. 路线表添加索引
ALTER TABLE route ADD INDEX idx_start_point (start_point);
ALTER TABLE route ADD INDEX idx_end_point (end_point);

-- 9. 优化查询性能
-- 分析表并更新统计信息
ANALYZE TABLE car, driver, trip_record, warning_record, vehicle_statistics, cost_analysis, driver_performance, route;

-- 优化表结构
OPTIMIZE TABLE car, driver, trip_record, warning_record, vehicle_statistics, cost_analysis, driver_performance, route;