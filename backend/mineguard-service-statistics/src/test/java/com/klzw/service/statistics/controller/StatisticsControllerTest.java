package com.klzw.service.statistics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.klzw.service.statistics.dto.StatisticsQueryDTO;
import com.klzw.service.statistics.service.StatisticsService;
import com.klzw.service.statistics.vo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * StatisticsController切片测试类
 */
@ExtendWith(MockitoExtension.class)
class StatisticsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StatisticsService statisticsService;

    @InjectMocks
    private StatisticsController statisticsController;

    private ObjectMapper objectMapper;
    private TripStatisticsVO testTripStatisticsVO;
    private CostStatisticsVO testCostStatisticsVO;
    private VehicleStatisticsVO testVehicleStatisticsVO;
    private TransportStatisticsVO testTransportStatisticsVO;
    private OverallStatisticsVO testOverallStatisticsVO;
    private FaultStatisticsVO testFaultStatisticsVO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(statisticsController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // 初始化测试数据
        testTripStatisticsVO = new TripStatisticsVO();
        testTripStatisticsVO.setId(1L);
        testTripStatisticsVO.setStatisticsDate(LocalDate.now());
        testTripStatisticsVO.setTripCount(10);
        testTripStatisticsVO.setTotalDistance(new BigDecimal("500.5"));
        testTripStatisticsVO.setTotalDuration(new BigDecimal("48.5"));
        testTripStatisticsVO.setCompletedTripCount(8);
        testTripStatisticsVO.setCancelledTripCount(2);

        testCostStatisticsVO = new CostStatisticsVO();
        testCostStatisticsVO.setId(1L);
        testCostStatisticsVO.setStatisticsDate(LocalDate.now());
        testCostStatisticsVO.setFuelCost(new BigDecimal("1000.0"));
        testCostStatisticsVO.setMaintenanceCost(new BigDecimal("500.0"));
        testCostStatisticsVO.setTotalCost(new BigDecimal("4500.0"));

        testVehicleStatisticsVO = new VehicleStatisticsVO();
        testVehicleStatisticsVO.setId(1L);
        testVehicleStatisticsVO.setVehicleId(1L);
        testVehicleStatisticsVO.setStatisticsDate(LocalDate.now());
        testVehicleStatisticsVO.setTripCount(5);
        testVehicleStatisticsVO.setTotalDistance(new BigDecimal("250.0"));

        testTransportStatisticsVO = new TransportStatisticsVO();
        testTransportStatisticsVO.setId(1L);
        testTransportStatisticsVO.setStatisticsDate(LocalDate.now());
        testTransportStatisticsVO.setTotalCargoWeight(new BigDecimal("5000.0"));
        testTransportStatisticsVO.setTotalTrips(10);
        testTransportStatisticsVO.setTotalVehicles(5);

        testOverallStatisticsVO = new OverallStatisticsVO();
        testOverallStatisticsVO.setStartDate(LocalDate.now());
        testOverallStatisticsVO.setEndDate(LocalDate.now());
        testOverallStatisticsVO.setTotalTripCount(10);
        testOverallStatisticsVO.setTotalDistance(new BigDecimal("500.5"));
        testOverallStatisticsVO.setTotalCost(new BigDecimal("4500.0"));

        testFaultStatisticsVO = new FaultStatisticsVO();
        testFaultStatisticsVO.setId(1L);
        testFaultStatisticsVO.setVehicleId(1L);
        testFaultStatisticsVO.setStatisticsDate(LocalDate.now());
        testFaultStatisticsVO.setFaultCount(5);
        testFaultStatisticsVO.setMinorFaultCount(2);
        testFaultStatisticsVO.setMajorFaultCount(2);
        testFaultStatisticsVO.setCriticalFaultCount(1);
    }

    /**
     * 测试获取行程统计
     */
    @Test
    void testGetTripStatistics() throws Exception {
        when(statisticsService.getTripStatistics(any(StatisticsQueryDTO.class)))
                .thenReturn(Collections.singletonList(testTripStatisticsVO));

        mockMvc.perform(get("/statistics/trip")
                        .param("startDate", LocalDate.now().toString())
                        .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].tripCount").value(10));

        verify(statisticsService, times(1)).getTripStatistics(any(StatisticsQueryDTO.class));
    }

    /**
     * 测试获取成本统计
     */
    @Test
    void testGetCostStatistics() throws Exception {
        when(statisticsService.getCostStatistics(any(StatisticsQueryDTO.class)))
                .thenReturn(Collections.singletonList(testCostStatisticsVO));

        mockMvc.perform(get("/statistics/cost")
                        .param("startDate", LocalDate.now().toString())
                        .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].totalCost").value(4500.0));

        verify(statisticsService, times(1)).getCostStatistics(any(StatisticsQueryDTO.class));
    }

    /**
     * 测试获取车辆统计
     */
    @Test
    void testGetVehicleStatistics() throws Exception {
        when(statisticsService.getVehicleStatistics(any(StatisticsQueryDTO.class)))
                .thenReturn(Collections.singletonList(testVehicleStatisticsVO));

        mockMvc.perform(get("/statistics/vehicle")
                        .param("vehicleId", "1")
                        .param("startDate", LocalDate.now().toString())
                        .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].vehicleId").value(1));

        verify(statisticsService, times(1)).getVehicleStatistics(any(StatisticsQueryDTO.class));
    }

    /**
     * 测试获取司机统计
     */
    @Test
    void testGetDriverStatistics() throws Exception {
        DriverStatisticsVO driverStatisticsVO = new DriverStatisticsVO();
        driverStatisticsVO.setId(1L);
        driverStatisticsVO.setUserId(1L);
        driverStatisticsVO.setStatisticsDate(LocalDate.now());
        driverStatisticsVO.setTripCount(10);

        when(statisticsService.getDriverStatistics(any(StatisticsQueryDTO.class)))
                .thenReturn(Collections.singletonList(driverStatisticsVO));

        mockMvc.perform(get("/statistics/driver")
                        .param("userId", "1")
                        .param("startDate", LocalDate.now().toString())
                        .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        verify(statisticsService, times(1)).getDriverStatistics(any(StatisticsQueryDTO.class));
    }

    /**
     * 测试获取总体统计
     */
    @Test
    void testGetOverallStatistics() throws Exception {
        when(statisticsService.getOverallStatistics(any(StatisticsQueryDTO.class)))
                .thenReturn(testOverallStatisticsVO);

        mockMvc.perform(get("/statistics/overall")
                        .param("startDate", LocalDate.now().toString())
                        .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalTripCount").value(10))
                .andExpect(jsonPath("$.data.totalCost").value(4500.0));

        verify(statisticsService, times(1)).getOverallStatistics(any(StatisticsQueryDTO.class));
    }

    /**
     * 测试获取故障统计
     */
    @Test
    void testGetFaultStatistics() throws Exception {
        when(statisticsService.getFaultStatistics(any(StatisticsQueryDTO.class)))
                .thenReturn(testFaultStatisticsVO);

        mockMvc.perform(get("/statistics/fault")
                        .param("vehicleId", "1")
                        .param("startDate", LocalDate.now().toString())
                        .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.faultCount").value(5));

        verify(statisticsService, times(1)).getFaultStatistics(any(StatisticsQueryDTO.class));
    }

    /**
     * 测试获取故障总体统计
     */
    @Test
    void testGetFaultOverallStatistics() throws Exception {
        when(statisticsService.getFaultOverallStatistics(any(StatisticsQueryDTO.class)))
                .thenReturn(testFaultStatisticsVO);

        mockMvc.perform(get("/statistics/fault/overall")
                        .param("startDate", LocalDate.now().toString())
                        .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.faultCount").value(5));

        verify(statisticsService, times(1)).getFaultOverallStatistics(any(StatisticsQueryDTO.class));
    }

    /**
     * 测试获取运输统计
     */
    @Test
    void testGetTransportStatistics() throws Exception {
        when(statisticsService.getTransportStatistics(any(StatisticsQueryDTO.class)))
                .thenReturn(Collections.singletonList(testTransportStatisticsVO));

        mockMvc.perform(get("/statistics/transport")
                        .param("startDate", LocalDate.now().toString())
                        .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].totalTrips").value(10));

        verify(statisticsService, times(1)).getTransportStatistics(any(StatisticsQueryDTO.class));
    }

    /**
     * 测试计算行程统计
     */
    @Test
    void testCalculateTripStatistics() throws Exception {
        when(statisticsService.calculateTripStatistics(anyString()))
                .thenReturn(testTripStatisticsVO);

        mockMvc.perform(post("/statistics/trip/calculate")
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.tripCount").value(10));

        verify(statisticsService, times(1)).calculateTripStatistics(anyString());
    }

    /**
     * 测试计算成本统计
     */
    @Test
    void testCalculateCostStatistics() throws Exception {
        when(statisticsService.calculateCostStatistics(anyString()))
                .thenReturn(testCostStatisticsVO);

        mockMvc.perform(post("/statistics/cost/calculate")
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalCost").value(4500.0));

        verify(statisticsService, times(1)).calculateCostStatistics(anyString());
    }

    /**
     * 测试计算车辆统计
     */
    @Test
    void testCalculateVehicleStatistics() throws Exception {
        when(statisticsService.calculateVehicleStatistics(anyLong(), anyString()))
                .thenReturn(testVehicleStatisticsVO);

        mockMvc.perform(post("/statistics/vehicle/calculate")
                        .param("vehicleId", "1")
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.vehicleId").value(1));

        verify(statisticsService, times(1)).calculateVehicleStatistics(anyLong(), anyString());
    }

    /**
     * 测试计算司机统计
     */
    @Test
    void testCalculateDriverStatistics() throws Exception {
        DriverStatisticsVO driverStatisticsVO = new DriverStatisticsVO();
        driverStatisticsVO.setId(1L);
        driverStatisticsVO.setUserId(1L);
        driverStatisticsVO.setStatisticsDate(LocalDate.now());

        when(statisticsService.calculateDriverStatistics(anyLong(), anyString()))
                .thenReturn(driverStatisticsVO);

        mockMvc.perform(post("/statistics/driver/calculate")
                        .param("userId", "1")
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(statisticsService, times(1)).calculateDriverStatistics(anyLong(), anyString());
    }

    /**
     * 测试计算故障统计
     */
    @Test
    void testCalculateFaultStatistics() throws Exception {
        doNothing().when(statisticsService).calculateFaultStatistics(anyLong(), anyString());

        mockMvc.perform(post("/statistics/fault/calculate")
                        .param("vehicleId", "1")
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(statisticsService, times(1)).calculateFaultStatistics(anyLong(), anyString());
    }

    /**
     * 测试计算运输统计
     */
    @Test
    void testCalculateTransportStatistics() throws Exception {
        when(statisticsService.calculateTransportStatistics(anyString()))
                .thenReturn(testTransportStatisticsVO);

        mockMvc.perform(post("/statistics/transport/calculate")
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalTrips").value(10));

        verify(statisticsService, times(1)).calculateTransportStatistics(anyString());
    }

    /**
     * 测试获取行程统计 - 空结果
     */
    @Test
    void testGetTripStatisticsEmpty() throws Exception {
        when(statisticsService.getTripStatistics(any(StatisticsQueryDTO.class)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/statistics/trip")
                        .param("startDate", LocalDate.now().toString())
                        .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());

        verify(statisticsService, times(1)).getTripStatistics(any(StatisticsQueryDTO.class));
    }

    /**
     * 测试获取总体统计 - 无参数
     */
    @Test
    void testGetOverallStatisticsNoParams() throws Exception {
        when(statisticsService.getOverallStatistics(any(StatisticsQueryDTO.class)))
                .thenReturn(testOverallStatisticsVO);

        mockMvc.perform(get("/statistics/overall"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(statisticsService, times(1)).getOverallStatistics(any(StatisticsQueryDTO.class));
    }
}
