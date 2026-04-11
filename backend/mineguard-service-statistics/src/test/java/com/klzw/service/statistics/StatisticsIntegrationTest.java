package com.klzw.service.statistics;

import com.klzw.common.core.config.DotenvInitializer;
import com.klzw.service.statistics.dto.StatisticsQueryDTO;
import com.klzw.service.statistics.entity.*;
import com.klzw.service.statistics.mapper.*;
import com.klzw.service.statistics.service.StatisticsService;
import com.klzw.service.statistics.vo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * StatisticsService集成测试类
 */
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = DotenvInitializer.class)
@Transactional
@Tag("integration")
class StatisticsIntegrationTest {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private TripStatisticsMapper tripStatisticsMapper;

    @Autowired
    private CostStatisticsMapper costStatisticsMapper;

    @Autowired
    private VehicleStatisticsMapper vehicleStatisticsMapper;

    @Autowired
    private DriverStatisticsMapper driverStatisticsMapper;

    @Autowired
    private TransportStatisticsMapper transportStatisticsMapper;

    @Autowired
    private FaultStatisticsMapper faultStatisticsMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private TripStatistics testTripStatistics;
    private CostStatistics testCostStatistics;
    private VehicleStatistics testVehicleStatistics;
    private DriverStatistics testDriverStatistics;
    private TransportStatistics testTransportStatistics;
    private FaultStatistics testFaultStatistics;

    @BeforeEach
    void setUp() {
        // 使用truncate table清理测试数据，避免数据饱满
        jdbcTemplate.execute("TRUNCATE TABLE trip_statistics");
        jdbcTemplate.execute("TRUNCATE TABLE cost_statistics");
        jdbcTemplate.execute("TRUNCATE TABLE vehicle_statistics");
        jdbcTemplate.execute("TRUNCATE TABLE driver_statistics");
        jdbcTemplate.execute("TRUNCATE TABLE transport_statistics");
        jdbcTemplate.execute("TRUNCATE TABLE fault_statistics");
        
        // 清理Redis缓存
        redisTemplate.delete("statistics:trip:" + LocalDate.now());
        redisTemplate.delete("statistics:cost:" + LocalDate.now());
        
        // 初始化行程统计数据
        testTripStatistics = new TripStatistics();
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
        tripStatisticsMapper.insert(testTripStatistics);

        // 初始化成本统计数据
        testCostStatistics = new CostStatistics();
        testCostStatistics.setStatisticsDate(LocalDate.now());
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
        costStatisticsMapper.insert(testCostStatistics);

        // 初始化车辆统计数据
        testVehicleStatistics = new VehicleStatistics();
        testVehicleStatistics.setVehicleId(1L);
        testVehicleStatistics.setStatisticsDate(LocalDate.now());
        testVehicleStatistics.setTripCount(5);
        testVehicleStatistics.setTotalDistance(new BigDecimal("250.0"));
        testVehicleStatistics.setTotalDuration(new BigDecimal("24.0"));
        testVehicleStatistics.setCargoWeight(new BigDecimal("500.0"));
        testVehicleStatistics.setFuelConsumption(new BigDecimal("50.0"));
        testVehicleStatistics.setFuelCost(new BigDecimal("500.0"));
        testVehicleStatistics.setMaintenanceCount(1);
        testVehicleStatistics.setMaintenanceCost(new BigDecimal("200.0"));
        testVehicleStatistics.setWarningCount(2);
        testVehicleStatistics.setViolationCount(0);
        testVehicleStatistics.setIdleDuration(new BigDecimal("2.0"));
        testVehicleStatistics.setIdleDistance(new BigDecimal("10.0"));
        testVehicleStatistics.setCreateTime(LocalDateTime.now());
        testVehicleStatistics.setUpdateTime(LocalDateTime.now());
        vehicleStatisticsMapper.insert(testVehicleStatistics);

        // 初始化司机统计数据
        testDriverStatistics = new DriverStatistics();
        testDriverStatistics.setUserId(1L);
        testDriverStatistics.setStatisticsDate(LocalDate.now());
        testDriverStatistics.setAttendanceDays(20);
        testDriverStatistics.setAttendanceHours(new BigDecimal("160.0"));
        testDriverStatistics.setTripCount(10);
        testDriverStatistics.setTotalDistance(new BigDecimal("500.0"));
        testDriverStatistics.setCargoWeight(new BigDecimal("1000.0"));
        testDriverStatistics.setLateCount(1);
        testDriverStatistics.setEarlyLeaveCount(0);
        testDriverStatistics.setWarningCount(2);
        testDriverStatistics.setViolationCount(0);
        testDriverStatistics.setOverSpeedCount(0);
        testDriverStatistics.setRouteDeviationCount(0);
        testDriverStatistics.setPerformanceScore(new BigDecimal("95.5"));
        testDriverStatistics.setCreateTime(LocalDateTime.now());
        testDriverStatistics.setUpdateTime(LocalDateTime.now());
        driverStatisticsMapper.insert(testDriverStatistics);

        // 初始化运输统计数据
        testTransportStatistics = new TransportStatistics();
        testTransportStatistics.setStatisticsDate(LocalDate.now());
        testTransportStatistics.setTotalCargoWeight(new BigDecimal("5000.0"));
        testTransportStatistics.setTotalTrips(10);
        testTransportStatistics.setTotalVehicles(5);
        testTransportStatistics.setTotalDrivers(5);
        testTransportStatistics.setAvgCargoPerTrip(new BigDecimal("500.0"));
        testTransportStatistics.setAvgTripsPerVehicle(new BigDecimal("2.0"));
        testTransportStatistics.setCreateTime(LocalDateTime.now());
        testTransportStatistics.setUpdateTime(LocalDateTime.now());
        transportStatisticsMapper.insert(testTransportStatistics);

        // 初始化故障统计数据
        testFaultStatistics = new FaultStatistics();
        testFaultStatistics.setVehicleId(1L);
        testFaultStatistics.setStatisticsDate(LocalDate.now());
        testFaultStatistics.setFaultCount(5);
        testFaultStatistics.setMinorFaultCount(2);
        testFaultStatistics.setMajorFaultCount(2);
        testFaultStatistics.setCriticalFaultCount(1);
        testFaultStatistics.setTotalRepairCost(new BigDecimal("1000.0"));
        testFaultStatistics.setAvgRepairTime(new BigDecimal("2.5"));
        testFaultStatistics.setTopFaultType("发动机故障");
        testFaultStatistics.setTopFaultCount(2);
        testFaultStatistics.setRepairedCount(3);
        testFaultStatistics.setPendingCount(2);
        testFaultStatistics.setCreateTime(LocalDateTime.now());
        testFaultStatistics.setUpdateTime(LocalDateTime.now());
        faultStatisticsMapper.insert(testFaultStatistics);
    }

