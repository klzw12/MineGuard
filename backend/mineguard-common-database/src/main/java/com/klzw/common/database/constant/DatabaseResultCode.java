package com.klzw.common.database.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据库模块错误码枚举
 * <p>
 * 错误码范围：1100-1199
 * <p>
 * 错误码说明：
 * - 1100: 数据库通用错误
 * - 1101-1109: 数据库连接相关错误
 * - 1110-1119: SQL执行相关错误
 * - 1120-1129: 事务相关错误
 * - 1130-1139: 数据源相关错误
 * - 1140-1149: 分页相关错误
 * - 1150-1159: 数据完整性相关错误
 * - 1160-1169: MyBatis-Plus错误
 */
@Getter
@AllArgsConstructor
public enum DatabaseResultCode {

    DATABASE_ERROR(1100, "数据库操作失败"),

    DATABASE_CONNECTION_ERROR(1101, "数据库连接失败"),
    CONNECTION_TIMEOUT(1102, "数据库连接超时"),
    CONNECTION_POOL_EXHAUSTED(1103, "数据库连接池耗尽"),
    CONNECTION_CLOSED(1104, "数据库连接已关闭"),
    INVALID_CONNECTION_CONFIG(1105, "数据库连接配置无效"),

    SQL_EXECUTION_ERROR(1110, "SQL执行失败"),
    SQL_SYNTAX_ERROR(1111, "SQL语法错误"),
    QUERY_TIMEOUT(1112, "查询超时"),
    UPDATE_TIMEOUT(1113, "更新超时"),
    BATCH_EXECUTION_ERROR(1114, "批量执行失败"),

    TRANSACTION_ERROR(1120, "事务执行失败"),
    TRANSACTION_TIMEOUT(1121, "事务超时"),
    TRANSACTION_ROLLBACK(1122, "事务回滚"),
    TRANSACTION_NOT_ACTIVE(1123, "事务未激活"),
    NESTED_TRANSACTION_ERROR(1124, "嵌套事务错误"),

    DATA_SOURCE_ERROR(1130, "数据源配置错误"),
    DATA_SOURCE_NOT_FOUND(1131, "数据源不存在"),
    DATA_SOURCE_SWITCH_ERROR(1132, "数据源切换失败"),
    MASTER_SLAVE_SYNC_ERROR(1133, "主从同步错误"),
    READ_WRITE_SPLIT_ERROR(1134, "读写分离错误"),

    PAGINATION_ERROR(1140, "分页参数错误"),
    PAGE_NUMBER_INVALID(1141, "页码无效"),
    PAGE_SIZE_INVALID(1142, "每页大小无效"),
    PAGE_SIZE_EXCEEDED(1143, "每页大小超过限制"),
    SORT_FIELD_INVALID(1144, "排序字段无效"),

    RECORD_NOT_FOUND(1150, "记录不存在"),
    DUPLICATE_KEY_ERROR(1151, "主键冲突"),
    DATA_INTEGRITY_ERROR(1152, "数据完整性错误"),
    FOREIGN_KEY_CONSTRAINT_ERROR(1153, "外键约束错误"),
    UNIQUE_CONSTRAINT_ERROR(1154, "唯一约束错误"),
    NULL_VALUE_ERROR(1155, "空值错误"),

    MAPPER_NOT_FOUND(1160, "Mapper未找到"),
    ENTITY_NOT_FOUND(1161, "实体类未找到"),
    TABLE_NOT_FOUND(1162, "表不存在"),
    FIELD_NOT_FOUND(1163, "字段不存在"),
    TYPE_HANDLER_ERROR(1164, "类型处理器错误");

    private final int code;
    private final String message;
}
