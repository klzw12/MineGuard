package com.klzw.service.statistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.klzw.common.core.client.VehicleClient;
import com.klzw.service.statistics.dto.StatisticsQueryDTO;
import com.klzw.service.statistics.entity.FaultStatistics;
import com.klzw.service.statistics.mapper.FaultStatisticsMapper;
import com.klzw.service.statistics.service.FaultStatisticsService;
import com.klzw.service.statistics.vo.FaultStatisticsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FaultStatisticsServiceImpl implements FaultStatisticsService {

    private final FaultStatisticsMapper faultStatisticsMapper;
    private final VehicleClient vehicleClient;

    @Override
    public FaultStatisticsVO calculateFaultStatistics(Long vehicleId, String date) {
        LocalDate statisticsDate = LocalDate.parse(date);
        
        FaultStatistics existing = faultStatisticsMapper.selectOne(
            new LambdaQueryWrapper<FaultStatistics>()
                .eq(FaultStatistics::getVehicleId, vehicleId)
                .eq(FaultStatistics::getStatisticsDate, statisticsDate)
        );
        
        if (existing != null) {
            faultStatisticsMapper.deleteById(existing.getId());
        }
        
        FaultStatistics entity = new FaultStatistics();
        entity.setVehicleId(vehicleId);
        entity.setStatisticsDate(statisticsDate);
        
        try {
            Map<String, Object> response = vehicleClient.getFaultStatistics(vehicleId, date);
            
            if (response != null) {
                entity.setFaultCount(getIntValue(response, "faultCount"));
                entity.setMinorFaultCount(getIntValue(response, "minorFaultCount"));
                entity.setMajorFaultCount(getIntValue(response, "majorFaultCount"));
                entity.setCriticalFaultCount(getIntValue(response, "criticalFaultCount"));
                entity.setTotalRepairCost(getBigDecimalValue(response, "totalRepairCost"));
                entity.setAvgRepairTime(getBigDecimalValue(response, "avgRepairTime"));
                entity.setTopFaultType((String) response.get("topFaultType"));
                entity.setTopFaultCount(getIntValue(response, "topFaultCount"));
                entity.setRepairedCount(getIntValue(response, "repairedCount"));
                entity.setPendingCount(getIntValue(response, "pendingCount"));
            }
        } catch (Exception e) {
            log.warn("获取车辆故障统计数据失败：车辆 ID={}, 错误={}", vehicleId, e.getMessage());
            entity.setFaultCount(0);
            entity.setMinorFaultCount(0);
            entity.setMajorFaultCount(0);
            entity.setCriticalFaultCount(0);
            entity.setTotalRepairCost(BigDecimal.ZERO);
            entity.setAvgRepairTime(BigDecimal.ZERO);
            entity.setRepairedCount(0);
            entity.setPendingCount(0);
        }
        
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        
        faultStatisticsMapper.insert(entity);
        log.info("计算故障统计数据：车辆ID={}, 日期={}, 故障数={}", vehicleId, date, entity.getFaultCount());
        
        return convertToVO(entity);
    }

    @Override
    public FaultStatisticsVO getFaultStatistics(Long vehicleId, LocalDate date) {
        FaultStatistics entity = faultStatisticsMapper.selectOne(
            new LambdaQueryWrapper<FaultStatistics>()
                .eq(vehicleId != null, FaultStatistics::getVehicleId, vehicleId)
                .eq(FaultStatistics::getStatisticsDate, date)
        );
        
        return entity != null ? convertToVO(entity) : null;
    }

    @Override
    public List<FaultStatisticsVO> getFaultStatisticsList(StatisticsQueryDTO queryDTO) {
        LambdaQueryWrapper<FaultStatistics> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getVehicleId() != null) {
            wrapper.eq(FaultStatistics::getVehicleId, queryDTO.getVehicleId());
        }
        if (queryDTO.getStartDate() != null) {
            wrapper.ge(FaultStatistics::getStatisticsDate, queryDTO.getStartDate());
        }
        if (queryDTO.getEndDate() != null) {
            wrapper.le(FaultStatistics::getStatisticsDate, queryDTO.getEndDate());
        }
        
        wrapper.orderByDesc(FaultStatistics::getStatisticsDate);
        
        List<FaultStatistics> list = faultStatisticsMapper.selectList(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public FaultStatisticsVO getOverallFaultStatistics(LocalDate startDate, LocalDate endDate) {
        List<FaultStatistics> list = faultStatisticsMapper.findByDateRange(startDate, endDate);
        
        if (list.isEmpty()) {
            return new FaultStatisticsVO();
        }
        
        int totalFaultCount = 0;
        int totalMinorFault = 0;
        int totalMajorFault = 0;
        int totalCriticalFault = 0;
        BigDecimal totalCost = BigDecimal.ZERO;
        int totalRepaired = 0;
        int totalPending = 0;
        Map<String, Integer> faultTypeCount = new HashMap<>();
        
        for (FaultStatistics stat : list) {
            if (stat.getFaultCount() != null) totalFaultCount += stat.getFaultCount();
            if (stat.getMinorFaultCount() != null) totalMinorFault += stat.getMinorFaultCount();
            if (stat.getMajorFaultCount() != null) totalMajorFault += stat.getMajorFaultCount();
            if (stat.getCriticalFaultCount() != null) totalCriticalFault += stat.getCriticalFaultCount();
            if (stat.getTotalRepairCost() != null) totalCost = totalCost.add(stat.getTotalRepairCost());
            if (stat.getRepairedCount() != null) totalRepaired += stat.getRepairedCount();
            if (stat.getPendingCount() != null) totalPending += stat.getPendingCount();
            
            if (stat.getTopFaultType() != null) {
                faultTypeCount.merge(stat.getTopFaultType(), 
                    stat.getTopFaultCount() != null ? stat.getTopFaultCount() : 1, Integer::sum);
            }
        }
        
        FaultStatisticsVO vo = new FaultStatisticsVO();
        vo.setTotalFaultCount(totalFaultCount);
        vo.setFaultCount(totalFaultCount);
        vo.setMinorFaultCount(totalMinorFault);
        vo.setMajorFaultCount(totalMajorFault);
        vo.setCriticalFaultCount(totalCriticalFault);
        vo.setTotalCost(totalCost);
        vo.setTotalRepairCost(totalCost);
        vo.setRepairedCount(totalRepaired);
        vo.setPendingCount(totalPending);
        
        String topFaultType = faultTypeCount.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
        vo.setTopFaultType(topFaultType);
        
        Map<String, Integer> levelDistribution = new HashMap<>();
        levelDistribution.put("轻微故障", totalMinorFault);
        levelDistribution.put("一般故障", totalMajorFault);
        levelDistribution.put("严重故障", totalCriticalFault);
        vo.setFaultLevelDistribution(levelDistribution);
        
        vo.setFaultTypeDistribution(getFaultTypeDistribution(startDate, endDate));
        vo.setFaultTrend(getFaultTrend(startDate, endDate));
        
        return vo;
    }

    @Override
    public List<FaultStatisticsVO.FaultTypeDistribution> getFaultTypeDistribution(LocalDate startDate, LocalDate endDate) {
        List<FaultStatistics> list = faultStatisticsMapper.findByDateRange(startDate, endDate);
        
        Map<String, Integer> typeCountMap = new HashMap<>();
        int totalCount = 0;
        
        for (FaultStatistics stat : list) {
            if (stat.getTopFaultType() != null) {
                int count = stat.getTopFaultCount() != null ? stat.getTopFaultCount() : 1;
                typeCountMap.merge(stat.getTopFaultType(), count, Integer::sum);
                totalCount += count;
            }
        }
        
        List<FaultStatisticsVO.FaultTypeDistribution> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : typeCountMap.entrySet()) {
            FaultStatisticsVO.FaultTypeDistribution dist = new FaultStatisticsVO.FaultTypeDistribution();
            dist.setFaultType(entry.getKey());
            dist.setCount(entry.getValue());
            if (totalCount > 0) {
                dist.setPercentage(BigDecimal.valueOf(entry.getValue() * 100.0 / totalCount)
                    .setScale(2, RoundingMode.HALF_UP));
            } else {
                dist.setPercentage(BigDecimal.ZERO);
            }
            result.add(dist);
        }
        
        result.sort((a, b) -> b.getCount().compareTo(a.getCount()));
        return result.stream().limit(10).collect(Collectors.toList());
    }

    @Override
    public List<FaultStatisticsVO.FaultTrendItem> getFaultTrend(LocalDate startDate, LocalDate endDate) {
        List<FaultStatistics> list = faultStatisticsMapper.findByDateRange(startDate, endDate);
        
        Map<LocalDate, Integer> countByDate = new HashMap<>();
        Map<LocalDate, BigDecimal> costByDate = new HashMap<>();
        
        for (FaultStatistics stat : list) {
            LocalDate date = stat.getStatisticsDate();
            countByDate.merge(date, stat.getFaultCount() != null ? stat.getFaultCount() : 0, Integer::sum);
            costByDate.merge(date, stat.getTotalRepairCost() != null ? stat.getTotalRepairCost() : BigDecimal.ZERO, BigDecimal::add);
        }
        
        List<FaultStatisticsVO.FaultTrendItem> result = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            FaultStatisticsVO.FaultTrendItem item = new FaultStatisticsVO.FaultTrendItem();
            item.setDate(current);
            item.setFaultCount(countByDate.getOrDefault(current, 0));
            item.setRepairCost(costByDate.getOrDefault(current, BigDecimal.ZERO));
            result.add(item);
            current = current.plusDays(1);
        }
        
        return result;
    }

    @Override
    public void calculateDailyFaultStatistics(LocalDate date) {
        log.info("开始计算每日故障统计：日期={}", date);
        
        try {
            List<Long> vehicleIds = vehicleClient.getVehicleIds();
            
            if (vehicleIds != null) {
                for (Long vehicleId : vehicleIds) {
                    try {
                        calculateFaultStatistics(vehicleId, date.toString());
                    } catch (Exception e) {
                        log.error("计算车辆故障统计失败：车辆 ID={}", vehicleId, e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取车辆列表失败", e);
        }
        
        log.info("每日故障统计计算完成：日期={}", date);
    }

    private FaultStatisticsVO convertToVO(FaultStatistics entity) {
        FaultStatisticsVO vo = new FaultStatisticsVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private Integer getIntValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) return 0;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }

    private BigDecimal getBigDecimalValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return BigDecimal.ZERO;
    }
}
