package com.klzw.common.mongodb.constant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MongoDbConstants 单元测试
 */
@DisplayName("MongoDbConstants常量测试")
class MongoDbConstantsTest {

    @Test
    @DisplayName("集合名称常量应正确")
    void collectionConstants_shouldBeCorrect() {
        assertEquals("user_profile", MongoDbConstants.COLLECTION_USER_PROFILE);
        assertEquals("vehicle_trajectory", MongoDbConstants.COLLECTION_VEHICLE_TRAJECTORY);
        assertEquals("vehicle_maintenance", MongoDbConstants.COLLECTION_VEHICLE_MAINTENANCE);
        assertEquals("trip_history", MongoDbConstants.COLLECTION_TRIP_HISTORY);
        assertEquals("warning_event", MongoDbConstants.COLLECTION_WARNING_EVENT);
        assertEquals("statistics_data", MongoDbConstants.COLLECTION_STATISTICS_DATA);
        assertEquals("cost_record", MongoDbConstants.COLLECTION_COST_RECORD);
        assertEquals("operation_log", MongoDbConstants.COLLECTION_OPERATION_LOG);
        assertEquals("exception_log", MongoDbConstants.COLLECTION_EXCEPTION_LOG);
        assertEquals("device_data", MongoDbConstants.COLLECTION_DEVICE_DATA);
        assertEquals("message_history", MongoDbConstants.COLLECTION_MESSAGE_HISTORY);
    }

    @Test
    @DisplayName("字段名称常量应正确")
    void fieldConstants_shouldBeCorrect() {
        assertEquals("timestamp", MongoDbConstants.FIELD_TIMESTAMP);
        assertEquals("warningTime", MongoDbConstants.FIELD_WARNING_TIME);
        assertEquals("carId", MongoDbConstants.FIELD_CAR_ID);
        assertEquals("warningType", MongoDbConstants.FIELD_WARNING_TYPE);
        assertEquals("warningLevel", MongoDbConstants.FIELD_WARNING_LEVEL);
        assertEquals("status", MongoDbConstants.FIELD_STATUS);
        assertEquals("deviceId", MongoDbConstants.FIELD_DEVICE_ID);
        assertEquals("createTime", MongoDbConstants.FIELD_CREATE_TIME);
        assertEquals("requestTime", MongoDbConstants.FIELD_REQUEST_TIME);
        assertEquals("occurTime", MongoDbConstants.FIELD_OCCUR_TIME);
        assertEquals("expireTime", MongoDbConstants.FIELD_EXPIRE_TIME);
        assertEquals("statDate", MongoDbConstants.FIELD_STAT_DATE);
        assertEquals("costTime", MongoDbConstants.FIELD_COST_TIME);
    }

    @Test
    @DisplayName("状态值常量应正确")
    void statusConstants_shouldBeCorrect() {
        assertEquals("running", MongoDbConstants.STATUS_RUNNING);
    }

    @Test
    @DisplayName("TTL过期时间常量应正确")
    void ttlConstants_shouldBeCorrect() {
        assertEquals(2592000L, MongoDbConstants.TTL_30_DAYS);
        assertEquals(7776000L, MongoDbConstants.TTL_90_DAYS);
        assertEquals(15552000L, MongoDbConstants.TTL_6_MONTHS);
        assertEquals(31536000L, MongoDbConstants.TTL_1_YEAR);
        assertEquals(63072000L, MongoDbConstants.TTL_2_YEARS);
    }

    @Test
    @DisplayName("私有构造函数应无法实例化")
    void privateConstructor_shouldNotBeInstantiable() throws Exception {
        Constructor<MongoDbConstants> constructor = MongoDbConstants.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        
        assertDoesNotThrow(() -> constructor.newInstance());
    }

}
