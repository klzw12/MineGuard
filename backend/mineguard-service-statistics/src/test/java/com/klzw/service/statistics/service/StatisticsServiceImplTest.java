package com.klzw.service.statistics.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.klzw.common.core.client.AiClient;
import com.klzw.common.core.client.CostClient;
import com.klzw.common.core.client.TripClient;
import com.klzw.common.core.client.VehicleClient;
import com.klzw.common.core.client.WarningClient;
import com.klzw.common.core.domain.dto.CostStatisticsResponseDTO;
import com.klzw.common.core.result.Result;
import com.klzw.service.statistics.dto.StatisticsQueryDTO;
import com.klzw.service.statistics.entity.*;
import com.klzw.service.statistics.mapper.*;
import com.klzw.service.statistics.service.impl.StatisticsServiceImpl;
import com.klzw.service.statistics.vo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.quality.Strictness.LENIENT;

import org.mockito.junit.jupiter.MockitoSettings;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
public class StatisticsServiceImplTest {

    @Mock
    private TripStatisticsMapper tripStatisticsMapper;

    @Mock
    private CostStatisticsMapper costStatisticsMapper;

    @Mock
    private VehicleStatisticsMapper vehicleStatisticsMapper;

    @Mock
    private DriverStatisticsMapper driverStatisticsMapper;

    @Mock
    private TransportStatisticsMapper transportStatisticsMapper;

    @Mock
    private FaultStatisticsMapper faultStatisticsMapper;

    @Mock
    private TripClient tripClient;

    @Mock
    private CostClient costClient;

    @Mock
    private VehicleClient vehicleClient;

    @Mock
    private AiClient aiClient;

    @Mock
    private WarningClient warningClient;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private StatisticsServiceImpl statisticsService;

    private StatisticsQueryDTO queryDTO;
    private TripStatistics tripStatistics;
    private CostStatistics costStatistics;
    private VehicleStatistics vehicleStatistics;
    private DriverStatistics driverStatistics;
    private TransportStatistics transportStatistics;
    private FaultStatistics faultStatistics;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        queryDTO = new StatisticsQueryDTO();
        queryDTO.setStartDate(LocalDate.now().minusDays(7));
        queryDTO.setEndDate(LocalDate.now());

        tripStatistics = new TripStatistics();
        tripStatistics.setId(1L);
        tripStatistics.setStatisticsDate(LocalDate.now());
        tripStatistics.setTripCount(10);
        tripStatistics.setTotalDistance(new BigDecimal(1000));
        tripStatistics.setTotalDuration(new BigDecimal(600));
        tripStatistics.setCompletedTripCount(8);
        tripStatistics.setCancelledTripCount(2);
        tripStatistics.setAverageSpeed(new BigDecimal(60));
        tripStatistics.setFuelConsumption(new BigDecimal(50));
        tripStatistics.setCargoWeight(new BigDecimal(10000));
        tripStatistics.setCreateTime(LocalDateTime.now());
        tripStatistics.setUpdateTime(LocalDateTime.now());

        costStatistics = new CostStatistics();
        costStatistics.setId(1L);
        costStatistics.setStatisticsDate(LocalDate.now());
        costStatistics.setFuelCost(new BigDecimal(1000));
        costStatistics.setMaintenanceCost(new BigDecimal(500));
        costStatistics.setLaborCost(new BigDecimal(2000));
        costStatistics.setInsuranceCost(new BigDecimal(300));
        costStatistics.setDepreciationCost(new BigDecimal(200));
        costStatistics.setManagementCost(new BigDecimal(100));
        costStatistics.setOtherCost(new BigDecimal(50));
        costStatistics.setTotalCost(new BigDecimal(4150));
        costStatistics.setCreateTime(LocalDateTime.now());
        costStatistics.setUpdateTime(LocalDateTime.now());

