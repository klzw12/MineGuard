package com.klzw.service.cost.service;

import com.klzw.service.cost.dto.CostBudgetDTO;
import com.klzw.service.cost.dto.CostDetailDTO;
import com.klzw.service.cost.dto.CostQueryDTO;
import com.klzw.service.cost.dto.SalaryConfigDTO;
import com.klzw.service.cost.dto.SalaryRecordDTO;
import com.klzw.service.cost.vo.CostBudgetVO;
import com.klzw.service.cost.vo.CostDetailVO;
import com.klzw.service.cost.vo.CostStatisticsVO;
import com.klzw.service.cost.vo.SalaryConfigVO;
import com.klzw.service.cost.vo.SalaryRecordVO;

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

    SalaryRecordVO addSalaryRecord(SalaryRecordDTO dto);

    SalaryRecordVO updateSalaryRecord(SalaryRecordDTO dto);

    void deleteSalaryRecord(Long id);

    SalaryRecordVO getSalaryRecord(Long id);

    List<SalaryRecordVO> getSalaryRecordList(String keyword, String period, Integer page, Integer pageSize);

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
}
