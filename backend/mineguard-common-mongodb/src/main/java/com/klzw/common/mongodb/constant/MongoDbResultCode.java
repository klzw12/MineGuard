package com.klzw.common.mongodb.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * MongoDB模块错误码枚举
 * <p>
 * 错误码范围：1300-1399
 * <p>
 * 错误码说明：
 * - 1300: MongoDB通用错误
 * - 1301-1309: MongoDB连接相关错误
 * - 1310-1319: 集合操作相关错误
 * - 1320-1329: 文档操作相关错误
 * - 1330-1339: 索引相关错误
 * - 1340-1349: 聚合操作相关错误
 * - 1350-1359: 地理空间相关错误
 */
@Getter
@AllArgsConstructor
public enum MongoDbResultCode {

    /**
     * MongoDB通用错误
     */
    MONGODB_ERROR(1300, "MongoDB操作失败"),

    /**
     * MongoDB连接错误
     */
    CONNECTION_ERROR(1301, "MongoDB连接失败"),
    CONNECTION_TIMEOUT(1302, "MongoDB连接超时"),
    CONNECTION_POOL_EXHAUSTED(1303, "MongoDB连接池耗尽"),
    INVALID_CONNECTION_STRING(1304, "无效的MongoDB连接字符串"),
    AUTHENTICATION_ERROR(1305, "MongoDB认证失败"),

    /**
     * 集合操作错误
     */
    COLLECTION_ERROR(1310, "集合操作失败"),
    COLLECTION_NOT_FOUND(1311, "集合不存在"),
    COLLECTION_ALREADY_EXISTS(1312, "集合已存在"),
    CREATE_COLLECTION_ERROR(1313, "创建集合失败"),
    DROP_COLLECTION_ERROR(1314, "删除集合失败"),

    /**
     * 文档操作错误
     */
    DOCUMENT_ERROR(1320, "文档操作失败"),
    DOCUMENT_NOT_FOUND(1321, "文档不存在"),
    INSERT_DOCUMENT_ERROR(1322, "插入文档失败"),
    UPDATE_DOCUMENT_ERROR(1323, "更新文档失败"),
    DELETE_DOCUMENT_ERROR(1324, "删除文档失败"),
    DUPLICATE_KEY_ERROR(1325, "文档键值冲突"),

    /**
     * 索引错误
     */
    INDEX_ERROR(1330, "索引操作失败"),
    CREATE_INDEX_ERROR(1331, "创建索引失败"),
    DROP_INDEX_ERROR(1332, "删除索引失败"),
    INDEX_NOT_FOUND(1333, "索引不存在"),
    INVALID_INDEX_SPEC(1334, "无效的索引规范"),
    TTL_INDEX_ERROR(1335, "TTL索引操作失败"),

    /**
     * 聚合操作错误
     */
    AGGREGATION_ERROR(1340, "聚合操作失败"),
    PIPELINE_ERROR(1341, "聚合管道错误"),
    AGGREGATION_TIMEOUT(1342, "聚合操作超时"),
    INVALID_AGGREGATION_STAGE(1343, "无效的聚合阶段"),

    /**
     * 地理空间错误
     */
    GEO_ERROR(1350, "地理空间操作失败"),
    INVALID_GEO_JSON(1351, "无效的GeoJSON格式"),
    GEO_INDEX_MISSING(1352, "缺少地理空间索引"),
    GEO_QUERY_ERROR(1353, "地理空间查询失败"),
    COORDINATE_OUT_OF_RANGE(1354, "坐标值超出范围"),

    /**
     * 时序数据错误
     */
    TIME_SERIES_ERROR(1360, "时序数据操作失败"),
    INVALID_TIME_FIELD(1361, "无效的时间字段"),
    TIME_SERIES_COLLECTION_ERROR(1362, "时序集合操作失败"),

    /**
     * 其他错误
     */
    QUERY_ERROR(1370, "查询操作失败"),
    QUERY_TIMEOUT(1371, "查询超时"),
    BULK_OPERATION_ERROR(1372, "批量操作失败"),
    TRANSACTION_ERROR(1373, "事务操作失败"),
    SESSION_ERROR(1374, "会话操作失败");

    private final int code;
    private final String message;
}