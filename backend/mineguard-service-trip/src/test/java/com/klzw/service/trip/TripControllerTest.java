package com.klzw.service.trip;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.klzw.common.core.domain.PageRequest;
import com.klzw.common.core.domain.dto.TripResponse;
import com.klzw.common.core.result.PageResult;
import com.klzw.common.core.result.Result;
import com.klzw.service.trip.controller.TripController;
import com.klzw.service.trip.dto.TripDTO;
import com.klzw.service.trip.dto.TripStatisticsResponseDTO;
import com.klzw.service.trip.service.TripService;
import com.klzw.service.trip.vo.TripStatisticsVO;
import com.klzw.service.trip.vo.TripTrackVO;
import com.klzw.service.trip.vo.TripVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TripController切片测试类
 * 测试覆盖率目标: 20%
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TripController切片测试")
public class TripControllerTest {

    @Mock
    private TripService tripService;

    @InjectMocks
    private TripController tripController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private TripVO testTripVO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tripController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // 初始化测试行程VO
        testTripVO = new TripVO();
        testTripVO.setId("1");
        testTripVO.setTripNo("TRIP202604091000001");
        testTripVO.setVehicleId("100");
        testTripVO.setDriverId("200");
        testTripVO.setStartLocation("起点");
        testTripVO.setEndLocation("终点");
        testTripVO.setStatus(0);
    }

    @Test
    @DisplayName("测试创建行程接口")
    void testCreate() throws Exception {
        TripDTO tripDTO = new TripDTO();
        tripDTO.setVehicleId(100L);
        tripDTO.setDriverId(200L);
        tripDTO.setStartLocation("起点");
        tripDTO.setEndLocation("终点");
        tripDTO.setStartLongitude(116.397428);
        tripDTO.setStartLatitude(39.90923);
        tripDTO.setEndLongitude(116.487428);
        tripDTO.setEndLatitude(39.91923);
        tripDTO.setEstimatedStartTime(LocalDateTime.now());
        tripDTO.setEstimatedEndTime(LocalDateTime.now().plusHours(1));

        when(tripService.create(any(TripDTO.class))).thenReturn(1L);

        mockMvc.perform(post("/trip")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tripDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1));

        verify(tripService).create(any(TripDTO.class));
    }

    @Test
    @DisplayName("测试获取行程详情接口")
    void testGetById() throws Exception {
        when(tripService.getById(1L)).thenReturn(testTripVO);

        mockMvc.perform(get("/trip/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.tripNo").value("TRIP202604091000001"));

        verify(tripService).getById(1L);
    }

    @Test
    @DisplayName("测试分页查询行程接口")
    void testPage() throws Exception {
        PageResult<TripVO> pageResult = PageResult.of(1, 1, 10, Collections.singletonList(testTripVO));
        when(tripService.page(any(PageRequest.class))).thenReturn(pageResult);

        mockMvc.perform(get("/trip/page")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1));

        verify(tripService).page(any(PageRequest.class));
    }

    @Test
    @DisplayName("测试开始行程接口")
    void testStartTrip() throws Exception {
        doNothing().when(tripService).startTrip(1L);

        mockMvc.perform(post("/trip/1/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(tripService).startTrip(1L);
    }

    @Test
    @DisplayName("测试结束行程接口")
    void testEndTrip() throws Exception {
        doNothing().when(tripService).endTrip(1L, 116.487428, 39.91923);

        mockMvc.perform(post("/trip/1/end")
                        .param("endLongitude", "116.487428")
                        .param("endLatitude", "39.91923"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(tripService).endTrip(1L, 116.487428, 39.91923);
    }

    @Test
    @DisplayName("测试暂停行程接口")
    void testPauseTrip() throws Exception {
        doNothing().when(tripService).pauseTrip(1L);

        mockMvc.perform(post("/trip/1/pause"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(tripService).pauseTrip(1L);
    }

    @Test
    @DisplayName("测试恢复行程接口")
    void testResumeTrip() throws Exception {
        doNothing().when(tripService).resumeTrip(1L);

        mockMvc.perform(post("/trip/1/resume"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(tripService).resumeTrip(1L);
    }

    @Test
    @DisplayName("测试根据车辆ID查询行程接口")
    void testGetByVehicleId() throws Exception {
        when(tripService.getByVehicleId(100L)).thenReturn(Collections.singletonList(testTripVO));

        mockMvc.perform(get("/trip/vehicle/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].vehicleId").value(100));

        verify(tripService).getByVehicleId(100L);
    }

    @Test
    @DisplayName("测试根据司机ID查询行程接口")
    void testGetByDriverId() throws Exception {
        when(tripService.getByDriverId(200L)).thenReturn(Collections.singletonList(testTripVO));

        mockMvc.perform(get("/trip/driver/200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].driverId").value(200));

        verify(tripService).getByDriverId(200L);
    }

    @Test
    @DisplayName("测试根据行程编号查询接口")
    void testGetByTripNo() throws Exception {
        when(tripService.getByTripNo("TRIP202604091000001")).thenReturn(testTripVO);

        mockMvc.perform(get("/trip/no/TRIP202604091000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.tripNo").value("TRIP202604091000001"));

        verify(tripService).getByTripNo("TRIP202604091000001");
    }

    @Test
    @DisplayName("测试获取行程轨迹接口")
    void testGetTripTrack() throws Exception {
        TripTrackVO trackVO = new TripTrackVO();
        trackVO.setTripId("1");
        trackVO.setVehicleId("100");
        trackVO.setLongitude(116.397428);
        trackVO.setLatitude(39.90923);
        trackVO.setSpeed(60.0);

        when(tripService.getTracksByTripId(1L)).thenReturn(Collections.singletonList(trackVO));

        mockMvc.perform(get("/trip/1/track"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].tripId").value("1"));

        verify(tripService).getTracksByTripId(1L);
    }

    @Test
    @DisplayName("测试获取行程统计接口")
    void testGetTripStatistics() throws Exception {
        TripStatisticsVO statisticsVO = new TripStatisticsVO();
        statisticsVO.setTripId(1L);
        statisticsVO.setDurationMinutes(60L);
        statisticsVO.setEstimatedDistance(BigDecimal.valueOf(10.5));
        statisticsVO.setActualDistance(BigDecimal.valueOf(10.2));
        statisticsVO.setAverageSpeed(60.0);

        when(tripService.getTripStatistics(1L)).thenReturn(statisticsVO);

        mockMvc.perform(get("/trip/1/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.tripId").value(1))
                .andExpect(jsonPath("$.data.durationMinutes").value(60));

        verify(tripService).getTripStatistics(1L);
    }

    @Test
    @DisplayName("测试删除行程接口")
    void testDelete() throws Exception {
        doNothing().when(tripService).delete(1L);

        mockMvc.perform(delete("/trip/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(tripService).delete(1L);
    }

    @Test
    @DisplayName("测试更新行程接口")
    void testUpdate() throws Exception {
        TripDTO tripDTO = new TripDTO();
        tripDTO.setVehicleId(100L);
        tripDTO.setDriverId(200L);
        tripDTO.setStartLocation("起点");
        tripDTO.setEndLocation("终点");
        tripDTO.setEstimatedStartTime(LocalDateTime.now());
        tripDTO.setEstimatedEndTime(LocalDateTime.now().plusHours(1));

        doNothing().when(tripService).update(eq(1L), any(TripDTO.class));

        mockMvc.perform(put("/trip/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tripDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(tripService).update(eq(1L), any(TripDTO.class));
    }

    @Test
    @DisplayName("测试接受行程接口")
    void testAcceptTrip() throws Exception {
        doNothing().when(tripService).acceptTrip(1L);

        mockMvc.perform(post("/trip/1/accept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(tripService).acceptTrip(1L);
    }

    @Test
    @DisplayName("测试获取车辆最近行程接口")
    void testGetLatestTrip() throws Exception {
        TripResponse response = new TripResponse();
        response.setId(1L);
        response.setVehicleId(100L);
        response.setDriverId(200L);
        response.setTripNo("TRIP202604091000001");
        response.setStatus(0);

        when(tripService.getLatestTripByVehicleId(100L)).thenReturn(response);

        mockMvc.perform(get("/trip/latest/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.vehicleId").value(100));

        verify(tripService).getLatestTripByVehicleId(100L);
    }

    @Test
    @DisplayName("测试按日期范围查询行程统计接口")
    void testGetStatisticsByDateRange() throws Exception {
        TripStatisticsResponseDTO dto = new TripStatisticsResponseDTO();
        dto.setTripCount(10);
        dto.setTotalDistance(BigDecimal.valueOf(100.5));
        dto.setTotalDuration(BigDecimal.valueOf(5.5));
        dto.setCompletedTripCount(8);
        dto.setCancelledTripCount(2);
        dto.setAverageSpeed(BigDecimal.valueOf(55.0));

        when(tripService.getStatisticsByDateRange("2024-01-01", "2024-01-31")).thenReturn(dto);

        mockMvc.perform(get("/trip/statistics")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.tripCount").value(10));

        verify(tripService).getStatisticsByDateRange("2024-01-01", "2024-01-31");
    }
}
