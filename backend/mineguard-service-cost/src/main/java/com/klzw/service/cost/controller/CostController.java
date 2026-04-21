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
@RequestMapping("/cost")
@RequiredArgsConstructor
public class CostController {

    private final CostService costService;

    @PostMapping("/detail")
    public Result<Map<String, Object>> addCostDetail(@RequestBody Map<String, Object> request) {
        log.debug("添加成本明细：{}", request);
        
        CostDetailDTO dto = new CostDetailDTO();
        if (request.get("costType") != null) {
            dto.setCostType(((Number) request.get("costType")).intValue());
        }
        dto.setCostName((String) request.get("costName"));
        if (request.get("amount") != null) {
            dto.setAmount(new BigDecimal(((Number) request.get("amount")).doubleValue()));
        }
        if (request.get("costDate") != null) {
            Object costDateObj = request.get("costDate");
            if (costDateObj instanceof String) {
                dto.setCostDate(LocalDate.parse((String) costDateObj));
            } else if (costDateObj instanceof java.util.List) {
                java.util.List<?> dateList = (java.util.List<?>) costDateObj;
                if (dateList.size() >= 3) {
                    int year = ((Number) dateList.get(0)).intValue();
                    int month = ((Number) dateList.get(1)).intValue();
                    int day = ((Number) dateList.get(2)).intValue();
                    dto.setCostDate(LocalDate.of(year, month, day));
                }
            }
        }
        dto.setPaymentMethod((String) request.get("paymentMethod"));
        dto.setInvoiceNo((String) request.get("invoiceNo"));
        dto.setDescription((String) request.get("description"));
        dto.setRemark((String) request.get("remark"));
        if (request.get("vehicleId") != null) {
            dto.setVehicleId(((Number) request.get("vehicleId")).longValue());
        }
        if (request.get("userId") != null) {
            dto.setUserId(((Number) request.get("userId")).longValue());
        }
        if (request.get("tripId") != null) {
            dto.setTripId(((Number) request.get("tripId")).longValue());
        }
        
        CostDetailVO vo = costService.addCostDetail(dto);
        
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("id", vo.getId());
        response.put("costNo", vo.getCostNo());
        response.put("costType", vo.getCostType());
        response.put("costName", vo.getCostName());
        response.put("costTypeName", vo.getCostTypeName());
        response.put("amount", vo.getAmount());
        response.put("costDate", vo.getCostDate());
        response.put("vehicleId", vo.getVehicleId());
        response.put("userId", vo.getUserId());
        response.put("tripId", vo.getTripId());
        
        return Result.success(response);
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

    @GetMapping("/detail/trip/{tripId}")
    public Result<List<Map<String, Object>>> getCostDetailListByTripId(@PathVariable Long tripId) {
        log.debug("获取行程成本明细列表：tripId={}", tripId);
        CostQueryDTO queryDTO = new CostQueryDTO();
        queryDTO.setTripId(tripId);
        List<CostDetailVO> list = costService.getCostDetailList(queryDTO);
        
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (CostDetailVO vo : list) {
            Map<String, Object> item = new java.util.HashMap<>();
            item.put("id", vo.getId());
            item.put("costType", vo.getCostType());
            item.put("costTypeName", vo.getCostTypeName());
            item.put("costName", vo.getCostName());
            item.put("amount", vo.getAmount());
            item.put("description", vo.getDescription());
            item.put("createTime", vo.getCreateTime());
            result.add(item);
        }
        
        return Result.success(result);
    }

    @GetMapping("/statistics")
    public Result<CostStatisticsVO> getCostStatistics(CostQueryDTO queryDTO) {
        log.debug("统计成本：{}", queryDTO);
        CostStatisticsVO vo = costService.getCostStatistics(queryDTO);
        return Result.success(vo);
    }
    
    @GetMapping("/statistics/internal")
    public Result<com.klzw.common.core.domain.dto.CostStatisticsResponseDTO> getCostStatisticsInternal(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        log.debug("内部调用统计成本：startDate={}, endDate={}", startDate, endDate);
        
        CostQueryDTO queryDTO = new CostQueryDTO();
        queryDTO.setStartDate(LocalDate.parse(startDate));
        queryDTO.setEndDate(LocalDate.parse(endDate));
        
        CostStatisticsVO vo = costService.getCostStatistics(queryDTO);
        
        com.klzw.common.core.domain.dto.CostStatisticsResponseDTO response = new com.klzw.common.core.domain.dto.CostStatisticsResponseDTO();
        response.setTotalAmount(vo.getTotalAmount());
        response.setRecordCount(vo.getRecordCount());
        response.setFuelCost(vo.getFuelCost());
        response.setMaintenanceCost(vo.getMaintenanceCost());
        response.setLaborCost(vo.getLaborCost());
        response.setInsuranceCost(vo.getInsuranceCost());
        response.setDepreciationCost(vo.getDepreciationCost());
        response.setManagementCost(vo.getManagementCost());
        response.setOtherCost(vo.getOtherCost());
        response.setTripCommissionCost(vo.getTripCommissionCost());
        
        return Result.success(response);
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
    
    @PostMapping("/calculate-salaries")
    public Result<Map<String, Object>> calculateSalaries(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("手动触发薪酬计算：{} 至 {}", startDate, endDate);
        Map<String, Object> result = costService.calculateSalaries(startDate, endDate);
        return Result.success(result);
    }

    @PostMapping("/calculate-salaries-by-month")
    public Result<Map<String, Object>> calculateSalariesByMonth(
            @RequestParam String yearMonth) {
        log.info("按月份计算薪酬：{}", yearMonth);
        Map<String, Object> result = costService.calculateSalariesByMonth(yearMonth);
        return Result.success(result);
    }

    @PostMapping("/reset-performance")
    public Result<Map<String, Object>> resetPerformance() {
        log.info("清空绩效");
        Map<String, Object> result = costService.resetPerformance();
        return Result.success(result);
    }

    @GetMapping("/salary-config/check")
    public Result<Boolean> checkSalaryConfig(@RequestParam Long userId) {
        log.debug("检查用户是否设置了起薪：userId={}", userId);
        boolean hasConfig = costService.hasSalaryConfig(userId);
        return Result.success(hasConfig);
    }
    
    @GetMapping("/salary-config/params")
    public Result<Map<String, Object>> getSalaryConfigParams() {
        log.debug("获取薪酬配置参数");
        Map<String, Object> params = costService.getSalaryConfigParams();
        return Result.success(params);
    }

    @GetMapping("/statistics/driver/{userId}")
    public Result<Map<String, Object>> getDriverCostStatistics(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.debug("获取司机成本统计：userId={}, {} 至 {}", userId, startDate, endDate);
        return Result.success(costService.getDriverCostStatistics(userId, startDate, endDate));
    }
}
