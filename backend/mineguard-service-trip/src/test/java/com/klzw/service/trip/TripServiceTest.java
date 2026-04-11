package com.klzw.service.trip;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.klzw.common.core.client.DispatchClient;
import com.klzw.common.core.client.UserClient;
import com.klzw.common.core.client.VehicleClient;
import com.klzw.common.core.client.WarningClient;
import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.domain.dto.VehicleStatus;
import com.klzw.common.core.result.Result;
import com.klzw.service.trip.dto.TripDTO;
import com.klzw.service.trip.entity.Trip;
import com.klzw.service.trip.enums.TripStatusEnum;
import com.klzw.service.trip.exception.TripException;
import com.klzw.service.trip.mapper.TripMapper;
import com.klzw.service.trip.processor.TripStatusProcessor;
import com.klzw.service.trip.service.TripTrackService;
import com.klzw.service.trip.service.impl.TripServiceImpl;
import com.klzw.service.trip.vo.TripStatisticsVO;
import com.klzw.service.trip.vo.TripVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TripService单元测试类
 * 测试覆盖率目标: 70%
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TripService单元测试")
public class TripServiceTest {

    @Mock
    private UserClient userClient;

    @Mock
    private VehicleClient vehicleClient;

    @Mock
    private DispatchClient dispatchClient;

    @Mock
    private WarningClient warningClient;

    @Mock
    private TripStatusProcessor tripStatusProcessor;

    @Mock
    private TripTrackService tripTrackService;

    @Mock
    private TripMapper tripMapper;

    @InjectMocks
    private TripServiceImpl tripService;

    private Trip testTrip;
    private TripDTO testTripDTO;

    @BeforeEach
    void setUp() {
        // 通过反射设置 baseMapper，因为 ServiceImpl 需要它
        ReflectionTestUtils.setField(tripService, "baseMapper", tripMapper);

        // 初始化测试行程数据
        testTrip = new Trip();
        testTrip.setId(1L);
        testTrip.setTripNo("TRIP202604091000001");
        testTrip.setVehicleId(100L);
        testTrip.setDriverId(200L);
        testTrip.setStartLocation("起点");
        testTrip.setEndLocation("终点");
        testTrip.setStartLongitude(116.397428);
        testTrip.setStartLatitude(39.90923);
        testTrip.setEndLongitude(116.487428);
        testTrip.setEndLatitude(39.91923);
        testTrip.setStatus(TripStatusEnum.PENDING.getCode());
        testTrip.setEstimatedMileage(BigDecimal.valueOf(10.5));
        testTrip.setEstimatedDuration(30);

        // 初始化测试DTO
        testTripDTO = new TripDTO();
        testTripDTO.setVehicleId(100L);
        testTripDTO.setDriverId(200L);
        testTripDTO.setStartLocation("起点");
        testTripDTO.setEndLocation("终点");
        testTripDTO.setStartLongitude(116.397428);
        testTripDTO.setStartLatitude(39.90923);
        testTripDTO.setEndLongitude(116.487428);
        testTripDTO.setEndLatitude(39.91923);
        testTripDTO.setEstimatedStartTime(LocalDateTime.now());
        testTripDTO.setEstimatedEndTime(LocalDateTime.now().plusHours(1));
    }

    private com.klzw.common.core.domain.PageRequest createPageRequest(int page, int size) {
        com.klzw.common.core.domain.PageRequest request = new com.klzw.common.core.domain.PageRequest();
        request.setPage(page);
        request.setSize(size);
        return request;
    }

    @Test
    @DisplayName("测试创建行程")
    void testCreateTrip() {
        // 模拟车辆验证
        VehicleStatus vehicleStatus = new VehicleStatus();
        vehicleStatus.setStatus(0); // IDLE状态
        when(vehicleClient.existsById(100L)).thenReturn(Result.success(true));
        when(vehicleClient.getStatus(100L)).thenReturn(Result.success(vehicleStatus));
        when(userClient.existsUser(200L)).thenReturn(Result.success(true));

        // 模拟数据库插入
        when(tripMapper.insert(any(Trip.class))).thenAnswer(invocation -> {
            Trip trip = invocation.getArgument(0);
            trip.setId(1L);
            return 1;
        });

        // 执行创建
        Long tripId = tripService.create(testTripDTO);

        // 验证
        assertNotNull(tripId);
        assertEquals(1L, tripId);
        verify(tripMapper).insert(any(Trip.class));
        verify(tripStatusProcessor).processStatusChange(any(Trip.class), eq(-1), eq(TripStatusEnum.PENDING.getCode()));
    }

