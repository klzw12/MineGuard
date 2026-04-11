package com.klzw.service.cost;

import com.klzw.common.core.config.DotenvInitializer;
import com.klzw.service.cost.dto.CostBudgetDTO;
import com.klzw.service.cost.dto.CostDetailDTO;
import com.klzw.service.cost.dto.CostQueryDTO;
import com.klzw.service.cost.entity.CostBudget;
import com.klzw.service.cost.entity.CostDetail;
import com.klzw.service.cost.enums.BudgetStatusEnum;
import com.klzw.service.cost.enums.CostTypeEnum;
import com.klzw.service.cost.mapper.CostBudgetMapper;
import com.klzw.service.cost.mapper.CostDetailMapper;
import com.klzw.service.cost.service.CostService;
import com.klzw.service.cost.vo.CostBudgetVO;
import com.klzw.service.cost.vo.CostDetailVO;
import com.klzw.service.cost.vo.CostStatisticsVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CostService集成测试类
 */
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = DotenvInitializer.class)
@Transactional
@Tag("integration")
class CostIntegrationTest {

    @Autowired
    private CostService costService;

    @Autowired
    private CostDetailMapper costDetailMapper;

    @Autowired
    private CostBudgetMapper costBudgetMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private CostDetailDTO testCostDetailDTO;
    private CostBudgetDTO testCostBudgetDTO;

    @BeforeEach
    void setUp() {
        // 使用truncate table清理测试数据，避免数据饱满
        jdbcTemplate.execute("TRUNCATE TABLE cost_detail");
        jdbcTemplate.execute("TRUNCATE TABLE cost_budget");
        
        // 初始化测试成本明细DTO
        testCostDetailDTO = new CostDetailDTO();
        testCostDetailDTO.setCostType(CostTypeEnum.FUEL.getCode());
        testCostDetailDTO.setAmount(new BigDecimal("500.00"));
        testCostDetailDTO.setVehicleId(1L);
        testCostDetailDTO.setUserId(1L);
        testCostDetailDTO.setCostDate(LocalDate.now());
        testCostDetailDTO.setPaymentMethod("现金");
        testCostDetailDTO.setDescription("测试燃油成本");

        // 初始化测试预算DTO
        testCostBudgetDTO = new CostBudgetDTO();
        testCostBudgetDTO.setBudgetName("2024年1月测试预算");
        testCostBudgetDTO.setBudgetType(1);
        testCostBudgetDTO.setBudgetYear(2024);
        testCostBudgetDTO.setBudgetMonth(1);
        testCostBudgetDTO.setFuelBudget(new BigDecimal("10000.00"));
        testCostBudgetDTO.setMaintenanceBudget(new BigDecimal("5000.00"));
        testCostBudgetDTO.setLaborBudget(new BigDecimal("20000.00"));
    }

    /**
     * 测试添加成本明细完整流程
     */
    @Test
    void testAddCostDetailFlow() {
        CostDetailVO result = costService.addCostDetail(testCostDetailDTO);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getCostNo());
        assertEquals(CostTypeEnum.FUEL.getCode(), result.getCostType());
        assertEquals("燃油成本", result.getCostTypeName());
        assertEquals(new BigDecimal("500.00"), result.getAmount());

