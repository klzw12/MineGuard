package com.klzw.common.map.integration;

import com.klzw.common.map.AbstractMapIntegrationTest;
import com.klzw.common.map.domain.GeoFence;
import com.klzw.common.map.domain.GeoPoint;
import com.klzw.common.map.service.GaodeMapService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Map模块集成测试")
class MapIntegrationTest extends AbstractMapIntegrationTest {

    @Autowired
    private GaodeMapService gaodeMapService;

    @Test
    @DisplayName("集成测试 - 地理围栏功能")
    void testGeoFenceFunctionality() {
        // 创建圆形围栏
        GeoPoint center = new GeoPoint(116.4074, 39.9042);
        GeoFence circleFence = gaodeMapService.createCircleFence("测试圆形围栏", center, 1000);
        assertNotNull(circleFence);
        assertEquals("测试圆形围栏", circleFence.getName());
        assertEquals("circle", circleFence.getType());

        // 创建多边形围栏
        List<GeoPoint> coordinates = new ArrayList<>();
        coordinates.add(new GeoPoint(116.4074, 39.9042));
        coordinates.add(new GeoPoint(116.4810, 39.9042));
        coordinates.add(new GeoPoint(116.4810, 39.9965));
        GeoFence polygonFence = gaodeMapService.createPolygonFence("测试多边形围栏", coordinates);
        assertNotNull(polygonFence);
        assertEquals("测试多边形围栏", polygonFence.getName());
        assertEquals("polygon", polygonFence.getType());

        // 测试围栏内点
        GeoPoint insidePoint = new GeoPoint(116.4074, 39.9042);
        assertTrue(gaodeMapService.isPointInFence(insidePoint, circleFence));

        // 测试围栏外点
        GeoPoint outsidePoint = new GeoPoint(116.4810, 39.9965);
        assertFalse(gaodeMapService.isPointInFence(outsidePoint, circleFence));

        // 获取围栏列表
        List<GeoFence> fences = gaodeMapService.getFences();
        assertNotNull(fences);
        assertEquals(2, fences.size());

        // 删除围栏
        gaodeMapService.deleteFence(circleFence.getId());
        gaodeMapService.deleteFence(polygonFence.getId());
        List<GeoFence> fencesAfterDelete = gaodeMapService.getFences();
        assertEquals(0, fencesAfterDelete.size());
    }

    @Test
    @DisplayName("集成测试 - 服务Bean注入")
    void testServiceBeanInjection() {
        assertNotNull(gaodeMapService);
    }
}
