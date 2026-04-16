package com.klzw.service.cost.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.klzw.common.core.client.PythonClient;
import com.klzw.common.core.client.StatisticsClient;
import com.klzw.common.core.client.TransportClient;
import com.klzw.common.core.client.TripClient;
import com.klzw.common.core.client.VehicleClient;
import com.klzw.common.core.result.Result;
import com.klzw.service.cost.dto.CostBudgetDTO;
import com.klzw.service.cost.dto.CostDetailDTO;
import com.klzw.service.cost.dto.CostQueryDTO;
import com.klzw.service.cost.entity.CostBudget;
import com.klzw.service.cost.entity.CostDetail;
import com.klzw.service.cost.enums.BudgetStatusEnum;
import com.klzw.service.cost.enums.CostTypeEnum;
import com.klzw.service.cost.mapper.CostBudgetMapper;
import com.klzw.service.cost.mapper.CostDetailMapper;
import com.klzw.service.cost.service.impl.CostServiceImpl;
import com.klzw.service.cost.vo.CostBudgetVO;
import com.klzw.service.cost.vo.CostDetailVO;
import com.klzw.service.cost.vo.CostStatisticsVO;
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
 * CostService单元测试类
 */
@ExtendWith(MockitoExtension.class)
class CostServiceTest {

    @InjectMocks
    private CostServiceImpl costService;

    @Mock
    private CostDetailMapper costDetailMapper;

    @Mock
    private CostBudgetMapper costBudgetMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private PythonClient pythonClient;

    @Mock
    private TransportClient transportClient;

    @Mock
    private TripClient tripClient;

    @Mock
    private VehicleClient vehicleClient;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private CostDetail testCostDetail;
    private CostBudget testCostBudget;

    @BeforeEach
    void setUp() {
        // 初始化测试成本明细数据
        testCostDetail = new CostDetail();
        testCostDetail.setId(1L);
        testCostDetail.setCostNo("COST20240101000001");
        testCostDetail.setCostType(CostTypeEnum.FUEL.getCode());
        testCostDetail.setCostName("燃油成本");
        testCostDetail.setAmount(new BigDecimal("500.00"));
        testCostDetail.setVehicleId(1L);
        testCostDetail.setUserId(1L);
        testCostDetail.setCostDate(LocalDate.now());
        testCostDetail.setCreateTime(LocalDateTime.now());
        testCostDetail.setUpdateTime(LocalDateTime.now());
        testCostDetail.setDeleted(0);

        // 初始化测试预算数据
        testCostBudget = new CostBudget();
        testCostBudget.setId(1L);
        testCostBudget.setBudgetNo("BUDGET20240101000001");
        testCostBudget.setBudgetName("2024年1月预算");
        testCostBudget.setBudgetType(1);
        testCostBudget.setBudgetYear(2024);
        testCostBudget.setBudgetMonth(1);
        testCostBudget.setFuelBudget(new BigDecimal("10000.00"));
        testCostBudget.setMaintenanceBudget(new BigDecimal("5000.00"));
        testCostBudget.setLaborBudget(new BigDecimal("20000.00"));
        testCostBudget.setTotalBudget(new BigDecimal("35000.00"));
        testCostBudget.setStatus(BudgetStatusEnum.DRAFT.getCode());
        testCostBudget.setCreateTime(LocalDateTime.now());
        testCostBudget.setUpdateTime(LocalDateTime.now());
        testCostBudget.setDeleted(0);
    }

    @AfterEach
    void tearDown() {
        reset(costDetailMapper, costBudgetMapper, redisTemplate, pythonClient, transportClient, tripClient, vehicleClient);
    }

    /**
     * 测试添加成本明细
     */
    @Test
    void testAddCostDetail() {
        CostDetailDTO dto = new CostDetailDTO();
        dto.setCostType(CostTypeEnum.FUEL.getCode());
        dto.setAmount(new BigDecimal("500.00"));
        dto.setVehicleId(1L);
        dto.setCostDate(LocalDate.now());

        when(costDetailMapper.insert(any(CostDetail.class))).thenAnswer(invocation -> {
            CostDetail detail = invocation.getArgument(0);
            detail.setId(1L);
            return 1;
        });

        CostDetailVO result = costService.addCostDetail(dto);

        assertNotNull(result);
        assertEquals(CostTypeEnum.FUEL.getCode(), result.getCostType());
        assertEquals(new BigDecimal("500.00"), result.getAmount());
        verify(costDetailMapper, times(1)).insert(any(CostDetail.class));
    }

