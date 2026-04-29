package com.klzw.service.trip;

import com.klzw.common.redis.service.RedisCacheService;
import com.klzw.service.trip.document.TripTrackDocument;
import com.klzw.common.core.domain.dto.TripTrackDTO;
import com.klzw.service.trip.exception.TripException;
import com.klzw.service.trip.repository.TripTrackMongoRepository;
import com.klzw.service.trip.service.TripTrackService;
import com.klzw.service.trip.service.TripValidatorService;
import com.klzw.service.trip.service.impl.TripTrackServiceImpl;
import com.klzw.service.trip.vo.TripTrackVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TripTrackService单元测试类
 * 测试覆盖率目标: 70%
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TripTrackService单元测试")
public class TripTrackServiceTest {

    @Mock
    private RedisCacheService redisCacheService;

    @Mock
    private TripValidatorService tripValidatorService;

    @Mock
    private TripTrackMongoRepository tripTrackMongoRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private TripTrackServiceImpl tripTrackService;

    private TripTrackDTO testTrackDTO;
    private TripTrackDocument testTrackDocument;

    @BeforeEach
    void setUp() {
        // 初始化测试轨迹DTO
        testTrackDTO = new TripTrackDTO();
        testTrackDTO.setTripId(1L);
        testTrackDTO.setVehicleId(100L);
        testTrackDTO.setDriverId(200L);
        testTrackDTO.setLongitude(116.397428);
        testTrackDTO.setLatitude(39.90923);
        testTrackDTO.setSpeed(60.0);
        testTrackDTO.setDirection(90.0);
        testTrackDTO.setAltitude(100.0);
        testTrackDTO.setRecordTime(System.currentTimeMillis());

        // 初始化测试轨迹文档
        testTrackDocument = new TripTrackDocument();
        testTrackDocument.setId("doc1");
        testTrackDocument.setTripId(1L);
        testTrackDocument.setVehicleId(100L);
        testTrackDocument.setLongitude(116.397428);
        testTrackDocument.setLatitude(39.90923);
        testTrackDocument.setSpeed(60.0);
        testTrackDocument.setDirection(90.0);
        testTrackDocument.setAltitude(100.0);
        testTrackDocument.setRecordTime(LocalDateTime.now());
        testTrackDocument.setCreateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("测试保存轨迹点")
    void testSaveTrackPoint() {
        doNothing().when(tripValidatorService).validateTripInProgress(1L);
        when(redisCacheService.lSize("trip:track:1")).thenReturn(0L);

        tripTrackService.uploadTrack(testTrackDTO);

        verify(tripValidatorService).validateTripInProgress(1L);
        verify(redisCacheService).lPush(eq("trip:track:1"), eq(testTrackDTO));
        verify(redisCacheService).set(eq("trip:track:vehicle:100"), eq(1L), eq(2L), any());
    }

    @Test
    @DisplayName("测试保存轨迹点 - 验证失败")
    void testSaveTrackPoint_ValidateFailed() {
        // 模拟验证失败
        doThrow(new TripException(com.klzw.service.trip.constant.TripResultCode.TRIP_STATUS_ERROR))
                .when(tripValidatorService).validateTripInProgress(1L);

        // 执行并验证异常
        assertThrows(TripException.class, () -> tripTrackService.uploadTrack(testTrackDTO));
        verify(redisCacheService, never()).lPush(anyString(), any());
    }

    @Test
    @DisplayName("测试批量保存轨迹点")
    void testSaveTrackPointBatch() {
        TripTrackDTO dto1 = new TripTrackDTO();
        dto1.setTripId(1L);
        dto1.setVehicleId(100L);
        dto1.setLongitude(116.397428);
        dto1.setLatitude(39.90923);

        TripTrackDTO dto2 = new TripTrackDTO();
        dto2.setTripId(1L);
        dto2.setVehicleId(100L);
        dto2.setLongitude(116.407428);
        dto2.setLatitude(39.91923);

        List<TripTrackDTO> dtoList = Arrays.asList(dto1, dto2);

        // 模拟验证通过
        doNothing().when(tripValidatorService).validateTripInProgress(1L);

        // 执行批量保存
        tripTrackService.uploadTrackBatch(dtoList);

        // 验证
        verify(tripValidatorService).validateTripInProgress(1L);
        verify(redisCacheService, times(2)).lPush(eq("trip:track:1"), any(TripTrackDTO.class));
    }

    @Test
    @DisplayName("测试批量保存轨迹点 - 空列表")
    void testSaveTrackPointBatch_EmptyList() {
        // 执行批量保存
        tripTrackService.uploadTrackBatch(Collections.emptyList());

        // 验证没有调用任何存储
        verify(redisCacheService, never()).lPush(anyString(), any());
        verify(tripValidatorService, never()).validateTripInProgress(anyLong());
    }

    @Test
    @DisplayName("测试根据行程ID获取轨迹 - 从Redis获取")
    @SuppressWarnings("unchecked")
    void testGetTracksByTripId_FromRedis() {
        List<TripTrackDTO> redisTracks = Arrays.asList(testTrackDTO);
        when(redisCacheService.lRange(eq("trip:track:1"), eq(0L), eq(-1L))).thenReturn((List) redisTracks);

        // 执行获取
        List<TripTrackVO> result = tripTrackService.getByTripId(1L);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getTripId());
        assertEquals("100", result.get(0).getVehicleId());
        verify(redisCacheService).lRange(eq("trip:track:1"), eq(0L), eq(-1L));
        verify(mongoTemplate, never()).find(any(Query.class), eq(TripTrackDocument.class));
    }

