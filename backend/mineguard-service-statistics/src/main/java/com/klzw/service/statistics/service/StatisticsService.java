package com.klzw.service.statistics.service;

import com.klzw.service.statistics.entity.TripStatistics;
import com.klzw.service.statistics.entity.CostStatistics;

import java.time.LocalDate;
import java.util.List;

public interface StatisticsService {

    /**
     * 计算并保存行程统计数据
     * @param date 统计日期
     * @return 行程统计数据
     */
    TripStatistics calculateTripStatistics(LocalDate date);

    /**
     * 计算并保存成本统计数据
     * @param date 统计日期
     * @return 成本统计数据
     */
    CostStatistics calculateCostStatistics(LocalDate date);

    /**
     * 获取行程统计数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 行程统计数据列表
     */
    List<TripStatistics> getTripStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 获取成本统计数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 成本统计数据列表
     */
    List<CostStatistics> getCostStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 获取总体统计数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 总体统计数据
     */
    Object getOverallStatistics(LocalDate startDate, LocalDate endDate);
}