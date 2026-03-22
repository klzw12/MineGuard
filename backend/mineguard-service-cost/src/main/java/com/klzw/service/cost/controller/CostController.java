package com.klzw.service.cost.controller;

import com.klzw.common.core.result.Result;
import com.klzw.service.cost.dto.CostBudgetDTO;
import com.klzw.service.cost.dto.CostDetailDTO;
import com.klzw.service.cost.dto.CostQueryDTO;
import com.klzw.service.cost.dto.SalaryConfigDTO;
import com.klzw.service.cost.service.CostService;
import com.klzw.service.cost.vo.CostBudgetVO;
import com.klzw.service.cost.vo.CostDetailVO;
import com.klzw.service.cost.vo.CostStatisticsVO;
import com.klzw.service.cost.vo.SalaryConfigVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/cost")
@RequiredArgsConstructor
public class CostController {

    private final CostService costService;

    @PostMapping("/detail")
    public Result<CostDetailVO> addCostDetail(@RequestBody CostDetailDTO costDetailDTO) {
        log.debug("添加成本明细：{}", costDetailDTO);
        CostDetailVO vo = costService.addCostDetail(costDetailDTO);
        return Result.success(vo);
    }

    @PutMapping("/detail")
    public Result<CostDetailVO> updateCostDetail(@RequestBody CostDetailDTO costDetailDTO) {
        log.debug("更新成本明细：{}", costDetailDTO);
        CostDetailVO vo = costService.updateCostDetail(costDetailDTO);
        return Result.success(vo);
    }

    @DeleteMapping("/detail/{id}")
    public Result<Void> deleteCostDetail(@PathVariable Long id) {
        log.debug("删除成本明细：ID={}", id);
        costService.deleteCostDetail(id);
        return Result.success();
    }

    @GetMapping("/detail/{id}")
    public Result<CostDetailVO> getCostDetail(@PathVariable Long id) {
        log.debug("获取成本明细：ID={}", id);
        CostDetailVO vo = costService.getCostDetail(id);
        return Result.success(vo);
    }

    @GetMapping("/detail/list")
    public Result<List<CostDetailVO>> getCostDetailList(CostQueryDTO queryDTO) {
        log.debug("获取成本明细列表：{}", queryDTO);
        List<CostDetailVO> list = costService.getCostDetailList(queryDTO);
        return Result.success(list);
    }

    @GetMapping("/statistics")
    public Result<CostStatisticsVO> getCostStatistics(CostQueryDTO queryDTO) {
        log.debug("统计成本：{}", queryDTO);
        CostStatisticsVO vo = costService.getCostStatistics(queryDTO);
        return Result.success(vo);
    }
    
    /**
     * 记录 Trip 任务提成（内部调用）
     */
    @PostMapping("/commission")
    public Result<CostDetailVO> recordTripCommission(@RequestBody Map<String, Object> commissionData) {
        log.info("记录 Trip 任务提成：{}", commissionData);
        
        try {
            CostDetailDTO dto = new CostDetailDTO();
            dto.setCostType(((Number) commissionData.get("costType")).intValue());
            dto.setCostName((String) commissionData.get("costName"));
            dto.setAmount(new BigDecimal(((Number) commissionData.get("amount")).doubleValue()));
            
            if (commissionData.get("userId") != null) {
                dto.setUserId(((Number) commissionData.get("userId")).longValue());
            }
            if (commissionData.get("tripId") != null) {
                dto.setTripId(((Number) commissionData.get("tripId")).longValue());
            }
            
            dto.setRemark((String) commissionData.get("remark"));
            
            CostDetailVO vo = costService.addCostDetail(dto);
            return Result.success(vo);
            
        } catch (Exception e) {
            log.error("记录 Trip 任务提成失败", e);
            return Result.fail("记录 Trip 任务提成失败：" + e.getMessage());
        }
    }

    @PostMapping("/salary-config")
    public Result<SalaryConfigVO> addSalaryConfig(@RequestBody SalaryConfigDTO dto) {
        log.debug("添加薪资配置：{}", dto);
        SalaryConfigVO vo = costService.addSalaryConfig(dto);
        return Result.success(vo);
    }

    @PutMapping("/salary-config")
    public Result<SalaryConfigVO> updateSalaryConfig(@RequestBody SalaryConfigDTO dto) {
        log.debug("更新薪资配置：{}", dto);
        SalaryConfigVO vo = costService.updateSalaryConfig(dto);
        return Result.success(vo);
    }

