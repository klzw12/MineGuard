package com.klzw.service.statistics.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.klzw.common.core.client.VehicleClient;
import com.klzw.common.core.result.Result;
import com.klzw.service.statistics.dto.StatisticsQueryDTO;
import com.klzw.service.statistics.entity.FaultStatistics;
import com.klzw.service.statistics.mapper.FaultStatisticsMapper;
import com.klzw.service.statistics.service.impl.FaultStatisticsServiceImpl;
import com.klzw.service.statistics.vo.FaultStatisticsVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * FaultStatisticsService单元测试类
 */
@ExtendWith(MockitoExtension.class)
class FaultStatisticsServiceTest {

    @InjectMocks
    private FaultStatisticsServiceImpl faultStatisticsService;

    @Mock
    private FaultStatisticsMapper faultStatisticsMapper;

    @Mock
    private VehicleClient vehicleClient;

    private FaultStatistics testFaultStatistics;
    private StatisticsQueryDTO testQueryDTO;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testFaultStatistics = new FaultStatistics();
        testFaultStatistics.setId(1L);
        testFaultStatistics.setVehicleId(1L);
        testFaultStatistics.setStatisticsDate(LocalDate.now());
        testFaultStatistics.setFaultCount(10);
        testFaultStatistics.setMinorFaultCount(4);
        testFaultStatistics.setMajorFaultCount(4);
        testFaultStatistics.setCriticalFaultCount(2);
        testFaultStatistics.setTotalRepairCost(new BigDecimal("5000.0"));
        testFaultStatistics.setAvgRepairTime(new BigDecimal("2.5"));
        testFaultStatistics.setTopFaultType("发动机故障");
        testFaultStatistics.setTopFaultCount(3);
        testFaultStatistics.setRepairedCount(7);
        testFaultStatistics.setPendingCount(3);
        testFaultStatistics.setCreateTime(LocalDateTime.now());
        testFaultStatistics.setUpdateTime(LocalDateTime.now());

