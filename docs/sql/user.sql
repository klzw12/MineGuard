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
    `valid_period` VARCHAR(50) COMMENT '有效期限',
    `license_number` VARCHAR(50) COMMENT '驾驶证号',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-离职，1-在职',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_belonging_team` (`belonging_team`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='司机信息表';

-- =====================================================
-- 6. 司机出勤记录表
-- =====================================================
CREATE TABLE IF NOT EXISTS `driver_attendance` (
    `id` BIGINT PRIMARY KEY COMMENT '主键ID',
    `driver_id` BIGINT NOT NULL COMMENT '司机ID',
    `attendance_date` DATE NOT NULL COMMENT '出勤日期',
    `check_in_time` DATETIME COMMENT '上班时间',
    `check_out_time` DATETIME COMMENT '下班时间',
    `status` TINYINT DEFAULT 1 COMMENT '出勤状态：1-正常，2-迟到，3-早退，4-缺勤',
    `late_minutes` INT DEFAULT 0 COMMENT '迟到分钟数',
    `early_leave_minutes` INT DEFAULT 0 COMMENT '早退分钟数',
    `remark` VARCHAR(255) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX `idx_driver_id` (`driver_id`),
    INDEX `idx_attendance_date` (`attendance_date`),
    INDEX `idx_status` (`status`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='司机出勤记录表';

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
-- 初始化数据
-- =====================================================

-- 初始化角色数据
INSERT INTO `role` (`id`, `role_name`, `role_code`, `description`) VALUES
(1, '系统管理员', 'ADMIN', '系统管理员，拥有所有权限'),
(2, '司机', 'DRIVER', '司机角色，负责车辆驾驶'),
(3, '运营人员', 'OPERATOR', '运营人员角色，处理调度工作等，分担管理员的业务'),
(4, '维修员', 'REPAIRMAN', '维修员角色，负责车辆维修'),
(5, '安全员', 'SAFETY_OFFICER', '安全员角色，负责安全监督');

-- 注意：管理员用户初始化
-- 请通过系统的初始化脚本或管理界面创建管理员用户
-- 避免在数据库文件中直接存储密码哈希值
-- 推荐使用系统提供的密码设置功能，确保与系统加密逻辑一致

-- =====================================================
-- 11. 站内消息表（MongoDB）
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

