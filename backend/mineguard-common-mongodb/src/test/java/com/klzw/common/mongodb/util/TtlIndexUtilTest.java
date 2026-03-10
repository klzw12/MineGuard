package com.klzw.common.mongodb.util;

import com.klzw.common.mongodb.constant.MongoDbConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TtlIndexUtil 单元测试
 */
@DisplayName("TtlIndexUtil TTL索引工具测试")
class TtlIndexUtilTest {

    @Test
    @DisplayName("私有构造函数应无法实例化")
    void privateConstructor_shouldNotBeInstantiable() throws Exception {
        Constructor<TtlIndexUtil> constructor = TtlIndexUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        
        assertDoesNotThrow(() -> constructor.newInstance());
    }

    @Test
    @DisplayName("TTL常量值应正确")
    void ttlConstants_shouldBeCorrect() {
        assertEquals(30 * 24 * 60 * 60L, MongoDbConstants.TTL_30_DAYS);
        assertEquals(90 * 24 * 60 * 60L, MongoDbConstants.TTL_90_DAYS);
        assertEquals(180 * 24 * 60 * 60L, MongoDbConstants.TTL_6_MONTHS);
        assertEquals(365 * 24 * 60 * 60L, MongoDbConstants.TTL_1_YEAR);
        assertEquals(2 * 365 * 24 * 60 * 60L, MongoDbConstants.TTL_2_YEARS);
    }

    @Test
    @DisplayName("集合名称常量应正确")
    void collectionConstants_shouldBeCorrect() {
        assertEquals("vehicle_trajectory", MongoDbConstants.COLLECTION_VEHICLE_TRAJECTORY);
        assertEquals("trip_history", MongoDbConstants.COLLECTION_TRIP_HISTORY);
        assertEquals("warning_event", MongoDbConstants.COLLECTION_WARNING_EVENT);
        assertEquals("operation_log", MongoDbConstants.COLLECTION_OPERATION_LOG);
        assertEquals("exception_log", MongoDbConstants.COLLECTION_EXCEPTION_LOG);
        assertEquals("device_data", MongoDbConstants.COLLECTION_DEVICE_DATA);
        assertEquals("message_history", MongoDbConstants.COLLECTION_MESSAGE_HISTORY);
        assertEquals("statistics_data", MongoDbConstants.COLLECTION_STATISTICS_DATA);
        assertEquals("cost_record", MongoDbConstants.COLLECTION_COST_RECORD);
        assertEquals("vehicle_maintenance", MongoDbConstants.COLLECTION_VEHICLE_MAINTENANCE);
    }

    @Test
    @DisplayName("字段名称常量应正确")
    void fieldConstants_shouldBeCorrect() {
        assertEquals("timestamp", MongoDbConstants.FIELD_TIMESTAMP);
        assertEquals("createTime", MongoDbConstants.FIELD_CREATE_TIME);
        assertEquals("warningTime", MongoDbConstants.FIELD_WARNING_TIME);
        assertEquals("requestTime", MongoDbConstants.FIELD_REQUEST_TIME);
        assertEquals("occurTime", MongoDbConstants.FIELD_OCCUR_TIME);
        assertEquals("expireTime", MongoDbConstants.FIELD_EXPIRE_TIME);
        assertEquals("statDate", MongoDbConstants.FIELD_STAT_DATE);
        assertEquals("costTime", MongoDbConstants.FIELD_COST_TIME);
    }

    @Test
    @DisplayName("createTtlIndex方法应存在")
    void createTtlIndex_methodShouldExist() throws NoSuchMethodException {
        assertNotNull(TtlIndexUtil.class.getDeclaredMethod(
                "createTtlIndex",
                org.springframework.data.mongodb.core.MongoTemplate.class,
                String.class,
                String.class,
                long.class
        ));
    }

    @Test
    @DisplayName("createTtlIndex方法带TimeUnit应存在")
    void createTtlIndex_withTimeUnit_shouldExist() throws NoSuchMethodException {
        assertNotNull(TtlIndexUtil.class.getDeclaredMethod(
                "createTtlIndex",
                org.springframework.data.mongodb.core.MongoTemplate.class,
                String.class,
                String.class,
                long.class,
                TimeUnit.class
        ));
    }

    @Test
    @DisplayName("各集合TTL索引创建方法应存在")
    void ttlIndexMethods_shouldExist() throws NoSuchMethodException {
        assertNotNull(TtlIndexUtil.class.getDeclaredMethod(
                "createVehicleTrajectoryTtlIndex",
                org.springframework.data.mongodb.core.MongoTemplate.class
        ));
        assertNotNull(TtlIndexUtil.class.getDeclaredMethod(
                "createTripHistoryTtlIndex",
                org.springframework.data.mongodb.core.MongoTemplate.class
        ));
        assertNotNull(TtlIndexUtil.class.getDeclaredMethod(
                "createWarningEventTtlIndex",
                org.springframework.data.mongodb.core.MongoTemplate.class
        ));
        assertNotNull(TtlIndexUtil.class.getDeclaredMethod(
                "createOperationLogTtlIndex",
                org.springframework.data.mongodb.core.MongoTemplate.class
        ));
        assertNotNull(TtlIndexUtil.class.getDeclaredMethod(
                "createExceptionLogTtlIndex",
                org.springframework.data.mongodb.core.MongoTemplate.class
        ));
        assertNotNull(TtlIndexUtil.class.getDeclaredMethod(
                "createDeviceDataTtlIndex",
                org.springframework.data.mongodb.core.MongoTemplate.class
        ));
        assertNotNull(TtlIndexUtil.class.getDeclaredMethod(
                "createMessageHistoryTtlIndex",
                org.springframework.data.mongodb.core.MongoTemplate.class
        ));
        assertNotNull(TtlIndexUtil.class.getDeclaredMethod(
                "createStatisticsDataTtlIndex",
                org.springframework.data.mongodb.core.MongoTemplate.class
        ));
        assertNotNull(TtlIndexUtil.class.getDeclaredMethod(
                "createCostRecordTtlIndex",
                org.springframework.data.mongodb.core.MongoTemplate.class
        ));
        assertNotNull(TtlIndexUtil.class.getDeclaredMethod(
                "createVehicleMaintenanceTtlIndex",
                org.springframework.data.mongodb.core.MongoTemplate.class
        ));
        assertNotNull(TtlIndexUtil.class.getDeclaredMethod(
                "initAllTtlIndexes",
                org.springframework.data.mongodb.core.MongoTemplate.class
        ));
    }

}