        // 验证数据库中的记录
        CostDetail savedDetail = costDetailMapper.selectById(result.getId());
        assertNotNull(savedDetail);
        assertEquals(CostTypeEnum.FUEL.getCode(), savedDetail.getCostType());
    }

    /**
     * 测试更新成本明细流程
     */
    @Test
    void testUpdateCostDetailFlow() {
        // 先创建成本明细
        CostDetailVO created = costService.addCostDetail(testCostDetailDTO);
        Long detailId = created.getId();

        // 更新成本明细
        CostDetailDTO updateDTO = new CostDetailDTO();
        updateDTO.setId(detailId);
        updateDTO.setAmount(new BigDecimal("600.00"));
        updateDTO.setDescription("更新后的描述");

        CostDetailVO updated = costService.updateCostDetail(updateDTO);

        assertNotNull(updated);
        assertEquals(new BigDecimal("600.00"), updated.getAmount());

        // 验证数据库中的更新
        CostDetail savedDetail = costDetailMapper.selectById(detailId);
        assertNotNull(savedDetail);
        assertEquals(new BigDecimal("600.00"), savedDetail.getAmount());
    }

    /**
     * 测试删除成本明细流程
     */
    @Test
    void testDeleteCostDetailFlow() {
        // 先创建成本明细
        CostDetailVO created = costService.addCostDetail(testCostDetailDTO);
        Long detailId = created.getId();

        // 删除成本明细
        costService.deleteCostDetail(detailId);

        // 验证删除（MyBatis Plus的逻辑删除）
        CostDetail deletedDetail = costDetailMapper.selectById(detailId);
        assertNull(deletedDetail);
    }

    /**
     * 测试获取成本明细列表流程
     */
    @Test
    void testGetCostDetailListFlow() {
        // 创建多个成本明细
        for (int i = 0; i < 5; i++) {
            CostDetailDTO dto = new CostDetailDTO();
            dto.setCostType(CostTypeEnum.FUEL.getCode());
            dto.setAmount(new BigDecimal("100.00").multiply(new BigDecimal(i + 1)));
            dto.setVehicleId(1L);
            dto.setCostDate(LocalDate.now().minusDays(i));
            costService.addCostDetail(dto);
        }

        // 查询成本明细列表
        CostQueryDTO queryDTO = new CostQueryDTO();
        queryDTO.setVehicleId(1L);

        List<CostDetailVO> result = costService.getCostDetailList(queryDTO);

        assertNotNull(result);
        assertTrue(result.size() >= 5);
    }

    /**
     * 测试获取成本统计流程
     */
    @Test
    void testGetCostStatisticsFlow() {
        // 创建不同类型的成本明细
        CostDetailDTO fuelDTO = new CostDetailDTO();
        fuelDTO.setCostType(CostTypeEnum.FUEL.getCode());
        fuelDTO.setAmount(new BigDecimal("1000.00"));
        fuelDTO.setCostDate(LocalDate.now());
        costService.addCostDetail(fuelDTO);

        CostDetailDTO maintenanceDTO = new CostDetailDTO();
        maintenanceDTO.setCostType(CostTypeEnum.MAINTENANCE.getCode());
        maintenanceDTO.setAmount(new BigDecimal("500.00"));
        maintenanceDTO.setCostDate(LocalDate.now());
        costService.addCostDetail(maintenanceDTO);

        // 查询成本统计
        CostQueryDTO queryDTO = new CostQueryDTO();
        queryDTO.setStartDate(LocalDate.now());
        queryDTO.setEndDate(LocalDate.now());

        CostStatisticsVO result = costService.getCostStatistics(queryDTO);

        assertNotNull(result);
        assertEquals(new BigDecimal("1500.00"), result.getTotalAmount());
        assertEquals(2, result.getRecordCount());
        assertNotNull(result.getTypeAmountMap());
    }

    /**
     * 测试添加预算完整流程
     */
    @Test
    void testAddBudgetFlow() {
        CostBudgetVO result = costService.addBudget(testCostBudgetDTO);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getBudgetNo());
        assertEquals("2024年1月测试预算", result.getBudgetName());
        assertEquals(BudgetStatusEnum.DRAFT.getCode(), result.getStatus());
        assertEquals(new BigDecimal("35000.00"), result.getTotalBudget());

        // 验证数据库中的记录
        CostBudget savedBudget = costBudgetMapper.selectById(result.getId());
        assertNotNull(savedBudget);
        assertEquals("2024年1月测试预算", savedBudget.getBudgetName());
    }

    /**
     * 测试更新预算流程
     */
    @Test
    void testUpdateBudgetFlow() {
        // 先创建预算
        CostBudgetVO created = costService.addBudget(testCostBudgetDTO);
        Long budgetId = created.getId();

        // 更新预算
        CostBudgetDTO updateDTO = new CostBudgetDTO();
        updateDTO.setId(budgetId);
        updateDTO.setFuelBudget(new BigDecimal("12000.00"));

        CostBudgetVO updated = costService.updateBudget(updateDTO);

        assertNotNull(updated);
        assertEquals(new BigDecimal("37000.00"), updated.getTotalBudget());

        // 验证数据库中的更新
        CostBudget savedBudget = costBudgetMapper.selectById(budgetId);
        assertNotNull(savedBudget);
        assertEquals(new BigDecimal("12000.00"), savedBudget.getFuelBudget());
    }

    /**
     * 测试删除预算流程
     */
    @Test
    void testDeleteBudgetFlow() {
        // 先创建预算
        CostBudgetVO created = costService.addBudget(testCostBudgetDTO);
        Long budgetId = created.getId();

        // 删除预算
        costService.deleteBudget(budgetId);

        // 验证删除
        CostBudget deletedBudget = costBudgetMapper.selectById(budgetId);
        assertNull(deletedBudget);
    }

    /**
     * 测试获取预算列表流程
     */
    @Test
    void testGetBudgetListFlow() {
        // 创建多个预算
        for (int i = 0; i < 3; i++) {
            CostBudgetDTO dto = new CostBudgetDTO();
            dto.setBudgetName("测试预算" + i);
            dto.setBudgetType(1);
            dto.setBudgetYear(2024);
            dto.setBudgetMonth(i + 1);
            dto.setFuelBudget(new BigDecimal("10000.00"));
            costService.addBudget(dto);
        }

        // 查询预算列表
        List<CostBudgetVO> result = costService.getBudgetList(1, 2024);

        assertNotNull(result);
        assertTrue(result.size() >= 3);
    }

    /**
     * 测试检查预算使用情况流程
     */
    @Test
    void testCheckBudgetUsageFlow() {
        // 创建预算
        CostBudgetVO budget = costService.addBudget(testCostBudgetDTO);
        Long budgetId = budget.getId();

        // 创建一些成本明细
        CostDetailDTO costDTO = new CostDetailDTO();
        costDTO.setCostType(CostTypeEnum.FUEL.getCode());
        costDTO.setAmount(new BigDecimal("3000.00"));
        costDTO.setCostDate(LocalDate.of(2024, 1, 15));
        costService.addCostDetail(costDTO);

        // 检查预算使用情况
        Map<String, Object> usage = costService.checkBudgetUsage(budgetId);

        assertNotNull(usage);
        assertEquals(budgetId, usage.get("budgetId"));
        assertNotNull(usage.get("usedAmount"));
        assertNotNull(usage.get("usageRate"));
    }

    /**
     * 测试生成成本报表流程
     */
    @Test
    void testGenerateCostReportFlow() {
        // 创建一些成本明细
        for (int i = 0; i < 5; i++) {
            CostDetailDTO dto = new CostDetailDTO();
            dto.setCostType(i % 2 == 0 ? CostTypeEnum.FUEL.getCode() : CostTypeEnum.MAINTENANCE.getCode());
            dto.setAmount(new BigDecimal("100.00"));
            dto.setCostDate(LocalDate.now().minusDays(i));
            costService.addCostDetail(dto);
        }

        // 生成成本报表
        Map<String, Object> report = costService.generateCostReport(
            LocalDate.now().minusDays(7),
            LocalDate.now()
        );

        assertNotNull(report);
        assertNotNull(report.get("totalAmount"));
        assertNotNull(report.get("recordCount"));
        assertNotNull(report.get("typeAmountMap"));
    }

    /**
     * 测试获取成本趋势流程
     */
    @Test
    void testGetCostTrendFlow() {
        // 创建一些成本明细
        for (int i = 0; i < 7; i++) {
            CostDetailDTO dto = new CostDetailDTO();
            dto.setCostType(CostTypeEnum.FUEL.getCode());
            dto.setAmount(new BigDecimal("100.00"));
            dto.setCostDate(LocalDate.now().minusDays(i));
            costService.addCostDetail(dto);
        }

        // 获取成本趋势
        Map<String, Object> trend = costService.getCostTrend(
            LocalDate.now().minusDays(7),
            LocalDate.now(),
            "day"
        );

        assertNotNull(trend);
        assertNotNull(trend.get("trendData"));
        assertNotNull(trend.get("labels"));
        assertNotNull(trend.get("values"));
    }

    /**
     * 测试完整业务流程：创建成本-创建预算-检查使用情况
     */
    @Test
    void testCompleteBusinessFlow() {
        // 1. 创建预算
        CostBudgetVO budget = costService.addBudget(testCostBudgetDTO);
        Long budgetId = budget.getId();
        assertNotNull(budgetId);
        assertEquals(BudgetStatusEnum.DRAFT.getCode(), budget.getStatus());

        // 2. 创建成本明细
        CostDetailVO costDetail = costService.addCostDetail(testCostDetailDTO);
        assertNotNull(costDetail.getId());
        assertEquals(new BigDecimal("500.00"), costDetail.getAmount());

        // 3. 查询成本统计
        CostQueryDTO queryDTO = new CostQueryDTO();
        queryDTO.setStartDate(LocalDate.now());
        queryDTO.setEndDate(LocalDate.now());
        CostStatisticsVO statistics = costService.getCostStatistics(queryDTO);
        assertNotNull(statistics);
        assertTrue(statistics.getTotalAmount().compareTo(BigDecimal.ZERO) > 0);

        // 4. 检查预算使用情况
        Map<String, Object> usage = costService.checkBudgetUsage(budgetId);
        assertNotNull(usage);
        assertNotNull(usage.get("usedAmount"));

        // 5. 生成成本报表
        Map<String, Object> report = costService.generateCostReport(
            LocalDate.now().minusDays(7),
            LocalDate.now()
        );
        assertNotNull(report);
        assertNotNull(report.get("totalAmount"));
    }

    /**
     * 测试多类型成本统计
     */
    @Test
    void testMultipleCostTypesStatistics() {
        // 创建不同类型的成本
        CostDetailDTO fuelDTO = new CostDetailDTO();
        fuelDTO.setCostType(CostTypeEnum.FUEL.getCode());
        fuelDTO.setAmount(new BigDecimal("2000.00"));
        fuelDTO.setCostDate(LocalDate.now());
        costService.addCostDetail(fuelDTO);

        CostDetailDTO maintenanceDTO = new CostDetailDTO();
        maintenanceDTO.setCostType(CostTypeEnum.MAINTENANCE.getCode());
        maintenanceDTO.setAmount(new BigDecimal("1000.00"));
        maintenanceDTO.setCostDate(LocalDate.now());
        costService.addCostDetail(maintenanceDTO);

        CostDetailDTO laborDTO = new CostDetailDTO();
        laborDTO.setCostType(CostTypeEnum.LABOR.getCode());
        laborDTO.setAmount(new BigDecimal("3000.00"));
        laborDTO.setCostDate(LocalDate.now());
        costService.addCostDetail(laborDTO);

        // 查询统计
        CostQueryDTO queryDTO = new CostQueryDTO();
        queryDTO.setStartDate(LocalDate.now());
        queryDTO.setEndDate(LocalDate.now());

        CostStatisticsVO result = costService.getCostStatistics(queryDTO);

        assertNotNull(result);
        assertEquals(new BigDecimal("6000.00"), result.getTotalAmount());
        assertEquals(3, result.getRecordCount());
        assertEquals(new BigDecimal("2000.00"), result.getFuelCost());
        assertEquals(new BigDecimal("1000.00"), result.getMaintenanceCost());
        assertEquals(new BigDecimal("3000.00"), result.getLaborCost());
    }

    /**
     * 测试计算并记录Trip提成
     */
    @Test
    void testCalculateAndRecordTripCommission() {
        // 调用方法
        Long result = costService.calculateAndRecordTripCommission(1L, 1L, 1000);

        // 验证结果
        assertNotNull(result);
    }

    /**
     * 测试获取能耗分析
     */
    @Test
    void testGetEnergyConsumptionAnalysis() {
        // 创建燃油成本
        CostDetailDTO fuelDTO = new CostDetailDTO();
        fuelDTO.setCostType(CostTypeEnum.FUEL.getCode());
        fuelDTO.setAmount(new BigDecimal("1000.00"));
        fuelDTO.setCostDate(LocalDate.now());
        costService.addCostDetail(fuelDTO);

        // 调用方法
        Map<String, Object> result = costService.getEnergyConsumptionAnalysis(LocalDate.now().minusDays(7), LocalDate.now());

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.get("totalFuelCost"));
    }

    /**
     * 测试获取车辆利用率分析
     */
    @Test
    void testGetVehicleUtilizationAnalysis() {
        // 调用方法
        Map<String, Object> result = costService.getVehicleUtilizationAnalysis(LocalDate.now().minusDays(7), LocalDate.now());

        // 验证结果
        assertNotNull(result);
    }

    /**
     * 测试获取空载率分析
     */
    @Test
    void testGetIdleRateAnalysis() {
        // 调用方法
        Map<String, Object> result = costService.getIdleRateAnalysis(LocalDate.now().minusDays(7), LocalDate.now());

        // 验证结果
        assertNotNull(result);
    }

    /**
     * 测试获取总体成本分析
     */
    @Test
    void testGetOverallCostAnalysis() {
        // 创建成本明细
        CostDetailDTO fuelDTO = new CostDetailDTO();
        fuelDTO.setCostType(CostTypeEnum.FUEL.getCode());
        fuelDTO.setAmount(new BigDecimal("1000.00"));
        fuelDTO.setCostDate(LocalDate.now());
        costService.addCostDetail(fuelDTO);

        // 创建预算
        costService.addBudget(testCostBudgetDTO);

        // 调用方法
        Map<String, Object> result = costService.getOverallCostAnalysis(LocalDate.now().minusDays(7), LocalDate.now());

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.get("totalCost"));
    }

    /**
     * 测试获取预算预警
     */
    @Test
    void testGetBudgetAlerts() {
        // 创建预算
        costService.addBudget(testCostBudgetDTO);

        // 调用方法
        List<Map<String, Object>> result = costService.getBudgetAlerts();

        // 验证结果
        assertNotNull(result);
    }
} 
