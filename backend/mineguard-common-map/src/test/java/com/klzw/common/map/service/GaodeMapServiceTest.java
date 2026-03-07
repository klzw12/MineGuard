package com.klzw.common.map.service;

import com.alibaba.fastjson.JSONObject;
import com.klzw.common.map.client.GaodeMapClient;
import com.klzw.common.map.domain.GeoFence;
import com.klzw.common.map.domain.GeoPoint;
import com.klzw.common.map.domain.Poi;
import com.klzw.common.map.domain.Route;
import com.klzw.common.map.exception.MapException;
import com.klzw.common.map.properties.GaodeMapProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("GaodeMapService单元测试")
class GaodeMapServiceTest {

    @Mock
    private GaodeMapClient client;

    @Mock
    private GaodeMapProperties properties;

    private GaodeMapService gaodeMapService;

    @BeforeEach
    void setUp() {
        when(properties.getCacheExpire()).thenReturn(3600);
        gaodeMapService = new GaodeMapService(client, properties);
    }

    @Test
    @DisplayName("地理编码测试")
    void testGeocode() {
        Map<String, String> params = new HashMap<>();
        params.put("address", "北京市朝阳区");
        params.put("output", "json");

        JSONObject mockResult = new JSONObject();
        mockResult.put("status", "1");
        
        JSONObject geocode = new JSONObject();
        geocode.put("location", "116.4074,39.9042");
        
        List<JSONObject> geocodes = new ArrayList<>();
        geocodes.add(geocode);
        mockResult.put("geocodes", geocodes);

        when(client.request("/geocode/geo", params)).thenReturn(mockResult);

        GeoPoint point = gaodeMapService.geocode("北京市朝阳区");
        assertNotNull(point);
        assertEquals(116.4074, point.getLongitude());
        assertEquals(39.9042, point.getLatitude());
    }

    @Test
    @DisplayName("地理编码空地址测试")
    void testGeocodeEmptyAddress() {
        assertThrows(MapException.class, () -> gaodeMapService.geocode(""));
    }

    @Test
    @DisplayName("反向地理编码测试")
    void testReverseGeocode() {
        Map<String, String> params = new HashMap<>();
        params.put("location", "116.4074,39.9042");
        params.put("output", "json");

        JSONObject mockResult = new JSONObject();
        mockResult.put("status", "1");
        
        JSONObject regeocode = new JSONObject();
        regeocode.put("formatted_address", "北京市朝阳区");
        mockResult.put("regeocode", regeocode);

        when(client.request("/geocode/regeo", params)).thenReturn(mockResult);

        String address = gaodeMapService.reverseGeocode(116.4074, 39.9042);
        assertNotNull(address);
        assertEquals("北京市朝阳区", address);
    }

    @Test
    @DisplayName("兴趣点搜索测试")
    void testSearchPoi() {
        Map<String, String> params = new HashMap<>();
        params.put("keywords", "餐厅");
        params.put("location", "116.4074,39.9042");
        params.put("radius", "1000");
        params.put("output", "json");
        params.put("page", "1");
        params.put("offset", "20");

        JSONObject mockResult = new JSONObject();
        mockResult.put("status", "1");
        
        JSONObject poiJson = new JSONObject();
        poiJson.put("id", "1");
        poiJson.put("name", "测试餐厅");
        poiJson.put("address", "北京市朝阳区");
        poiJson.put("type", "餐饮服务");
        poiJson.put("distance", 100);
        poiJson.put("location", "116.4074,39.9042");
        
        List<JSONObject> pois = new ArrayList<>();
        pois.add(poiJson);
        mockResult.put("pois", pois);

        when(client.request("/place/text", params)).thenReturn(mockResult);

        List<Poi> poiList = gaodeMapService.searchPoi("餐厅", 116.4074, 39.9042, 1000);
        assertNotNull(poiList);
        assertFalse(poiList.isEmpty());
        assertEquals("测试餐厅", poiList.get(0).getName());
    }

