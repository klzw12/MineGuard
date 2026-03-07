package com.klzw.common.database.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据库模块错误码枚举
 * <p>
 * 错误码范围：1000-1099
 * <p>
 * 错误码说明：
 * - 1000: 数据库通用错误
 * - 1001-1009: 数据库连接相关错误
 * - 1010-1019: SQL执行相关错误
 * - 1020-1029: 事务相关错误
 * - 1030-1039: 数据源相关错误
 * - 1040-1049: 分页相关错误
 * - 1050-1059: 数据完整性相关错误
 */
@Getter
@AllArgsConstructor
public enum DatabaseResultCode {

    /**
     * 数据库通用错误
     */
    DATABASE_ERROR(1000, "数据库操作失败"),

    /**
     * 数据库连接错误
     */
    DATABASE_CONNECTION_ERROR(1001, "数据库连接失败"),
    CONNECTION_TIMEOUT(1002, "数据库连接超时"),
    CONNECTION_POOL_EXHAUSTED(1003, "数据库连接池耗尽"),
    CONNECTION_CLOSED(1004, "数据库连接已关闭"),
    INVALID_CONNECTION_CONFIG(1005, "数据库连接配置无效"),

    /**
     * SQL执行错误
     */
    SQL_EXECUTION_ERROR(1010, "SQL执行失败"),
    SQL_SYNTAX_ERROR(1011, "SQL语法错误"),
    QUERY_TIMEOUT(1012, "查询超时"),
    UPDATE_TIMEOUT(1013, "更新超时"),
    BATCH_EXECUTION_ERROR(1014, "批量执行失败"),

    /**
     * 事务错误
     */
    TRANSACTION_ERROR(1020, "事务执行失败"),
    TRANSACTION_TIMEOUT(1021, "事务超时"),
    TRANSACTION_ROLLBACK(1022, "事务回滚"),
    TRANSACTION_NOT_ACTIVE(1023, "事务未激活"),
    NESTED_TRANSACTION_ERROR(1024, "嵌套事务错误"),

    /**
     * 数据源错误
     */
    DATA_SOURCE_ERROR(1030, "数据源配置错误"),
    DATA_SOURCE_NOT_FOUND(1031, "数据源不存在"),
    DATA_SOURCE_SWITCH_ERROR(1032, "数据源切换失败"),
    MASTER_SLAVE_SYNC_ERROR(1033, "主从同步错误"),
    READ_WRITE_SPLIT_ERROR(1034, "读写分离错误"),

    /**
     * 分页错误
     */
    PAGINATION_ERROR(1040, "分页参数错误"),
    PAGE_NUMBER_INVALID(1041, "页码无效"),
    PAGE_SIZE_INVALID(1042, "每页大小无效"),
    PAGE_SIZE_EXCEEDED(1043, "每页大小超过限制"),
    SORT_FIELD_INVALID(1044, "排序字段无效"),

    /**
     * 数据完整性错误
     */
    RECORD_NOT_FOUND(1050, "记录不存在"),
    DUPLICATE_KEY_ERROR(1051, "主键冲突"),
    DATA_INTEGRITY_ERROR(1052, "数据完整性错误"),
    FOREIGN_KEY_CONSTRAINT_ERROR(1053, "外键约束错误"),
    UNIQUE_CONSTRAINT_ERROR(1054, "唯一约束错误"),
    NULL_VALUE_ERROR(1055, "空值错误"),

    /**
     * MyBatis-Plus错误
     */
    MAPPER_NOT_FOUND(1060, "Mapper未找到"),
    ENTITY_NOT_FOUND(1061, "实体类未找到"),
    TABLE_NOT_FOUND(1062, "表不存在"),
    FIELD_NOT_FOUND(1063, "字段不存在"),
    TYPE_HANDLER_ERROR(1064, "类型处理器错误");

    private final int code;
    private final String message;
}
