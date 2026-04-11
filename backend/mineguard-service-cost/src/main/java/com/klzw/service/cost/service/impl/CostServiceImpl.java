package com.klzw.service.cost.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.klzw.common.core.client.PythonClient;
import com.klzw.common.core.client.StatisticsClient;
import com.klzw.common.core.client.TransportClient;
import com.klzw.common.core.client.TripClient;
import com.klzw.common.core.client.VehicleClient;
import com.klzw.service.cost.dto.CostBudgetDTO;
import com.klzw.service.cost.dto.CostDetailDTO;
import com.klzw.service.cost.dto.CostQueryDTO;
import com.klzw.service.cost.dto.SalaryConfigDTO;
import com.klzw.service.cost.dto.SalaryRecordDTO;
import com.klzw.service.cost.entity.CostBudget;
import com.klzw.service.cost.entity.CostDetail;
import com.klzw.service.cost.entity.SalaryConfig;
import com.klzw.service.cost.entity.SalaryRecord;
import com.klzw.service.cost.enums.BudgetStatusEnum;
import com.klzw.service.cost.enums.BudgetTypeEnum;
import com.klzw.service.cost.enums.CostTypeEnum;
import com.klzw.service.cost.mapper.CostBudgetMapper;
import com.klzw.service.cost.mapper.CostDetailMapper;
import com.klzw.service.cost.mapper.SalaryConfigMapper;
import com.klzw.service.cost.mapper.SalaryRecordMapper;
import com.klzw.service.cost.service.CostService;
import com.klzw.service.cost.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CostServiceImpl implements CostService {

    private final CostDetailMapper costDetailMapper;
    private final SalaryConfigMapper salaryConfigMapper;
    private final SalaryRecordMapper salaryRecordMapper;
    private final CostBudgetMapper costBudgetMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PythonClient pythonClient;
    private final TransportClient transportClient;
    private final TripClient tripClient;
    private final VehicleClient vehicleClient;
    
    private static final long CACHE_EXPIRE_DAYS = 1; // 缓存过期时间：1 天

    @Override
    public CostDetailVO addCostDetail(CostDetailDTO dto) {
        CostDetail entity = new CostDetail();
        BeanUtils.copyProperties(dto, entity);
        entity.setCostNo(generateCostNo());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setDeleted(0);
        
        if (dto.getCostType() != null) {
            CostTypeEnum typeEnum = CostTypeEnum.getByCode(dto.getCostType());
            if (typeEnum != null) {
                entity.setCostName(typeEnum.getName());
            }
        }
        
        costDetailMapper.insert(entity);
        log.info("添加成本明细：编号={}, 类型={}, 金额={}", entity.getCostNo(), entity.getCostType(), entity.getAmount());
        
        return convertToCostDetailVO(entity);
    }

    @Override
    public CostDetailVO updateCostDetail(CostDetailDTO dto) {
        CostDetail entity = costDetailMapper.selectById(dto.getId());
        if (entity == null) {
            throw new RuntimeException("成本明细不存在");
        }
        
        BeanUtils.copyProperties(dto, entity, "id", "costNo", "createTime", "deleted");
        entity.setUpdateTime(LocalDateTime.now());
        
        if (dto.getCostType() != null) {
            CostTypeEnum typeEnum = CostTypeEnum.getByCode(dto.getCostType());
            if (typeEnum != null) {
                entity.setCostName(typeEnum.getName());
            }
        }
        
        costDetailMapper.updateById(entity);
        log.info("更新成本明细：ID={}, 金额={}", entity.getId(), entity.getAmount());
        
        return convertToCostDetailVO(entity);
    }

    @Override
    public void deleteCostDetail(Long id) {
        costDetailMapper.deleteById(id);
        log.info("删除成本明细：ID={}", id);
    }

    @Override
    public CostDetailVO getCostDetail(Long id) {
        CostDetail entity = costDetailMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("成本明细不存在");
        }
        return convertToCostDetailVO(entity);
    }

    @Override
    public List<CostDetailVO> getCostDetailList(CostQueryDTO queryDTO) {
        LambdaQueryWrapper<CostDetail> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getVehicleId() != null) {
            wrapper.eq(CostDetail::getVehicleId, queryDTO.getVehicleId());
        }
        if (queryDTO.getUserId() != null) {
            wrapper.eq(CostDetail::getUserId, queryDTO.getUserId());
        }
        if (queryDTO.getCostType() != null) {
            wrapper.eq(CostDetail::getCostType, queryDTO.getCostType());
        }
        if (queryDTO.getStartDate() != null) {
            wrapper.ge(CostDetail::getCostDate, queryDTO.getStartDate());
        }
        if (queryDTO.getEndDate() != null) {
            wrapper.le(CostDetail::getCostDate, queryDTO.getEndDate());
        }
        
        wrapper.orderByDesc(CostDetail::getCostDate);
        
        List<CostDetail> list = costDetailMapper.selectList(wrapper);
        return list.stream().map(this::convertToCostDetailVO).collect(Collectors.toList());
    }

    @Override
    public CostStatisticsVO getCostStatistics(CostQueryDTO queryDTO) {
        // 先尝试从缓存读取（如果是单个日期查询）
        if (queryDTO.getStartDate() != null && queryDTO.getStartDate().equals(queryDTO.getEndDate())) {
            String cacheKey = "statistics:cost:" + queryDTO.getStartDate();
            @SuppressWarnings("unchecked")
            CostStatisticsVO cached = (CostStatisticsVO) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("缓存命中：{}", cacheKey);
                return cached;
            }
        }
        
        List<CostDetailVO> list = getCostDetailList(queryDTO);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        Map<Integer, BigDecimal> typeAmountMap = new HashMap<>();
        Map<Integer, String> typeNames = new HashMap<>();
        
        for (CostDetailVO detail : list) {
            totalAmount = totalAmount.add(detail.getAmount());
            Integer costType = detail.getCostType();
            BigDecimal typeAmount = typeAmountMap.getOrDefault(costType, BigDecimal.ZERO);
            typeAmount = typeAmount.add(detail.getAmount());
            typeAmountMap.put(costType, typeAmount);
            
            if (detail.getCostTypeName() != null) {
                typeNames.put(costType, detail.getCostTypeName());
            }
        }
        
        CostStatisticsVO vo = new CostStatisticsVO();
        vo.setStartDate(queryDTO.getStartDate());
        vo.setEndDate(queryDTO.getEndDate());
        vo.setTotalAmount(totalAmount);
        vo.setRecordCount(list.size());
        vo.setTypeAmountMap(typeAmountMap);
        vo.setTypeNames(typeNames);
        
        vo.setFuelCost(typeAmountMap.getOrDefault(CostTypeEnum.FUEL.getCode(), BigDecimal.ZERO));
        vo.setMaintenanceCost(typeAmountMap.getOrDefault(CostTypeEnum.MAINTENANCE.getCode(), BigDecimal.ZERO));
        vo.setLaborCost(typeAmountMap.getOrDefault(CostTypeEnum.LABOR.getCode(), BigDecimal.ZERO));
        vo.setInsuranceCost(typeAmountMap.getOrDefault(CostTypeEnum.INSURANCE.getCode(), BigDecimal.ZERO));
        vo.setDepreciationCost(typeAmountMap.getOrDefault(CostTypeEnum.DEPRECIATION.getCode(), BigDecimal.ZERO));
        vo.setManagementCost(typeAmountMap.getOrDefault(CostTypeEnum.MANAGEMENT.getCode(), BigDecimal.ZERO));
        vo.setOtherCost(typeAmountMap.getOrDefault(CostTypeEnum.OTHER.getCode(), BigDecimal.ZERO));
        
        log.debug("统计成本：总成本={}", totalAmount);
        
        // 写入缓存
        if (queryDTO.getStartDate() != null && queryDTO.getStartDate().equals(queryDTO.getEndDate())) {
            String cacheKey = "statistics:cost:" + queryDTO.getStartDate();
            redisTemplate.opsForValue().set(cacheKey, vo, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
        }
        
        return vo;
    }

    @Override
    public SalaryConfigVO addSalaryConfig(SalaryConfigDTO dto) {
        SalaryConfig entity = new SalaryConfig();
        BeanUtils.copyProperties(dto, entity);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setDeleted(0);
        
        salaryConfigMapper.insert(entity);
        log.info("添加薪资配置：角色={}", entity.getRoleName());
        
        return convertToSalaryConfigVO(entity);
    }

    @Override
    public SalaryConfigVO updateSalaryConfig(SalaryConfigDTO dto) {
        SalaryConfig entity = salaryConfigMapper.selectById(dto.getId());
        if (entity == null) {
            throw new RuntimeException("薪资配置不存在");
        }
        
        BeanUtils.copyProperties(dto, entity, "id", "createTime", "deleted");
        entity.setUpdateTime(LocalDateTime.now());
        
        salaryConfigMapper.updateById(entity);
        log.info("更新薪资配置：ID={}", entity.getId());
        
        return convertToSalaryConfigVO(entity);
    }

    @Override
    public void deleteSalaryConfig(Long id) {
        salaryConfigMapper.deleteById(id);
        log.info("删除薪资配置：ID={}", id);
    }

    @Override
    public SalaryConfigVO getSalaryConfig(Long id) {
        SalaryConfig entity = salaryConfigMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("薪资配置不存在");
        }
        return convertToSalaryConfigVO(entity);
    }

    @Override
    public List<SalaryConfigVO> getSalaryConfigList() {
        LambdaQueryWrapper<SalaryConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SalaryConfig::getStatus, 1);
        wrapper.orderByAsc(SalaryConfig::getRoleCode);
        
        List<SalaryConfig> list = salaryConfigMapper.selectList(wrapper);
        return list.stream().map(this::convertToSalaryConfigVO).collect(Collectors.toList());
    }

    @Override
    public SalaryRecordVO addSalaryRecord(SalaryRecordDTO dto) {
        SalaryRecord entity = new SalaryRecord();
        BeanUtils.copyProperties(dto, entity);
        if (entity.getTotalSalary() == null) {
            entity.setTotalSalary(calculateTotalSalary(entity));
        }
        entity.setStatus(1);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setDeleted(0);
        
        salaryRecordMapper.insert(entity);
        log.info("添加薪酬记录：ID={}, 司机={}, 周期={}", entity.getId(), entity.getDriverName(), entity.getPeriod());
        
        return convertToSalaryRecordVO(entity);
    }

    @Override
    public SalaryRecordVO updateSalaryRecord(SalaryRecordDTO dto) {
        SalaryRecord entity = salaryRecordMapper.selectById(dto.getId());
        if (entity == null) {
            throw new RuntimeException("薪酬记录不存在");
        }
        
        BeanUtils.copyProperties(dto, entity, "id", "createTime", "deleted");
        if (entity.getTotalSalary() == null) {
            entity.setTotalSalary(calculateTotalSalary(entity));
        }
        entity.setUpdateTime(LocalDateTime.now());
        
        salaryRecordMapper.updateById(entity);
        log.info("更新薪酬记录：ID={}", entity.getId());
        
        return convertToSalaryRecordVO(entity);
    }

    @Override
    public void deleteSalaryRecord(Long id) {
        SalaryRecord entity = salaryRecordMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("薪酬记录不存在");
        }
        
        entity.setDeleted(1);
        entity.setUpdateTime(LocalDateTime.now());
        salaryRecordMapper.updateById(entity);
        log.info("删除薪酬记录：ID={}", id);
    }

    @Override
    public SalaryRecordVO getSalaryRecord(Long id) {
        SalaryRecord entity = salaryRecordMapper.selectById(id);
        if (entity == null) {
            return null;
        }
        return convertToSalaryRecordVO(entity);
    }

    @Override
    public List<SalaryRecordVO> getSalaryRecordList(String keyword, String period, Integer page, Integer pageSize) {
        LambdaQueryWrapper<SalaryRecord> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(SalaryRecord::getDriverName, keyword)
                    .or().like(SalaryRecord::getVehicleNo, keyword));
        }
        
        if (period != null && !period.isEmpty()) {
            wrapper.eq(SalaryRecord::getPeriod, period);
        }
        
        wrapper.eq(SalaryRecord::getStatus, 1);
        wrapper.orderByDesc(SalaryRecord::getCreateTime);
        
        if (page != null && pageSize != null) {
            wrapper.last("LIMIT " + ((page - 1) * pageSize) + ", " + pageSize);
        }
        
        List<SalaryRecord> list = salaryRecordMapper.selectList(wrapper);
        return list.stream().map(this::convertToSalaryRecordVO).collect(Collectors.toList());
    }

    private BigDecimal calculateTotalSalary(SalaryRecord entity) {
        BigDecimal base = entity.getBaseSalary() != null ? entity.getBaseSalary() : BigDecimal.ZERO;
        BigDecimal bonus = entity.getBonus() != null ? entity.getBonus() : BigDecimal.ZERO;
        BigDecimal deduction = entity.getDeduction() != null ? entity.getDeduction() : BigDecimal.ZERO;
        return base.add(bonus).subtract(deduction);
    }

    private SalaryRecordVO convertToSalaryRecordVO(SalaryRecord entity) {
        SalaryRecordVO vo = new SalaryRecordVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public CostBudgetVO addBudget(CostBudgetDTO dto) {
        CostBudget entity = new CostBudget();
        BeanUtils.copyProperties(dto, entity);
        entity.setBudgetNo(generateBudgetNo());
        entity.setTotalBudget(calculateTotalBudget(entity));
        entity.setStatus(BudgetStatusEnum.DRAFT.getCode());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setDeleted(0);
        
        costBudgetMapper.insert(entity);
        log.info("添加成本预算：编号={}, 名称={}", entity.getBudgetNo(), entity.getBudgetName());
        
        return convertToCostBudgetVO(entity);
    }

    @Override
    public CostBudgetVO updateBudget(CostBudgetDTO dto) {
        CostBudget entity = costBudgetMapper.selectById(dto.getId());
        if (entity == null) {
            throw new RuntimeException("成本预算不存在");
        }
        
        BeanUtils.copyProperties(dto, entity, "id", "budgetNo", "createTime", "deleted");
        entity.setTotalBudget(calculateTotalBudget(entity));
        entity.setUpdateTime(LocalDateTime.now());
        
        costBudgetMapper.updateById(entity);
        log.info("更新成本预算：ID={}", entity.getId());
        
        return convertToCostBudgetVO(entity);
    }

    @Override
    public void deleteBudget(Long id) {
        costBudgetMapper.deleteById(id);
        log.info("删除成本预算：ID={}", id);
    }

    @Override
    public CostBudgetVO getBudget(Long id) {
        CostBudget entity = costBudgetMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("成本预算不存在");
        }
        return convertToCostBudgetVO(entity);
    }

    @Override
    public List<CostBudgetVO> getBudgetList(Integer budgetType, Integer budgetYear) {
        LambdaQueryWrapper<CostBudget> wrapper = new LambdaQueryWrapper<>();
        
        if (budgetType != null) {
            wrapper.eq(CostBudget::getBudgetType, budgetType);
        }
        if (budgetYear != null) {
            wrapper.eq(CostBudget::getBudgetYear, budgetYear);
        }
        
        wrapper.orderByDesc(CostBudget::getCreateTime);
        
        List<CostBudget> list = costBudgetMapper.selectList(wrapper);
        return list.stream().map(this::convertToCostBudgetVO).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> checkBudgetUsage(Long budgetId) {
        CostBudget budget = costBudgetMapper.selectById(budgetId);
        if (budget == null) {
            throw new RuntimeException("预算不存在");
        }
        
        LocalDate startDate = budget.getStartDate();
        LocalDate endDate = budget.getEndDate();
        
        if (startDate == null || endDate == null) {
            if (budget.getBudgetYear() != null) {
                if (budget.getBudgetMonth() != null) {
                    startDate = LocalDate.of(budget.getBudgetYear(), budget.getBudgetMonth(), 1);
                    endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
                } else if (budget.getBudgetQuarter() != null) {
                    int startMonth = (budget.getBudgetQuarter() - 1) * 3 + 1;
                    startDate = LocalDate.of(budget.getBudgetYear(), startMonth, 1);
                    endDate = startDate.plusMonths(3).minusDays(1);
                } else {
                    startDate = LocalDate.of(budget.getBudgetYear(), 1, 1);
                    endDate = LocalDate.of(budget.getBudgetYear(), 12, 31);
                }
            }
        }
        
        List<CostDetail> costs = costDetailMapper.selectList(
            new LambdaQueryWrapper<CostDetail>()
                .ge(startDate != null, CostDetail::getCostDate, startDate)
                .le(endDate != null, CostDetail::getCostDate, endDate)
                .eq(CostDetail::getDeleted, 0)
        );
        
        BigDecimal totalUsed = BigDecimal.ZERO;
        Map<Integer, BigDecimal> typeUsedMap = new HashMap<>();
        
        for (CostDetail cost : costs) {
            if (cost.getAmount() != null) {
                totalUsed = totalUsed.add(cost.getAmount());
                Integer costType = cost.getCostType();
                typeUsedMap.put(costType, typeUsedMap.getOrDefault(costType, BigDecimal.ZERO).add(cost.getAmount()));
            }
        }
        
        BigDecimal usageRate = BigDecimal.ZERO;
        if (budget.getTotalBudget() != null && budget.getTotalBudget().compareTo(BigDecimal.ZERO) > 0) {
            usageRate = totalUsed.divide(budget.getTotalBudget(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("budgetId", budgetId);
        result.put("budgetName", budget.getBudgetName());
        result.put("totalBudget", budget.getTotalBudget());
        result.put("usedAmount", totalUsed);
        result.put("remainingAmount", budget.getTotalBudget().subtract(totalUsed));
        result.put("usageRate", usageRate);
        result.put("typeBreakdown", typeUsedMap);
        result.put("isOverBudget", usageRate.compareTo(BigDecimal.valueOf(100)) >= 0);
        result.put("isNearLimit", usageRate.compareTo(BigDecimal.valueOf(80)) >= 0);
        
        return result;
    }

    @Override
    public Map<String, Object> generateCostReport(LocalDate startDate, LocalDate endDate) {
        List<CostDetail> costs = costDetailMapper.selectList(
            new LambdaQueryWrapper<CostDetail>()
                .ge(CostDetail::getCostDate, startDate)
                .le(CostDetail::getCostDate, endDate)
                .eq(CostDetail::getDeleted, 0)
                .orderByAsc(CostDetail::getCostDate)
        );
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        Map<Integer, BigDecimal> typeAmountMap = new HashMap<>();
        Map<Integer, Integer> typeCountMap = new HashMap<>();
        Map<String, BigDecimal> dailyAmountMap = new HashMap<>();
        
        for (CostDetail cost : costs) {
            if (cost.getAmount() != null) {
                totalAmount = totalAmount.add(cost.getAmount());
                
                Integer costType = cost.getCostType();
                typeAmountMap.put(costType, typeAmountMap.getOrDefault(costType, BigDecimal.ZERO).add(cost.getAmount()));
                typeCountMap.put(costType, typeCountMap.getOrDefault(costType, 0) + 1);
                
                if (cost.getCostDate() != null) {
                    String dateStr = cost.getCostDate().toString();
                    dailyAmountMap.put(dateStr, dailyAmountMap.getOrDefault(dateStr, BigDecimal.ZERO).add(cost.getAmount()));
                }
            }
        }
        
        Map<String, Object> report = new HashMap<>();
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("totalAmount", totalAmount);
        report.put("recordCount", costs.size());
        report.put("typeAmountMap", typeAmountMap);
        report.put("typeCountMap", typeCountMap);
        report.put("dailyAmountMap", dailyAmountMap);
        report.put("fuelCost", typeAmountMap.getOrDefault(CostTypeEnum.FUEL.getCode(), BigDecimal.ZERO));
        report.put("maintenanceCost", typeAmountMap.getOrDefault(CostTypeEnum.MAINTENANCE.getCode(), BigDecimal.ZERO));
        report.put("laborCost", typeAmountMap.getOrDefault(CostTypeEnum.LABOR.getCode(), BigDecimal.ZERO));
        report.put("insuranceCost", typeAmountMap.getOrDefault(CostTypeEnum.INSURANCE.getCode(), BigDecimal.ZERO));
        report.put("depreciationCost", typeAmountMap.getOrDefault(CostTypeEnum.DEPRECIATION.getCode(), BigDecimal.ZERO));
        report.put("managementCost", typeAmountMap.getOrDefault(CostTypeEnum.MANAGEMENT.getCode(), BigDecimal.ZERO));
        report.put("otherCost", typeAmountMap.getOrDefault(CostTypeEnum.OTHER.getCode(), BigDecimal.ZERO));
        
        if (costs.size() > 0) {
            report.put("avgDailyCost", totalAmount.divide(BigDecimal.valueOf(dailyAmountMap.size()), 2, RoundingMode.HALF_UP));
        } else {
            report.put("avgDailyCost", BigDecimal.ZERO);
        }
        
        return report;
    }

    @Override
    public List<Map<String, Object>> getBudgetAlerts() {
        List<Map<String, Object>> alerts = new ArrayList<>();
        
        List<CostBudget> activeBudgets = costBudgetMapper.selectList(
            new LambdaQueryWrapper<CostBudget>()
                .eq(CostBudget::getStatus, BudgetStatusEnum.ACTIVE.getCode())
                .eq(CostBudget::getDeleted, 0)
        );
        
        for (CostBudget budget : activeBudgets) {
            try {
                Map<String, Object> usage = checkBudgetUsage(budget.getId());
                
                if ((Boolean) usage.get("isOverBudget")) {
                    Map<String, Object> alert = new HashMap<>();
                    alert.put("type", "OVER_BUDGET");
                    alert.put("level", "ERROR");
                    alert.put("budgetId", budget.getId());
                    alert.put("budgetName", budget.getBudgetName());
                    alert.put("message", String.format("预算超支：%s，已使用%.2f%%", 
                        budget.getBudgetName(), usage.get("usageRate")));
                    alert.put("usageRate", usage.get("usageRate"));
                    alert.put("overAmount", ((BigDecimal) usage.get("usedAmount")).subtract(budget.getTotalBudget()));
                    alerts.add(alert);
                } else if ((Boolean) usage.get("isNearLimit")) {
                    Map<String, Object> alert = new HashMap<>();
                    alert.put("type", "NEAR_LIMIT");
                    alert.put("level", "WARNING");
                    alert.put("budgetId", budget.getId());
                    alert.put("budgetName", budget.getBudgetName());
                    alert.put("message", String.format("预算即将用尽：%s，已使用%.2f%%", 
                        budget.getBudgetName(), usage.get("usageRate")));
                    alert.put("usageRate", usage.get("usageRate"));
                    alert.put("remainingAmount", usage.get("remainingAmount"));
                    alerts.add(alert);
                }
            } catch (Exception e) {
                log.error("检查预算使用情况失败：{}", budget.getId(), e);
            }
        }
        
        return alerts;
    }

    @Override
    public Map<String, Object> getCostTrend(LocalDate startDate, LocalDate endDate, String granularity) {
        List<CostDetail> costs = costDetailMapper.selectList(
            new LambdaQueryWrapper<CostDetail>()
                .ge(CostDetail::getCostDate, startDate)
                .le(CostDetail::getCostDate, endDate)
                .eq(CostDetail::getDeleted, 0)
                .orderByAsc(CostDetail::getCostDate)
        );
        
        Map<String, BigDecimal> trendMap = new LinkedHashMap<>();
        
        for (CostDetail cost : costs) {
            if (cost.getAmount() != null && cost.getCostDate() != null) {
                String key;
                LocalDate date = cost.getCostDate();
                
                if ("month".equals(granularity)) {
                    key = String.format("%d-%02d", date.getYear(), date.getMonthValue());
                } else if ("week".equals(granularity)) {
                    LocalDate weekStart = date.minusDays(date.getDayOfWeek().getValue() - 1);
                    key = weekStart.toString();
                } else {
                    key = date.toString();
                }
                
                trendMap.put(key, trendMap.getOrDefault(key, BigDecimal.ZERO).add(cost.getAmount()));
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("granularity", granularity);
        result.put("trendData", trendMap);
        result.put("labels", new ArrayList<>(trendMap.keySet()));
        result.put("values", new ArrayList<>(trendMap.values()));
        
        return result;
    }

    @Override
    public Map<String, Object> getEnergyConsumptionAnalysis(LocalDate startDate, LocalDate endDate) {
        log.info("开始能耗分析：{} 至 {}", startDate, endDate);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            BigDecimal totalFuelCost = BigDecimal.ZERO;
            BigDecimal totalFuelAmount = BigDecimal.ZERO;
            BigDecimal totalCargoWeight = BigDecimal.ZERO;
            BigDecimal totalDistance = BigDecimal.ZERO;
            
            List<CostDetail> fuelCosts = costDetailMapper.selectList(
                new LambdaQueryWrapper<CostDetail>()
                    .eq(CostDetail::getCostType, CostTypeEnum.FUEL.getCode())
                    .ge(CostDetail::getCostDate, startDate)
                    .le(CostDetail::getCostDate, endDate)
                    .eq(CostDetail::getDeleted, 0)
            );
            
            for (CostDetail cost : fuelCosts) {
                if (cost.getAmount() != null) {
                    totalFuelCost = totalFuelCost.add(cost.getAmount());
                }
                if (cost.getQuantity() != null) {
                    totalFuelAmount = totalFuelAmount.add(cost.getQuantity());
                }
            }
            
            try {
                var transportStatsResult = transportClient.getTransportStatistics(startDate.toString(), endDate.toString());
                if (transportStatsResult != null && transportStatsResult.getCode() == 200 && transportStatsResult.getData() != null) {
                    Map<String, Object> transportStats = transportStatsResult.getData();
                    if (transportStats.get("totalCargoWeight") != null) {
                        totalCargoWeight = new BigDecimal(transportStats.get("totalCargoWeight").toString());
                    }
                }
            } catch (Exception e) {
                log.warn("获取运输统计数据失败：{}", e.getMessage());
            }
            
            try {
                var tripStatsResult = tripClient.getStatistics(startDate.toString(), endDate.toString());
                if (tripStatsResult != null && tripStatsResult.getCode() == 200 && tripStatsResult.getData() != null) {
                    Map<String, Object> tripStats = tripStatsResult.getData();
                    if (tripStats.get("totalDistance") != null) {
                        totalDistance = new BigDecimal(tripStats.get("totalDistance").toString());
                    }
                }
            } catch (Exception e) {
                log.warn("获取行程统计数据失败：{}", e.getMessage());
            }
            
            BigDecimal fuelPerTonKm = BigDecimal.ZERO;
            if (totalCargoWeight.compareTo(BigDecimal.ZERO) > 0 && totalDistance.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal tonKm = totalCargoWeight.multiply(totalDistance.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP));
                if (tonKm.compareTo(BigDecimal.ZERO) > 0) {
                    fuelPerTonKm = totalFuelAmount.divide(tonKm, 4, RoundingMode.HALF_UP);
                }
            }
            
            BigDecimal costPerTonKm = BigDecimal.ZERO;
            if (totalCargoWeight.compareTo(BigDecimal.ZERO) > 0 && totalDistance.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal tonKm = totalCargoWeight.multiply(totalDistance.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP));
                if (tonKm.compareTo(BigDecimal.ZERO) > 0) {
                    costPerTonKm = totalFuelCost.divide(tonKm, 2, RoundingMode.HALF_UP);
                }
            }
            
            result.put("startDate", startDate);
            result.put("endDate", endDate);
            result.put("totalFuelCost", totalFuelCost);
            result.put("totalFuelAmount", totalFuelAmount);
            result.put("totalCargoWeight", totalCargoWeight);
            result.put("totalDistance", totalDistance);
            result.put("fuelPerTonKm", fuelPerTonKm);
            result.put("costPerTonKm", costPerTonKm);
            result.put("avgFuelCostPerDay", totalFuelCost.divide(BigDecimal.valueOf(startDate.until(endDate).getDays() + 1), 2, RoundingMode.HALF_UP));
            
        } catch (Exception e) {
            log.error("能耗分析失败", e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getVehicleUtilizationAnalysis(LocalDate startDate, LocalDate endDate) {
        log.info("开始车辆利用率分析：{} 至 {}", startDate, endDate);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            int totalVehicles = 0;
            try {
                var vehicleCountResult = vehicleClient.getVehicleCount();
                if (vehicleCountResult != null && vehicleCountResult.getCode() == 200 && vehicleCountResult.getData() != null) {
                    totalVehicles = vehicleCountResult.getData();
                }
            } catch (Exception e) {
                log.warn("获取车辆数量失败：{}", e.getMessage());
            }
            
            List<Map<String, Object>> vehicleStats = null;
            try {
                var vehicleStatsResult = transportClient.getVehicleStatistics(startDate.toString(), endDate.toString());
                if (vehicleStatsResult != null && vehicleStatsResult.getCode() == 200 && vehicleStatsResult.getData() != null) {
                    vehicleStats = vehicleStatsResult.getData();
                }
            } catch (Exception e) {
                log.warn("获取车辆统计数据失败：{}", e.getMessage());
            }
            
            int activeVehicles = 0;
            BigDecimal avgUtilizationRate = BigDecimal.ZERO;
            BigDecimal avgIdleRate = BigDecimal.ZERO;
            
            if (vehicleStats != null) {
                activeVehicles = (int) vehicleStats.stream()
                    .map(s -> s.get("vehicleId"))
                    .distinct()
                    .count();
                
                BigDecimal totalUtilization = BigDecimal.ZERO;
                BigDecimal totalIdleRate = BigDecimal.ZERO;
                int count = 0;
                
                for (Map<String, Object> stat : vehicleStats) {
                    if (stat.get("utilizationRate") != null) {
                        totalUtilization = totalUtilization.add(new BigDecimal(stat.get("utilizationRate").toString()));
                    }
                    if (stat.get("idleRate") != null) {
                        totalIdleRate = totalIdleRate.add(new BigDecimal(stat.get("idleRate").toString()));
                    }
                    count++;
                }
                
                if (count > 0) {
                    avgUtilizationRate = totalUtilization.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
                    avgIdleRate = totalIdleRate.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
                }
            }
            
            BigDecimal attendanceRate = BigDecimal.ZERO;
            if (totalVehicles > 0) {
                attendanceRate = BigDecimal.valueOf(activeVehicles * 100.0 / totalVehicles)
                    .setScale(2, RoundingMode.HALF_UP);
            }
            
            result.put("startDate", startDate);
            result.put("endDate", endDate);
            result.put("totalVehicles", totalVehicles);
            result.put("activeVehicles", activeVehicles);
            result.put("attendanceRate", attendanceRate);
            result.put("avgUtilizationRate", avgUtilizationRate);
            result.put("avgIdleRate", avgIdleRate);
            result.put("idleVehicles", totalVehicles - activeVehicles);
            
        } catch (Exception e) {
            log.error("车辆利用率分析失败", e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getIdleRateAnalysis(LocalDate startDate, LocalDate endDate) {
        log.info("开始空载率分析：{} 至 {}", startDate, endDate);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            BigDecimal totalDistance = BigDecimal.ZERO;
            BigDecimal loadedDistance = BigDecimal.ZERO;
            BigDecimal emptyDistance = BigDecimal.ZERO;
            int totalTrips = 0;
            int loadedTrips = 0;
            int emptyTrips = 0;
            
            try {
                var tripStatsResult = tripClient.getStatistics(startDate.toString(), endDate.toString());
                if (tripStatsResult != null && tripStatsResult.getCode() == 200 && tripStatsResult.getData() != null) {
                    Map<String, Object> tripStats = tripStatsResult.getData();
                    if (tripStats.get("totalDistance") != null) {
                        totalDistance = new BigDecimal(tripStats.get("totalDistance").toString());
                    }
                    if (tripStats.get("loadedDistance") != null) {
                        loadedDistance = new BigDecimal(tripStats.get("loadedDistance").toString());
                    }
                    if (tripStats.get("emptyDistance") != null) {
                        emptyDistance = new BigDecimal(tripStats.get("emptyDistance").toString());
                    }
                    if (tripStats.get("totalTrips") != null) {
                        totalTrips = ((Number) tripStats.get("totalTrips")).intValue();
                    }
                    if (tripStats.get("loadedTrips") != null) {
                        loadedTrips = ((Number) tripStats.get("loadedTrips")).intValue();
                    }
                    if (tripStats.get("emptyTrips") != null) {
                        emptyTrips = ((Number) tripStats.get("emptyTrips")).intValue();
                    }
                }
            } catch (Exception e) {
                log.warn("获取行程统计数据失败：{}", e.getMessage());
            }
            
            BigDecimal idleRate = BigDecimal.ZERO;
            if (totalDistance.compareTo(BigDecimal.ZERO) > 0) {
                idleRate = emptyDistance.divide(totalDistance, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            }
            
            BigDecimal loadRate = BigDecimal.ZERO;
            if (totalTrips > 0) {
                loadRate = BigDecimal.valueOf(loadedTrips * 100.0 / totalTrips)
                    .setScale(2, RoundingMode.HALF_UP);
            }
            
            result.put("startDate", startDate);
            result.put("endDate", endDate);
            result.put("totalDistance", totalDistance);
            result.put("loadedDistance", loadedDistance);
            result.put("emptyDistance", emptyDistance);
            result.put("idleRate", idleRate);
            result.put("totalTrips", totalTrips);
            result.put("loadedTrips", loadedTrips);
            result.put("emptyTrips", emptyTrips);
            result.put("loadRate", loadRate);
            
        } catch (Exception e) {
            log.error("空载率分析失败", e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getOverallCostAnalysis(LocalDate startDate, LocalDate endDate) {
        log.info("开始总体成本分析：{} 至 {}", startDate, endDate);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> energyAnalysis = getEnergyConsumptionAnalysis(startDate, endDate);
            Map<String, Object> utilizationAnalysis = getVehicleUtilizationAnalysis(startDate, endDate);
            Map<String, Object> idleAnalysis = getIdleRateAnalysis(startDate, endDate);
            Map<String, Object> costReport = generateCostReport(startDate, endDate);
            
            result.put("startDate", startDate);
            result.put("endDate", endDate);
            result.put("energyAnalysis", energyAnalysis);
            result.put("utilizationAnalysis", utilizationAnalysis);
            result.put("idleAnalysis", idleAnalysis);
            result.put("costReport", costReport);
            
            BigDecimal totalCost = (BigDecimal) costReport.getOrDefault("totalAmount", BigDecimal.ZERO);
            BigDecimal fuelCost = (BigDecimal) costReport.getOrDefault("fuelCost", BigDecimal.ZERO);
            BigDecimal maintenanceCost = (BigDecimal) costReport.getOrDefault("maintenanceCost", BigDecimal.ZERO);
            BigDecimal laborCost = (BigDecimal) costReport.getOrDefault("laborCost", BigDecimal.ZERO);
            
            result.put("totalCost", totalCost);
            result.put("fuelCost", fuelCost);
            result.put("maintenanceCost", maintenanceCost);
            result.put("laborCost", laborCost);
            
            if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
                result.put("fuelCostRatio", fuelCost.multiply(BigDecimal.valueOf(100)).divide(totalCost, 2, RoundingMode.HALF_UP));
                result.put("maintenanceCostRatio", maintenanceCost.multiply(BigDecimal.valueOf(100)).divide(totalCost, 2, RoundingMode.HALF_UP));
                result.put("laborCostRatio", laborCost.multiply(BigDecimal.valueOf(100)).divide(totalCost, 2, RoundingMode.HALF_UP));
            } else {
                result.put("fuelCostRatio", BigDecimal.ZERO);
                result.put("maintenanceCostRatio", BigDecimal.ZERO);
                result.put("laborCostRatio", BigDecimal.ZERO);
            }
            
            List<Map<String, Object>> budgetAlerts = getBudgetAlerts();
            result.put("budgetAlerts", budgetAlerts);
            result.put("alertCount", budgetAlerts.size());
            
        } catch (Exception e) {
            log.error("总体成本分析失败", e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    private String generateCostNo() {
        return "COST" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private String generateBudgetNo() {
        return "BUDGET" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private BigDecimal calculateTotalBudget(CostBudget budget) {
        BigDecimal total = BigDecimal.ZERO;
        if (budget.getFuelBudget() != null) total = total.add(budget.getFuelBudget());
        if (budget.getMaintenanceBudget() != null) total = total.add(budget.getMaintenanceBudget());
        if (budget.getLaborBudget() != null) total = total.add(budget.getLaborBudget());
        if (budget.getInsuranceBudget() != null) total = total.add(budget.getInsuranceBudget());
        if (budget.getDepreciationBudget() != null) total = total.add(budget.getDepreciationBudget());
        if (budget.getManagementBudget() != null) total = total.add(budget.getManagementBudget());
        if (budget.getOtherBudget() != null) total = total.add(budget.getOtherBudget());
        return total;
    }

    private CostDetailVO convertToCostDetailVO(CostDetail entity) {
        CostDetailVO vo = new CostDetailVO();
        BeanUtils.copyProperties(entity, vo);
        
        if (entity.getCostType() != null) {
            CostTypeEnum typeEnum = CostTypeEnum.getByCode(entity.getCostType());
            if (typeEnum != null) {
                vo.setCostTypeName(typeEnum.getName());
            }
        }
        
        return vo;
    }

    private SalaryConfigVO convertToSalaryConfigVO(SalaryConfig entity) {
        SalaryConfigVO vo = new SalaryConfigVO();
        BeanUtils.copyProperties(entity, vo);
        
        if (entity.getStatus() != null) {
            vo.setStatusName(entity.getStatus() == 1 ? "启用" : "禁用");
        }
        
        return vo;
    }

    private CostBudgetVO convertToCostBudgetVO(CostBudget entity) {
        CostBudgetVO vo = new CostBudgetVO();
        BeanUtils.copyProperties(entity, vo);
        
        if (entity.getBudgetType() != null) {
            BudgetTypeEnum typeEnum = BudgetTypeEnum.getByCode(entity.getBudgetType());
            if (typeEnum != null) {
                vo.setBudgetTypeName(typeEnum.getName());
            }
        }
        
        if (entity.getStatus() != null) {
            BudgetStatusEnum statusEnum = BudgetStatusEnum.getByCode(entity.getStatus());
            if (statusEnum != null) {
                vo.setStatusName(statusEnum.getName());
            }
        }
        
        vo.setUsedAmount(BigDecimal.ZERO);
        vo.setRemainingAmount(entity.getTotalBudget());
        
        return vo;
    }
    
    @Override
    public Long calculateAndRecordTripCommission(Long tripId, Long driverId, double estimatedAmount) {
        log.info("计算并记录 Trip 提成：tripId={}, driverId={}, estimatedAmount={}", tripId, driverId, estimatedAmount);
        
        int pythonScore = 60;
        double commission = 0;
        
        try {
            pythonScore = pythonClient.analyzeDrivingBehavior(tripId);
            log.info("Python 评分结果：tripId={}, score={}", tripId, pythonScore);
        } catch (Exception e) {
            log.warn("调用 Python 服务分析驾驶行为失败，使用默认分数60：tripId={}, error={}", tripId, e.getMessage());
        }
        
        try {
            commission = estimatedAmount * (pythonScore / 100.0);
            log.info("提成计算：预计金额={}元，Python 得分={}, 提成={}元", estimatedAmount, pythonScore, commission);
            
            CostDetailDTO dto = new CostDetailDTO();
            dto.setCostType(3);
            dto.setCostName("Trip 任务提成");
            dto.setAmount(BigDecimal.valueOf(commission));
            dto.setUserId(driverId);
            dto.setTripId(tripId);
            dto.setCostDate(LocalDate.now());
            dto.setRemark(String.format("Python 评分=%d, 提成比例=%d%%", pythonScore, pythonScore));
            
            CostDetailVO vo = addCostDetail(dto);
            log.info("Trip 提成记录成功：tripId={}, costId={}, commission={}元", tripId, vo.getId(), commission);
            
            return vo.getId();
            
        } catch (Exception e) {
            log.error("保存 Trip 提成记录失败：tripId={}, driverId={}", tripId, driverId, e);
            return null;
        }
    }
}
