-- Active: 1772211149535@@127.0.0.1@3306@mineguard_test
-- =====================================================
-- MineGuard 用户域数据库设计
-- 模块: user-service
-- 版本: 1.1
-- 日期: 2026-03-15
-- 说明: 用户域相关表结构，包含用户、角色、权限、人员信息等
-- =====================================================
drop database if exists mineguard_test;
drop database if exists MineGuard;

create database if not exists MineGuard;
use MineGuard;

create database if not exists mineguard_test;
use mineguard_test;
-- =====================================================
-- =====================================================
-- MineGuard 用户域数据库设计
-- 模块: user-service
-- 版本: 1.1
-- 日期: 2026-03-15
-- 说明: 用户域相关表结构，包含用户、角色、权限、人员信息等
-- =====================================================

-- =====================================================
-- 1. 用户表
-- =====================================================
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(50) COMMENT '真实姓名',
    `id_card` VARCHAR(255) COMMENT '身份证号（AES加密）',
    `gender` TINYINT COMMENT '性别：1-男，2-女',
    `nation` VARCHAR(20) COMMENT '民族',
    `birth_date` VARCHAR(20) COMMENT '出生日期',
    `address` VARCHAR(255) COMMENT '住址',
    `phone` VARCHAR(20) COMMENT '手机号',
    `email` VARCHAR(100) COMMENT '邮箱',
    `avatar_url` VARCHAR(255) COMMENT '头像URL',
    `id_card_front_url` VARCHAR(255) COMMENT '身份证正面URL',
    `id_card_back_url` VARCHAR(255) COMMENT '身份证背面URL',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `role_id` BIGINT COMMENT '角色ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    INDEX `idx_status` (`status`),
    INDEX `idx_role_id` (`role_id`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =====================================================
-- 2. 角色表
-- =====================================================
CREATE TABLE IF NOT EXISTS `role` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `description` VARCHAR(255) COMMENT '描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    UNIQUE KEY `uk_role_code` (`role_code`),
    INDEX `idx_role_name` (`role_name`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- =====================================================
-- 3. 权限表（预留，未来实现）
-- =====================================================
CREATE TABLE IF NOT EXISTS `permission` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `permission_name` VARCHAR(50) NOT NULL COMMENT '权限名称',
    `permission_code` VARCHAR(100) NOT NULL COMMENT '权限编码',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父权限ID',
    `permission_type` TINYINT DEFAULT 1 COMMENT '权限类型：1-菜单，2-按钮，3-接口',
    `path` VARCHAR(255) COMMENT '路径',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_permission_type` (`permission_type`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- =====================================================
-- 4. 角色权限关联表
-- =====================================================
CREATE TABLE IF NOT EXISTS `role_permission` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    INDEX `idx_role_id` (`role_id`),
    INDEX `idx_permission_id` (`permission_id`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- =====================================================
-- 5. 司机信息表
-- 说明：司机信息通过实名认证+资格认证被动创建，不可主动创建
-- =====================================================
CREATE TABLE IF NOT EXISTS `driver` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `driver_name` VARCHAR(50) NOT NULL COMMENT '司机姓名',
    `gender` TINYINT COMMENT '性别：1-男，2-女',
    `id_card` VARCHAR(255) NOT NULL COMMENT '身份证号（AES加密）',
    `id_card_front_url` VARCHAR(255) COMMENT '身份证正面URL',
    `id_card_back_url` VARCHAR(255) COMMENT '身份证背面URL',
    `driving_license_url` VARCHAR(255) COMMENT '驾驶证URL',
    `license_type` VARCHAR(10) COMMENT '准驾车型',
    `belonging_team` VARCHAR(100) COMMENT '隶属班组',
    `address` VARCHAR(255) COMMENT '住址',
    `birth_date` VARCHAR(20) COMMENT '出生日期',
    `first_issue_date` VARCHAR(20) COMMENT '初次领证日期',
    `driving_years` INTEGER COMMENT '驾龄（年）',
    `valid_period` VARCHAR(50) COMMENT '有效期限',
    `license_number` VARCHAR(50) COMMENT '驾驶证号',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-离职，1-在职',
    `score` INT DEFAULT 10 COMMENT '司机评分：默认10分，最低10分，满分100分',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    UNIQUE KEY `uk_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_score` (`score`),
    INDEX `idx_belonging_team` (`belonging_team`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='司机信息表';

-- =====================================================
-- 5.1 司机常用车辆关系表
-- 说明：记录司机与车辆的常用关系，一个司机可以有多个常用车辆
-- =====================================================
CREATE TABLE IF NOT EXISTS `driver_vehicle` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `driver_id` BIGINT NOT NULL COMMENT '司机ID',
    `vehicle_id` BIGINT NOT NULL COMMENT '车辆ID',
    `use_count` INT DEFAULT 0 COMMENT '使用次数',
    `last_use_time` DATETIME COMMENT '最后使用时间',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认车辆：0-否，1-是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    UNIQUE KEY `uk_driver_vehicle` (`driver_id`, `vehicle_id`),
    INDEX `idx_driver_id` (`driver_id`),
    INDEX `idx_vehicle_id` (`vehicle_id`),
    INDEX `idx_is_default` (`is_default`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='司机常用车辆关系表';

-- =====================================================
-- 6. 用户出勤记录表
-- =====================================================
CREATE TABLE IF NOT EXISTS `user_attendance` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `attendance_date` DATE NOT NULL COMMENT '出勤日期',
    `check_in_time` DATETIME COMMENT '上班时间',
    `check_out_time` DATETIME COMMENT '下班时间',
    `check_in_latitude` DOUBLE COMMENT '上班打卡纬度',
    `check_in_longitude` DOUBLE COMMENT '上班打卡经度',
    `check_in_address` VARCHAR(255) COMMENT '上班打卡地址',
    `check_out_latitude` DOUBLE COMMENT '下班打卡纬度',
    `check_out_longitude` DOUBLE COMMENT '下班打卡经度',
    `check_out_address` VARCHAR(255) COMMENT '下班打卡地址',
    `status` TINYINT DEFAULT 1 COMMENT '出勤状态：1-正常，2-迟到，3-早退，4-缺勤，5-请假',
    `late_minutes` INT DEFAULT 0 COMMENT '迟到分钟数',
    `early_leave_minutes` INT DEFAULT 0 COMMENT '早退分钟数',
    `leave_type` TINYINT COMMENT '请假类型：1-事假，2-病假，3-年假，4-调休',
    `leave_start_time` DATETIME COMMENT '请假开始时间',
    `leave_end_time` DATETIME COMMENT '请假结束时间',
    `remark` VARCHAR(255) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_attendance_date` (`attendance_date`),
    INDEX `idx_status` (`status`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户出勤记录表';

-- =====================================================
-- 7. 安全员信息表
-- =====================================================
CREATE TABLE IF NOT EXISTS `safety_officer` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `officer_name` VARCHAR(50) NOT NULL COMMENT '安全员姓名',
    `gender` TINYINT COMMENT '性别：1-男，2-女',
    `id_card` VARCHAR(255) NOT NULL COMMENT '身份证号（AES加密）',
    `id_card_front_url` VARCHAR(255) COMMENT '身份证正面URL',
    `id_card_back_url` VARCHAR(255) COMMENT '身份证背面URL',
    `emergency_cert_url` VARCHAR(255) COMMENT '应急救援证URL',
    `cert_number` VARCHAR(50) COMMENT '证书编号',
    `training_project` VARCHAR(100) COMMENT '培训项目',
    `valid_period` VARCHAR(50) COMMENT '有效期',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-离职，1-在职',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安全员信息表';

-- =====================================================
-- 8. 维修员信息表
-- =====================================================
CREATE TABLE IF NOT EXISTS `repairman` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `repairman_name` VARCHAR(50) NOT NULL COMMENT '维修员姓名',
    `gender` TINYINT COMMENT '性别：1-男，2-女',
    `id_card` VARCHAR(255) NOT NULL COMMENT '身份证号（AES加密）',
    `id_card_front_url` VARCHAR(255) COMMENT '身份证正面URL',
    `id_card_back_url` VARCHAR(255) COMMENT '身份证背面URL',
    `repair_cert_url` VARCHAR(255) COMMENT '维修资格证URL',
    `cert_number` VARCHAR(50) COMMENT '证书编号',
    `repair_level` VARCHAR(20) COMMENT '维修等级：初级/中级/高级',
    `repair_type` VARCHAR(50) COMMENT '维修类别',
    `valid_until` DATE COMMENT '有效期至',
    `issue_date` DATE COMMENT '发证日期',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-离职，1-在职',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修员信息表';

-- =====================================================
-- 9. 角色变更申请表
-- =====================================================
CREATE TABLE IF NOT EXISTS `role_change_apply` (
    `id` BIGINT PRIMARY KEY COMMENT '申请ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `current_role_id` BIGINT NOT NULL COMMENT '当前角色ID',
    `current_role_code` VARCHAR(50) NOT NULL COMMENT '当前角色编码',
    `current_role_name` VARCHAR(50) NOT NULL COMMENT '当前角色名称',
    `apply_role_id` BIGINT NOT NULL COMMENT '申请角色ID',
    `apply_role_code` VARCHAR(50) NOT NULL COMMENT '申请角色编码',
    `apply_role_name` VARCHAR(50) NOT NULL COMMENT '申请角色名称',
    `apply_reason` VARCHAR(255) NOT NULL COMMENT '申请原因',
    `status` INT NOT NULL DEFAULT 1 COMMENT '申请状态：1-待处理，2-已通过，3-已拒绝',
    `admin_opinion` VARCHAR(255) COMMENT '管理员处理意见',
    `handle_time` DATETIME COMMENT '处理时间',
    `handler_id` BIGINT COMMENT '处理人ID',
    `handler_name` VARCHAR(50) COMMENT '处理人姓名',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色变更申请表';

-- =====================================================
-- 10. 用户申诉表
-- =====================================================
CREATE TABLE IF NOT EXISTS `user_appeal` (
    `id` BIGINT PRIMARY KEY COMMENT '申诉ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `real_name` VARCHAR(50) COMMENT '真实姓名',
    `phone` VARCHAR(20) COMMENT '手机号',
    `appeal_reason` VARCHAR(500) NOT NULL COMMENT '申诉原因',
    `status` INT NOT NULL DEFAULT 1 COMMENT '申诉状态：1-待处理，2-已通过，3-已拒绝',
    `admin_opinion` VARCHAR(255) COMMENT '管理员处理意见',
    `handle_time` DATETIME COMMENT '处理时间',
    `handler_id` BIGINT COMMENT '处理人ID',
    `handler_name` VARCHAR(50) COMMENT '处理人姓名',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户申诉表';

-- =====================================================
-- 11. 用户通知表
-- 说明：存储用户的系统通知消息
-- =====================================================
CREATE TABLE IF NOT EXISTS `user_notification` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(100) NOT NULL COMMENT '通知标题',
    `content` TEXT COMMENT '通知内容',
    `type` INT COMMENT '通知类型：1-系统通知，2-预警通知，3-调度通知，4-审批通知',
    `business_id` BIGINT COMMENT '业务ID（如行程ID、车辆ID等）',
    `business_type` VARCHAR(50) COMMENT '业务类型：TRIP-行程，VEHICLE-车辆，DRIVER-司机，DISPATCH-调度',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
    `read_time` DATETIME COMMENT '阅读时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_type` (`type`),
    INDEX `idx_business` (`business_id`, `business_type`),
    INDEX `idx_is_read` (`is_read`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户通知表';

-- =====================================================
-- 12. 站内消息表（MongoDB）
-- =====================================================
-- 消息存储在MongoDB中，集合名称：messages
-- 字段说明：
-- {
--   "id": "ObjectId",
--   "messageId": "String - 消息唯一标识",
--   "sender": "String - 发送者ID",
--   "receiver": "String - 接收者ID",
--   "type": "String - 消息类型",
--   "content": "String - 消息内容",
--   "status": "String - 消息状态（UNREAD/READ）",
--   "priority": "String - 优先级（HIGH/MEDIUM/LOW）",
--   "createdAt": "DateTime - 创建时间",
--   "updatedAt": "DateTime - 更新时间",
--   "readAt": "DateTime - 阅读时间",
--   "businessId": "String - 业务ID",
--   "businessType": "String - 业务类型"
-- }
--
-- MongoDB命令创建集合：
-- db.createCollection("messages")
-- db.messages.createIndex({ "receiver": 1, "createdAt": -1 })
-- db.messages.createIndex({ "messageId": 1 }, { unique: true })

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
    `vehicle_id` BIGINT COMMENT '分配车辆ID',
    `driver_id` BIGINT COMMENT '分配司机ID',
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
    INDEX `idx_vehicle_id` (`vehicle_id`),
    INDEX `idx_driver_id` (`driver_id`),
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
-- =====================================================
-- MineGuard 行程域数据库设计
-- 模块: trip-service
-- 版本: 1.3
-- 日期: 2026-03-22
-- 说明: 行程域相关表结构，包含行程等
-- 注意: 路线模板在dispatch模块的route_template表，实际导航使用高德地图API
--       行程通知使用user模块的user_notification表，通过business_id和business_type关联
-- =====================================================

-- =====================================================
-- 1. 行程表
-- 说明: 行程是司机接单后创建的实际运输记录
-- 起点终点坐标用于高德地图导航
-- =====================================================
CREATE TABLE IF NOT EXISTS `trip` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `trip_no` VARCHAR(50) NOT NULL COMMENT '行程编号',
    `dispatch_task_id` BIGINT COMMENT '调度任务ID(调度任务接单后创建)',
    `vehicle_id` BIGINT NOT NULL COMMENT '车辆ID',
    `driver_id` BIGINT NOT NULL COMMENT '司机ID',
    `start_location` VARCHAR(255) COMMENT '起始位置名称',
    `start_longitude` DOUBLE COMMENT '起始经度',
    `start_latitude` DOUBLE COMMENT '起始纬度',
    `end_location` VARCHAR(255) COMMENT '结束位置名称',
    `end_longitude` DOUBLE COMMENT '结束经度',
    `end_latitude` DOUBLE COMMENT '结束纬度',
    `estimated_start_time` DATETIME COMMENT '预计开始时间',
    `estimated_end_time` DATETIME COMMENT '预计结束时间',
    `actual_start_time` DATETIME COMMENT '实际开始时间',
    `actual_end_time` DATETIME COMMENT '实际结束时间',
    `status` INT DEFAULT 0 COMMENT '状态：0-待开始，1-已接单，2-进行中，3-已完成，4-已取消',
    `trip_type` INT DEFAULT 0 COMMENT '行程类型',
    `estimated_mileage` DECIMAL(10,2) COMMENT '预计里程',
    `actual_mileage` DECIMAL(10,2) COMMENT '实际里程',
    `estimated_duration` INT COMMENT '预计时长（分钟）',
    `actual_duration` INT COMMENT '实际时长（分钟）',
    `fuel_consumption` DOUBLE COMMENT '燃油消耗',
    `average_speed` DOUBLE COMMENT '平均速度',
    `cargo_weight` DECIMAL(10,2) COMMENT '货物重量(吨)',
    `cancellation_reason` VARCHAR(255) COMMENT '取消原因',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    UNIQUE KEY `uk_trip_no` (`trip_no`),
    INDEX `idx_dispatch_task_id` (`dispatch_task_id`),
    INDEX `idx_vehicle_id` (`vehicle_id`),
    INDEX `idx_driver_id` (`driver_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行程表';

-- =====================================================
-- =====================================================

-- 注意：行程数据应通过系统的调度服务或管理界面创建
-- 避免在数据库文件中直接插入行程数据
-- 成本明细表
CREATE TABLE IF NOT EXISTS `cost_detail` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `cost_no` VARCHAR(50) NOT NULL COMMENT '成本编号',
    `cost_type` INT NOT NULL COMMENT '成本类型：1-燃油成本 2-维修成本 3-人工成本 4-保险成本 5-折旧成本 6-管理成本 7-其他成本',
    `cost_name` VARCHAR(100) NOT NULL COMMENT '成本名称',
    `amount` DECIMAL(12,2) NOT NULL COMMENT '金额',
    `vehicle_id` BIGINT COMMENT '车辆ID',
    `trip_id` BIGINT COMMENT '行程ID',
    `user_id` BIGINT COMMENT '操作人ID',
    `cost_date` DATE NOT NULL COMMENT '成本日期',
    `payment_method` VARCHAR(50) COMMENT '支付方式',
    `invoice_no` VARCHAR(100) COMMENT '发票号',
    `description` VARCHAR(500) COMMENT '描述',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人',
    `update_by` BIGINT COMMENT '更新人',
    `deleted` INT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_cost_no` (`cost_no`),
    KEY `idx_cost_type` (`cost_type`),
    KEY `idx_vehicle_id` (`vehicle_id`),
    KEY `idx_cost_date` (`cost_date`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成本明细表';

-- 薪资配置表
CREATE TABLE IF NOT EXISTS `salary_config` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `role_name` VARCHAR(100) NOT NULL COMMENT '角色名称',
    `base_salary` DECIMAL(12,2) COMMENT '基本工资',
    `daily_salary` DECIMAL(12,2) COMMENT '日工资',
    `hourly_salary` DECIMAL(12,2) COMMENT '时工资',
    `overtime_rate` DECIMAL(5,2) COMMENT '加班费率',
    `performance_bonus` DECIMAL(12,2) COMMENT '绩效奖金',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `effective_date` DATE COMMENT '生效日期',
    `expiry_date` DATE COMMENT '失效日期',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人',
    `update_by` BIGINT COMMENT '更新人',
    `deleted` INT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_role_code` (`role_code`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='薪资配置表';

-- 成本预算表
CREATE TABLE IF NOT EXISTS `cost_budget` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `budget_no` VARCHAR(50) NOT NULL COMMENT '预算编号',
    `budget_name` VARCHAR(100) NOT NULL COMMENT '预算名称',
    `budget_type` INT NOT NULL COMMENT '预算类型：1-月度 2-季度 3-年度',
    `budget_year` INT NOT NULL COMMENT '预算年份',
    `budget_month` INT COMMENT '预算月份',
    `budget_quarter` INT COMMENT '预算季度',
    `start_date` DATE COMMENT '开始日期',
    `end_date` DATE COMMENT '结束日期',
    `fuel_budget` DECIMAL(12,2) DEFAULT 0 COMMENT '燃油预算',
    `maintenance_budget` DECIMAL(12,2) DEFAULT 0 COMMENT '维修预算',
    `labor_budget` DECIMAL(12,2) DEFAULT 0 COMMENT '人工预算',
    `insurance_budget` DECIMAL(12,2) DEFAULT 0 COMMENT '保险预算',
    `depreciation_budget` DECIMAL(12,2) DEFAULT 0 COMMENT '折旧预算',
    `management_budget` DECIMAL(12,2) DEFAULT 0 COMMENT '管理预算',
    `other_budget` DECIMAL(12,2) DEFAULT 0 COMMENT '其他预算',
    `total_budget` DECIMAL(12,2) NOT NULL COMMENT '总预算',
    `status` INT NOT NULL DEFAULT 0 COMMENT '状态：0-草稿 1-已审批 2-生效中 3-执行中 4-已完成 5-已过期',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人',
    `update_by` BIGINT COMMENT '更新人',
    `deleted` INT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_budget_no` (`budget_no`),
    KEY `idx_budget_type` (`budget_type`),
    KEY `idx_budget_year` (`budget_year`),
    KEY `idx_status` (`status`),
    KEY `idx_date_range` (`start_date`, `end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成本预算表';

-- ==========================================================
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