        testQueryDTO = new StatisticsQueryDTO();
        testQueryDTO.setStartDate(LocalDate.now());
        testQueryDTO.setEndDate(LocalDate.now());
        testQueryDTO.setVehicleId(1L);
    }

    @AfterEach
    void tearDown() {
        reset(faultStatisticsMapper, vehicleClient);
    }

    /**
     * 测试获取故障统计
     */
    @Test
    void testGetFaultStatistics() {
        // Mock数据库查询
        when(faultStatisticsMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(testFaultStatistics);

        // 执行测试
        FaultStatisticsVO result = faultStatisticsService.getFaultStatistics(1L, LocalDate.now());

        // 验证结果
        assertNotNull(result);
        assertEquals(10, result.getFaultCount());
        assertEquals(new BigDecimal("5000.0"), result.getTotalRepairCost());
        verify(faultStatisticsMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试获取故障趋势
     */
    @Test
    void testGetFaultTrend() {
        // 准备多日数据
        FaultStatistics stats1 = new FaultStatistics();
        stats1.setId(1L);
        stats1.setStatisticsDate(LocalDate.now().minusDays(1));
        stats1.setFaultCount(8);
        stats1.setTotalRepairCost(new BigDecimal("3000.0"));

        FaultStatistics stats2 = new FaultStatistics();
        stats2.setId(2L);
        stats2.setStatisticsDate(LocalDate.now());
        stats2.setFaultCount(10);
        stats2.setTotalRepairCost(new BigDecimal("5000.0"));

        // Mock数据库查询
        when(faultStatisticsMapper.findByDateRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(stats1, stats2));

        // 执行测试
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now();
        List<FaultStatisticsVO.FaultTrendItem> result = faultStatisticsService.getFaultTrend(startDate, endDate);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(8, result.get(0).getFaultCount());
        assertEquals(10, result.get(1).getFaultCount());
        verify(faultStatisticsMapper, times(1)).findByDateRange(any(LocalDate.class), any(LocalDate.class));
    }

    /**
     * 测试按车辆获取故障
     */
    @Test
    void testGetFaultByVehicle() {
        // Mock数据库查询
        when(faultStatisticsMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(testFaultStatistics);

        // 执行测试
        FaultStatisticsVO result = faultStatisticsService.getFaultStatistics(1L, LocalDate.now());

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getVehicleId());
        assertEquals(10, result.getFaultCount());
        verify(faultStatisticsMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试计算故障统计
     */
    @Test
    void testCalculateFaultStatistics() {
        // Mock数据库查询和插入
        when(faultStatisticsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(faultStatisticsMapper.insert(any(FaultStatistics.class))).thenAnswer(invocation -> {
            FaultStatistics stats = invocation.getArgument(0);
            stats.setId(1L);
            return 1;
        });

        // Mock VehicleClient
        Map<String, Object> faultData = new HashMap<>();
        faultData.put("faultCount", 10);
        faultData.put("minorFaultCount", 4);
        faultData.put("majorFaultCount", 4);
        faultData.put("criticalFaultCount", 2);
        faultData.put("totalRepairCost", 5000.0);
        faultData.put("avgRepairTime", 2.5);
        faultData.put("topFaultType", "发动机故障");
        faultData.put("topFaultCount", 3);
        faultData.put("repairedCount", 7);
        faultData.put("pendingCount", 3);
        Result<Map<String, Object>> faultResult = Result.success(faultData);
        when(vehicleClient.getFaultStatistics(anyLong(), anyString())).thenReturn(faultResult);

        // 执行测试
        FaultStatisticsVO result = faultStatisticsService.calculateFaultStatistics(1L, LocalDate.now().toString());

        // 验证结果
        assertNotNull(result);
        assertEquals(10, result.getFaultCount());
        verify(faultStatisticsMapper, times(1)).insert(any(FaultStatistics.class));
    }

    /**
     * 测试获取故障统计列表
     */
    @Test
    void testGetFaultStatisticsList() {
        // Mock数据库查询
        when(faultStatisticsMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(testFaultStatistics));

        // 执行测试
        List<FaultStatisticsVO> result = faultStatisticsService.getFaultStatisticsList(testQueryDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getFaultCount());
        verify(faultStatisticsMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试获取总体故障统计
     */
    @Test
    void testGetOverallFaultStatistics() {
        // Mock数据库查询
        when(faultStatisticsMapper.findByDateRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(testFaultStatistics));

        // 执行测试
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        FaultStatisticsVO result = faultStatisticsService.getOverallFaultStatistics(startDate, endDate);

        // 验证结果
        assertNotNull(result);
        assertEquals(10, result.getTotalFaultCount());
        assertEquals(new BigDecimal("5000.0"), result.getTotalRepairCost());
        // getOverallFaultStatistics 内部调用了 getFaultTypeDistribution 和 getFaultTrend，它们各自又调用了 findByDateRange
        // 所以总共调用了 3 次
        verify(faultStatisticsMapper, times(3)).findByDateRange(any(LocalDate.class), any(LocalDate.class));
    }

    /**
     * 测试获取故障类型分布
     */
    @Test
    void testGetFaultTypeDistribution() {
        // 准备测试数据
        FaultStatistics stats1 = new FaultStatistics();
        stats1.setTopFaultType("发动机故障");
        stats1.setTopFaultCount(5);

        FaultStatistics stats2 = new FaultStatistics();
        stats2.setTopFaultType("刹车故障");
        stats2.setTopFaultCount(3);

        // Mock数据库查询
        when(faultStatisticsMapper.findByDateRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(stats1, stats2));

        // 执行测试
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        List<FaultStatisticsVO.FaultTypeDistribution> result = faultStatisticsService.getFaultTypeDistribution(startDate, endDate);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.size() > 0);
        verify(faultStatisticsMapper, times(1)).findByDateRange(any(LocalDate.class), any(LocalDate.class));
    }

    /**
     * 测试计算每日故障统计
     */
    @Test
    void testCalculateDailyFaultStatistics() {
        // Mock VehicleClient获取车辆列表
        List<Long> vehicleIds = Arrays.asList(1L, 2L, 3L);
        Result<List<Long>> vehicleResult = Result.success(vehicleIds);
        when(vehicleClient.getVehicleIds()).thenReturn(vehicleResult);

        // Mock数据库查询和插入
        when(faultStatisticsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(faultStatisticsMapper.insert(any(FaultStatistics.class))).thenReturn(1);

        // Mock VehicleClient获取故障统计
        Map<String, Object> faultData = new HashMap<>();
        faultData.put("faultCount", 5);
        Result<Map<String, Object>> faultResult = Result.success(faultData);
        when(vehicleClient.getFaultStatistics(anyLong(), anyString())).thenReturn(faultResult);

        // 执行测试
        faultStatisticsService.calculateDailyFaultStatistics(LocalDate.now());

        // 验证结果 - 应该为每个车辆调用一次
        verify(faultStatisticsMapper, times(3)).insert(any(FaultStatistics.class));
    }

    /**
     * 测试获取故障统计 - 无数据
     */
    @Test
    void testGetFaultStatisticsNoData() {
        // Mock数据库查询返回空
        when(faultStatisticsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // 执行测试
        FaultStatisticsVO result = faultStatisticsService.getFaultStatistics(999L, LocalDate.now());

        // 验证结果
        assertNull(result);
        verify(faultStatisticsMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试获取总体故障统计 - 无数据
     */
    @Test
    void testGetOverallFaultStatisticsNoData() {
        // Mock数据库查询返回空
        when(faultStatisticsMapper.findByDateRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // 执行测试
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        FaultStatisticsVO result = faultStatisticsService.getOverallFaultStatistics(startDate, endDate);

        // 验证结果 - 无数据时返回空的VO对象
        assertNotNull(result);
        // totalFaultCount 默认为 null，需要检查是否为 null 或 0
        assertTrue(result.getTotalFaultCount() == null || result.getTotalFaultCount() == 0);
        // 当列表为空时，直接返回新的 VO，不会调用 getFaultTypeDistribution 和 getFaultTrend
        verify(faultStatisticsMapper, times(1)).findByDateRange(any(LocalDate.class), any(LocalDate.class));
    }

    /**
     * 测试获取故障趋势 - 空数据
     */
    @Test
    void testGetFaultTrendEmpty() {
        // Mock数据库查询返回空
        when(faultStatisticsMapper.findByDateRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // 执行测试
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now();
        List<FaultStatisticsVO.FaultTrendItem> result = faultStatisticsService.getFaultTrend(startDate, endDate);

        // 验证结果 - 应该返回2天的数据，但故障数为0
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(0, result.get(0).getFaultCount());
        assertEquals(0, result.get(1).getFaultCount());
    }

    /**
     * 测试计算故障统计 - VehicleClient异常
     */
    @Test
    void testCalculateFaultStatisticsWithException() {
        // Mock数据库查询和插入
        when(faultStatisticsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(faultStatisticsMapper.insert(any(FaultStatistics.class))).thenAnswer(invocation -> {
            FaultStatistics stats = invocation.getArgument(0);
            stats.setId(1L);
            return 1;
        });

        // Mock VehicleClient抛出异常
        when(vehicleClient.getFaultStatistics(anyLong(), anyString()))
                .thenThrow(new RuntimeException("Vehicle service unavailable"));

        // 执行测试
        FaultStatisticsVO result = faultStatisticsService.calculateFaultStatistics(1L, LocalDate.now().toString());

        // 验证结果 - 应该使用默认值
        assertNotNull(result);
        assertEquals(0, result.getFaultCount());
        verify(faultStatisticsMapper, times(1)).insert(any(FaultStatistics.class));
    }

    /**
     * 测试获取故障统计列表 - 多条件查询
     */
    @Test
    void testGetFaultStatisticsListWithMultipleConditions() {
        // Mock数据库查询
        when(faultStatisticsMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(testFaultStatistics));

        // 执行测试
        StatisticsQueryDTO queryDTO = new StatisticsQueryDTO();
        queryDTO.setVehicleId(1L);
        queryDTO.setStartDate(LocalDate.now().minusDays(7));
        queryDTO.setEndDate(LocalDate.now());

        List<FaultStatisticsVO> result = faultStatisticsService.getFaultStatisticsList(queryDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(faultStatisticsMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }
}
