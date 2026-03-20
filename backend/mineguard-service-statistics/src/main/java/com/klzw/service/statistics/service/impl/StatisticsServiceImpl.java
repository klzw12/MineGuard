package com.klzw.service.statistics.service.impl;

import com.klzw.service.statistics.entity.TripStatistics;
import com.klzw.service.statistics.entity.CostStatistics;
import com.klzw.service.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    @Override
    public TripStatistics calculateTripStatistics(LocalDate date) {
        TripStatistics statistics = new TripStatistics();
        statistics.setStatisticsDate(date);
        statistics.setTripCount(10); // 模拟数据
        statistics.setTotalDistance(new BigDecimal(500)); // 模拟数据
        statistics.setTotalDuration(new BigDecimal(10)); // 模拟数据
        statistics.setCompletedTripCount(8); // 模拟数据
        statistics.setCancelledTripCount(2); // 模拟数据
        statistics.setAverageSpeed(new BigDecimal(50)); // 模拟数据
        statistics.setFuelConsumption(new BigDecimal(50)); // 模拟数据
        statistics.setCreateTime(LocalDateTime.now());
        statistics.setUpdateTime(LocalDateTime.now());
        
        log.info("计算行程统计数据：日期={}, 行程数={}, 总距离={}", 
                date, statistics.getTripCount(), statistics.getTotalDistance());
        
        // 这里应该使用MyBatis-Plus或其他ORM框架保存到数据库
        // 暂时返回模拟数据
        return statistics;
    }

    @Override
    public CostStatistics calculateCostStatistics(LocalDate date) {
        CostStatistics statistics = new CostStatistics();
        statistics.setStatisticsDate(date);
        statistics.setFuelCost(new BigDecimal(2000)); // 模拟数据
        statistics.setMaintenanceCost(new BigDecimal(500)); // 模拟数据
        statistics.setLaborCost(new BigDecimal(3000)); // 模拟数据
        statistics.setOtherCost(new BigDecimal(500)); // 模拟数据
        statistics.setTotalCost(statistics.getFuelCost().add(statistics.getMaintenanceCost())
                .add(statistics.getLaborCost()).add(statistics.getOtherCost()));
        statistics.setCreateTime(LocalDateTime.now());
        statistics.setUpdateTime(LocalDateTime.now());
        
        log.info("计算成本统计数据：日期={}, 总成本={}", 
                date, statistics.getTotalCost());
        
        // 这里应该使用MyBatis-Plus或其他ORM框架保存到数据库
        // 暂时返回模拟数据
        return statistics;
    }

    @Override
    public List<TripStatistics> getTripStatistics(LocalDate startDate, LocalDate endDate) {
        List<TripStatistics> statisticsList = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            TripStatistics statistics = calculateTripStatistics(currentDate);
            statisticsList.add(statistics);
            currentDate = currentDate.plusDays(1);
        }
        
        return statisticsList;
    }

    @Override
    public List<CostStatistics> getCostStatistics(LocalDate startDate, LocalDate endDate) {
        List<CostStatistics> statisticsList = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            CostStatistics statistics = calculateCostStatistics(currentDate);
            statisticsList.add(statistics);
            currentDate = currentDate.plusDays(1);
        }
        
        return statisticsList;
    }

    @Override
    public Object getOverallStatistics(LocalDate startDate, LocalDate endDate) {
        List<TripStatistics> tripStatisticsList = getTripStatistics(startDate, endDate);
        List<CostStatistics> costStatisticsList = getCostStatistics(startDate, endDate);
        
        // 计算总体统计数据
        int totalTripCount = 0;
        BigDecimal totalDistance = BigDecimal.ZERO;
        BigDecimal totalDuration = BigDecimal.ZERO;
        int totalCompletedTripCount = 0;
        int totalCancelledTripCount = 0;
        BigDecimal totalFuelCost = BigDecimal.ZERO;
        BigDecimal totalMaintenanceCost = BigDecimal.ZERO;
        BigDecimal totalLaborCost = BigDecimal.ZERO;
        BigDecimal totalOtherCost = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        
        for (TripStatistics tripStats : tripStatisticsList) {
            totalTripCount += tripStats.getTripCount();
            totalDistance = totalDistance.add(tripStats.getTotalDistance());
            totalDuration = totalDuration.add(tripStats.getTotalDuration());
            totalCompletedTripCount += tripStats.getCompletedTripCount();
            totalCancelledTripCount += tripStats.getCancelledTripCount();
        }
        
        for (CostStatistics costStats : costStatisticsList) {
            totalFuelCost = totalFuelCost.add(costStats.getFuelCost());
            totalMaintenanceCost = totalMaintenanceCost.add(costStats.getMaintenanceCost());
            totalLaborCost = totalLaborCost.add(costStats.getLaborCost());
            totalOtherCost = totalOtherCost.add(costStats.getOtherCost());
            totalCost = totalCost.add(costStats.getTotalCost());
        }
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("totalTripCount", totalTripCount);
        result.put("totalDistance", totalDistance);
        result.put("totalDuration", totalDuration);
        result.put("totalCompletedTripCount", totalCompletedTripCount);
        result.put("totalCancelledTripCount", totalCancelledTripCount);
        result.put("totalFuelCost", totalFuelCost);
        result.put("totalMaintenanceCost", totalMaintenanceCost);
        result.put("totalLaborCost", totalLaborCost);
        result.put("totalOtherCost", totalOtherCost);
        result.put("totalCost", totalCost);
        
        // 调用 Python 服务进行数据清洗
        try {
            // 准备分析数据
            List<Map<String, Object>> statisticsData = new ArrayList<>();
            for (TripStatistics tripStats : tripStatisticsList) {
                Map<String, Object> data = new HashMap<>();
                data.put("date", tripStats.getStatisticsDate().toString());
                data.put("tripCount", tripStats.getTripCount());
                data.put("distance", tripStats.getTotalDistance().doubleValue());
                data.put("duration", tripStats.getTotalDuration().doubleValue());
                statisticsData.add(data);
            }
            
            // 构建请求数据
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("data", statisticsData);
            
            // 转换为 JSON
            String jsonData = com.klzw.common.core.util.JsonUtils.toJson(requestData);
            
            // 调用 Python 服务进行数据清洗
            String url = "http://python-service:8008/api/clean/statistics-data";
            String response = com.klzw.common.core.util.HttpUtils.postJson(url, jsonData);
            
            // 解析响应
            Map<String, Object> cleanResult = com.klzw.common.core.util.JsonUtils.fromJson(response, Map.class);
            result.put("cleaning_report", cleanResult.get("cleaning_report"));
            
            log.info("调用 Python 服务进行数据清洗成功");
        } catch (Exception e) {
            log.warn("调用 Python 服务进行数据清洗失败: {}", e.getMessage());
            // 失败时不影响原有功能
        }
        
        return result;
    }
}