    @Test
    @DisplayName("测试开始行程")
    void testStartTrip() {
        // 模拟行程存在且状态为待开始
        testTrip.setStatus(TripStatusEnum.PENDING.getCode());
        when(tripMapper.selectById(1L)).thenReturn(testTrip);

        // 执行开始
        tripService.startTrip(1L);

        // 验证状态已更新
        assertEquals(TripStatusEnum.IN_PROGRESS.getCode(), testTrip.getStatus());
        assertNotNull(testTrip.getActualStartTime());
        verify(tripMapper).updateById(any(Trip.class));
        verify(tripStatusProcessor).processStatusChange(any(Trip.class), eq(TripStatusEnum.PENDING.getCode()), eq(TripStatusEnum.IN_PROGRESS.getCode()));
    }

    @Test
    @DisplayName("测试开始行程 - 行程不存在")
    void testStartTrip_NotFound() {
        when(tripMapper.selectById(1L)).thenReturn(null);

        assertThrows(TripException.class, () -> tripService.startTrip(1L));
    }

    @Test
    @DisplayName("测试开始行程 - 状态错误")
    void testStartTrip_StatusError() {
        testTrip.setStatus(TripStatusEnum.IN_PROGRESS.getCode());
        when(tripMapper.selectById(1L)).thenReturn(testTrip);

        assertThrows(TripException.class, () -> tripService.startTrip(1L));
    }

    @Test
    @DisplayName("测试结束行程")
    void testEndTrip() {
        testTrip.setStatus(TripStatusEnum.IN_PROGRESS.getCode());
        testTrip.setStartLongitude(116.397428);
        testTrip.setStartLatitude(39.90923);
        when(tripMapper.selectById(1L)).thenReturn(testTrip);
        when(tripTrackService.getTracksFromRedis(1L)).thenReturn(Collections.emptyList());

        // 执行结束
        tripService.endTrip(1L, 116.487428, 39.91923);

        // 验证状态已更新
        assertEquals(TripStatusEnum.COMPLETED.getCode(), testTrip.getStatus());
        assertNotNull(testTrip.getActualEndTime());
        assertEquals(116.487428, testTrip.getEndLongitude());
        assertEquals(39.91923, testTrip.getEndLatitude());
        verify(tripMapper).updateById(any(Trip.class));
        verify(tripStatusProcessor).processStatusChange(any(Trip.class), eq(TripStatusEnum.IN_PROGRESS.getCode()), eq(TripStatusEnum.COMPLETED.getCode()));
    }

    @Test
    @DisplayName("测试结束行程 - 行程不存在")
    void testEndTrip_NotFound() {
        when(tripMapper.selectById(1L)).thenReturn(null);

        assertThrows(TripException.class, () -> tripService.endTrip(1L, 116.487428, 39.91923));
    }

    @Test
    @DisplayName("测试结束行程 - 状态错误")
    void testEndTrip_StatusError() {
        testTrip.setStatus(TripStatusEnum.PENDING.getCode());
        when(tripMapper.selectById(1L)).thenReturn(testTrip);

        assertThrows(TripException.class, () -> tripService.endTrip(1L, 116.487428, 39.91923));
    }

    @Test
    @DisplayName("测试暂停行程")
    void testPauseTrip() {
        testTrip.setStatus(TripStatusEnum.IN_PROGRESS.getCode());
        when(tripMapper.selectById(1L)).thenReturn(testTrip);

        // 执行暂停
        tripService.pauseTrip(1L);

        // 验证状态已更新
        assertEquals(TripStatusEnum.PAUSED.getCode(), testTrip.getStatus());
        verify(tripMapper).updateById(any(Trip.class));
        verify(tripStatusProcessor).processStatusChange(any(Trip.class), eq(TripStatusEnum.IN_PROGRESS.getCode()), eq(TripStatusEnum.PAUSED.getCode()));
    }

    @Test
    @DisplayName("测试暂停行程 - 行程不存在")
    void testPauseTrip_NotFound() {
        when(tripMapper.selectById(1L)).thenReturn(null);

        assertThrows(TripException.class, () -> tripService.pauseTrip(1L));
    }

    @Test
    @DisplayName("测试暂停行程 - 状态错误")
    void testPauseTrip_StatusError() {
        testTrip.setStatus(TripStatusEnum.PENDING.getCode());
        when(tripMapper.selectById(1L)).thenReturn(testTrip);

        assertThrows(TripException.class, () -> tripService.pauseTrip(1L));
    }

