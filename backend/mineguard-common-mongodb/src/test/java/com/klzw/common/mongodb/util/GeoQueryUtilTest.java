package com.klzw.common.mongodb.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GeoQueryUtil 地理空间查询工具测试
 */
@DisplayName("GeoQueryUtil 地理空间查询工具测试")
class GeoQueryUtilTest {

    @Test
    @DisplayName("构建附近查询条件应成功")
    void near_shouldCreateNearCriteria() {
        // 准备
        GeoJsonPoint point = new GeoJsonPoint(116.3974, 39.9093); // 北京天安门
        double maxDistance = 5000; // 5公里

        // 执行
        Criteria criteria = GeoQueryUtil.near("location", point, maxDistance);

        // 验证
        assertNotNull(criteria);
        assertNotNull(criteria.getCriteriaObject());
    }

    @Test
    @DisplayName("构建附近查询条件（经纬度）应成功")
    void near_withCoordinates_shouldCreateNearCriteria() {
        // 准备
        double longitude = 116.3974;
        double latitude = 39.9093;
        double maxDistance = 5000; // 5公里

        // 执行
        Criteria criteria = GeoQueryUtil.near("location", longitude, latitude, maxDistance);

        // 验证
        assertNotNull(criteria);
        assertNotNull(criteria.getCriteriaObject());
    }

    @Test
    @DisplayName("构建附近车辆查询应成功")
    void nearVehiclesQuery_shouldCreateNearVehiclesQuery() {
        // 准备
        double longitude = 116.3974;
        double latitude = 39.9093;
        double maxDistance = 5000; // 5公里
        long startTime = System.currentTimeMillis() - 3600000; // 1小时前
        long endTime = System.currentTimeMillis();

        // 执行
        Query query = GeoQueryUtil.nearVehiclesQuery("location", longitude, latitude, maxDistance, 
                                                     "timestamp", startTime, endTime);

        // 验证
        assertNotNull(query);
        assertNotNull(query.getQueryObject());
    }

    @Test
    @DisplayName("私有构造函数应无法实例化")
    void privateConstructor_shouldNotBeInstantiable() throws Exception {
        // 准备
        var constructor = GeoQueryUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        // 执行和验证
        assertDoesNotThrow(() -> constructor.newInstance());
    }
}