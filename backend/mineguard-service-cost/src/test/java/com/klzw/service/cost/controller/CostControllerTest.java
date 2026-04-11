package com.klzw.service.cost.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.klzw.service.cost.dto.CostBudgetDTO;
import com.klzw.service.cost.dto.CostDetailDTO;
import com.klzw.service.cost.dto.CostQueryDTO;
import com.klzw.service.cost.enums.BudgetStatusEnum;
import com.klzw.service.cost.enums.CostTypeEnum;
import com.klzw.service.cost.service.CostService;
import com.klzw.service.cost.vo.CostBudgetVO;
import com.klzw.service.cost.vo.CostDetailVO;
import com.klzw.service.cost.vo.CostStatisticsVO;
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
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CostController切片测试类
 */
@ExtendWith(MockitoExtension.class)
class CostControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CostService costService;

    @InjectMocks
    private CostController costController;

    private ObjectMapper objectMapper;
    private CostDetailVO testCostDetailVO;
    private CostBudgetVO testCostBudgetVO;
    private CostStatisticsVO testCostStatisticsVO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(costController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // 初始化测试成本明细VO
        testCostDetailVO = new CostDetailVO();
        testCostDetailVO.setId(1L);
        testCostDetailVO.setCostNo("COST20240101000001");
        testCostDetailVO.setCostType(CostTypeEnum.FUEL.getCode());
        testCostDetailVO.setCostTypeName("燃油成本");
        testCostDetailVO.setCostName("燃油成本");
        testCostDetailVO.setAmount(new BigDecimal("500.00"));
        testCostDetailVO.setVehicleId(1L);
        testCostDetailVO.setUserId(1L);
        testCostDetailVO.setCostDate(LocalDate.now());
        testCostDetailVO.setCreateTime(LocalDateTime.now());

        // 初始化测试预算VO
        testCostBudgetVO = new CostBudgetVO();
        testCostBudgetVO.setId(1L);
        testCostBudgetVO.setBudgetNo("BUDGET20240101000001");
        testCostBudgetVO.setBudgetName("2024年1月预算");
        testCostBudgetVO.setBudgetType(1);
        testCostBudgetVO.setBudgetTypeName("月度预算");
        testCostBudgetVO.setBudgetYear(2024);
        testCostBudgetVO.setBudgetMonth(1);
        testCostBudgetVO.setFuelBudget(new BigDecimal("10000.00"));
        testCostBudgetVO.setTotalBudget(new BigDecimal("35000.00"));
        testCostBudgetVO.setStatus(BudgetStatusEnum.DRAFT.getCode());
        testCostBudgetVO.setStatusName("草稿");
        testCostBudgetVO.setCreateTime(LocalDateTime.now());

        // 初始化测试统计VO
        testCostStatisticsVO = new CostStatisticsVO();
        testCostStatisticsVO.setStartDate(LocalDate.now().minusDays(7));
        testCostStatisticsVO.setEndDate(LocalDate.now());
        testCostStatisticsVO.setTotalAmount(new BigDecimal("5000.00"));
        testCostStatisticsVO.setRecordCount(10);
        testCostStatisticsVO.setFuelCost(new BigDecimal("2000.00"));
        testCostStatisticsVO.setMaintenanceCost(new BigDecimal("1000.00"));
    }

    /**
     * 测试添加成本明细
     */
    @Test
    void testAddCostDetail() throws Exception {
        when(costService.addCostDetail(any(CostDetailDTO.class))).thenReturn(testCostDetailVO);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("costType", CostTypeEnum.FUEL.getCode());
        requestBody.put("amount", 500.00);
        requestBody.put("vehicleId", 1);
        requestBody.put("costDate", LocalDate.now().toString());

        mockMvc.perform(post("/cost/detail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.costType").value(CostTypeEnum.FUEL.getCode()))
                .andExpect(jsonPath("$.data.amount").value(500.00));
    }

    /**
     * 测试更新成本明细
     */
    @Test
    void testUpdateCostDetail() throws Exception {
        when(costService.updateCostDetail(any(CostDetailDTO.class))).thenReturn(testCostDetailVO);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("id", 1);
        requestBody.put("amount", 600.00);

        mockMvc.perform(put("/cost/detail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试删除成本明细
     */
    @Test
    void testDeleteCostDetail() throws Exception {
        doNothing().when(costService).deleteCostDetail(1L);

        mockMvc.perform(delete("/cost/detail/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试获取成本明细详情
     */
    @Test
    void testGetCostDetail() throws Exception {
        when(costService.getCostDetail(1L)).thenReturn(testCostDetailVO);

        mockMvc.perform(get("/cost/detail/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.costTypeName").value("燃油成本"));
    }

    /**
     * 测试获取成本明细列表
     */
    @Test
    void testGetCostDetailList() throws Exception {
        when(costService.getCostDetailList(any(CostQueryDTO.class)))
                .thenReturn(Collections.singletonList(testCostDetailVO));

        mockMvc.perform(get("/cost/detail/list")
                        .param("vehicleId", "1")
                        .param("startDate", LocalDate.now().minusDays(7).toString())
                        .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    /**
     * 测试获取成本统计
     */
    @Test
    void testGetCostStatistics() throws Exception {
        when(costService.getCostStatistics(any(CostQueryDTO.class))).thenReturn(testCostStatisticsVO);

        mockMvc.perform(get("/cost/statistics")
                        .param("startDate", LocalDate.now().minusDays(7).toString())
                        .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalAmount").value(5000.00));
    }

    /**
     * 测试添加预算
     */
    @Test
    void testAddBudget() throws Exception {
        when(costService.addBudget(any(CostBudgetDTO.class))).thenReturn(testCostBudgetVO);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("budgetName", "2024年1月预算");
        requestBody.put("budgetType", 1);
        requestBody.put("budgetYear", 2024);
        requestBody.put("budgetMonth", 1);
        requestBody.put("fuelBudget", 10000.00);

        mockMvc.perform(post("/cost/budget")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.budgetName").value("2024年1月预算"));
    }

    /**
     * 测试更新预算
     */
    @Test
    void testUpdateBudget() throws Exception {
        when(costService.updateBudget(any(CostBudgetDTO.class))).thenReturn(testCostBudgetVO);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("id", 1);
        requestBody.put("fuelBudget", 12000.00);

        mockMvc.perform(put("/cost/budget")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试删除预算
     */
    @Test
    void testDeleteBudget() throws Exception {
        doNothing().when(costService).deleteBudget(1L);

        mockMvc.perform(delete("/cost/budget/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试获取预算详情
     */
    @Test
    void testGetBudget() throws Exception {
        when(costService.getBudget(1L)).thenReturn(testCostBudgetVO);

        mockMvc.perform(get("/cost/budget/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.budgetName").value("2024年1月预算"));
    }

    /**
     * 测试获取预算列表
     */
    @Test
    void testGetBudgetList() throws Exception {
        when(costService.getBudgetList(any(), any()))
                .thenReturn(Collections.singletonList(testCostBudgetVO));

        mockMvc.perform(get("/cost/budget/list")
                        .param("budgetType", "1")
                        .param("budgetYear", "2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    /**
     * 测试检查预算使用情况
     */
    @Test
    void testCheckBudgetUsage() throws Exception {
        Map<String, Object> usageData = new HashMap<>();
        usageData.put("budgetId", 1L);
        usageData.put("budgetName", "2024年1月预算");
        usageData.put("totalBudget", new BigDecimal("35000.00"));
        usageData.put("usedAmount", new BigDecimal("5000.00"));
        usageData.put("usageRate", new BigDecimal("14.29"));

        when(costService.checkBudgetUsage(1L)).thenReturn(usageData);

        mockMvc.perform(get("/cost/budget/1/usage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.budgetId").value(1));
    }

    /**
     * 测试生成成本报表
     */
    @Test
    void testGenerateCostReport() throws Exception {
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("startDate", LocalDate.now().minusDays(7));
        reportData.put("endDate", LocalDate.now());
        reportData.put("totalAmount", new BigDecimal("5000.00"));
        reportData.put("recordCount", 10);

        when(costService.generateCostReport(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(reportData);

        mockMvc.perform(get("/cost/report")
                        .param("startDate", LocalDate.now().minusDays(7).toString())
                        .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalAmount").value(5000.00));
    }

    /**
     * 测试获取预算预警列表
     */
    @Test
    void testGetBudgetAlerts() throws Exception {
        List<Map<String, Object>> alerts = new ArrayList<>();
        Map<String, Object> alert = new HashMap<>();
        alert.put("type", "NEAR_LIMIT");
        alert.put("level", "WARNING");
        alert.put("budgetId", 1L);
        alert.put("budgetName", "2024年1月预算");
        alert.put("message", "预算即将用尽");
        alerts.add(alert);

        when(costService.getBudgetAlerts()).thenReturn(alerts);

        mockMvc.perform(get("/cost/budget/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    /**
     * 测试获取成本趋势
     */
    @Test
    void testGetCostTrend() throws Exception {
        Map<String, Object> trendData = new HashMap<>();
        trendData.put("startDate", LocalDate.now().minusDays(7));
        trendData.put("endDate", LocalDate.now());
        trendData.put("granularity", "day");
        trendData.put("labels", Arrays.asList("2024-01-01", "2024-01-02"));
        trendData.put("values", Arrays.asList(500, 600));

        when(costService.getCostTrend(any(LocalDate.class), any(LocalDate.class), anyString()))
                .thenReturn(trendData);

        mockMvc.perform(get("/cost/trend")
                        .param("startDate", LocalDate.now().minusDays(7).toString())
                        .param("endDate", LocalDate.now().toString())
                        .param("granularity", "day"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.granularity").value("day"));
    }

    /**
     * 测试获取能耗分析
     */
    @Test
    void testGetEnergyConsumptionAnalysis() throws Exception {
        Map<String, Object> analysisData = new HashMap<>();
        analysisData.put("totalFuelCost", new BigDecimal("5000.00"));
        analysisData.put("totalFuelAmount", new BigDecimal("1000.00"));
        analysisData.put("totalCargoWeight", new BigDecimal("100.00"));
        analysisData.put("totalDistance", new BigDecimal("50000.00"));

        when(costService.getEnergyConsumptionAnalysis(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(analysisData);

        mockMvc.perform(get("/cost/analysis/energy")
                        .param("startDate", LocalDate.now().minusDays(7).toString())
                        .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalFuelCost").value(5000.00));
    }

    /**
     * 测试获取车辆利用率分析
     */
    @Test
    void testGetVehicleUtilizationAnalysis() throws Exception {
        Map<String, Object> analysisData = new HashMap<>();
        analysisData.put("totalVehicles", 10);
        analysisData.put("activeVehicles", 8);
        analysisData.put("attendanceRate", new BigDecimal("80.00"));
        analysisData.put("avgUtilizationRate", new BigDecimal("75.00"));

        when(costService.getVehicleUtilizationAnalysis(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(analysisData);

        mockMvc.perform(get("/cost/analysis/utilization")
                        .param("startDate", LocalDate.now().minusDays(7).toString())
                        .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalVehicles").value(10));
    }

    /**
     * 测试获取空载率分析
     */
    @Test
    void testGetIdleRateAnalysis() throws Exception {
        Map<String, Object> analysisData = new HashMap<>();
        analysisData.put("totalDistance", new BigDecimal("100000.00"));
        analysisData.put("loadedDistance", new BigDecimal("80000.00"));
        analysisData.put("emptyDistance", new BigDecimal("20000.00"));
        analysisData.put("idleRate", new BigDecimal("20.00"));

        when(costService.getIdleRateAnalysis(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(analysisData);

        mockMvc.perform(get("/cost/analysis/idle-rate")
                        .param("startDate", LocalDate.now().minusDays(7).toString())
                        .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.idleRate").value(20.00));
    }

    /**
     * 测试获取总体成本分析
     */
    @Test
    void testGetOverallCostAnalysis() throws Exception {
        Map<String, Object> analysisData = new HashMap<>();
        analysisData.put("totalCost", new BigDecimal("10000.00"));
        analysisData.put("fuelCost", new BigDecimal("5000.00"));
        analysisData.put("maintenanceCost", new BigDecimal("2000.00"));
        analysisData.put("laborCost", new BigDecimal("3000.00"));

        when(costService.getOverallCostAnalysis(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(analysisData);

        mockMvc.perform(get("/cost/analysis/overall")
                        .param("startDate", LocalDate.now().minusDays(7).toString())
                        .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalCost").value(10000.00));
    }

    /**
     * 测试记录Trip提成
     */
    @Test
    void testRecordTripCommission() throws Exception {
        when(costService.addCostDetail(any(CostDetailDTO.class))).thenReturn(testCostDetailVO);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("costType", CostTypeEnum.LABOR.getCode());
        requestBody.put("costName", "Trip任务提成");
        requestBody.put("amount", 800.00);
        requestBody.put("userId", 1);
        requestBody.put("tripId", 1);
        requestBody.put("remark", "Python评分=80, 提成比例=80%");

        mockMvc.perform(post("/cost/commission")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