    /**
     * 测试更新成本明细
     */
    @Test
    void testUpdateCostDetail() {
        CostDetailDTO dto = new CostDetailDTO();
        dto.setId(1L);
        dto.setAmount(new BigDecimal("600.00"));

        when(costDetailMapper.selectById(1L)).thenReturn(testCostDetail);
        when(costDetailMapper.updateById(any(CostDetail.class))).thenReturn(1);

        CostDetailVO result = costService.updateCostDetail(dto);

        assertNotNull(result);
        verify(costDetailMapper, times(1)).selectById(1L);
        verify(costDetailMapper, times(1)).updateById(any(CostDetail.class));
    }

    /**
     * 测试更新不存在的成本明细
     */
    @Test
    void testUpdateCostDetailNotFound() {
        CostDetailDTO dto = new CostDetailDTO();
        dto.setId(999L);

        when(costDetailMapper.selectById(999L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            costService.updateCostDetail(dto);
        });
    }

    /**
     * 测试删除成本明细
     */
    @Test
    void testDeleteCostDetail() {
        when(costDetailMapper.deleteById(1L)).thenReturn(1);

        costService.deleteCostDetail(1L);

        verify(costDetailMapper, times(1)).deleteById(1L);
    }

    /**
     * 测试获取成本明细
     */
    @Test
    void testGetCostDetail() {
        when(costDetailMapper.selectById(1L)).thenReturn(testCostDetail);

        CostDetailVO result = costService.getCostDetail(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("燃油成本", result.getCostTypeName());
        verify(costDetailMapper, times(1)).selectById(1L);
    }

    /**
     * 测试获取不存在的成本明细
     */
    @Test
    void testGetCostDetailNotFound() {
        when(costDetailMapper.selectById(999L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            costService.getCostDetail(999L);
        });
    }

    /**
     * 测试获取成本明细列表
     */
    @Test
    void testGetCostDetailList() {
        List<CostDetail> costDetails = Collections.singletonList(testCostDetail);

        when(costDetailMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(costDetails);

        CostQueryDTO queryDTO = new CostQueryDTO();
        queryDTO.setVehicleId(1L);

        List<CostDetailVO> result = costService.getCostDetailList(queryDTO);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getVehicleId());
    }

    /**
     * 测试获取成本统计
     */
    @Test
    void testGetCostStatistics() {
        List<CostDetail> costDetails = new ArrayList<>();
        costDetails.add(testCostDetail);

        CostDetail costDetail2 = new CostDetail();
        costDetail2.setId(2L);
        costDetail2.setCostType(CostTypeEnum.MAINTENANCE.getCode());
        costDetail2.setAmount(new BigDecimal("300.00"));
        costDetail2.setCostDate(LocalDate.now());
        costDetails.add(costDetail2);

        when(costDetailMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(costDetails);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        CostQueryDTO queryDTO = new CostQueryDTO();
        queryDTO.setStartDate(LocalDate.now());
        queryDTO.setEndDate(LocalDate.now());

        CostStatisticsVO result = costService.getCostStatistics(queryDTO);

        assertNotNull(result);
        assertEquals(new BigDecimal("800.00"), result.getTotalAmount());
        assertEquals(2, result.getRecordCount());
    }

    /**
     * 测试获取成本统计带缓存
     */
    @Test
    void testGetCostStatisticsWithCache() {
        CostStatisticsVO cachedVO = new CostStatisticsVO();
        cachedVO.setTotalAmount(new BigDecimal("1000.00"));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(cachedVO);

        CostQueryDTO queryDTO = new CostQueryDTO();
        queryDTO.setStartDate(LocalDate.now());
        queryDTO.setEndDate(LocalDate.now());

        CostStatisticsVO result = costService.getCostStatistics(queryDTO);

        assertNotNull(result);
        assertEquals(new BigDecimal("1000.00"), result.getTotalAmount());
    }

    /**
     * 测试添加预算
     */
    @Test
    void testAddBudget() {
        CostBudgetDTO dto = new CostBudgetDTO();
        dto.setBudgetName("2024年1月预算");
        dto.setBudgetType(1);
        dto.setBudgetYear(2024);
        dto.setBudgetMonth(1);
        dto.setFuelBudget(new BigDecimal("10000.00"));

        when(costBudgetMapper.insert(any(CostBudget.class))).thenAnswer(invocation -> {
            CostBudget budget = invocation.getArgument(0);
            budget.setId(1L);
            return 1;
        });

        CostBudgetVO result = costService.addBudget(dto);

        assertNotNull(result);
        assertEquals("2024年1月预算", result.getBudgetName());
        assertEquals(BudgetStatusEnum.DRAFT.getCode(), result.getStatus());
        verify(costBudgetMapper, times(1)).insert(any(CostBudget.class));
    }

    /**
     * 测试更新预算
     */
    @Test
    void testUpdateBudget() {
        CostBudgetDTO dto = new CostBudgetDTO();
        dto.setId(1L);
        dto.setFuelBudget(new BigDecimal("12000.00"));

        when(costBudgetMapper.selectById(1L)).thenReturn(testCostBudget);
        when(costBudgetMapper.updateById(any(CostBudget.class))).thenReturn(1);

        CostBudgetVO result = costService.updateBudget(dto);

        assertNotNull(result);
        verify(costBudgetMapper, times(1)).selectById(1L);
        verify(costBudgetMapper, times(1)).updateById(any(CostBudget.class));
    }

    /**
     * 测试更新不存在的预算
     */
    @Test
    void testUpdateBudgetNotFound() {
        CostBudgetDTO dto = new CostBudgetDTO();
        dto.setId(999L);

        when(costBudgetMapper.selectById(999L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            costService.updateBudget(dto);
        });
    }

    /**
     * 测试删除预算
     */
    @Test
    void testDeleteBudget() {
        when(costBudgetMapper.deleteById(1L)).thenReturn(1);

        costService.deleteBudget(1L);

        verify(costBudgetMapper, times(1)).deleteById(1L);
    }

    /**
     * 测试获取预算
     */
    @Test
    void testGetBudget() {
        when(costBudgetMapper.selectById(1L)).thenReturn(testCostBudget);

        CostBudgetVO result = costService.getBudget(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("2024年1月预算", result.getBudgetName());
        verify(costBudgetMapper, times(1)).selectById(1L);
    }

    /**
     * 测试获取不存在的预算
     */
    @Test
    void testGetBudgetNotFound() {
        when(costBudgetMapper.selectById(999L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            costService.getBudget(999L);
        });
    }

    /**
     * 测试获取预算列表
     */
    @Test
    void testGetBudgetList() {
        List<CostBudget> budgets = Collections.singletonList(testCostBudget);

        when(costBudgetMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(budgets);

        List<CostBudgetVO> result = costService.getBudgetList(1, 2024);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("2024年1月预算", result.get(0).getBudgetName());
    }

    /**
     * 测试检查预算使用情况
     */
    @Test
    void testCheckBudgetUsage() {
        testCostBudget.setBudgetYear(2024);
        testCostBudget.setBudgetMonth(1);

        List<CostDetail> costs = new ArrayList<>();
        CostDetail cost1 = new CostDetail();
        cost1.setAmount(new BigDecimal("3000.00"));
        cost1.setCostType(CostTypeEnum.FUEL.getCode());
        costs.add(cost1);

        CostDetail cost2 = new CostDetail();
        cost2.setAmount(new BigDecimal("2000.00"));
        cost2.setCostType(CostTypeEnum.MAINTENANCE.getCode());
        costs.add(cost2);

        when(costBudgetMapper.selectById(1L)).thenReturn(testCostBudget);
        when(costDetailMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(costs);

        Map<String, Object> result = costService.checkBudgetUsage(1L);

        assertNotNull(result);
        assertEquals(1L, result.get("budgetId"));
        assertEquals(new BigDecimal("5000.00"), result.get("usedAmount"));
        assertNotNull(result.get("usageRate"));
    }

    /**
     * 测试生成成本报表
     */
    @Test
    void testGenerateCostReport() {
        List<CostDetail> costs = Collections.singletonList(testCostDetail);

        when(costDetailMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(costs);

        Map<String, Object> result = costService.generateCostReport(
            LocalDate.now().minusDays(7),
            LocalDate.now()
        );

        assertNotNull(result);
        assertEquals(new BigDecimal("500.00"), result.get("totalAmount"));
        assertEquals(1, result.get("recordCount"));
    }

    /**
     * 测试获取预算预警列表
     */
    @Test
    void testGetBudgetAlerts() {
        testCostBudget.setStatus(BudgetStatusEnum.ACTIVE.getCode());

        List<CostBudget> budgets = Collections.singletonList(testCostBudget);

        when(costBudgetMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(budgets);
        when(costBudgetMapper.selectById(1L)).thenReturn(testCostBudget);
        when(costDetailMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        List<Map<String, Object>> result = costService.getBudgetAlerts();

        assertNotNull(result);
        // 由于没有成本记录，使用率为0，不会触发预警
    }

    /**
     * 测试获取成本趋势
     */
    @Test
    void testGetCostTrend() {
        List<CostDetail> costs = new ArrayList<>();
        costs.add(testCostDetail);

        CostDetail cost2 = new CostDetail();
        cost2.setId(2L);
        cost2.setCostType(CostTypeEnum.MAINTENANCE.getCode());
        cost2.setAmount(new BigDecimal("300.00"));
        cost2.setCostDate(LocalDate.now());
        costs.add(cost2);

        when(costDetailMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(costs);

        Map<String, Object> result = costService.getCostTrend(
            LocalDate.now().minusDays(7),
            LocalDate.now(),
            "day"
        );

        assertNotNull(result);
        assertNotNull(result.get("trendData"));
        assertNotNull(result.get("labels"));
        assertNotNull(result.get("values"));
    }

    /**
     * 测试获取能耗分析
     */
    @Test
    void testGetEnergyConsumptionAnalysis() {
        List<CostDetail> fuelCosts = Collections.singletonList(testCostDetail);

        when(costDetailMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(fuelCosts);

        // Mock transport client
        Map<String, Object> transportData = new HashMap<>();
        transportData.put("totalCargoWeight", 100);
        Result<Map<String, Object>> transportResult = Result.success(transportData);
        when(transportClient.getTransportStatistics(anyString(), anyString())).thenReturn(transportResult);

        // Mock trip client
        Map<String, Object> tripData = new HashMap<>();
        tripData.put("totalDistance", 50000);
        Result<Map<String, Object>> tripResult = Result.success(tripData);
        when(tripClient.getStatistics(anyString(), anyString())).thenReturn(tripResult);

        Map<String, Object> result = costService.getEnergyConsumptionAnalysis(
            LocalDate.now().minusDays(7),
            LocalDate.now()
        );

        assertNotNull(result);
        assertEquals(new BigDecimal("500.00"), result.get("totalFuelCost"));
    }

    /**
     * 测试获取车辆利用率分析
     */
    @Test
    void testGetVehicleUtilizationAnalysis() {
        // Mock vehicle client
        Result<Integer> vehicleCountResult = Result.success(10);
        when(vehicleClient.getVehicleCount()).thenReturn(vehicleCountResult);

        // Mock transport client
        List<Map<String, Object>> vehicleStats = new ArrayList<>();
        Result<List<Map<String, Object>>> transportResult = Result.success(vehicleStats);
        when(transportClient.getVehicleStatistics(anyString(), anyString())).thenReturn(transportResult);

        Map<String, Object> result = costService.getVehicleUtilizationAnalysis(
            LocalDate.now().minusDays(7),
            LocalDate.now()
        );

        assertNotNull(result);
        assertEquals(10, result.get("totalVehicles"));
    }

    /**
     * 测试获取空载率分析
     */
    @Test
    void testGetIdleRateAnalysis() {
        // Mock trip client
        Map<String, Object> tripData = new HashMap<>();
        tripData.put("totalDistance", 100000);
        tripData.put("loadedDistance", 80000);
        tripData.put("emptyDistance", 20000);
        tripData.put("totalTrips", 100);
        tripData.put("loadedTrips", 80);
        tripData.put("emptyTrips", 20);
        Result<Map<String, Object>> tripResult = Result.success(tripData);
        when(tripClient.getStatistics(anyString(), anyString())).thenReturn(tripResult);

        Map<String, Object> result = costService.getIdleRateAnalysis(
            LocalDate.now().minusDays(7),
            LocalDate.now()
        );

        assertNotNull(result);
        assertEquals(new BigDecimal("100000"), result.get("totalDistance"));
        assertEquals(new BigDecimal("20.0000"), result.get("idleRate"));
    }

    /**
     * 测试获取总体成本分析
     */
    @Test
    void testGetOverallCostAnalysis() {
        List<CostDetail> costs = Collections.singletonList(testCostDetail);

        when(costDetailMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(costs);
        when(costBudgetMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        // Mock transport client
        Map<String, Object> transportData = new HashMap<>();
        transportData.put("totalCargoWeight", 100);
        Result<Map<String, Object>> transportResult = Result.success(transportData);
        when(transportClient.getTransportStatistics(anyString(), anyString())).thenReturn(transportResult);

        // Mock trip client
        Map<String, Object> tripData = new HashMap<>();
        tripData.put("totalDistance", 50000);
        Result<Map<String, Object>> tripResult = Result.success(tripData);
        when(tripClient.getStatistics(anyString(), anyString())).thenReturn(tripResult);

        // Mock vehicle client
        Result<Integer> vehicleCountResult = Result.success(10);
        when(vehicleClient.getVehicleCount()).thenReturn(vehicleCountResult);

        Result<List<Map<String, Object>>> vehicleStatsResult = Result.success(Collections.emptyList());
        when(transportClient.getVehicleStatistics(anyString(), anyString())).thenReturn(vehicleStatsResult);

        Map<String, Object> result = costService.getOverallCostAnalysis(
            LocalDate.now().minusDays(7),
            LocalDate.now()
        );

        assertNotNull(result);
        assertNotNull(result.get("energyAnalysis"));
        assertNotNull(result.get("utilizationAnalysis"));
        assertNotNull(result.get("idleAnalysis"));
        assertNotNull(result.get("costReport"));
    }

    /**
     * 测试计算并记录Trip提成
     */
    @Test
    void testCalculateAndRecordTripCommission() {
        when(pythonClient.analyzeDrivingBehavior(1L)).thenReturn(Result.success(80));
        when(costDetailMapper.insert(any(CostDetail.class))).thenAnswer(invocation -> {
            CostDetail detail = invocation.getArgument(0);
            detail.setId(100L);
            return 1;
        });

        Long result = costService.calculateAndRecordTripCommission(1L, 1L, 1000.0);

        assertNotNull(result);
        assertEquals(100L, result);
        verify(pythonClient, times(1)).analyzeDrivingBehavior(1L);
        verify(costDetailMapper, times(1)).insert(any(CostDetail.class));
    }

    /**
     * 测试计算并记录Trip提成失败时使用默认分数
     */
    @Test
    void testCalculateAndRecordTripCommissionWithDefaultScore() {
        when(pythonClient.analyzeDrivingBehavior(1L)).thenThrow(new RuntimeException("Python服务异常"));
        when(costDetailMapper.insert(any(CostDetail.class))).thenAnswer(invocation -> {
            CostDetail detail = invocation.getArgument(0);
            detail.setId(100L);
            return 1;
        });

        Long result = costService.calculateAndRecordTripCommission(1L, 1L, 1000.0);

        assertNotNull(result);
        assertEquals(100L, result);
        // 默认分数为60，提成应为 1000 * 0.6 = 600
        verify(pythonClient, times(1)).analyzeDrivingBehavior(1L);
    }
}
