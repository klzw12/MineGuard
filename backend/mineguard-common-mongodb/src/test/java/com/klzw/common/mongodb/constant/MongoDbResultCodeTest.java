package com.klzw.common.mongodb.constant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MongoDbResultCode 单元测试
 */
@DisplayName("MongoDbResultCode错误码测试")
class MongoDbResultCodeTest {

    @Test
    @DisplayName("错误码枚举数量应正确")
    void enumCount_shouldBeCorrect() {
        assertEquals(40, MongoDbResultCode.values().length);
    }

    @Test
    @DisplayName("通用错误码应正确")
    void generalErrorCodes_shouldBeCorrect() {
        assertEquals(1300, MongoDbResultCode.MONGODB_ERROR.getCode());
        assertEquals("MongoDB操作失败", MongoDbResultCode.MONGODB_ERROR.getMessage());
    }

    @Test
    @DisplayName("连接错误码应正确")
    void connectionErrorCodes_shouldBeCorrect() {
        assertEquals(1301, MongoDbResultCode.CONNECTION_ERROR.getCode());
        assertEquals("MongoDB连接失败", MongoDbResultCode.CONNECTION_ERROR.getMessage());
        
        assertEquals(1302, MongoDbResultCode.CONNECTION_TIMEOUT.getCode());
        assertEquals("MongoDB连接超时", MongoDbResultCode.CONNECTION_TIMEOUT.getMessage());
        
        assertEquals(1303, MongoDbResultCode.CONNECTION_POOL_EXHAUSTED.getCode());
        assertEquals("MongoDB连接池耗尽", MongoDbResultCode.CONNECTION_POOL_EXHAUSTED.getMessage());
        
        assertEquals(1304, MongoDbResultCode.INVALID_CONNECTION_STRING.getCode());
        assertEquals("无效的MongoDB连接字符串", MongoDbResultCode.INVALID_CONNECTION_STRING.getMessage());
        
        assertEquals(1305, MongoDbResultCode.AUTHENTICATION_ERROR.getCode());
        assertEquals("MongoDB认证失败", MongoDbResultCode.AUTHENTICATION_ERROR.getMessage());
    }

    @Test
    @DisplayName("集合操作错误码应正确")
    void collectionErrorCodes_shouldBeCorrect() {
        assertEquals(1310, MongoDbResultCode.COLLECTION_ERROR.getCode());
        assertEquals(1311, MongoDbResultCode.COLLECTION_NOT_FOUND.getCode());
        assertEquals(1312, MongoDbResultCode.COLLECTION_ALREADY_EXISTS.getCode());
        assertEquals(1313, MongoDbResultCode.CREATE_COLLECTION_ERROR.getCode());
        assertEquals(1314, MongoDbResultCode.DROP_COLLECTION_ERROR.getCode());
    }

    @Test
    @DisplayName("文档操作错误码应正确")
    void documentErrorCodes_shouldBeCorrect() {
        assertEquals(1320, MongoDbResultCode.DOCUMENT_ERROR.getCode());
        assertEquals(1321, MongoDbResultCode.DOCUMENT_NOT_FOUND.getCode());
        assertEquals(1322, MongoDbResultCode.INSERT_DOCUMENT_ERROR.getCode());
        assertEquals(1323, MongoDbResultCode.UPDATE_DOCUMENT_ERROR.getCode());
        assertEquals(1324, MongoDbResultCode.DELETE_DOCUMENT_ERROR.getCode());
        assertEquals(1325, MongoDbResultCode.DUPLICATE_KEY_ERROR.getCode());
    }

    @Test
    @DisplayName("索引错误码应正确")
    void indexErrorCodes_shouldBeCorrect() {
        assertEquals(1330, MongoDbResultCode.INDEX_ERROR.getCode());
        assertEquals(1331, MongoDbResultCode.CREATE_INDEX_ERROR.getCode());
        assertEquals(1332, MongoDbResultCode.DROP_INDEX_ERROR.getCode());
        assertEquals(1333, MongoDbResultCode.INDEX_NOT_FOUND.getCode());
        assertEquals(1334, MongoDbResultCode.INVALID_INDEX_SPEC.getCode());
        assertEquals(1335, MongoDbResultCode.TTL_INDEX_ERROR.getCode());
    }

    @Test
    @DisplayName("聚合操作错误码应正确")
    void aggregationErrorCodes_shouldBeCorrect() {
        assertEquals(1340, MongoDbResultCode.AGGREGATION_ERROR.getCode());
        assertEquals(1341, MongoDbResultCode.PIPELINE_ERROR.getCode());
        assertEquals(1342, MongoDbResultCode.AGGREGATION_TIMEOUT.getCode());
        assertEquals(1343, MongoDbResultCode.INVALID_AGGREGATION_STAGE.getCode());
    }

    @Test
    @DisplayName("地理空间错误码应正确")
    void geoErrorCodes_shouldBeCorrect() {
        assertEquals(1350, MongoDbResultCode.GEO_ERROR.getCode());
        assertEquals(1351, MongoDbResultCode.INVALID_GEO_JSON.getCode());
        assertEquals(1352, MongoDbResultCode.GEO_INDEX_MISSING.getCode());
        assertEquals(1353, MongoDbResultCode.GEO_QUERY_ERROR.getCode());
        assertEquals(1354, MongoDbResultCode.COORDINATE_OUT_OF_RANGE.getCode());
    }

    @Test
    @DisplayName("时序数据错误码应正确")
    void timeSeriesErrorCodes_shouldBeCorrect() {
        assertEquals(1360, MongoDbResultCode.TIME_SERIES_ERROR.getCode());
        assertEquals(1361, MongoDbResultCode.INVALID_TIME_FIELD.getCode());
        assertEquals(1362, MongoDbResultCode.TIME_SERIES_COLLECTION_ERROR.getCode());
    }

    @Test
    @DisplayName("其他错误码应正确")
    void otherErrorCodes_shouldBeCorrect() {
        assertEquals(1370, MongoDbResultCode.QUERY_ERROR.getCode());
        assertEquals(1371, MongoDbResultCode.QUERY_TIMEOUT.getCode());
        assertEquals(1372, MongoDbResultCode.BULK_OPERATION_ERROR.getCode());
        assertEquals(1373, MongoDbResultCode.TRANSACTION_ERROR.getCode());
        assertEquals(1374, MongoDbResultCode.SESSION_ERROR.getCode());
    }

    @Test
    @DisplayName("valueOf应正确返回枚举值")
    void valueOf_shouldReturnCorrectEnum() {
        assertEquals(MongoDbResultCode.MONGODB_ERROR, MongoDbResultCode.valueOf("MONGODB_ERROR"));
        assertEquals(MongoDbResultCode.CONNECTION_ERROR, MongoDbResultCode.valueOf("CONNECTION_ERROR"));
        assertEquals(MongoDbResultCode.DOCUMENT_NOT_FOUND, MongoDbResultCode.valueOf("DOCUMENT_NOT_FOUND"));
    }

}
