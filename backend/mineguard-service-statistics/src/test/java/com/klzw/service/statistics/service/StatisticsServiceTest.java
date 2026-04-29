package com.klzw.service.statistics.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.klzw.common.core.client.AiClient;
import com.klzw.common.core.client.CostClient;
import com.klzw.common.core.client.TripClient;
import com.klzw.common.core.client.VehicleClient;
import com.klzw.common.core.client.WarningClient;
import com.klzw.common.core.result.Result;
import com.klzw.service.statistics.dto.StatisticsQueryDTO;
import com.klzw.service.statistics.entity.*;
import com.klzw.service.statistics.mapper.*;
import com.klzw.service.statistics.service.impl.StatisticsServiceImpl;
import com.klzw.service.statistics.vo.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * StatisticsService单元测试类
 */
@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @InjectMocks
    private StatisticsServiceImpl statisticsService;

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

    private TripStatistics testTripStatistics;
    private CostStatistics testCostStatistics;
    private VehicleStatistics testVehicleStatistics;
    private TransportStatistics testTransportStatistics;
    private StatisticsQueryDTO testQueryDTO;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testTripStatistics = new TripStatistics();
        testTripStatistics.setId(1L);
        testTripStatistics.setStatisticsDate(LocalDate.now());
        testTripStatistics.setTripCount(10);
        testTripStatistics.setTotalDistance(new BigDecimal("500.5"));
        testTripStatistics.setTotalDuration(new BigDecimal("48.5"));
        testTripStatistics.setCompletedTripCount(8);
        testTripStatistics.setCancelledTripCount(2);
        testTripStatistics.setAverageSpeed(new BigDecimal("60.5"));
        testTripStatistics.setFuelConsumption(new BigDecimal("100.5"));
        testTripStatistics.setCargoWeight(new BigDecimal("1000.0"));
        testTripStatistics.setCreateTime(LocalDateTime.now());
        testTripStatistics.setUpdateTime(LocalDateTime.now());

        testCostStatistics = new CostStatistics();
        testCostStatistics.setId(1L);
        testCostStatistics.setStatisticsMonth(LocalDate.now());
        testCostStatistics.setFuelCost(new BigDecimal("1000.0"));
        testCostStatistics.setMaintenanceCost(new BigDecimal("500.0"));
        testCostStatistics.setLaborCost(new BigDecimal("2000.0"));
        testCostStatistics.setInsuranceCost(new BigDecimal("300.0"));
        testCostStatistics.setDepreciationCost(new BigDecimal("400.0"));
        testCostStatistics.setManagementCost(new BigDecimal("200.0"));
        testCostStatistics.setOtherCost(new BigDecimal("100.0"));
        testCostStatistics.setTotalCost(new BigDecimal("4500.0"));
        testCostStatistics.setCreateTime(LocalDateTime.now());
        testCostStatistics.setUpdateTime(LocalDateTime.now());

        testVehicleStatistics = new VehicleStatistics();
        testVehicleStatistics.setId(1L);
        testVehicleStatistics.setVehicleId(1L);
        testVehicleStatistics.setStatisticsDate(LocalDate.now());
        testVehicleStatistics.setTripCount(5);
        testVehicleStatistics.setTotalDistance(new BigDecimal("250.0"));
        testVehicleStatistics.setTotalDuration(new BigDecimal("24.0"));
        testVehicleStatistics.setCreateTime(LocalDateTime.now());
        testVehicleStatistics.setUpdateTime(LocalDateTime.now());

        testTransportStatistics = new TransportStatistics();
        testTransportStatistics.setId(1L);
        testTransportStatistics.setStatisticsDate(LocalDate.now());
        testTransportStatistics.setTotalCargoWeight(new BigDecimal("5000.0"));
        testTransportStatistics.setTotalTrips(10);
        testTransportStatistics.setTotalVehicles(5);
        testTransportStatistics.setTotalDrivers(5);
        testTransportStatistics.setCreateTime(LocalDateTime.now());
        testTransportStatistics.setUpdateTime(LocalDateTime.now());

        testQueryDTO = new StatisticsQueryDTO();
        testQueryDTO.setStartDate(LocalDate.now());
        testQueryDTO.setEndDate(LocalDate.now());
        testQueryDTO.setVehicleId(1L);
        testQueryDTO.setUserId(1L);
    }

    @AfterEach
    void tearDown() {
        reset(tripStatisticsMapper, costStatisticsMapper, vehicleStatisticsMapper,
                driverStatisticsMapper, transportStatisticsMapper, faultStatisticsMapper,
                tripClient, costClient, vehicleClient, aiClient, warningClient,
                redisTemplate, valueOperations);
    }

    /**
     * 测试获取行程统计
     */
    @Test
    void testGetTripStatistics() {
        // Mock缓存未命中
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        // Mock数据库查询
        when(tripStatisticsMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(testTripStatistics));

        // 执行测试
        List<TripStatisticsVO> result = statisticsService.getTripStatistics(testQueryDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getTripCount());
        assertEquals(new BigDecimal("500.5"), result.get(0).getTotalDistance());
        verify(tripStatisticsMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试获取运输统计
     */
    @Test
    void testGetTransportStatistics() {
        // Mock数据库查询
        when(transportStatisticsMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(testTransportStatistics));

        // 执行测试
        List<TransportStatisticsVO> result = statisticsService.getTransportStatistics(testQueryDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getTotalTrips());
        assertEquals(new BigDecimal("5000.0"), result.get(0).getTotalCargoWeight());
        verify(transportStatisticsMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试获取总体统计
     */
    @Test
    void testGetOverallStatistics() {
        // Mock缓存未命中
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        // Mock数据库查询
        when(tripStatisticsMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(testTripStatistics));
        when(costStatisticsMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(testCostStatistics));

        // 执行测试
        OverallStatisticsVO result = statisticsService.getOverallStatistics(testQueryDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(10, result.getTotalTripCount());
        assertEquals(new BigDecimal("500.5"), result.getTotalDistance());
        assertEquals(new BigDecimal("4500.0"), result.getTotalCost());
        verify(tripStatisticsMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
        verify(costStatisticsMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试获取运输趋势 - 通过获取多日数据
     */
    @Test
    void testGetTransportTrend() {
        // 准备多日数据
        TransportStatistics stats1 = new TransportStatistics();
        stats1.setId(1L);
        stats1.setStatisticsDate(LocalDate.now().minusDays(1));
        stats1.setTotalTrips(8);
        stats1.setTotalCargoWeight(new BigDecimal("4000.0"));

        TransportStatistics stats2 = new TransportStatistics();
        stats2.setId(2L);
        stats2.setStatisticsDate(LocalDate.now());
        stats2.setTotalTrips(10);
        stats2.setTotalCargoWeight(new BigDecimal("5000.0"));

        // Mock数据库查询
        when(transportStatisticsMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(stats1, stats2));

        // 执行测试
        StatisticsQueryDTO queryDTO = new StatisticsQueryDTO();
        queryDTO.setStartDate(LocalDate.now().minusDays(1));
        queryDTO.setEndDate(LocalDate.now());

        List<TransportStatisticsVO> result = statisticsService.getTransportStatistics(queryDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(transportStatisticsMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试计算行程统计
     */
    @Test
    void testCalculateTripStatistics() {
        // Mock删除缓存
        when(redisTemplate.delete(anyString())).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Mock数据库查询和插入
        when(tripStatisticsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(tripStatisticsMapper.insert(any(TripStatistics.class))).thenAnswer(invocation -> {
            TripStatistics stats = invocation.getArgument(0);
            stats.setId(1L);
            return 1;
        });

        // Mock TripClient
        Map<String, Object> tripData = new HashMap<>();
        tripData.put("tripCount", 10);
        tripData.put("totalDistance", 500.5);
        tripData.put("totalDuration", 48.5);
        tripData.put("completedTripCount", 8);
        tripData.put("cancelledTripCount", 2);
        tripData.put("averageSpeed", 60.5);
        tripData.put("fuelConsumption", 100.5);
        tripData.put("cargoWeight", 1000.0);
        Result<Map<String, Object>> tripResult = Result.success(tripData);
        when(tripClient.getStatistics(anyString(), anyString())).thenReturn(tripResult);

        // 执行测试
        TripStatisticsVO result = statisticsService.calculateTripStatistics(LocalDate.now().toString());

        // 验证结果
        assertNotNull(result);
        assertEquals(10, result.getTripCount());
        verify(tripStatisticsMapper, times(1)).insert(any(TripStatistics.class));
    }

    /**
     * 测试计算成本统计
     */
    @Test
    void testCalculateCostStatistics() {
        // Mock删除缓存
        when(redisTemplate.delete(anyString())).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Mock数据库查询和插入
        when(costStatisticsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(costStatisticsMapper.insert(any(CostStatistics.class))).thenAnswer(invocation -> {
            CostStatistics stats = invocation.getArgument(0);
            stats.setId(1L);
            return 1;
        });

        // Mock CostClient
        com.klzw.common.core.domain.dto.CostStatisticsResponseDTO costData = new com.klzw.common.core.domain.dto.CostStatisticsResponseDTO();
        costData.setFuelCost(new BigDecimal("1000.0"));
        costData.setMaintenanceCost(new BigDecimal("500.0"));
        costData.setLaborCost(new BigDecimal("2000.0"));
        costData.setInsuranceCost(new BigDecimal("300.0"));
        costData.setDepreciationCost(new BigDecimal("400.0"));
        costData.setManagementCost(new BigDecimal("200.0"));
        costData.setOtherCost(new BigDecimal("100.0"));
        when(costClient.getCostStatistics(anyString(), anyString())).thenReturn(com.klzw.common.core.result.Result.success(costData));

        // 执行测试
        CostStatisticsVO result = statisticsService.calculateCostStatistics(LocalDate.now().toString());

        // 验证结果
        assertNotNull(result);
        assertEquals(new BigDecimal("1000.0"), result.getFuelCost());
        verify(costStatisticsMapper, times(1)).insert(any(CostStatistics.class));
    }

    /**
     * 测试计算车辆统计
     */
    @Test
    void testCalculateVehicleStatistics() {
        // Mock删除缓存
        when(redisTemplate.delete(anyString())).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Mock数据库查询和插入
        when(vehicleStatisticsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(vehicleStatisticsMapper.insert(any(VehicleStatistics.class))).thenAnswer(invocation -> {
            VehicleStatistics stats = invocation.getArgument(0);
            stats.setId(1L);
            return 1;
        });

        // 执行测试
        VehicleStatisticsVO result = statisticsService.calculateVehicleStatistics(1L, LocalDate.now().toString(), LocalDate.now().toString());

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getVehicleId());
        verify(vehicleStatisticsMapper, times(1)).insert(any(VehicleStatistics.class));
    }

    /**
     * 测试计算运输统计
     */
    @Test
    void testCalculateTransportStatistics() {
        // Mock删除缓存
        when(redisTemplate.delete(anyString())).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Mock数据库查询和插入
        when(transportStatisticsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(tripStatisticsMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(testTripStatistics));
        when(transportStatisticsMapper.insert(any(TransportStatistics.class))).thenAnswer(invocation -> {
            TransportStatistics stats = invocation.getArgument(0);
            stats.setId(1L);
            return 1;
        });

        // 执行测试
        TransportStatisticsVO result = statisticsService.calculateTransportStatistics(LocalDate.now().toString());

        // 验证结果
        assertNotNull(result);
        verify(transportStatisticsMapper, times(1)).insert(any(TransportStatistics.class));
    }

    /**
     * 测试获取故障统计
     */
    @Test
    void testGetFaultStatistics() {
        // 准备测试数据
        FaultStatistics faultStatistics = new FaultStatistics();
        faultStatistics.setId(1L);
        faultStatistics.setVehicleId(1L);
        faultStatistics.setStatisticsDate(LocalDate.now());
        faultStatistics.setFaultCount(5);
        faultStatistics.setMinorFaultCount(2);
        faultStatistics.setMajorFaultCount(2);
        faultStatistics.setCriticalFaultCount(1);

        // Mock数据库查询
        when(faultStatisticsMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(faultStatistics));

        // 执行测试
        FaultStatisticsVO result = statisticsService.getFaultStatistics(testQueryDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(5, result.getFaultCount());
        verify(faultStatisticsMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试获取故障总体统计
     */
    @Test
    void testGetFaultOverallStatistics() {
        // 准备测试数据
        FaultStatistics faultStatistics = new FaultStatistics();
        faultStatistics.setId(1L);
        faultStatistics.setVehicleId(1L);
        faultStatistics.setStatisticsDate(LocalDate.now());
        faultStatistics.setFaultCount(5);
        faultStatistics.setMinorFaultCount(2);
        faultStatistics.setMajorFaultCount(2);
        faultStatistics.setCriticalFaultCount(1);
        faultStatistics.setTotalRepairCost(new BigDecimal("1000.0"));
        faultStatistics.setRepairedCount(3);
        faultStatistics.setPendingCount(2);

        // Mock数据库查询
        when(faultStatisticsMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(faultStatistics));

        // 执行测试
        FaultStatisticsVO result = statisticsService.getFaultOverallStatistics(testQueryDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(5, result.getTotalFaultCount());
        assertEquals(new BigDecimal("1000.0"), result.getTotalRepairCost());
        verify(faultStatisticsMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试获取司机统计
     */
    @Test
    void testGetDriverStatistics() {
        // 准备测试数据
        DriverStatistics driverStatistics = new DriverStatistics();
        driverStatistics.setId(1L);
        driverStatistics.setUserId(1L);
        driverStatistics.setStatisticsDate(LocalDate.now());
        driverStatistics.setTripCount(10);
        driverStatistics.setTotalDistance(new BigDecimal("500.0"));
        driverStatistics.setAttendanceDays(20);
        driverStatistics.setPerformanceScore(new BigDecimal("95.5"));

        // Mock数据库查询
        when(driverStatisticsMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(driverStatistics));

        // 执行测试
        List<DriverStatisticsVO> result = statisticsService.getDriverStatistics(testQueryDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getTripCount());
        verify(driverStatisticsMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试获取成本统计
     */
    @Test
    void testGetCostStatistics() {
        // Mock缓存未命中
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        // Mock数据库查询
        when(costStatisticsMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(testCostStatistics));

        // 执行测试
        List<CostStatisticsVO> result = statisticsService.getCostStatistics(testQueryDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(new BigDecimal("4500.0"), result.get(0).getTotalCost());
        verify(costStatisticsMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试计算月度行程统计
     */
    @Test
    void testCalculateMonthlyTripStatistics() {
        // Mock删除缓存
        when(redisTemplate.delete(anyString())).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Mock数据库查询和插入
        when(tripStatisticsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(tripStatisticsMapper.insert(any(TripStatistics.class))).thenReturn(1);

        // Mock TripClient
        Map<String, Object> tripData = new HashMap<>();
        tripData.put("tripCount", 10);
        Result<Map<String, Object>> tripResult = Result.success(tripData);
        when(tripClient.getStatistics(anyString(), anyString())).thenReturn(tripResult);

        // 执行测试
        LocalDate startDate = LocalDate.now().minusDays(2);
        LocalDate endDate = LocalDate.now();
        statisticsService.calculateMonthlyTripStatistics(startDate, endDate);

        // 验证结果 - 应该调用3次（3天）
        verify(tripStatisticsMapper, times(3)).insert(any(TripStatistics.class));
    }

    /**
     * 测试计算月度成本统计
     */
    @Test
    void testCalculateMonthlyCostStatistics() {
        // Mock删除缓存
        when(redisTemplate.delete(anyString())).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Mock数据库查询和插入
        when(costStatisticsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(costStatisticsMapper.insert(any(CostStatistics.class))).thenReturn(1);

        // Mock CostClient
        com.klzw.common.core.domain.dto.CostStatisticsResponseDTO costData = new com.klzw.common.core.domain.dto.CostStatisticsResponseDTO();
        costData.setFuelCost(new BigDecimal("1000.0"));
        when(costClient.getCostStatistics(anyString(), anyString())).thenReturn(com.klzw.common.core.result.Result.success(costData));

        // 执行测试
        LocalDate startDate = LocalDate.now().minusDays(2);
        LocalDate endDate = LocalDate.now();
        statisticsService.calculateMonthlyCostStatistics(startDate, endDate);

        // 验证结果 - 应该调用3次（3天）
        verify(costStatisticsMapper, times(3)).insert(any(CostStatistics.class));
    }
}
