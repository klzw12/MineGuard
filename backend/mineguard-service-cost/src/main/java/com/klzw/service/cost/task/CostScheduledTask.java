package com.klzw.service.cost.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.klzw.service.cost.entity.CostBudget;
import com.klzw.service.cost.entity.CostDetail;
import com.klzw.service.cost.enums.BudgetStatusEnum;
import com.klzw.service.cost.enums.BudgetTypeEnum;
import com.klzw.service.cost.enums.CostTypeEnum;
import com.klzw.service.cost.mapper.CostBudgetMapper;
import com.klzw.service.cost.mapper.CostDetailMapper;
import com.klzw.service.cost.service.CostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CostScheduledTask {

    private final CostService costService;
    private final CostBudgetMapper costBudgetMapper;
    private final CostDetailMapper costDetailMapper;

    @Scheduled(cron = "0 0 1 1 * ?")
    public void calculateMonthlySalary() {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        log.info("开始执行月度薪酬计算任务，计算月份：{}", lastMonth);
        
        try {
            LocalDate startDate = lastMonth.atDay(1);
            LocalDate endDate = lastMonth.atEndOfMonth();
            
            Map<String, Object> result = costService.calculateSalaries(startDate, endDate);
            
            log.info("月度薪酬计算完成：成功={}, 失败={}", 
                result.get("successCount"), result.get("failCount"));
        } catch (Exception e) {
            log.error("月度薪酬计算任务执行失败", e);
        }
    }

    @Scheduled(cron = "0 0 2 1 * ?")
    public void checkMonthlyBudget() {
        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);
        log.info("开始执行月度预算检查任务，当前月份：{}", currentMonth);
        
        try {
            List<CostBudget> budgets = costBudgetMapper.selectList(
                new LambdaQueryWrapper<CostBudget>()
                    .eq(CostBudget::getBudgetType, BudgetTypeEnum.MONTHLY.getCode())
                    .eq(CostBudget::getBudgetYear, currentMonth.getYear())
                    .eq(CostBudget::getBudgetMonth, currentMonth.getMonthValue())
                    .eq(CostBudget::getStatus, BudgetStatusEnum.ACTIVE.getCode())
            );
            
            for (CostBudget budget : budgets) {
                checkBudgetUsage(budget, currentMonth);
            }
            
            log.info("月度预算检查任务执行完成，检查了 {} 个预算", budgets.size());
        } catch (Exception e) {
            log.error("月度预算检查任务执行失败", e);
        }
    }

    @Scheduled(cron = "0 0 3 1 * ?")
    public void generateMonthlyCostReport() {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        log.info("开始生成月度成本报表，月份：{}", lastMonth);
        
        try {
            LocalDate startDate = lastMonth.atDay(1);
            LocalDate endDate = lastMonth.atEndOfMonth();
            
            Map<String, Object> report = generateCostReport(startDate, endDate);
            
            log.info("月度成本报表生成完成：总成本={}, 记录数={}", 
                report.get("totalAmount"), report.get("recordCount"));
        } catch (Exception e) {
            log.error("月度成本报表生成失败", e);
        }
    }

    @Scheduled(cron = "0 0 4 * * ?")
    public void updateBudgetStatus() {
        log.info("开始更新预算状态");
        
        try {
            LocalDate today = LocalDate.now();
            
            List<CostBudget> expiredBudgets = costBudgetMapper.selectList(
                new LambdaQueryWrapper<CostBudget>()
                    .eq(CostBudget::getStatus, BudgetStatusEnum.ACTIVE.getCode())
                    .lt(CostBudget::getEndDate, today)
            );
            
            for (CostBudget budget : expiredBudgets) {
                budget.setStatus(BudgetStatusEnum.EXPIRED.getCode());
                budget.setUpdateTime(LocalDateTime.now());
                costBudgetMapper.updateById(budget);
                log.info("预算已过期：编号={}, 名称={}", budget.getBudgetNo(), budget.getBudgetName());
            }
            
            log.info("预算状态更新完成，更新了 {} 个过期预算", expiredBudgets.size());
        } catch (Exception e) {
            log.error("预算状态更新失败", e);
        }
    }

    @Scheduled(cron = "0 0 5 1 1,4,7,10 ?")
    public void checkQuarterlyBudget() {
        log.info("开始执行季度预算检查任务");
        
        try {
            int currentQuarter = (LocalDate.now().getMonthValue() - 1) / 3 + 1;
            int currentYear = LocalDate.now().getYear();
            
            List<CostBudget> quarterlyBudgets = costBudgetMapper.selectList(
                new LambdaQueryWrapper<CostBudget>()
                    .eq(CostBudget::getBudgetType, BudgetTypeEnum.QUARTERLY.getCode())
                    .eq(CostBudget::getBudgetYear, currentYear)
                    .eq(CostBudget::getBudgetQuarter, currentQuarter)
                    .eq(CostBudget::getStatus, BudgetStatusEnum.ACTIVE.getCode())
            );
            
            for (CostBudget budget : quarterlyBudgets) {
                checkQuarterlyBudgetUsage(budget, currentYear, currentQuarter);
            }
            
            log.info("季度预算检查任务执行完成，检查了 {} 个预算", quarterlyBudgets.size());
        } catch (Exception e) {
            log.error("季度预算检查任务执行失败", e);
        }
    }

    @Scheduled(cron = "0 0 6 1 1 ?")
    public void checkYearlyBudget() {
        log.info("开始执行年度预算检查任务");
        
        try {
            int currentYear = LocalDate.now().getYear();
            
            List<CostBudget> yearlyBudgets = costBudgetMapper.selectList(
                new LambdaQueryWrapper<CostBudget>()
                    .eq(CostBudget::getBudgetType, BudgetTypeEnum.YEARLY.getCode())
                    .eq(CostBudget::getBudgetYear, currentYear)
                    .eq(CostBudget::getStatus, BudgetStatusEnum.ACTIVE.getCode())
            );
            
            for (CostBudget budget : yearlyBudgets) {
                checkYearlyBudgetUsage(budget, currentYear);
            }
            
            log.info("年度预算检查任务执行完成，检查了 {} 个预算", yearlyBudgets.size());
        } catch (Exception e) {
            log.error("年度预算检查任务执行失败", e);
        }
    }

    private void checkBudgetUsage(CostBudget budget, LocalDate month) {
        LocalDate startDate = month.withDayOfMonth(1);
        LocalDate endDate = month.withDayOfMonth(month.lengthOfMonth());
        
        List<CostDetail> costs = costDetailMapper.selectList(
            new LambdaQueryWrapper<CostDetail>()
                .ge(CostDetail::getCostDate, startDate)
                .le(CostDetail::getCostDate, endDate)
                .eq(CostDetail::getDeleted, 0)
        );
        
        BigDecimal totalUsed = BigDecimal.ZERO;
        for (CostDetail cost : costs) {
            if (cost.getAmount() != null) {
                totalUsed = totalUsed.add(cost.getAmount());
            }
        }
        
        BigDecimal usageRate = BigDecimal.ZERO;
        if (budget.getTotalBudget() != null && budget.getTotalBudget().compareTo(BigDecimal.ZERO) > 0) {
            usageRate = totalUsed.divide(budget.getTotalBudget(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }
        
        if (usageRate.compareTo(BigDecimal.valueOf(80)) >= 0) {
            log.warn("预算使用率超过80%：预算={}, 已使用={}, 使用率={}%", 
                budget.getBudgetName(), totalUsed, usageRate);
        }
        
        if (usageRate.compareTo(BigDecimal.valueOf(100)) >= 0) {
            log.error("预算超支：预算={}, 已使用={}, 超支={}", 
                budget.getBudgetName(), totalUsed, totalUsed.subtract(budget.getTotalBudget()));
        }
    }

    private void checkQuarterlyBudgetUsage(CostBudget budget, int year, int quarter) {
        int startMonth = (quarter - 1) * 3 + 1;
        int endMonth = startMonth + 2;
        
        LocalDate startDate = LocalDate.of(year, startMonth, 1);
        LocalDate endDate = LocalDate.of(year, endMonth, YearMonth.of(year, endMonth).lengthOfMonth());
        
        List<CostDetail> costs = costDetailMapper.selectList(
            new LambdaQueryWrapper<CostDetail>()
                .ge(CostDetail::getCostDate, startDate)
                .le(CostDetail::getCostDate, endDate)
                .eq(CostDetail::getDeleted, 0)
        );
        
        BigDecimal totalUsed = BigDecimal.ZERO;
        for (CostDetail cost : costs) {
            if (cost.getAmount() != null) {
                totalUsed = totalUsed.add(cost.getAmount());
            }
        }
        
        BigDecimal usageRate = BigDecimal.ZERO;
        if (budget.getTotalBudget() != null && budget.getTotalBudget().compareTo(BigDecimal.ZERO) > 0) {
            usageRate = totalUsed.divide(budget.getTotalBudget(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }
        
        log.info("季度预算使用情况：预算={}, 已使用={}, 使用率={}%", 
            budget.getBudgetName(), totalUsed, usageRate);
    }

    private void checkYearlyBudgetUsage(CostBudget budget, int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        
        List<CostDetail> costs = costDetailMapper.selectList(
            new LambdaQueryWrapper<CostDetail>()
                .ge(CostDetail::getCostDate, startDate)
                .le(CostDetail::getCostDate, endDate)
                .eq(CostDetail::getDeleted, 0)
        );
        
        BigDecimal totalUsed = BigDecimal.ZERO;
        for (CostDetail cost : costs) {
            if (cost.getAmount() != null) {
                totalUsed = totalUsed.add(cost.getAmount());
            }
        }
        
        BigDecimal usageRate = BigDecimal.ZERO;
        if (budget.getTotalBudget() != null && budget.getTotalBudget().compareTo(BigDecimal.ZERO) > 0) {
            usageRate = totalUsed.divide(budget.getTotalBudget(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }
        
        log.info("年度预算使用情况：预算={}, 已使用={}, 使用率={}%", 
            budget.getBudgetName(), totalUsed, usageRate);
    }

    private Map<String, Object> generateCostReport(LocalDate startDate, LocalDate endDate) {
        List<CostDetail> costs = costDetailMapper.selectList(
            new LambdaQueryWrapper<CostDetail>()
                .ge(CostDetail::getCostDate, startDate)
                .le(CostDetail::getCostDate, endDate)
                .eq(CostDetail::getDeleted, 0)
                .orderByAsc(CostDetail::getCostDate)
        );
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        Map<Integer, BigDecimal> typeAmountMap = new HashMap<>();
        
        for (CostDetail cost : costs) {
            if (cost.getAmount() != null) {
                totalAmount = totalAmount.add(cost.getAmount());
                
                Integer costType = cost.getCostType();
                BigDecimal typeAmount = typeAmountMap.getOrDefault(costType, BigDecimal.ZERO);
                typeAmountMap.put(costType, typeAmount.add(cost.getAmount()));
            }
        }
        
        Map<String, Object> report = new HashMap<>();
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("totalAmount", totalAmount);
        report.put("recordCount", costs.size());
        report.put("typeBreakdown", typeAmountMap);
        report.put("fuelCost", typeAmountMap.getOrDefault(CostTypeEnum.FUEL.getCode(), BigDecimal.ZERO));
        report.put("maintenanceCost", typeAmountMap.getOrDefault(CostTypeEnum.MAINTENANCE.getCode(), BigDecimal.ZERO));
        report.put("laborCost", typeAmountMap.getOrDefault(CostTypeEnum.LABOR.getCode(), BigDecimal.ZERO));
        report.put("insuranceCost", typeAmountMap.getOrDefault(CostTypeEnum.INSURANCE.getCode(), BigDecimal.ZERO));
        report.put("otherCost", typeAmountMap.getOrDefault(CostTypeEnum.OTHER.getCode(), BigDecimal.ZERO));
        
        return report;
    }
}