    /**
     * 测试获取行程统计完整流程
     */
    @Test
    void testGetTripStatisticsFlow() {
        StatisticsQueryDTO queryDTO = new StatisticsQueryDTO();
        queryDTO.setStartDate(LocalDate.now());
        queryDTO.setEndDate(LocalDate.now());

        List<TripStatisticsVO> result = statisticsService.getTripStatistics(queryDTO);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(10, result.get(0).getTripCount());
        assertTrue(result.get(0).getTotalDistance().compareTo(new BigDecimal("500.5")) == 0);
    }

    /**
     * 测试获取成本统计完整流程
     */
    @Test
    void testGetCostStatisticsFlow() {
        StatisticsQueryDTO queryDTO = new StatisticsQueryDTO();
        queryDTO.setStartDate(LocalDate.now());
        queryDTO.setEndDate(LocalDate.now());

        List<CostStatisticsVO> result = statisticsService.getCostStatistics(queryDTO);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.get(0).getTotalCost().compareTo(new BigDecimal("4500.0")) == 0);
    }

    /**
     * 测试获取车辆统计完整流程
     */
    @Test
    void testGetVehicleStatisticsFlow() {
        StatisticsQueryDTO queryDTO = new StatisticsQueryDTO();
        queryDTO.setVehicleId(1L);
        queryDTO.setStartDate(LocalDate.now());
        queryDTO.setEndDate(LocalDate.now());

        List<VehicleStatisticsVO> result = statisticsService.getVehicleStatistics(queryDTO);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1L, result.get(0).getVehicleId());
        assertEquals(5, result.get(0).getTripCount());
    }

    /**
     * 测试获取司机统计完整流程
     */
    @Test
    void testGetDriverStatisticsFlow() {
        StatisticsQueryDTO queryDTO = new StatisticsQueryDTO();
        queryDTO.setUserId(1L);
        queryDTO.setStartDate(LocalDate.now());
        queryDTO.setEndDate(LocalDate.now());

        List<DriverStatisticsVO> result = statisticsService.getDriverStatistics(queryDTO);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1L, result.get(0).getUserId());
        assertEquals(10, result.get(0).getTripCount());
    }

    /**
     * 测试获取运输统计完整流程
     */
    @Test
    void testGetTransportStatisticsFlow() {
        StatisticsQueryDTO queryDTO = new StatisticsQueryDTO();
        queryDTO.setStartDate(LocalDate.now());
        queryDTO.setEndDate(LocalDate.now());

        List<TransportStatisticsVO> result = statisticsService.getTransportStatistics(queryDTO);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(10, result.get(0).getTotalTrips());
        assertTrue(result.get(0).getTotalCargoWeight().compareTo(new BigDecimal("5000.0")) == 0);
    }

    /**
     * 测试获取总体统计完整流程
     */
    @Test
    void testGetOverallStatisticsFlow() {
        StatisticsQueryDTO queryDTO = new StatisticsQueryDTO();
        queryDTO.setStartDate(LocalDate.now());
        queryDTO.setEndDate(LocalDate.now());

        OverallStatisticsVO result = statisticsService.getOverallStatistics(queryDTO);

        assertNotNull(result);
        assertEquals(10, result.getTotalTripCount());
        assertTrue(result.getTotalDistance().compareTo(new BigDecimal("500.5")) == 0);
        assertTrue(result.getTotalCost().compareTo(new BigDecimal("4500.0")) == 0);
        assertNotNull(result.getTripStatisticsList());
        assertNotNull(result.getCostStatisticsList());
    }

    /**
     * 测试获取故障统计完整流程
     */
    @Test
    void testGetFaultStatisticsFlow() {
        StatisticsQueryDTO queryDTO = new StatisticsQueryDTO();
        queryDTO.setVehicleId(1L);
        queryDTO.setStartDate(LocalDate.now());
        queryDTO.setEndDate(LocalDate.now());

        FaultStatisticsVO result = statisticsService.getFaultStatistics(queryDTO);

        assertNotNull(result);
        assertEquals(5, result.getFaultCount());
        assertTrue(result.getTotalRepairCost().compareTo(new BigDecimal("1000.0")) == 0);
    }

    /**
     * 测试获取故障总体统计完整流程
     */
    @Test
    void testGetFaultOverallStatisticsFlow() {
        StatisticsQueryDTO queryDTO = new StatisticsQueryDTO();
        queryDTO.setStartDate(LocalDate.now());
        queryDTO.setEndDate(LocalDate.now());

        FaultStatisticsVO result = statisticsService.getFaultOverallStatistics(queryDTO);

        assertNotNull(result);
        assertEquals(5, result.getTotalFaultCount());
        assertTrue(result.getTotalRepairCost().compareTo(new BigDecimal("1000.0")) == 0);
    }

    /**
     * 测试多日数据查询
     */
    @Test
    void testMultiDayQuery() {
        // 插入第二天的数据
        TripStatistics secondDayTrip = new TripStatistics();
        secondDayTrip.setStatisticsDate(LocalDate.now().minusDays(1));
        secondDayTrip.setTripCount(8);
        secondDayTrip.setTotalDistance(new BigDecimal("400.0"));
        secondDayTrip.setTotalDuration(new BigDecimal("40.0"));
        secondDayTrip.setCreateTime(LocalDateTime.now());
        secondDayTrip.setUpdateTime(LocalDateTime.now());
        tripStatisticsMapper.insert(secondDayTrip);

        StatisticsQueryDTO queryDTO = new StatisticsQueryDTO();
        queryDTO.setStartDate(LocalDate.now().minusDays(1));
        queryDTO.setEndDate(LocalDate.now());

        List<TripStatisticsVO> result = statisticsService.getTripStatistics(queryDTO);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    /**
     * 测试空结果查询
     */
    @Test
    void testEmptyResultQuery() {
        StatisticsQueryDTO queryDTO = new StatisticsQueryDTO();
        queryDTO.setStartDate(LocalDate.now().plusDays(10));
        queryDTO.setEndDate(LocalDate.now().plusDays(10));

        List<TripStatisticsVO> result = statisticsService.getTripStatistics(queryDTO);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * 测试特定车辆查询
     */
    @Test
    void testSpecificVehicleQuery() {
        // 插入另一车辆的数据
        VehicleStatistics anotherVehicle = new VehicleStatistics();
        anotherVehicle.setVehicleId(2L);
        anotherVehicle.setStatisticsDate(LocalDate.now());
        anotherVehicle.setTripCount(3);
        anotherVehicle.setTotalDistance(new BigDecimal("150.0"));
        anotherVehicle.setCreateTime(LocalDateTime.now());
        anotherVehicle.setUpdateTime(LocalDateTime.now());
        vehicleStatisticsMapper.insert(anotherVehicle);

        StatisticsQueryDTO queryDTO = new StatisticsQueryDTO();
        queryDTO.setVehicleId(1L);
        queryDTO.setStartDate(LocalDate.now());
        queryDTO.setEndDate(LocalDate.now());

        List<VehicleStatisticsVO> result = statisticsService.getVehicleStatistics(queryDTO);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getVehicleId());
    }

    /**
     * 测试特定司机查询
     */
    @Test
    void testSpecificDriverQuery() {
        // 插入另一司机的数据
        DriverStatistics anotherDriver = new DriverStatistics();
        anotherDriver.setUserId(2L);
        anotherDriver.setStatisticsDate(LocalDate.now());
        anotherDriver.setTripCount(5);
        anotherDriver.setTotalDistance(new BigDecimal("250.0"));
        anotherDriver.setCreateTime(LocalDateTime.now());
        anotherDriver.setUpdateTime(LocalDateTime.now());
        driverStatisticsMapper.insert(anotherDriver);

        StatisticsQueryDTO queryDTO = new StatisticsQueryDTO();
        queryDTO.setUserId(1L);
        queryDTO.setStartDate(LocalDate.now());
        queryDTO.setEndDate(LocalDate.now());

        List<DriverStatisticsVO> result = statisticsService.getDriverStatistics(queryDTO);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUserId());
    }

    /**
     * 测试计算行程统计
     */
    @Test
    void testCalculateTripStatistics() {
        // 调用计算行程统计方法
        TripStatisticsVO result = statisticsService.calculateTripStatistics(LocalDate.now().toString());

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getTripCount());
        assertNotNull(result.getTotalDistance());
        assertNotNull(result.getTotalDuration());
    }

    /**
     * 测试计算成本统计
     */
    @Test
    void testCalculateCostStatistics() {
        // 调用计算成本统计方法
        CostStatisticsVO result = statisticsService.calculateCostStatistics(LocalDate.now().toString());

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getFuelCost());
        assertNotNull(result.getMaintenanceCost());
        assertNotNull(result.getTotalCost());
    }

    /**
     * 测试计算车辆统计
     */
    @Test
    void testCalculateVehicleStatistics() {
        // 调用计算车辆统计方法
        VehicleStatisticsVO result = statisticsService.calculateVehicleStatistics(1L, LocalDate.now().toString());

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getVehicleId());
        assertNotNull(result.getTripCount());
        assertNotNull(result.getTotalDistance());
    }

    /**
     * 测试计算司机统计
     */
    @Test
    void testCalculateDriverStatistics() {
        // 调用计算司机统计方法
        DriverStatisticsVO result = statisticsService.calculateDriverStatistics(1L, LocalDate.now().toString());

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertNotNull(result.getAttendanceDays());
        assertNotNull(result.getTripCount());
    }

    /**
     * 测试计算运输统计
     */
    @Test
    void testCalculateTransportStatistics() {
        // 调用计算运输统计方法
        TransportStatisticsVO result = statisticsService.calculateTransportStatistics(LocalDate.now().toString());

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getTotalCargoWeight());
        assertNotNull(result.getTotalTrips());
    }

    /**
     * 测试计算故障统计
     */
    @Test
    void testCalculateFaultStatistics() {
        // 调用计算故障统计方法
        statisticsService.calculateFaultStatistics(1L, LocalDate.now().toString());

        // 验证方法执行成功（无异常抛出）
        // 实际结果可以通过查询验证
        StatisticsQueryDTO queryDTO = new StatisticsQueryDTO();
        queryDTO.setVehicleId(1L);
        queryDTO.setStartDate(LocalDate.now());
        queryDTO.setEndDate(LocalDate.now());

        FaultStatisticsVO result = statisticsService.getFaultStatistics(queryDTO);
        assertNotNull(result);
    }

    /**
     * 测试计算月度行程统计
     */
    @Test
    void testCalculateMonthlyTripStatistics() {
        // 调用计算月度行程统计方法
        LocalDate startDate = LocalDate.now().minusDays(2);
        LocalDate endDate = LocalDate.now();
        statisticsService.calculateMonthlyTripStatistics(startDate, endDate);

        // 验证方法执行成功（无异常抛出）
        // 实际结果可以通过查询验证
        StatisticsQueryDTO queryDTO = new StatisticsQueryDTO();
        queryDTO.setStartDate(startDate);
        queryDTO.setEndDate(endDate);

        List<TripStatisticsVO> result = statisticsService.getTripStatistics(queryDTO);
        assertNotNull(result);
    }

    /**
     * 测试计算月度成本统计
     */
    @Test
    void testCalculateMonthlyCostStatistics() {
        // 调用计算月度成本统计方法
        LocalDate startDate = LocalDate.now().minusDays(2);
        LocalDate endDate = LocalDate.now();
        statisticsService.calculateMonthlyCostStatistics(startDate, endDate);

        // 验证方法执行成功（无异常抛出）
        // 实际结果可以通过查询验证
        StatisticsQueryDTO queryDTO = new StatisticsQueryDTO();
        queryDTO.setStartDate(startDate);
        queryDTO.setEndDate(endDate);

        List<CostStatisticsVO> result = statisticsService.getCostStatistics(queryDTO);
        assertNotNull(result);
    }
} 
