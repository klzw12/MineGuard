package com.klzw.common.mongodb.util;

import com.klzw.common.mongodb.constant.MongoDbConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TimeSeriesUtil 单元测试
 */
@DisplayName("TimeSeriesUtil时序数据工具测试")
class TimeSeriesUtilTest {

    @Test
    @DisplayName("构建时序数据查询应正确")
    void buildTimeSeriesQuery_shouldBeCorrect() {
        long startTime = System.currentTimeMillis() - 3600000;
        long endTime = System.currentTimeMillis();
        
        Query query = TimeSeriesUtil.buildTimeSeriesQuery("timestamp", startTime, endTime);
        
        assertNotNull(query);
        assertTrue(query.getQueryObject().containsKey("timestamp"));
        assertEquals(1, query.getQueryObject().keySet().size());
    }

    @Test
    @DisplayName("构建时序数据查询带额外条件应正确")
    void buildTimeSeriesQuery_withOtherCriteria_shouldBeCorrect() {
        long startTime = System.currentTimeMillis() - 3600000;
        long endTime = System.currentTimeMillis();
        
        Query query = TimeSeriesUtil.buildTimeSeriesQuery(
                "timestamp", 
                startTime, 
                endTime, 
                Criteria.where("deviceId").is("device001")
        );
        
        assertNotNull(query);
        assertTrue(query.getQueryObject().containsKey("timestamp"));
        assertTrue(query.getQueryObject().containsKey("deviceId"));
    }

    @Test
    @DisplayName("构建车辆轨迹查询应使用正确常量")
    void buildVehicleTrajectoryQuery_shouldUseCorrectConstants() {
        long carId = 12345L;
        long startTime = System.currentTimeMillis() - 3600000;
        long endTime = System.currentTimeMillis();
        
        Query query = TimeSeriesUtil.buildVehicleTrajectoryQuery(carId, startTime, endTime);
        
        assertNotNull(query);
        assertTrue(query.getQueryObject().containsKey(MongoDbConstants.FIELD_TIMESTAMP));
        assertTrue(query.getQueryObject().containsKey(MongoDbConstants.FIELD_CAR_ID));
    }

    @Test
    @DisplayName("构建设备数据查询应使用正确常量")
    void buildDeviceDataQuery_shouldUseCorrectConstants() {
        String deviceId = "device001";
        long startTime = System.currentTimeMillis() - 3600000;
        long endTime = System.currentTimeMillis();
        
        Query query = TimeSeriesUtil.buildDeviceDataQuery(deviceId, startTime, endTime);
        
        assertNotNull(query);
        assertTrue(query.getQueryObject().containsKey(MongoDbConstants.FIELD_TIMESTAMP));
        assertTrue(query.getQueryObject().containsKey(MongoDbConstants.FIELD_DEVICE_ID));
    }

    @Test
    @DisplayName("构建预警查询应使用正确常量")
    void buildWarningQuery_shouldUseCorrectConstants() {
        String warningType = "OVERSPEED";
        long startTime = System.currentTimeMillis() - 3600000;
        long endTime = System.currentTimeMillis();
        
        Query query = TimeSeriesUtil.buildWarningQuery(warningType, startTime, endTime);
        
        assertNotNull(query);
        assertTrue(query.getQueryObject().containsKey(MongoDbConstants.FIELD_TIMESTAMP));
        assertTrue(query.getQueryObject().containsKey(MongoDbConstants.FIELD_WARNING_TYPE));
    }

    @Test
    @DisplayName("构建行程历史查询应使用正确常量")
    void buildTripHistoryQuery_shouldUseCorrectConstants() {
        long carId = 12345L;
        long startTime = System.currentTimeMillis() - 3600000;
        long endTime = System.currentTimeMillis();
        
        Query query = TimeSeriesUtil.buildTripHistoryQuery(carId, startTime, endTime);
        
        assertNotNull(query);
        assertTrue(query.getQueryObject().containsKey(MongoDbConstants.FIELD_TIMESTAMP));
        assertTrue(query.getQueryObject().containsKey(MongoDbConstants.FIELD_CAR_ID));
    }

    @Test
    @DisplayName("查询应包含时间排序")
    void query_shouldContainTimeSort() {
        long startTime = System.currentTimeMillis() - 3600000;
        long endTime = System.currentTimeMillis();
        
        Query query = TimeSeriesUtil.buildTimeSeriesQuery("timestamp", startTime, endTime);
        
        assertNotNull(query);
        assertNotNull(query.getSortObject());
    }

    @Test
    @DisplayName("构建查询带多个额外条件应正确")
    void buildTimeSeriesQuery_withMultipleCriteria_shouldBeCorrect() {
        long startTime = System.currentTimeMillis() - 3600000;
        long endTime = System.currentTimeMillis();
        
        Query query = TimeSeriesUtil.buildTimeSeriesQuery(
                "timestamp", 
                startTime, 
                endTime, 
                Criteria.where("field1").is("value1"),
                Criteria.where("field2").gt(100)
        );
        
        assertNotNull(query);
        assertTrue(query.getQueryObject().containsKey("timestamp"));
        assertTrue(query.getQueryObject().containsKey("field1"));
        assertTrue(query.getQueryObject().containsKey("field2"));
    }

}
