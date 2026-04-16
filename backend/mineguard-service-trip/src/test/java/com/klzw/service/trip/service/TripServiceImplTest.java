package com.klzw.service.trip.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.klzw.common.core.client.DispatchClient;
import com.klzw.common.core.client.UserClient;
import com.klzw.common.core.client.VehicleClient;
import com.klzw.common.core.client.WarningClient;
import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.domain.dto.TripCreateRequest;
import com.klzw.common.core.domain.dto.TripResponse;
import com.klzw.common.core.enums.VehicleStatusEnum;
import com.klzw.common.core.result.PageResult;
import com.klzw.common.core.result.Result;
import com.klzw.service.trip.constant.TripResultCode;
import com.klzw.service.trip.dto.TripDTO;
import com.klzw.service.trip.dto.TripEndDTO;
import com.klzw.service.trip.dto.TripStatisticsResponseDTO;
import com.klzw.service.trip.entity.Trip;
import com.klzw.service.trip.enums.TripStatusEnum;
import com.klzw.service.trip.exception.TripException;
import com.klzw.service.trip.mapper.TripMapper;
import com.klzw.service.trip.processor.TripStatusProcessor;
import com.klzw.service.trip.service.impl.TripServiceImpl;
import com.klzw.service.trip.vo.TripStatisticsVO;
import com.klzw.service.trip.vo.TripVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TripServiceImplTest {

    @InjectMocks
    private TripServiceImpl tripService;

    @Mock
    private TripMapper tripMapper;

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

    private Trip trip;
    private TripDTO tripDTO;

    @BeforeEach
    void setUp() {
        trip = new Trip();
        trip.setId(1L);
        trip.setTripNo("TRIP202301010001");
        trip.setVehicleId(1L);
        trip.setDriverId(1L);
        trip.setStatus(TripStatusEnum.PENDING.getCode());
        trip.setStartLocation("起点");
        trip.setEndLocation("终点");
        trip.setStartLongitude(116.0);
        trip.setStartLatitude(39.0);
        trip.setEndLongitude(116.1);
        trip.setEndLatitude(39.1);

        tripDTO = new TripDTO();
        tripDTO.setVehicleId(1L);
        tripDTO.setDriverId(1L);
        tripDTO.setStartLocation("起点");
        tripDTO.setEndLocation("终点");
        tripDTO.setStartLongitude(116.0);
        tripDTO.setStartLatitude(39.0);
        tripDTO.setEndLongitude(116.1);
        tripDTO.setEndLatitude(39.1);
    }

    @Test
    void page() {
        // 模拟数据
        List<Trip> trips = new ArrayList<>();
        trips.add(trip);

        Page<Trip> page = new Page<>(1, 10);
        page.setRecords(trips);
        page.setTotal(1L);

        when(tripMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(vehicleClient.getById(1L)).thenReturn(Result.success(null));
        when(userClient.getUserById(1L)).thenReturn(Result.success(null));

        // 执行分页查询
        PageRequest pageRequest = new PageRequest();
        pageRequest.setPage(1);
        pageRequest.setSize(10);
        PageResult<TripVO> result = tripService.page(pageRequest, null);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
    }

    @Test
    void getById() {
        // 模拟数据
        when(tripMapper.selectById(1L)).thenReturn(trip);
        when(vehicleClient.getById(1L)).thenReturn(Result.success(null));
        when(userClient.getUserById(1L)).thenReturn(Result.success(null));

        // 执行查询
        TripVO result = tripService.getById(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("TRIP202301010001", result.getTripNo());
    }

    @Test
    void getById_NotFound() {
        // 模拟数据
        when(tripMapper.selectById(1L)).thenReturn(null);

        // 执行查询，预期抛出异常
        TripException exception = assertThrows(TripException.class, () -> tripService.getById(1L));
        assertEquals(TripResultCode.TRIP_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void create() {
        // 模拟数据
        when(vehicleClient.existsById(1L)).thenReturn(Result.success(true));
        com.klzw.common.core.domain.dto.VehicleStatus vehicleStatus = new com.klzw.common.core.domain.dto.VehicleStatus();
        vehicleStatus.setStatus(VehicleStatusEnum.IDLE.getCode());
        when(vehicleClient.getStatus(1L)).thenReturn(Result.success(vehicleStatus));
        when(userClient.existsUser(1L)).thenReturn(Result.success(true));
        when(tripMapper.insert(any(Trip.class))).thenAnswer(invocation -> {
            Trip savedTrip = invocation.getArgument(0);
            savedTrip.setId(1L);
            return 1;
        });
        doNothing().when(tripStatusProcessor).processStatusChange(any(Trip.class), anyInt(), anyInt());

        // 执行创建
        Long result = tripService.create(tripDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result);
    }

    @Test
    void createFromDispatch() {
        // 模拟数据
        TripCreateRequest request = new TripCreateRequest();
        request.setVehicleId(1L);
        request.setDriverId(1L);
        request.setDispatchTaskId(1L);
        request.setStartLocation("起点");
        request.setEndLocation("终点");
        request.setStartLongitude(116.0);
        request.setStartLatitude(39.0);
        request.setEndLongitude(116.1);
        request.setEndLatitude(39.1);

        when(vehicleClient.existsById(1L)).thenReturn(Result.success(true));
        com.klzw.common.core.domain.dto.VehicleStatus vehicleStatus = new com.klzw.common.core.domain.dto.VehicleStatus();
        vehicleStatus.setStatus(VehicleStatusEnum.IDLE.getCode());
        when(vehicleClient.getStatus(1L)).thenReturn(Result.success(vehicleStatus));
        when(userClient.existsUser(1L)).thenReturn(Result.success(true));
        when(tripMapper.insert(any(Trip.class))).thenAnswer(invocation -> {
            Trip savedTrip = invocation.getArgument(0);
            savedTrip.setId(1L);
            return 1;
        });
        doNothing().when(tripStatusProcessor).processStatusChange(any(Trip.class), anyInt(), anyInt());

        // 执行创建
        Long result = tripService.createFromDispatch(request);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result);
    }

    @Test
    void update() {
        // 模拟数据
        when(tripMapper.selectById(1L)).thenReturn(trip);
        when(vehicleClient.existsById(1L)).thenReturn(Result.success(true));
        com.klzw.common.core.domain.dto.VehicleStatus vehicleStatus = new com.klzw.common.core.domain.dto.VehicleStatus();
        vehicleStatus.setStatus(VehicleStatusEnum.IDLE.getCode());
        when(vehicleClient.getStatus(1L)).thenReturn(Result.success(vehicleStatus));
        when(userClient.existsUser(1L)).thenReturn(Result.success(true));
        when(tripMapper.updateById(any(Trip.class))).thenReturn(1);

        // 执行更新
        tripService.update(1L, tripDTO);

        // 验证方法调用
        verify(tripMapper, times(1)).selectById(1L);
        verify(tripMapper, times(1)).updateById(any(Trip.class));
    }

    @Test
    void update_TripNotFound() {
        // 模拟数据
        when(tripMapper.selectById(1L)).thenReturn(null);

        // 执行更新，预期抛出异常
        TripException exception = assertThrows(TripException.class, () -> tripService.update(1L, tripDTO));
        assertEquals(TripResultCode.TRIP_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void update_TripStatusError() {
        // 模拟数据
        trip.setStatus(TripStatusEnum.IN_PROGRESS.getCode());
        when(tripMapper.selectById(1L)).thenReturn(trip);

        // 执行更新，预期抛出异常
        TripException exception = assertThrows(TripException.class, () -> tripService.update(1L, tripDTO));
        assertEquals(TripResultCode.TRIP_STATUS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void delete() {
        // 模拟数据
        when(tripMapper.selectById(1L)).thenReturn(trip);
        when(tripMapper.deleteById(1L)).thenReturn(1);

        // 执行删除
        tripService.delete(1L);

        // 验证方法调用
        verify(tripMapper, times(1)).selectById(1L);
        verify(tripMapper, times(1)).deleteById(1L);
    }

    @Test
    void delete_TripNotFound() {
        // 模拟数据
        when(tripMapper.selectById(1L)).thenReturn(null);

        // 执行删除，预期抛出异常
        TripException exception = assertThrows(TripException.class, () -> tripService.delete(1L));
        assertEquals(TripResultCode.TRIP_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void delete_TripStatusError() {
        // 模拟数据
        trip.setStatus(TripStatusEnum.IN_PROGRESS.getCode());
        when(tripMapper.selectById(1L)).thenReturn(trip);

        // 执行删除，预期抛出异常
        TripException exception = assertThrows(TripException.class, () -> tripService.delete(1L));
        assertEquals(TripResultCode.TRIP_STATUS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void startTrip() {
        // 模拟数据
        when(tripMapper.selectById(1L)).thenReturn(trip);
        when(tripMapper.updateById(any(Trip.class))).thenReturn(1);
        doNothing().when(dispatchClient).startTaskByTrip(anyLong());
        doNothing().when(tripStatusProcessor).processStatusChange(any(Trip.class), anyInt(), anyInt());

        // 执行开始行程
        tripService.startTrip(1L);

        // 验证方法调用
        verify(tripMapper, times(1)).selectById(1L);
        verify(tripMapper, times(1)).updateById(any(Trip.class));
        verify(dispatchClient, times(1)).startTaskByTrip(anyLong());
        verify(tripStatusProcessor, times(1)).processStatusChange(any(Trip.class), anyInt(), anyInt());
    }

    @Test
    void startTrip_TripNotFound() {
        // 模拟数据
        when(tripMapper.selectById(1L)).thenReturn(null);

        // 执行开始行程，预期抛出异常
        TripException exception = assertThrows(TripException.class, () -> tripService.startTrip(1L));
        assertEquals(TripResultCode.TRIP_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void startTrip_TripStatusError() {
        // 模拟数据
        trip.setStatus(TripStatusEnum.COMPLETED.getCode());
        when(tripMapper.selectById(1L)).thenReturn(trip);

        // 执行开始行程，预期抛出异常
        TripException exception = assertThrows(TripException.class, () -> tripService.startTrip(1L));
        assertEquals(TripResultCode.TRIP_STATUS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void endTrip() {
        // 模拟数据
        trip.setStatus(TripStatusEnum.IN_PROGRESS.getCode());
        trip.setActualStartTime(LocalDateTime.now().minusHours(1));
        when(tripMapper.selectById(1L)).thenReturn(trip);
        when(tripMapper.updateById(any(Trip.class))).thenReturn(1);
        when(tripTrackService.getTracksFromRedis(1L)).thenReturn(new ArrayList<>());
        doNothing().when(dispatchClient).completeTaskByTrip(anyLong());
        doNothing().when(tripStatusProcessor).processStatusChange(any(Trip.class), anyInt(), anyInt());

        TripEndDTO dto = new TripEndDTO();
        dto.setEndLongitude(116.1);
        dto.setEndLatitude(39.1);
        tripService.endTrip(1L, dto);

        verify(tripMapper, times(1)).selectById(1L);
        verify(tripMapper, times(1)).updateById(any(Trip.class));
        verify(tripTrackService, times(1)).getTracksFromRedis(1L);
        verify(dispatchClient, times(1)).completeTaskByTrip(anyLong());
        verify(tripStatusProcessor, times(1)).processStatusChange(any(Trip.class), anyInt(), anyInt());
    }

    @Test
    void endTrip_TripNotFound() {
        when(tripMapper.selectById(1L)).thenReturn(null);

        TripEndDTO dto = new TripEndDTO();
        dto.setEndLongitude(116.1);
        dto.setEndLatitude(39.1);
        TripException exception = assertThrows(TripException.class, () -> tripService.endTrip(1L, dto));
        assertEquals(TripResultCode.TRIP_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void endTrip_TripStatusError() {
        trip.setStatus(TripStatusEnum.PENDING.getCode());
        when(tripMapper.selectById(1L)).thenReturn(trip);

        TripEndDTO dto = new TripEndDTO();
        dto.setEndLongitude(116.1);
        dto.setEndLatitude(39.1);
        TripException exception = assertThrows(TripException.class, () -> tripService.endTrip(1L, dto));
        assertEquals(TripResultCode.TRIP_STATUS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void getByVehicleId() {
        // 模拟数据
        List<Trip> trips = new ArrayList<>();
        trips.add(trip);
        when(tripMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(trips);
        when(vehicleClient.getById(1L)).thenReturn(Result.success(null));
        when(userClient.getUserById(1L)).thenReturn(Result.success(null));

        // 执行查询
        List<TripVO> result = tripService.getByVehicleId(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getByDriverId() {
        // 模拟数据
        List<Trip> trips = new ArrayList<>();
        trips.add(trip);
        when(tripMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(trips);
        when(vehicleClient.getById(1L)).thenReturn(Result.success(null));
        when(userClient.getUserById(1L)).thenReturn(Result.success(null));

        // 执行查询
        List<TripVO> result = tripService.getByDriverId(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getByTripNo() {
        // 模拟数据
        when(tripMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(trip);
        when(vehicleClient.getById(1L)).thenReturn(Result.success(null));
        when(userClient.getUserById(1L)).thenReturn(Result.success(null));

        // 执行查询
        TripVO result = tripService.getByTripNo("TRIP202301010001");

        // 验证结果
        assertNotNull(result);
        assertEquals("TRIP202301010001", result.getTripNo());
    }

    @Test
    void getByTripNo_NotFound() {
        // 模拟数据
        when(tripMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // 执行查询，预期抛出异常
        TripException exception = assertThrows(TripException.class, () -> tripService.getByTripNo("TRIP202301010001"));
        assertEquals(TripResultCode.TRIP_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void acceptTrip() {
        // 模拟数据
        when(tripMapper.selectById(1L)).thenReturn(trip);
        when(tripMapper.updateById(any(Trip.class))).thenReturn(1);
        doNothing().when(tripStatusProcessor).processStatusChange(any(Trip.class), anyInt(), anyInt());

        // 执行接单
        tripService.acceptTrip(1L);

        // 验证方法调用
        verify(tripMapper, times(1)).selectById(1L);
        verify(tripMapper, times(1)).updateById(any(Trip.class));
        verify(tripStatusProcessor, times(1)).processStatusChange(any(Trip.class), anyInt(), anyInt());
    }

    @Test
    void acceptTrip_TripNotFound() {
        // 模拟数据
        when(tripMapper.selectById(1L)).thenReturn(null);

        // 执行接单，预期抛出异常
        TripException exception = assertThrows(TripException.class, () -> tripService.acceptTrip(1L));
        assertEquals(TripResultCode.TRIP_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void acceptTrip_TripStatusError() {
        // 模拟数据
        trip.setStatus(TripStatusEnum.IN_PROGRESS.getCode());
        when(tripMapper.selectById(1L)).thenReturn(trip);

        // 执行接单，预期抛出异常
        TripException exception = assertThrows(TripException.class, () -> tripService.acceptTrip(1L));
        assertEquals(TripResultCode.TRIP_STATUS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void getLatestTripByVehicleId() {
        // 模拟数据
        when(tripMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(trip);

        // 执行查询
        TripResponse result = tripService.getLatestTripByVehicleId(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getVehicleId());
        assertEquals(1L, result.getDriverId());
        assertEquals("TRIP202301010001", result.getTripNo());
    }

    @Test
    void pauseTrip() {
        // 模拟数据
        trip.setStatus(TripStatusEnum.IN_PROGRESS.getCode());
        when(tripMapper.selectById(1L)).thenReturn(trip);
        when(tripMapper.updateById(any(Trip.class))).thenReturn(1);
        doNothing().when(tripStatusProcessor).processStatusChange(any(Trip.class), anyInt(), anyInt());

        // 执行暂停
        tripService.pauseTrip(1L);

        // 验证方法调用
        verify(tripMapper, times(1)).selectById(1L);
        verify(tripMapper, times(1)).updateById(any(Trip.class));
        verify(tripStatusProcessor, times(1)).processStatusChange(any(Trip.class), anyInt(), anyInt());
    }

    @Test
    void pauseTrip_TripNotFound() {
        // 模拟数据
        when(tripMapper.selectById(1L)).thenReturn(null);

        // 执行暂停，预期抛出异常
        TripException exception = assertThrows(TripException.class, () -> tripService.pauseTrip(1L));
        assertEquals(TripResultCode.TRIP_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void pauseTrip_TripStatusError() {
        // 模拟数据
        trip.setStatus(TripStatusEnum.PENDING.getCode());
        when(tripMapper.selectById(1L)).thenReturn(trip);

        // 执行暂停，预期抛出异常
        TripException exception = assertThrows(TripException.class, () -> tripService.pauseTrip(1L));
        assertEquals(TripResultCode.TRIP_STATUS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void resumeTrip() {
        // 模拟数据
        trip.setStatus(TripStatusEnum.PAUSED.getCode());
        when(tripMapper.selectById(1L)).thenReturn(trip);
        when(tripMapper.updateById(any(Trip.class))).thenReturn(1);
        doNothing().when(tripStatusProcessor).processStatusChange(any(Trip.class), anyInt(), anyInt());

        // 执行恢复
        tripService.resumeTrip(1L);

        // 验证方法调用
        verify(tripMapper, times(1)).selectById(1L);
        verify(tripMapper, times(1)).updateById(any(Trip.class));
        verify(tripStatusProcessor, times(1)).processStatusChange(any(Trip.class), anyInt(), anyInt());
    }

    @Test
    void resumeTrip_TripNotFound() {
        // 模拟数据
        when(tripMapper.selectById(1L)).thenReturn(null);

        // 执行恢复，预期抛出异常
        TripException exception = assertThrows(TripException.class, () -> tripService.resumeTrip(1L));
        assertEquals(TripResultCode.TRIP_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void resumeTrip_TripStatusError() {
        // 模拟数据
        trip.setStatus(TripStatusEnum.IN_PROGRESS.getCode());
        when(tripMapper.selectById(1L)).thenReturn(trip);

        // 执行恢复，预期抛出异常
        TripException exception = assertThrows(TripException.class, () -> tripService.resumeTrip(1L));
        assertEquals(TripResultCode.TRIP_STATUS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void getTripStatistics() {
        // 模拟数据
        trip.setActualStartTime(LocalDateTime.now().minusHours(1));
        trip.setActualEndTime(LocalDateTime.now());
        trip.setEstimatedMileage(new BigDecimal(10));
        when(tripMapper.selectById(1L)).thenReturn(trip);

        // 执行查询
        TripStatisticsVO result = tripService.getTripStatistics(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getTripId());
        assertNotNull(result.getDurationMinutes());
        assertEquals(new BigDecimal(10), result.getEstimatedDistance());
    }

    @Test
    void getStatisticsByDateRange() {
        // 模拟数据
        List<Trip> trips = new ArrayList<>();
        trip.setActualStartTime(LocalDateTime.now().minusDays(1));
        trip.setActualEndTime(LocalDateTime.now().minusDays(1).plusHours(1));
        trip.setEstimatedMileage(new BigDecimal(10));
        trip.setStatus(TripStatusEnum.COMPLETED.getCode());
        trips.add(trip);

        when(tripMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(trips);

        // 执行查询
        TripStatisticsResponseDTO result = tripService.getStatisticsByDateRange(LocalDate.now().minusDays(2).toString(), LocalDate.now().toString());

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTripCount());
        assertEquals(1, result.getCompletedTripCount());
        assertEquals(new BigDecimal(10), result.getTotalDistance());
    }

    @Test
    void getTracksByTripId() {
        // 模拟数据
        List<com.klzw.service.trip.vo.TripTrackVO> tracks = new ArrayList<>();
        when(tripTrackService.getByTripId(1L)).thenReturn(tracks);

        // 执行查询
        List<com.klzw.service.trip.vo.TripTrackVO> result = tripService.getTracksByTripId(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void getTripWarningRecords() {
        // 模拟数据
        List<Map<String, Object>> records = new ArrayList<>();
        Result<List<Map<String, Object>>> result = Result.success(new ArrayList<>());
        result.setCode(200);
        result.setData(records);
        when(warningClient.getRecordsByTripId(1L)).thenReturn(result);

        // 执行查询
        List<Map<String, Object>> resultRecords = tripService.getTripWarningRecords(1L);

        // 验证结果
        assertNotNull(resultRecords);
        assertEquals(0, resultRecords.size());
    }
}
