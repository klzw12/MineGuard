package com.klzw.service.ai.service;

import java.util.Map;

public interface AiService {

    /**
     * 分析统计数据
     * @param statisticsData 统计数据
     * @return 分析结果
     */
    Map<String, Object> analyzeStatisticsData(Map<String, Object> statisticsData);

    /**
     * 分析成本数据
     * @param costData 成本数据
     * @return 分析结果
     */
    Map<String, Object> analyzeCostData(Map<String, Object> costData);

    /**
     * 生成财务报表
     * @param financialData 财务数据
     * @return 报表数据
     */
    Map<String, Object> generateFinancialReport(Map<String, Object> financialData);

    /**
     * 生成优化建议
     * @param analysisData 分析数据
     * @return 优化建议
     */
    Map<String, Object> generateOptimizationSuggestions(Map<String, Object> analysisData);

    /**
     * 为调度服务提供智能建议
     * @param dispatchData 调度数据
     * @return 调度建议
     */
    Map<String, Object> generateDispatchSuggestions(Map<String, Object> dispatchData);

    /**
     * 获取当前使用的AI提供商
     * @return 当前AI提供商
     */
    String getCurrentProvider();

    /**
     * 切换AI提供商
     * @param provider 新的AI提供商
     * @return 切换结果
     */
    boolean switchProvider(String provider);

    /**
     * 分析驾驶行为
     * @param trackData 轨迹数据
     * @return 分析结果
     */
    Map<String, Object> analyzeDrivingBehavior(Map<String, Object> trackData);
}