    @DeleteMapping("/salary-config/{id}")
    public Result<Void> deleteSalaryConfig(@PathVariable Long id) {
        log.debug("删除薪资配置：ID={}", id);
        costService.deleteSalaryConfig(id);
        return Result.success();
    }

    @GetMapping("/salary-config/{id}")
    public Result<SalaryConfigVO> getSalaryConfig(@PathVariable Long id) {
        log.debug("获取薪资配置：ID={}", id);
        SalaryConfigVO vo = costService.getSalaryConfig(id);
        return Result.success(vo);
    }

    @GetMapping("/salary-config/list")
    public Result<List<SalaryConfigVO>> getSalaryConfigList() {
        log.debug("获取薪资配置列表");
        List<SalaryConfigVO> list = costService.getSalaryConfigList();
        return Result.success(list);
    }

    @PostMapping("/budget")
    public Result<CostBudgetVO> addBudget(@RequestBody CostBudgetDTO dto) {
        log.debug("添加成本预算：{}", dto);
        CostBudgetVO vo = costService.addBudget(dto);
        return Result.success(vo);
    }

    @PutMapping("/budget")
    public Result<CostBudgetVO> updateBudget(@RequestBody CostBudgetDTO dto) {
        log.debug("更新成本预算：{}", dto);
        CostBudgetVO vo = costService.updateBudget(dto);
        return Result.success(vo);
    }

    @DeleteMapping("/budget/{id}")
    public Result<Void> deleteBudget(@PathVariable Long id) {
        log.debug("删除成本预算：ID={}", id);
        costService.deleteBudget(id);
        return Result.success();
    }

    @GetMapping("/budget/{id}")
    public Result<CostBudgetVO> getBudget(@PathVariable Long id) {
        log.debug("获取成本预算：ID={}", id);
        CostBudgetVO vo = costService.getBudget(id);
        return Result.success(vo);
    }

    @GetMapping("/budget/list")
    public Result<List<CostBudgetVO>> getBudgetList(
            @RequestParam(value = "budgetType", required = false) Integer budgetType,
            @RequestParam(value = "budgetYear", required = false) Integer budgetYear) {
        log.debug("获取成本预算列表：budgetType={}, budgetYear={}", budgetType, budgetYear);
        List<CostBudgetVO> list = costService.getBudgetList(budgetType, budgetYear);
        return Result.success(list);
    }

    @GetMapping("/budget/{id}/usage")
    public Result<Map<String, Object>> checkBudgetUsage(@PathVariable Long id) {
        log.debug("检查预算使用情况：ID={}", id);
        return Result.success(costService.checkBudgetUsage(id));
    }

    @GetMapping("/report")
    public Result<Map<String, Object>> generateCostReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.debug("生成成本报表：{} 至 {}", startDate, endDate);
        return Result.success(costService.generateCostReport(startDate, endDate));
    }

    @GetMapping("/budget/alerts")
    public Result<List<Map<String, Object>>> getBudgetAlerts() {
        log.debug("获取预算预警列表");
        return Result.success(costService.getBudgetAlerts());
    }

    @GetMapping("/trend")
    public Result<Map<String, Object>> getCostTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "day") String granularity) {
        log.debug("获取成本趋势：{} 至 {}, 粒度={}", startDate, endDate, granularity);
        return Result.success(costService.getCostTrend(startDate, endDate, granularity));
    }

    @GetMapping("/analysis/energy")
    public Result<Map<String, Object>> getEnergyConsumptionAnalysis(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.debug("获取能耗分析：{} 至 {}", startDate, endDate);
        return Result.success(costService.getEnergyConsumptionAnalysis(startDate, endDate));
    }

    @GetMapping("/analysis/utilization")
    public Result<Map<String, Object>> getVehicleUtilizationAnalysis(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.debug("获取车辆利用率分析：{} 至 {}", startDate, endDate);
        return Result.success(costService.getVehicleUtilizationAnalysis(startDate, endDate));
    }

    @GetMapping("/analysis/idle-rate")
    public Result<Map<String, Object>> getIdleRateAnalysis(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.debug("获取空载率分析：{} 至 {}", startDate, endDate);
        return Result.success(costService.getIdleRateAnalysis(startDate, endDate));
    }

    @GetMapping("/analysis/overall")
    public Result<Map<String, Object>> getOverallCostAnalysis(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.debug("获取总体成本分析：{} 至 {}", startDate, endDate);
        return Result.success(costService.getOverallCostAnalysis(startDate, endDate));
    }
}
