package com.klzw.service.cost.service;

import com.klzw.service.cost.dto.CostBudgetDTO;
import com.klzw.service.cost.dto.CostDetailDTO;
import com.klzw.service.cost.dto.CostQueryDTO;
import com.klzw.service.cost.dto.SalaryConfigDTO;
import com.klzw.service.cost.vo.CostBudgetVO;
import com.klzw.service.cost.vo.CostDetailVO;
import com.klzw.service.cost.vo.CostStatisticsVO;
import com.klzw.service.cost.vo.SalaryConfigVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CostService {

    CostDetailVO addCostDetail(CostDetailDTO dto);

    CostDetailVO updateCostDetail(CostDetailDTO dto);

    void deleteCostDetail(Long id);

    CostDetailVO getCostDetail(Long id);

    List<CostDetailVO> getCostDetailList(CostQueryDTO queryDTO);

    CostStatisticsVO getCostStatistics(CostQueryDTO queryDTO);

    SalaryConfigVO addSalaryConfig(SalaryConfigDTO dto);

    SalaryConfigVO updateSalaryConfig(SalaryConfigDTO dto);

    void deleteSalaryConfig(Long id);

    SalaryConfigVO getSalaryConfig(Long id);

    List<SalaryConfigVO> getSalaryConfigList();



    CostBudgetVO addBudget(CostBudgetDTO dto);

    CostBudgetVO updateBudget(CostBudgetDTO dto);

    void deleteBudget(Long id);

    CostBudgetVO getBudget(Long id);

    List<CostBudgetVO> getBudgetList(Integer budgetType, Integer budgetYear);

    Map<String, Object> checkBudgetUsage(Long budgetId);

    Map<String, Object> generateCostReport(LocalDate startDate, LocalDate endDate);

    List<Map<String, Object>> getBudgetAlerts();

    Map<String, Object> getCostTrend(LocalDate startDate, LocalDate endDate, String granularity);

    Map<String, Object> getEnergyConsumptionAnalysis(LocalDate startDate, LocalDate endDate);

    Map<String, Object> getVehicleUtilizationAnalysis(LocalDate startDate, LocalDate endDate);

    Map<String, Object> getIdleRateAnalysis(LocalDate startDate, LocalDate endDate);

    Map<String, Object> getOverallCostAnalysis(LocalDate startDate, LocalDate endDate);
    
    /**
     * 计算并记录 Trip 提成
     * 公式：提成 = 预计金额 × (Python 得分 / 100)
     * 
     * @param tripId 行程 ID
     * @param driverId 司机 ID
     * @param estimatedAmount 预计金额
     * @return 成本记录 ID
     */
    Long calculateAndRecordTripCommission(Long tripId, Long driverId, double estimatedAmount);
    
    /**
     * 手动触发薪酬计算
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 计算结果
     */
    Map<String, Object> calculateSalaries(LocalDate startDate, LocalDate endDate);
    
    /**
     * 按月份计算薪酬
     * 
     * @param yearMonth 年月（格式：yyyy-MM）
     * @return 计算结果
     */
    Map<String, Object> calculateSalariesByMonth(String yearMonth);
    
    /**
     * 清空绩效
     * 
     * @return 清空结果
     */
    Map<String, Object> resetPerformance();
    
    /**
     * 检查用户是否设置了起薪
     * 
     * @param userId 用户ID
     * @return 是否设置了起薪
     */
    boolean hasSalaryConfig(Long userId);
    
    /**
     * 获取薪酬配置的基本参数
     * 
     * @return 配置参数
     */
    Map<String, Object> getSalaryConfigParams();

    /**
     * 获取司机成本统计
     * 
     * @param userId 司机用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 成本统计数据
     */
    Map<String, Object> getDriverCostStatistics(Long userId, LocalDate startDate, LocalDate endDate);
}
