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
        assertEquals(1400, MongoDbResultCode.MONGODB_ERROR.getCode());
        assertEquals("MongoDB操作失败", MongoDbResultCode.MONGODB_ERROR.getMessage());
    }

    @Test
    @DisplayName("连接错误码应正确")
    void connectionErrorCodes_shouldBeCorrect() {
        assertEquals(1401, MongoDbResultCode.CONNECTION_ERROR.getCode());
        assertEquals("MongoDB连接失败", MongoDbResultCode.CONNECTION_ERROR.getMessage());
        
        assertEquals(1402, MongoDbResultCode.CONNECTION_TIMEOUT.getCode());
        assertEquals("MongoDB连接超时", MongoDbResultCode.CONNECTION_TIMEOUT.getMessage());
        
        assertEquals(1403, MongoDbResultCode.CONNECTION_POOL_EXHAUSTED.getCode());
        assertEquals("MongoDB连接池耗尽", MongoDbResultCode.CONNECTION_POOL_EXHAUSTED.getMessage());
        
        assertEquals(1404, MongoDbResultCode.INVALID_CONNECTION_STRING.getCode());
        assertEquals("无效的MongoDB连接字符串", MongoDbResultCode.INVALID_CONNECTION_STRING.getMessage());
        
        assertEquals(1405, MongoDbResultCode.AUTHENTICATION_ERROR.getCode());
        assertEquals("MongoDB认证失败", MongoDbResultCode.AUTHENTICATION_ERROR.getMessage());
    }

    @Test
    @DisplayName("集合操作错误码应正确")
    void collectionErrorCodes_shouldBeCorrect() {
        assertEquals(1410, MongoDbResultCode.COLLECTION_ERROR.getCode());
        assertEquals(1411, MongoDbResultCode.COLLECTION_NOT_FOUND.getCode());
        assertEquals(1412, MongoDbResultCode.COLLECTION_ALREADY_EXISTS.getCode());
        assertEquals(1413, MongoDbResultCode.CREATE_COLLECTION_ERROR.getCode());
        assertEquals(1414, MongoDbResultCode.DROP_COLLECTION_ERROR.getCode());
    }

    @Test
    @DisplayName("文档操作错误码应正确")
    void documentErrorCodes_shouldBeCorrect() {
        assertEquals(1420, MongoDbResultCode.DOCUMENT_ERROR.getCode());
        assertEquals(1421, MongoDbResultCode.DOCUMENT_NOT_FOUND.getCode());
        assertEquals(1422, MongoDbResultCode.INSERT_DOCUMENT_ERROR.getCode());
        assertEquals(1423, MongoDbResultCode.UPDATE_DOCUMENT_ERROR.getCode());
        assertEquals(1424, MongoDbResultCode.DELETE_DOCUMENT_ERROR.getCode());
        assertEquals(1425, MongoDbResultCode.DUPLICATE_KEY_ERROR.getCode());
    }

    @Test
    @DisplayName("索引错误码应正确")
    void indexErrorCodes_shouldBeCorrect() {
        assertEquals(1430, MongoDbResultCode.INDEX_ERROR.getCode());
        assertEquals(1435, MongoDbResultCode.TTL_INDEX_ERROR.getCode());
    }

    @Test
    @DisplayName("聚合操作错误码应正确")
    void aggregationErrorCodes_shouldBeCorrect() {
        assertEquals(1440, MongoDbResultCode.AGGREGATION_ERROR.getCode());
        assertEquals(1441, MongoDbResultCode.PIPELINE_ERROR.getCode());
        assertEquals(1442, MongoDbResultCode.AGGREGATION_TIMEOUT.getCode());
        assertEquals(1443, MongoDbResultCode.INVALID_AGGREGATION_STAGE.getCode());
    }

    @Test
    @DisplayName("地理空间错误码应正确")
    void geoErrorCodes_shouldBeCorrect() {
        assertEquals(1450, MongoDbResultCode.GEO_ERROR.getCode());
        assertEquals(1451, MongoDbResultCode.INVALID_GEO_JSON.getCode());
        assertEquals(1452, MongoDbResultCode.GEO_INDEX_MISSING.getCode());
        assertEquals(1453, MongoDbResultCode.GEO_QUERY_ERROR.getCode());
        assertEquals(1454, MongoDbResultCode.COORDINATE_OUT_OF_RANGE.getCode());
    }

    @Test
    @DisplayName("时序数据错误码应正确")
    void timeSeriesErrorCodes_shouldBeCorrect() {
        assertEquals(1460, MongoDbResultCode.TIME_SERIES_ERROR.getCode());
        assertEquals(1461, MongoDbResultCode.INVALID_TIME_FIELD.getCode());
        assertEquals(1462, MongoDbResultCode.TIME_SERIES_COLLECTION_ERROR.getCode());
    }

    @Test
    @DisplayName("其他错误码应正确")
    void otherErrorCodes_shouldBeCorrect() {
        assertEquals(1470, MongoDbResultCode.QUERY_ERROR.getCode());
        assertEquals(1471, MongoDbResultCode.QUERY_TIMEOUT.getCode());
        assertEquals(1472, MongoDbResultCode.BULK_OPERATION_ERROR.getCode());
        assertEquals(1473, MongoDbResultCode.TRANSACTION_ERROR.getCode());
        assertEquals(1474, MongoDbResultCode.SESSION_ERROR.getCode());
    }

    @Test
    @DisplayName("valueOf应正确返回枚举值")
    void valueOf_shouldReturnCorrectEnum() {
        assertEquals(MongoDbResultCode.MONGODB_ERROR, MongoDbResultCode.valueOf("MONGODB_ERROR"));
        assertEquals(MongoDbResultCode.CONNECTION_ERROR, MongoDbResultCode.valueOf("CONNECTION_ERROR"));
        assertEquals(MongoDbResultCode.DOCUMENT_NOT_FOUND, MongoDbResultCode.valueOf("DOCUMENT_NOT_FOUND"));
    }

}
