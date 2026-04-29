package com.klzw.service.trip;

import com.klzw.common.core.config.DotenvInitializer;
import com.klzw.common.core.result.Result;
import com.klzw.service.trip.dto.TripDTO;
import com.klzw.service.trip.dto.TripEndDTO;
import com.klzw.service.trip.entity.Trip;
import com.klzw.service.trip.enums.TripStatusEnum;
import com.klzw.service.trip.service.TripService;
import com.klzw.service.trip.vo.TripVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import com.klzw.common.core.client.VehicleClient;
import com.klzw.common.core.client.UserClient;
import com.klzw.common.core.client.DispatchClient;
import com.klzw.common.core.client.WarningClient;
import com.klzw.common.core.client.MessageClient;
import com.klzw.common.core.domain.dto.VehicleStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(initializers = DotenvInitializer.class)
@ActiveProfiles("test")
@Transactional
public class TripIntegrationTest {

    @Autowired
    private TripService tripService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private VehicleClient vehicleClient;

    @MockBean
    private UserClient userClient;

    @MockBean
    private DispatchClient dispatchClient;

    @MockBean
    private WarningClient warningClient;

    @MockBean
    private MessageClient messageClient;

    private TripDTO tripDTO;

    @BeforeEach
    void setUp() {
        // 使用truncate table清理测试数据，避免数据饱满
        jdbcTemplate.execute("TRUNCATE TABLE trip");
        
        // 模拟外部依赖
        Mockito.when(vehicleClient.existsById(Mockito.anyLong())).thenReturn(Result.success(true));
        VehicleStatus vehicleStatus = new VehicleStatus();
        vehicleStatus.setStatus(0); // IDLE状态
        Mockito.when(vehicleClient.getStatus(Mockito.anyLong())).thenReturn(Result.success(vehicleStatus));
        // 模拟vehicleClient.getById
        Map<String, Object> vehicleMap = new HashMap<>();
        vehicleMap.put("id", 1L);
        vehicleMap.put("vehicleNo", "TEST-VEHICLE-001");
        vehicleMap.put("status", 0);
        Mockito.when(vehicleClient.getById(Mockito.anyLong())).thenReturn(Result.success(vehicleMap));
        // 模拟vehicleClient.updateStatus
        Mockito.when(vehicleClient.updateStatus(Mockito.anyLong(), Mockito.any(VehicleStatus.class))).thenReturn(Result.success(vehicleStatus));
        // 模拟userClient.existsUser
        Mockito.when(userClient.existsUser(Mockito.anyLong())).thenReturn(Result.success(true));
        // 模拟userClient.getUserById
        Mockito.when(userClient.getUserById(Mockito.anyLong())).thenReturn(Result.success(java.util.Map.of("realName", "测试司机")));
        // 模拟dispatchClient.startTaskByTrip
        Mockito.when(dispatchClient.startTaskByTrip(Mockito.anyLong())).thenReturn(Result.success());
        // 模拟dispatchClient.completeTaskByTrip
        Mockito.when(dispatchClient.completeTaskByTrip(Mockito.anyLong())).thenReturn(Result.success());
        // 模拟warningClient.getRecordsByTripId
        Mockito.when(warningClient.getRecordsByTripId(Mockito.anyLong())).thenReturn(Result.success(java.util.List.of()));
        // 模拟messageClient.sendMessage
        Mockito.doNothing().when(messageClient).sendMessage(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        
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
    void testCreateAndGetTrip() {
        // 创建行程
        Long tripId = tripService.create(tripDTO);
        assertNotNull(tripId);

        // 获取行程
        TripVO tripVO = tripService.getById(tripId);
        assertNotNull(tripVO);
        assertEquals(tripId.toString(), tripVO.getId());
        assertEquals("起点", tripVO.getStartLocation());
        assertEquals("终点", tripVO.getEndLocation());
        assertEquals(TripStatusEnum.PENDING.getCode(), tripVO.getStatus());
    }

    @Test
    void testUpdateTrip() {
        // 创建行程
        Long tripId = tripService.create(tripDTO);
        assertNotNull(tripId);

        // 更新行程
        tripDTO.setStartLocation("新起点");
        tripDTO.setEndLocation("新终点");
        tripService.update(tripId, tripDTO);

        // 验证更新后的数据
        TripVO tripVO = tripService.getById(tripId);
        assertNotNull(tripVO);
        assertEquals("新起点", tripVO.getStartLocation());
        assertEquals("新终点", tripVO.getEndLocation());
    }

    @Test
    void testStartAndEndTrip() {
        // 创建行程
        Long tripId = tripService.create(tripDTO);
        assertNotNull(tripId);

        // 开始行程
        tripService.startTrip(tripId);
        TripVO startedTrip = tripService.getById(tripId);
        assertEquals(TripStatusEnum.IN_PROGRESS.getCode(), startedTrip.getStatus());
        assertNotNull(startedTrip.getActualStartTime());

        // 结束行程
        TripEndDTO endDto = new TripEndDTO();
        endDto.setEndLongitude(116.1);
        endDto.setEndLatitude(39.1);
        tripService.endTrip(tripId, endDto);
        TripVO endedTrip = tripService.getById(tripId);
        assertEquals(TripStatusEnum.COMPLETED.getCode(), endedTrip.getStatus());
        assertNotNull(endedTrip.getActualEndTime());
        assertNotNull(endedTrip.getActualMileage());
        assertNotNull(endedTrip.getActualDuration());
    }

    @Test
    void testAcceptTrip() {
        // 创建行程
        Long tripId = tripService.create(tripDTO);
        assertNotNull(tripId);

        // 接单
        tripService.acceptTrip(tripId);
        TripVO acceptedTrip = tripService.getById(tripId);
        assertEquals(TripStatusEnum.ACCEPTED.getCode(), acceptedTrip.getStatus());
    }

    @Test
    void testPauseAndResumeTrip() {
        // 创建行程
        Long tripId = tripService.create(tripDTO);
        assertNotNull(tripId);

        // 开始行程
        tripService.startTrip(tripId);
        TripVO startedTrip = tripService.getById(tripId);
        assertEquals(TripStatusEnum.IN_PROGRESS.getCode(), startedTrip.getStatus());

        // 暂停行程
        tripService.pauseTrip(tripId);
        TripVO pausedTrip = tripService.getById(tripId);
        assertEquals(TripStatusEnum.PAUSED.getCode(), pausedTrip.getStatus());

        // 恢复行程
        tripService.resumeTrip(tripId);
        TripVO resumedTrip = tripService.getById(tripId);
        assertEquals(TripStatusEnum.IN_PROGRESS.getCode(), resumedTrip.getStatus());
    }

    @Test
    void testDeleteTrip() {
        // 创建行程
        Long tripId = tripService.create(tripDTO);
        assertNotNull(tripId);

        // 验证行程存在
        TripVO tripVO = tripService.getById(tripId);
        assertNotNull(tripVO);

        // 删除行程
        tripService.delete(tripId);

        // 验证行程不存在
        try {
            tripService.getById(tripId);
            fail("行程应该不存在");
        } catch (Exception e) {
            // 预期异常
        }
    }

    @Test
    void testGetByVehicleId() {
        // 创建行程
        Long tripId = tripService.create(tripDTO);
        assertNotNull(tripId);

        // 根据车辆ID查询行程
        List<TripVO> trips = tripService.getByVehicleId(1L);
        assertNotNull(trips);
        assertTrue(trips.size() >= 1);
        boolean found = false;
        for (TripVO trip : trips) {
            if (trip.getId().equals(tripId.toString())) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    void testGetByDriverId() {
        // 创建行程
        Long tripId = tripService.create(tripDTO);
        assertNotNull(tripId);

        // 根据司机ID查询行程
        List<TripVO> trips = tripService.getByDriverId(1L);
        assertNotNull(trips);
        assertTrue(trips.size() >= 1);
        boolean found = false;
        for (TripVO trip : trips) {
            if (trip.getId().equals(tripId.toString())) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    void testGetByTripNo() {
        // 创建行程
        Long tripId = tripService.create(tripDTO);
        assertNotNull(tripId);

        // 获取行程编号
        TripVO tripVO = tripService.getById(tripId);
        String tripNo = tripVO.getTripNo();
        assertNotNull(tripNo);

        // 根据行程编号查询行程
        TripVO tripByNo = tripService.getByTripNo(tripNo);
        assertNotNull(tripByNo);
        assertEquals(tripNo, tripByNo.getTripNo());
    }

    @Test
    void testGetLatestTripByVehicleId() {
        // 创建多个行程
        for (int i = 0; i < 3; i++) {
            TripDTO newTripDTO = new TripDTO();
            newTripDTO.setVehicleId(1L);
            newTripDTO.setDriverId(1L);
            newTripDTO.setStartLocation("起点" + i);
            newTripDTO.setEndLocation("终点" + i);
            newTripDTO.setStartLongitude(116.0 + i * 0.1);
            newTripDTO.setStartLatitude(39.0 + i * 0.1);
            newTripDTO.setEndLongitude(116.1 + i * 0.1);
            newTripDTO.setEndLatitude(39.1 + i * 0.1);
            tripService.create(newTripDTO);
        }

        // 获取最新行程
        var latestTrip = tripService.getLatestTripByVehicleId(1L);
        assertNotNull(latestTrip);
        assertNotNull(latestTrip.getId());
        assertEquals(1L, latestTrip.getVehicleId());
        assertEquals(1L, latestTrip.getDriverId());
    }

    @Test
    void testGetTripStatistics() {
        // 创建行程
        Long tripId = tripService.create(tripDTO);
        assertNotNull(tripId);

        // 开始行程
        tripService.startTrip(tripId);

        // 结束行程
        TripEndDTO endDto = new TripEndDTO();
        endDto.setEndLongitude(116.1);
        endDto.setEndLatitude(39.1);
        tripService.endTrip(tripId, endDto);

        // 获取行程统计
        var statistics = tripService.getTripStatistics(tripId);
        assertNotNull(statistics);
        assertEquals(tripId, statistics.getTripId());
        assertNotNull(statistics.getDurationMinutes());
        assertNotNull(statistics.getEstimatedDistance());
        assertNotNull(statistics.getActualDistance());
    }

    @Test
    void testGetStatisticsByDateRange() {
        // 创建行程
        Long tripId = tripService.create(tripDTO);
        assertNotNull(tripId);

        // 开始行程
        tripService.startTrip(tripId);

        // 结束行程
        TripEndDTO endDto = new TripEndDTO();
        endDto.setEndLongitude(116.1);
        endDto.setEndLatitude(39.1);
        tripService.endTrip(tripId, endDto);

        // 获取日期范围内的统计
        String startDate = LocalDateTime.now().minusDays(1).toLocalDate().toString();
        String endDate = LocalDateTime.now().plusDays(1).toLocalDate().toString();
        var statistics = tripService.getStatisticsByDateRange(startDate, endDate);
        assertNotNull(statistics);
        assertTrue(statistics.getTripCount() >= 1);
        assertTrue(statistics.getCompletedTripCount() >= 1);
        assertNotNull(statistics.getTotalDistance());
    }
}
