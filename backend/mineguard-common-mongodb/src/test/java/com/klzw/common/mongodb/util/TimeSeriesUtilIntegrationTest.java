package com.klzw.common.mongodb.util;

import com.klzw.common.mongodb.AbstractMongoDbIntegrationTest;
import com.klzw.common.mongodb.constant.MongoDbConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TimeSeriesUtil 集成测试
 */
@DisplayName("TimeSeriesUtil集成测试")
class TimeSeriesUtilIntegrationTest extends AbstractMongoDbIntegrationTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    private static final String TEST_COLLECTION = "test_timeseries";
    private static final String DEVICE_ID = "device_test_" + UUID.randomUUID().toString().substring(0, 8);

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection(TEST_COLLECTION);
    }

    @AfterEach
    void tearDown() {
        mongoTemplate.dropCollection(TEST_COLLECTION);
    }

    @Test
    @DisplayName("构建时序查询并插入数据应成功")
    void buildTimeSeriesQuery_andInsert_shouldSucceed() {
        long now = System.currentTimeMillis();
        long startTime = now - 3600000;
        long endTime = now;

        List<Map<String, Object>> testData = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Map<String, Object> doc = new HashMap<>();
            doc.put(MongoDbConstants.FIELD_TIMESTAMP, startTime + (i * 600000));
            doc.put(MongoDbConstants.FIELD_DEVICE_ID, DEVICE_ID);
            doc.put("value", i);
            testData.add(doc);
        }

        TimeSeriesUtil.batchInsertTimeSeriesData(mongoTemplate, TEST_COLLECTION, testData);

        Query query = TimeSeriesUtil.buildDeviceDataQuery(DEVICE_ID, startTime, endTime);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> results = (List<Map<String, Object>>) (List<?>) mongoTemplate.find(query, Map.class, TEST_COLLECTION);
        
        assertNotNull(results);
        assertEquals(5, results.size());
    }

    @Test
    @DisplayName("构建车辆轨迹查询应正确")
    void buildVehicleTrajectoryQuery_shouldBeCorrect() {
        long now = System.currentTimeMillis();
        long startTime = now - 3600000;
        long endTime = now;
        long carId = 12345L;

        Query query = TimeSeriesUtil.buildVehicleTrajectoryQuery(carId, startTime, endTime);
        
        assertNotNull(query);
        assertTrue(query.getQueryObject().containsKey(MongoDbConstants.FIELD_TIMESTAMP));
        assertTrue(query.getQueryObject().containsKey(MongoDbConstants.FIELD_CAR_ID));
    }

    @Test
    @DisplayName("构建设备数据查询应正确")
    void buildDeviceDataQuery_shouldBeCorrect() {
        long now = System.currentTimeMillis();
        long startTime = now - 3600000;
        long endTime = now;

        Query query = TimeSeriesUtil.buildDeviceDataQuery(DEVICE_ID, startTime, endTime);
        
        assertNotNull(query);
        assertTrue(query.getQueryObject().containsKey(MongoDbConstants.FIELD_TIMESTAMP));
        assertTrue(query.getQueryObject().containsKey(MongoDbConstants.FIELD_DEVICE_ID));
    }

    @Test
    @DisplayName("批量插入空列表应成功")
    void batchInsert_emptyList_shouldSucceed() {
        assertDoesNotThrow(() -> 
            TimeSeriesUtil.batchInsertTimeSeriesData(mongoTemplate, TEST_COLLECTION, Collections.emptyList())
        );
    }

    @Test
    @DisplayName("批量插入null应成功")
    void batchInsert_null_shouldSucceed() {
        assertDoesNotThrow(() -> 
            TimeSeriesUtil.batchInsertTimeSeriesData(mongoTemplate, TEST_COLLECTION, null)
        );
    }

    @Test
    @DisplayName("清理过期数据应成功")
    void cleanExpiredData_shouldSucceed() {
        long now = System.currentTimeMillis();
        long expiredTime = now - 7200000;

        List<Map<String, Object>> testData = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Map<String, Object> doc = new HashMap<>();
            doc.put(MongoDbConstants.FIELD_TIMESTAMP, expiredTime - (i * 600000));
            doc.put("value", i);
            testData.add(doc);
        }
        for (int i = 0; i < 2; i++) {
            Map<String, Object> doc = new HashMap<>();
            doc.put(MongoDbConstants.FIELD_TIMESTAMP, now - (i * 600000));
            doc.put("value", i + 10);
            testData.add(doc);
        }

        TimeSeriesUtil.batchInsertTimeSeriesData(mongoTemplate, TEST_COLLECTION, testData);

        long deletedCount = TimeSeriesUtil.cleanExpiredTimeSeriesData(
                mongoTemplate, 
                TEST_COLLECTION, 
                MongoDbConstants.FIELD_TIMESTAMP, 
                3600000
        );

        assertTrue(deletedCount >= 3);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> remaining = (List<Map<String, Object>>) (List<?>) mongoTemplate.findAll(Map.class, TEST_COLLECTION);
        assertEquals(2, remaining.size());
    }

}