    @Test
    @DisplayName("测试恢复行程")
    void testResumeTrip() {
        testTrip.setStatus(TripStatusEnum.PAUSED.getCode());
        when(tripMapper.selectById(1L)).thenReturn(testTrip);

        // 执行恢复
        tripService.resumeTrip(1L);

        // 验证状态已更新
        assertEquals(TripStatusEnum.IN_PROGRESS.getCode(), testTrip.getStatus());
        verify(tripMapper).updateById(any(Trip.class));
        verify(tripStatusProcessor).processStatusChange(any(Trip.class), eq(TripStatusEnum.PAUSED.getCode()), eq(TripStatusEnum.IN_PROGRESS.getCode()));
    }

    @Test
    @DisplayName("测试恢复行程 - 行程不存在")
    void testResumeTrip_NotFound() {
        when(tripMapper.selectById(1L)).thenReturn(null);

        assertThrows(TripException.class, () -> tripService.resumeTrip(1L));
    }

    @Test
    @DisplayName("测试恢复行程 - 状态错误")
    void testResumeTrip_StatusError() {
        testTrip.setStatus(TripStatusEnum.PENDING.getCode());
        when(tripMapper.selectById(1L)).thenReturn(testTrip);

        assertThrows(TripException.class, () -> tripService.resumeTrip(1L));
    }

    @Test
    @DisplayName("测试根据车辆ID获取行程")
    void testGetByVehicleId() {
        List<Trip> trips = List.of(testTrip);
        when(tripMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(trips);
        when(vehicleClient.getById(anyLong())).thenReturn(Result.success(new com.klzw.common.core.domain.dto.VehicleInfo()));
        when(userClient.getUserById(anyLong())).thenReturn(Result.success(Collections.singletonMap("realName", "司机")));

        List<TripVO> result = tripService.getByVehicleId(100L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(tripMapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试根据车辆ID获取行程 - 无数据")
    void testGetByVehicleId_NoData() {
        when(tripMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        List<TripVO> result = tripService.getByVehicleId(100L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试根据司机ID获取行程")
    void testGetByDriverId() {
        List<Trip> trips = List.of(testTrip);
        when(tripMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(trips);
        when(vehicleClient.getById(anyLong())).thenReturn(Result.success(new com.klzw.common.core.domain.dto.VehicleInfo()));
        when(userClient.getUserById(anyLong())).thenReturn(Result.success(Collections.singletonMap("realName", "司机")));

        List<TripVO> result = tripService.getByDriverId(200L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(tripMapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试根据司机ID获取行程 - 无数据")
    void testGetByDriverId_NoData() {
        when(tripMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        List<TripVO> result = tripService.getByDriverId(200L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试获取行程统计")
    void testGetTripStatistics() {
        testTrip.setActualStartTime(LocalDateTime.now().minusHours(1));
        testTrip.setActualEndTime(LocalDateTime.now());
        testTrip.setEstimatedMileage(BigDecimal.valueOf(10.5));
        when(tripMapper.selectById(1L)).thenReturn(testTrip);

        TripStatisticsVO result = tripService.getTripStatistics(1L);

        assertNotNull(result);
        assertEquals(1L, result.getTripId());
        assertNotNull(result.getDurationMinutes());
        assertNotNull(result.getEstimatedDistance());
    }

    @Test
    @DisplayName("测试获取行程统计 - 行程不存在")
    void testGetTripStatistics_NotFound() {
        when(tripMapper.selectById(1L)).thenReturn(null);

        assertThrows(TripException.class, () -> tripService.getTripStatistics(1L));
    }

    @Test
    @DisplayName("测试获取行程统计 - 无实际时间数据")
    void testGetTripStatistics_NoTimeData() {
        testTrip.setActualStartTime(null);
        testTrip.setActualEndTime(null);
        testTrip.setEstimatedMileage(BigDecimal.valueOf(10.5));
        when(tripMapper.selectById(1L)).thenReturn(testTrip);

        TripStatisticsVO result = tripService.getTripStatistics(1L);

        assertNotNull(result);
        assertEquals(1L, result.getTripId());
        assertNull(result.getDurationMinutes());
    }

    @Test
    @DisplayName("测试分页查询行程")
    void testPage() {
        Page<Trip> page = new Page<>(1, 10);
        page.setRecords(List.of(testTrip));
        page.setTotal(1);
        when(tripMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(vehicleClient.getById(anyLong())).thenReturn(Result.success(new com.klzw.common.core.domain.dto.VehicleInfo()));
        when(userClient.getUserById(anyLong())).thenReturn(Result.success(Collections.singletonMap("realName", "司机")));

        com.klzw.common.core.result.PageResult<TripVO> result = tripService.page(createPageRequest(1, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
    }
}