    @Test
    @DisplayName("测试根据行程ID获取轨迹 - 从MongoDB获取")
    void testGetTracksByTripId_FromMongoDB() {
        // 模拟Redis为空
        when(redisCacheService.lRange(eq("trip:track:1"), eq(0L), eq(-1L))).thenReturn(Collections.emptyList());
        // 模拟MongoDB查询
        List<TripTrackDocument> mongoTracks = Arrays.asList(testTrackDocument);
        when(mongoTemplate.find(any(Query.class), eq(TripTrackDocument.class))).thenReturn(mongoTracks);

        // 执行获取
        List<TripTrackVO> result = tripTrackService.getByTripId(1L);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getTripId());
        verify(redisCacheService).lRange(eq("trip:track:1"), eq(0L), eq(-1L));
        verify(mongoTemplate).find(any(Query.class), eq(TripTrackDocument.class));
    }

    @Test
    @DisplayName("测试根据行程ID获取轨迹 - 无数据")
    void testGetTracksByTripId_NoData() {
        // 模拟Redis和MongoDB都为空
        when(redisCacheService.lRange(eq("trip:track:1"), eq(0L), eq(-1L))).thenReturn(Collections.emptyList());
        when(mongoTemplate.find(any(Query.class), eq(TripTrackDocument.class))).thenReturn(Collections.emptyList());

        // 执行获取
        List<TripTrackVO> result = tripTrackService.getByTripId(1L);

        // 验证
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试获取轨迹回放")
    @SuppressWarnings("unchecked")
    void testGetTrackPlayback() {
        List<TripTrackDTO> redisTracks = Arrays.asList(testTrackDTO);
        when(redisCacheService.lRange(eq("trip:track:1"), eq(0L), eq(-1L))).thenReturn((List) redisTracks);

        // 执行获取
        List<TripTrackVO> result = tripTrackService.getByTripId(1L);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTrackDTO.getLongitude(), result.get(0).getLongitude());
        assertEquals(testTrackDTO.getLatitude(), result.get(0).getLatitude());
        assertEquals(testTrackDTO.getSpeed(), result.get(0).getSpeed());
    }

    @Test
    @DisplayName("测试获取最新轨迹点 - 从Redis获取")
    @SuppressWarnings("unchecked")
    void testGetLatestTrack_FromRedis() {
        List<?> redisTracks = Arrays.asList(testTrackDTO);
        when(redisCacheService.lRange(eq("trip:track:1"), eq(0L), eq(-1L))).thenReturn((List) redisTracks);

        // 执行获取
        TripTrackVO result = tripTrackService.getLatestTrack(1L);

        // 验证
        assertNotNull(result);
        assertEquals("1", result.getTripId());
        assertEquals("100", result.getVehicleId());
    }

    @Test
    @DisplayName("测试获取最新轨迹点 - 从MongoDB获取")
    void testGetLatestTrack_FromMongoDB() {
        // 模拟Redis为空
        when(redisCacheService.lRange(eq("trip:track:1"), eq(0L), eq(-1L))).thenReturn(Collections.emptyList());
        // 模拟MongoDB查询
        when(mongoTemplate.findOne(any(Query.class), eq(TripTrackDocument.class))).thenReturn(testTrackDocument);

        // 执行获取
        TripTrackVO result = tripTrackService.getLatestTrack(1L);

        // 验证
        assertNotNull(result);
        assertEquals("1", result.getTripId());
    }

    @Test
    @DisplayName("测试获取最新轨迹点 - 无数据")
    void testGetLatestTrack_NoData() {
        // 模拟Redis和MongoDB都为空
        when(redisCacheService.lRange(eq("trip:track:1"), eq(0L), eq(-1L))).thenReturn(Collections.emptyList());
        when(mongoTemplate.findOne(any(Query.class), eq(TripTrackDocument.class))).thenReturn(null);

        // 执行获取
        TripTrackVO result = tripTrackService.getLatestTrack(1L);

        // 验证
        assertNull(result);
    }

    @Test
    @DisplayName("测试从Redis获取轨迹点")
    @SuppressWarnings("unchecked")
    void testGetTracksFromRedis() {
        List<TripTrackDTO> expectedTracks = Arrays.asList(testTrackDTO);
        when(redisCacheService.lRange(eq("trip:track:1"), eq(0L), eq(-1L))).thenReturn((List) expectedTracks);

        // 执行获取
        List<TripTrackDTO> result = tripTrackService.getTracksFromRedis(1L);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(redisCacheService).lRange(eq("trip:track:1"), eq(0L), eq(-1L));
    }

    @Test
    @DisplayName("测试批量保存轨迹到MongoDB")
    void testBatchSaveTracks() {
        List<TripTrackDTO> dtoList = Arrays.asList(testTrackDTO);
        when(tripTrackMongoRepository.saveAll(anyList())).thenReturn(Arrays.asList(testTrackDocument));

        // 执行批量保存
        tripTrackService.batchSaveTracks(dtoList);

        // 验证
        verify(tripTrackMongoRepository).saveAll(anyList());
        verify(redisCacheService).delete(eq("trip:track:1"));
    }

    @Test
    @DisplayName("测试批量保存轨迹到MongoDB - 空列表")
    void testBatchSaveTracks_EmptyList() {
        // 执行批量保存
        tripTrackService.batchSaveTracks(Collections.emptyList());

        // 验证没有调用任何存储
        verify(tripTrackMongoRepository, never()).saveAll(anyList());
        verify(redisCacheService, never()).delete(anyString());
    }

    @Test
    @DisplayName("测试删除Redis中的轨迹数据")
    void testDeleteTracksFromRedis() {
        // 执行删除
        tripTrackService.deleteTracksFromRedis(1L);

        // 验证
        verify(redisCacheService).delete(eq("trip:track:1"));
    }
}