        vehicleStatistics = new VehicleStatistics();
        vehicleStatistics.setId(1L);
        vehicleStatistics.setVehicleId(1L);
        vehicleStatistics.setStatisticsDate(LocalDate.now());
        vehicleStatistics.setTripCount(5);
        vehicleStatistics.setTotalDistance(new BigDecimal(500));
        vehicleStatistics.setTotalDuration(new BigDecimal(300));
        vehicleStatistics.setCargoWeight(new BigDecimal(5000));
        vehicleStatistics.setFuelConsumption(new BigDecimal(25));
        vehicleStatistics.setFuelCost(new BigDecimal(500));
        vehicleStatistics.setMaintenanceCount(1);
        vehicleStatistics.setMaintenanceCost(new BigDecimal(200));
        vehicleStatistics.setWarningCount(2);
        vehicleStatistics.setViolationCount(0);
        vehicleStatistics.setIdleDuration(new BigDecimal(60));
        vehicleStatistics.setIdleDistance(new BigDecimal(50));
        vehicleStatistics.setCreateTime(LocalDateTime.now());
        vehicleStatistics.setUpdateTime(LocalDateTime.now());

        driverStatistics = new DriverStatistics();
        driverStatistics.setId(1L);
        driverStatistics.setUserId(1L);
        driverStatistics.setStatisticsDate(LocalDate.now());
        driverStatistics.setAttendanceDays(5);
        driverStatistics.setAttendanceHours(new BigDecimal(40));
        driverStatistics.setTripCount(5);
        driverStatistics.setTotalDistance(new BigDecimal(500));
        driverStatistics.setCargoWeight(new BigDecimal(5000));
        driverStatistics.setLateCount(0);
        driverStatistics.setEarlyLeaveCount(0);
        driverStatistics.setWarningCount(1);
        driverStatistics.setViolationCount(0);
        driverStatistics.setOverSpeedCount(1);
        driverStatistics.setRouteDeviationCount(0);
        driverStatistics.setPerformanceScore(new BigDecimal(95));
        driverStatistics.setCreateTime(LocalDateTime.now());
        driverStatistics.setUpdateTime(LocalDateTime.now());

        transportStatistics = new TransportStatistics();
        transportStatistics.setId(1L);
        transportStatistics.setStatisticsDate(LocalDate.now());
        transportStatistics.setTotalCargoWeight(new BigDecimal(10000));
        transportStatistics.setTotalTrips(10);
        transportStatistics.setTotalVehicles(5);
        transportStatistics.setTotalDrivers(3);
        transportStatistics.setAvgCargoPerTrip(new BigDecimal(1000));
        transportStatistics.setAvgTripsPerVehicle(new BigDecimal(2));
        transportStatistics.setCreateTime(LocalDateTime.now());
        transportStatistics.setUpdateTime(LocalDateTime.now());

        faultStatistics = new FaultStatistics();
        faultStatistics.setId(1L);
        faultStatistics.setVehicleId(1L);
        faultStatistics.setStatisticsDate(LocalDate.now());
        faultStatistics.setFaultCount(3);
        faultStatistics.setMinorFaultCount(2);
        faultStatistics.setMajorFaultCount(1);
        faultStatistics.setCriticalFaultCount(0);
        faultStatistics.setTotalRepairCost(new BigDecimal(1000));
        faultStatistics.setAvgRepairTime(new BigDecimal(120));
        faultStatistics.setTopFaultType("发动机故障");
        faultStatistics.setTopFaultCount(2);
        faultStatistics.setRepairedCount(2);
        faultStatistics.setPendingCount(1);
        faultStatistics.setCreateTime(LocalDateTime.now());
        faultStatistics.setUpdateTime(LocalDateTime.now());

