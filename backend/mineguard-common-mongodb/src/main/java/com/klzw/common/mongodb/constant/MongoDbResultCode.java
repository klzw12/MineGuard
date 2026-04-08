package com.klzw.common.mongodb.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * MongoDB模块错误码枚举
 * <p>
 * 错误码范围：1400-1499
 */
@Getter
@AllArgsConstructor
public enum MongoDbResultCode {

    MONGODB_ERROR(1400, "MongoDB操作失败"),

    CONNECTION_ERROR(1401, "MongoDB连接失败"),
    CONNECTION_TIMEOUT(1402, "MongoDB连接超时"),
    CONNECTION_POOL_EXHAUSTED(1403, "MongoDB连接池耗尽"),
    INVALID_CONNECTION_STRING(1404, "无效的MongoDB连接字符串"),
    AUTHENTICATION_ERROR(1405, "MongoDB认证失败"),

    COLLECTION_ERROR(1410, "集合操作失败"),
    COLLECTION_NOT_FOUND(1411, "集合不存在"),
    COLLECTION_ALREADY_EXISTS(1412, "集合已存在"),
    CREATE_COLLECTION_ERROR(1413, "创建集合失败"),
    DROP_COLLECTION_ERROR(1414, "删除集合失败"),

    DOCUMENT_ERROR(1420, "文档操作失败"),
    DOCUMENT_NOT_FOUND(1421, "文档不存在"),
    INSERT_DOCUMENT_ERROR(1422, "插入文档失败"),
    UPDATE_DOCUMENT_ERROR(1423, "更新文档失败"),
    DELETE_DOCUMENT_ERROR(1424, "删除文档失败"),
    DUPLICATE_KEY_ERROR(1425, "文档键值冲突"),

    INDEX_ERROR(1430, "索引操作失败"),
    CREATE_INDEX_ERROR(1431, "创建索引失败"),
    DROP_INDEX_ERROR(1432, "删除索引失败"),
    INDEX_NOT_FOUND(1433, "索引不存在"),
    INVALID_INDEX_SPEC(1434, "无效的索引规范"),
    TTL_INDEX_ERROR(1435, "TTL索引操作失败"),

    AGGREGATION_ERROR(1440, "聚合操作失败"),
    PIPELINE_ERROR(1441, "聚合管道错误"),
    AGGREGATION_TIMEOUT(1442, "聚合操作超时"),
    INVALID_AGGREGATION_STAGE(1443, "无效的聚合阶段"),

    GEO_ERROR(1450, "地理空间操作失败"),
    INVALID_GEO_JSON(1451, "无效的GeoJSON格式"),
    GEO_INDEX_MISSING(1452, "缺少地理空间索引"),
    GEO_QUERY_ERROR(1453, "地理空间查询失败"),
    COORDINATE_OUT_OF_RANGE(1454, "坐标值超出范围"),

    TIME_SERIES_ERROR(1460, "时序数据操作失败"),
    INVALID_TIME_FIELD(1461, "无效的时间字段"),
    TIME_SERIES_COLLECTION_ERROR(1462, "时序集合操作失败"),

    QUERY_ERROR(1470, "查询操作失败"),
    QUERY_TIMEOUT(1471, "查询超时"),
    BULK_OPERATION_ERROR(1472, "批量操作失败"),
    TRANSACTION_ERROR(1473, "事务操作失败"),
    SESSION_ERROR(1474, "会话操作失败");

    private final int code;
    private final String message;
}