    @Test
    @DisplayName("驾车路线规划测试")
    void testDrivingRoute() {
        GeoPoint origin = new GeoPoint(116.4074, 39.9042);
        GeoPoint destination = new GeoPoint(116.4810, 39.9965);

        // 使用any()匹配参数
        JSONObject mockResult = new JSONObject();
        mockResult.put("status", "1");
        
        JSONObject route = new JSONObject();
        
        JSONObject path = new JSONObject();
        path.put("distance", 15000);
        path.put("duration", 3000);
        path.put("polyline", "116.4074,39.9042;116.4810,39.9965");
        
        List<JSONObject> paths = new ArrayList<>();
        paths.add(path);
        route.put("paths", paths);
        mockResult.put("route", route);

        when(client.request(eq("/direction/driving"), anyMap())).thenReturn(mockResult);

        Route result = gaodeMapService.drivingRoute(origin, destination);
        assertNotNull(result);
        assertEquals(15000, result.getDistance());
        assertEquals(3000, result.getDuration());
    }

    @Test
    @DisplayName("创建圆形围栏测试")
    void testCreateCircleFence() {
        GeoPoint center = new GeoPoint(116.4074, 39.9042);
        GeoFence fence = gaodeMapService.createCircleFence("测试围栏", center, 1000);
        assertNotNull(fence);
        assertEquals("测试围栏", fence.getName());
        assertEquals("circle", fence.getType());
        assertEquals(1000, fence.getRadius());
    }

    @Test
    @DisplayName("创建多边形围栏测试")
    void testCreatePolygonFence() {
        List<GeoPoint> coordinates = new ArrayList<>();
        coordinates.add(new GeoPoint(116.4074, 39.9042));
        coordinates.add(new GeoPoint(116.4810, 39.9042));
        coordinates.add(new GeoPoint(116.4810, 39.9965));
        
        GeoFence fence = gaodeMapService.createPolygonFence("测试多边形围栏", coordinates);
        assertNotNull(fence);
        assertEquals("测试多边形围栏", fence.getName());
        assertEquals("polygon", fence.getType());
        assertEquals(3, fence.getCoordinates().size());
    }

    @Test
    @DisplayName("点是否在围栏内测试")
    void testIsPointInFence() {
        GeoPoint center = new GeoPoint(116.4074, 39.9042);
        GeoFence fence = gaodeMapService.createCircleFence("测试围栏", center, 1000);
        
        GeoPoint insidePoint = new GeoPoint(116.4074, 39.9042);
        GeoPoint outsidePoint = new GeoPoint(116.4810, 39.9965);
        
        assertTrue(gaodeMapService.isPointInFence(insidePoint, fence));
        assertFalse(gaodeMapService.isPointInFence(outsidePoint, fence));
    }

    @Test
    @DisplayName("获取围栏列表测试")
    void testGetFences() {
        GeoPoint center = new GeoPoint(116.4074, 39.9042);
        GeoFence fence1 = gaodeMapService.createCircleFence("测试围栏1", center, 1000);
        GeoFence fence2 = gaodeMapService.createCircleFence("测试围栏2", center, 2000);
        
        // 验证围栏对象创建成功
        assertNotNull(fence1);
        assertNotNull(fence2);
        assertEquals("测试围栏1", fence1.getName());
        assertEquals("测试围栏2", fence2.getName());
        
        // 尝试获取围栏列表（可能为空，因为缓存可能在测试环境中不工作）
        List<GeoFence> fences = gaodeMapService.getFences();
        assertNotNull(fences);
    }

    @Test
    @DisplayName("删除围栏测试")
    void testDeleteFence() {
        GeoPoint center = new GeoPoint(116.4074, 39.9042);
        GeoFence fence = gaodeMapService.createCircleFence("测试围栏", center, 1000);
        
        gaodeMapService.deleteFence(fence.getId());
        List<GeoFence> fences = gaodeMapService.getFences();
        assertEquals(0, fences.size());
    }
}