        // 模拟Redis操作
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testCalculateTripStatistics() {
        // 模拟依赖方法的返回值
        when(tripStatisticsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        
        Map<String, Object> tripStats = new HashMap<>();
        tripStats.put("tripCount", 10);
        tripStats.put("totalDistance", 1000);
        tripStats.put("totalDuration", 600);
        tripStats.put("completedTripCount", 8);
        tripStats.put("cancelledTripCount", 2);
        tripStats.put("averageSpeed", 60);
        tripStats.put("fuelConsumption", 50);
        tripStats.put("cargoWeight", 10000);
        
        Result<Map<String, Object>> tripResult = Result.success(new HashMap<>());
        tripResult.setCode(200);
        tripResult.setData(tripStats);
        
        when(tripClient.getStatistics(anyString(), anyString())).thenReturn(tripResult);
        when(tripStatisticsMapper.insert(any(TripStatistics.class))).thenReturn(1);

        // 调用被测方法
        TripStatisticsVO result = statisticsService.calculateTripStatistics(LocalDate.now().toString());

        // 验证结果
        assertNotNull(result);
        assertEquals(10, result.getTripCount());
        assertEquals(new BigDecimal(1000), result.getTotalDistance());
        assertEquals(new BigDecimal(600), result.getTotalDuration());
        assertEquals(8, result.getCompletedTripCount());
        assertEquals(2, result.getCancelledTripCount());
        assertEquals(new BigDecimal(60), result.getAverageSpeed());
        assertEquals(new BigDecimal(50), result.getFuelConsumption());
        assertEquals(new BigDecimal(10000), result.getCargoWeight());

        // 验证依赖方法被调用
        verify(tripClient, times(1)).getStatistics(anyString(), anyString());
        verify(tripStatisticsMapper, times(1)).insert(any(TripStatistics.class));
        verify(redisTemplate, times(1)).delete(anyString());
        verify(valueOperations, times(1)).set(anyString(), any(), anyLong(), any());
    }

    @Test
    void testCalculateTripStatisticsWithException() {
        // 模拟依赖方法的返回值
        when(tripStatisticsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(tripClient.getStatistics(anyString(), anyString())).thenThrow(new RuntimeException("测试异常"));
        when(tripStatisticsMapper.insert(any(TripStatistics.class))).thenReturn(1);

        // 调用被测方法
        TripStatisticsVO result = statisticsService.calculateTripStatistics(LocalDate.now().toString());

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.getTripCount());
        assertEquals(BigDecimal.ZERO, result.getTotalDistance());
        assertEquals(BigDecimal.ZERO, result.getTotalDuration());
        assertEquals(0, result.getCompletedTripCount());
        assertEquals(0, result.getCancelledTripCount());
        assertEquals(BigDecimal.ZERO, result.getAverageSpeed());
        assertEquals(BigDecimal.ZERO, result.getFuelConsumption());
        assertEquals(BigDecimal.ZERO, result.getCargoWeight());

        // 验证依赖方法被调用
        verify(tripClient, times(1)).getStatistics(anyString(), anyString());
        verify(tripStatisticsMapper, times(1)).insert(any(TripStatistics.class));
    }

    @Test
    void testCalculateCostStatistics() {
        // 模拟依赖方法的返回值
        when(costStatisticsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        
        CostStatisticsResponseDTO costStats = new CostStatisticsResponseDTO();
        costStats.setFuelCost(new BigDecimal(1000));
        costStats.setMaintenanceCost(new BigDecimal(500));
        costStats.setLaborCost(new BigDecimal(2000));
        costStats.setInsuranceCost(new BigDecimal(300));
        costStats.setDepreciationCost(new BigDecimal(200));
        costStats.setManagementCost(new BigDecimal(100));
        costStats.setOtherCost(new BigDecimal(50));
        
        when(costClient.getCostStatistics(anyString(), anyString())).thenReturn(com.klzw.common.core.result.Result.success(costStats));
        when(costStatisticsMapper.insert(any(CostStatistics.class))).thenReturn(1);

        // 调用被测方法
        CostStatisticsVO result = statisticsService.calculateCostStatistics(LocalDate.now().toString());

        // 验证结果
        assertNotNull(result);
        assertEquals(new BigDecimal(1000), result.getFuelCost());
        assertEquals(new BigDecimal(500), result.getMaintenanceCost());
        assertEquals(new BigDecimal(2000), result.getLaborCost());
        assertEquals(new BigDecimal(300), result.getInsuranceCost());
        assertEquals(new BigDecimal(200), result.getDepreciationCost());
        assertEquals(new BigDecimal(100), result.getManagementCost());
        assertEquals(new BigDecimal(50), result.getOtherCost());
        assertEquals(new BigDecimal(4150), result.getTotalCost());

        // 验证依赖方法被调用
        verify(costClient, times(1)).getCostStatistics(anyString(), anyString());
        verify(costStatisticsMapper, times(1)).insert(any(CostStatistics.class));
        verify(redisTemplate, times(1)).delete(anyString());
        verify(valueOperations, times(1)).set(anyString(), any(), anyLong(), any());
    }

    @Test
    void testCalculateCostStatisticsWithException() {
        // 模拟依赖方法的返回值
        when(costStatisticsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(costClient.getCostStatistics(anyString(), anyString())).thenThrow(new RuntimeException("测试异常"));
        when(costStatisticsMapper.insert(any(CostStatistics.class))).thenReturn(1);

        // 调用被测方法
        CostStatisticsVO result = statisticsService.calculateCostStatistics(LocalDate.now().toString());

        // 验证结果
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getFuelCost());
        assertEquals(BigDecimal.ZERO, result.getMaintenanceCost());
        assertEquals(BigDecimal.ZERO, result.getLaborCost());
        assertEquals(BigDecimal.ZERO, result.getInsuranceCost());
        assertEquals(BigDecimal.ZERO, result.getDepreciationCost());
        assertEquals(BigDecimal.ZERO, result.getManagementCost());
        assertEquals(BigDecimal.ZERO, result.getOtherCost());
        assertEquals(BigDecimal.ZERO, result.getTotalCost());

        // 验证依赖方法被调用
        verify(costClient, times(1)).getCostStatistics(anyString(), anyString());
        verify(costStatisticsMapper, times(1)).insert(any(CostStatistics.class));
    }

    @Test
    void testCalculateVehicleStatistics() {
        // 模拟依赖方法的返回值
        when(vehicleStatisticsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(vehicleStatisticsMapper.insert(any(VehicleStatistics.class))).thenReturn(1);

        // 调用被测方法
        VehicleStatisticsVO result = statisticsService.calculateVehicleStatistics(1L, LocalDate.now().toString());

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getVehicleId());
        assertEquals(0, result.getTripCount());
        assertEquals(BigDecimal.ZERO, result.getTotalDistance());
        assertEquals(BigDecimal.ZERO, result.getTotalDuration());
        assertEquals(BigDecimal.ZERO, result.getCargoWeight());
        assertEquals(BigDecimal.ZERO, result.getFuelConsumption());
        assertEquals(BigDecimal.ZERO, result.getFuelCost());
        assertEquals(0, result.getMaintenanceCount());
        assertEquals(BigDecimal.ZERO, result.getMaintenanceCost());
        assertEquals(0, result.getWarningCount());
        assertEquals(0, result.getViolationCount());
        assertEquals(BigDecimal.ZERO, result.getIdleDuration());
        assertEquals(BigDecimal.ZERO, result.getIdleDistance());

        // 验证依赖方法被调用
        verify(vehicleStatisticsMapper, times(1)).insert(any(VehicleStatistics.class));
        verify(redisTemplate, times(1)).delete(anyString());
        verify(valueOperations, times(1)).set(anyString(), any(), anyLong(), any());
    }

    @Test
    void testCalculateDriverStatistics() {
        // 模拟依赖方法的返回值
        when(driverStatisticsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(driverStatisticsMapper.insert(any(DriverStatistics.class))).thenReturn(1);

        // 调用被测方法
        DriverStatisticsVO result = statisticsService.calculateDriverStatistics(1L, LocalDate.now().toString());

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(0, result.getAttendanceDays());
        assertEquals(BigDecimal.ZERO, result.getAttendanceHours());
        assertEquals(0, result.getTripCount());
        assertEquals(BigDecimal.ZERO, result.getTotalDistance());
        assertEquals(BigDecimal.ZERO, result.getCargoWeight());
        assertEquals(0, result.getLateCount());
        assertEquals(0, result.getEarlyLeaveCount());
        assertEquals(0, result.getWarningCount());
        assertEquals(0, result.getViolationCount());
        assertEquals(0, result.getOverSpeedCount());
        assertEquals(0, result.getRouteDeviationCount());
        assertEquals(new BigDecimal(100), result.getPerformanceScore());

        // 验证依赖方法被调用
        verify(driverStatisticsMapper, times(1)).insert(any(DriverStatistics.class));
        verify(redisTemplate, times(1)).delete(anyString());
        verify(valueOperations, times(1)).set(anyString(), any(), anyLong(), any());
    }

    @Test
    void testCalculateTransportStatistics() {
        // 模拟依赖方法的返回值
        when(transportStatisticsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(tripStatisticsMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(transportStatisticsMapper.insert(any(TransportStatistics.class))).thenReturn(1);

        // 调用被测方法
        TransportStatisticsVO result = statisticsService.calculateTransportStatistics(LocalDate.now().toString());

        // 验证结果
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getTotalCargoWeight());
        assertEquals(0, result.getTotalTrips());
        assertEquals(0, result.getTotalVehicles());
        assertEquals(0, result.getTotalDrivers());
        assertEquals(BigDecimal.ZERO, result.getAvgCargoPerTrip());
        assertEquals(BigDecimal.ZERO, result.getAvgTripsPerVehicle());

        // 验证依赖方法被调用
        verify(transportStatisticsMapper, times(1)).insert(any(TransportStatistics.class));
        verify(redisTemplate, times(1)).delete(anyString());
        verify(valueOperations, times(1)).set(anyString(), any(), anyLong(), any());
    }

    @Test
    void testCalculateFaultStatistics() {
        // 模拟依赖方法的返回值
        when(faultStatisticsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        
        Map<String, Object> faultStats = new HashMap<>();
        faultStats.put("faultCount", 3);
        faultStats.put("minorFaultCount", 2);
        faultStats.put("majorFaultCount", 1);
        faultStats.put("criticalFaultCount", 0);
        faultStats.put("totalRepairCost", 1000);
        faultStats.put("avgRepairTime", 120);
        faultStats.put("topFaultType", "发动机故障");
        faultStats.put("topFaultCount", 2);
        faultStats.put("repairedCount", 2);
        faultStats.put("pendingCount", 1);
        
        Result<Map<String, Object>> faultResult = Result.success(new HashMap<>());
        faultResult.setCode(200);
        faultResult.setData(faultStats);
        
        when(vehicleClient.getFaultStatistics(anyLong(), anyString())).thenReturn(faultResult);
        when(faultStatisticsMapper.insert(any(FaultStatistics.class))).thenReturn(1);

        // 调用被测方法
        statisticsService.calculateFaultStatistics(1L, LocalDate.now().toString());

        // 验证依赖方法被调用
        verify(vehicleClient, times(1)).getFaultStatistics(anyLong(), anyString());
        verify(faultStatisticsMapper, times(1)).insert(any(FaultStatistics.class));
        verify(redisTemplate, times(1)).delete(anyString());
        verify(valueOperations, times(1)).set(anyString(), any(), anyLong(), any());
    }

    @Test
    void testCalculateFaultStatisticsWithException() {
        // 模拟依赖方法的返回值
        when(faultStatisticsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(vehicleClient.getFaultStatistics(anyLong(), anyString())).thenThrow(new RuntimeException("测试异常"));
        when(faultStatisticsMapper.insert(any(FaultStatistics.class))).thenReturn(1);

        // 调用被测方法
        statisticsService.calculateFaultStatistics(1L, LocalDate.now().toString());

        // 验证依赖方法被调用
        verify(vehicleClient, times(1)).getFaultStatistics(anyLong(), anyString());
        verify(faultStatisticsMapper, times(1)).insert(any(FaultStatistics.class));
    }

    @Test
    void testCalculateMonthlyTripStatistics() {
        // 模拟依赖方法的返回值
        when(tripStatisticsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(tripStatisticsMapper.insert(any(TripStatistics.class))).thenReturn(1);

        // 调用被测方法
        LocalDate startDate = LocalDate.now().minusDays(2);
        LocalDate endDate = LocalDate.now();
        statisticsService.calculateMonthlyTripStatistics(startDate, endDate);

        // 验证依赖方法被调用
        verify(tripStatisticsMapper, times(3)).insert(any(TripStatistics.class));
    }

    @Test
    void testCalculateMonthlyCostStatistics() {
        // 模拟依赖方法的返回值
        when(costStatisticsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(costStatisticsMapper.insert(any(CostStatistics.class))).thenReturn(1);

        // 调用被测方法
        LocalDate startDate = LocalDate.now().minusDays(2);
        LocalDate endDate = LocalDate.now();
        statisticsService.calculateMonthlyCostStatistics(startDate, endDate);

        // 验证依赖方法被调用
        verify(costStatisticsMapper, times(3)).insert(any(CostStatistics.class));
    }

    @Test
    void testGetTripStatistics() {
        // 模拟依赖方法的返回值
        when(tripStatisticsMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(tripStatistics));

        // 调用被测方法
        List<TripStatisticsVO> result = statisticsService.getTripStatistics(queryDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getTripCount());
        assertEquals(new BigDecimal(1000), result.get(0).getTotalDistance());

        // 验证依赖方法被调用
        verify(tripStatisticsMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void testGetCostStatistics() {
        // 模拟依赖方法的返回值
        when(costStatisticsMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(costStatistics));

        // 调用被测方法
        List<CostStatisticsVO> result = statisticsService.getCostStatistics(queryDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(new BigDecimal(1000), result.get(0).getFuelCost());
        assertEquals(new BigDecimal(4150), result.get(0).getTotalCost());

        // 验证依赖方法被调用
        verify(costStatisticsMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void testGetVehicleStatistics() {
        // 模拟依赖方法的返回值
        when(vehicleStatisticsMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(vehicleStatistics));

        // 调用被测方法
        List<VehicleStatisticsVO> result = statisticsService.getVehicleStatistics(queryDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getVehicleId());
        assertEquals(5, result.get(0).getTripCount());

        // 验证依赖方法被调用
        verify(vehicleStatisticsMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void testGetDriverStatistics() {
        // 模拟依赖方法的返回值
        when(driverStatisticsMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(driverStatistics));

        // 调用被测方法
        List<DriverStatisticsVO> result = statisticsService.getDriverStatistics(queryDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUserId());
        assertEquals(5, result.get(0).getTripCount());

        // 验证依赖方法被调用
        verify(driverStatisticsMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void testGetTransportStatistics() {
        // 模拟依赖方法的返回值
        when(transportStatisticsMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(transportStatistics));

        // 调用被测方法
        List<TransportStatisticsVO> result = statisticsService.getTransportStatistics(queryDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(new BigDecimal(10000), result.get(0).getTotalCargoWeight());
        assertEquals(10, result.get(0).getTotalTrips());

        // 验证依赖方法被调用
        verify(transportStatisticsMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void testGetOverallStatistics() {
        // 模拟依赖方法的返回值
        when(tripStatisticsMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(tripStatistics));
        when(costStatisticsMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(costStatistics));

        // 调用被测方法
        OverallStatisticsVO result = statisticsService.getOverallStatistics(queryDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(10, result.getTotalTripCount());
        assertEquals(new BigDecimal(1000), result.getTotalDistance());
        assertEquals(new BigDecimal(4150), result.getTotalCost());

        // 验证依赖方法被调用
        verify(tripStatisticsMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
        verify(costStatisticsMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void testGetFaultStatistics() {
        // 模拟依赖方法的返回值
        when(faultStatisticsMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(faultStatistics));

        // 调用被测方法
        FaultStatisticsVO result = statisticsService.getFaultStatistics(queryDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(3, result.getFaultCount());
        assertEquals(2, result.getMinorFaultCount());
        assertEquals(1, result.getMajorFaultCount());
        assertEquals(0, result.getCriticalFaultCount());
        assertEquals(new BigDecimal(1000), result.getTotalRepairCost());

        // 验证依赖方法被调用
        verify(faultStatisticsMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void testGetFaultOverallStatistics() {
        // 模拟依赖方法的返回值
        when(faultStatisticsMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(faultStatistics));

        // 调用被测方法
        FaultStatisticsVO result = statisticsService.getFaultOverallStatistics(queryDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(3, result.getTotalFaultCount());
        assertEquals(3, result.getFaultCount());
        assertEquals(2, result.getMinorFaultCount());
        assertEquals(1, result.getMajorFaultCount());
        assertEquals(0, result.getCriticalFaultCount());
        assertEquals(new BigDecimal(1000), result.getTotalCost());
        assertEquals(new BigDecimal(1000), result.getTotalRepairCost());
        assertEquals(2, result.getRepairedCount());
        assertEquals(1, result.getPendingCount());

        // 验证依赖方法被调用
        verify(faultStatisticsMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void testAnalyzeStatisticsWithAI() {
        // 模拟依赖方法的返回值
        when(tripStatisticsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(tripStatisticsMapper.insert(any(TripStatistics.class))).thenReturn(1);
        when(costStatisticsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(costStatisticsMapper.insert(any(CostStatistics.class))).thenReturn(1);
        
        Map<String, Object> tripStats = new HashMap<>();
        tripStats.put("tripCount", 10);
        
        Result<Map<String, Object>> tripResult = Result.success(new HashMap<>());
        tripResult.setCode(200);
        tripResult.setData(tripStats);
        
        when(tripClient.getStatistics(anyString(), anyString())).thenReturn(tripResult);
        
        CostStatisticsResponseDTO costStats = new CostStatisticsResponseDTO();
        costStats.setFuelCost(new BigDecimal(1000));
        
        when(costClient.getCostStatistics(anyString(), anyString())).thenReturn(com.klzw.common.core.result.Result.success(costStats));
        
        Map<String, Object> aiResult = new HashMap<>();
        aiResult.put("analysis", "测试分析结果");
        
        Result<Map<String, Object>> aiResponse = Result.success(new HashMap<>());
        aiResponse.setCode(200);
        aiResponse.setData(aiResult);
        
        when(aiClient.analyzeStatisticsData(anyMap())).thenReturn(aiResponse);

        // 调用被测方法
        Map<String, Object> result = statisticsService.analyzeStatisticsWithAI(LocalDate.now().toString());

        // 验证结果
        assertNotNull(result);
        assertEquals("测试分析结果", result.get("analysis"));

        // 验证依赖方法被调用
        verify(aiClient, times(1)).analyzeStatisticsData(anyMap());
    }

    @Test
    void testGetWarningTrend() {
        // 模拟依赖方法的返回值
        List<Map<String, Object>> trendData = new ArrayList<>();
        Map<String, Object> dayData = new HashMap<>();
        dayData.put("date", LocalDate.now().toString());
        dayData.put("count", 5);
        trendData.add(dayData);
        
        Result<List<Map<String, Object>>> warningResult = Result.success(new ArrayList<>());
        warningResult.setCode(200);
        warningResult.setData(trendData);
        
        when(warningClient.getTrend(anyInt())).thenReturn(warningResult);

        // 调用被测方法
        Map<String, Object> result = statisticsService.getWarningTrend(7);

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.get("trend"));

        // 验证依赖方法被调用
        verify(warningClient, times(1)).getTrend(anyInt());
    }

    @Test
    void testGetWarningStatistics() {
        // 模拟依赖方法的返回值
        Map<String, Object> warningStats = new HashMap<>();
        warningStats.put("totalCount", 10);
        warningStats.put("highLevelCount", 2);
        
        Result<Map<String, Object>> warningResult = Result.success(new HashMap<>());
        warningResult.setCode(200);
        warningResult.setData(warningStats);
        
        when(warningClient.getStatistics(anyString(), anyString())).thenReturn(warningResult);

        // 调用被测方法
        Map<String, Object> result = statisticsService.getWarningStatistics(LocalDate.now().minusDays(7).toString(), LocalDate.now().toString());

        // 验证结果
        assertNotNull(result);
        assertEquals(10, result.get("totalCount"));
        assertEquals(2, result.get("highLevelCount"));

        // 验证依赖方法被调用
        verify(warningClient, times(1)).getStatistics(anyString(), anyString());
    }
}
