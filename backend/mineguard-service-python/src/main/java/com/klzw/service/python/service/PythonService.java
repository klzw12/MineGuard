package com.klzw.service.python.service;

import java.util.Map;

/**
 * Python服务接口
 * 用于调用Python服务的功能，包括数据清洗、报表导出、数据分析
 */
public interface PythonService {

    /**
     * 清洗驾驶数据
     * 
     * @param drivingData 驾驶数据
     * @return 清洗结果
     */
    Map<String, Object> cleanDrivingData(Map<String, Object> drivingData);

    /**
     * 清洗统计数据
     * 
     * @param statisticsData 统计数据
     * @return 清洗结果
     */
    Map<String, Object> cleanStatisticsData(Map<String, Object> statisticsData);

    /**
     * 清洗成本数据
     * 
     * @param costData 成本数据
     * @return 清洗结果
     */
    Map<String, Object> cleanCostData(Map<String, Object> costData);

    /**
     * 分析驾驶行为
     * 
     * @param trackData 轨迹数据
     * @return 分析结果
     */
    Map<String, Object> analyzeDrivingBehavior(Map<String, Object> trackData);

    /**
     * 通过行程ID分析驾驶行为
     * 
     * @param tripId 行程ID
     * @return 分析结果
     */
    int analyzeDrivingBehavior(Long tripId);

    /**
     * 分析成本
     * 
     * @param costData 成本数据
     * @return 分析结果
     */
    Map<String, Object> analyzeCost(Map<String, Object> costData);

    /**
     * 分析车辆效率
     * 
     * @param vehicleData 车辆数据
     * @return 分析结果
     */
    Map<String, Object> analyzeVehicleEfficiency(Map<String, Object> vehicleData);

    /**
     * 导出统计报表
     * 
     * @param exportRequest 导出请求
     * @return 报表数据
     */
    byte[] exportStatistics(Map<String, Object> exportRequest);

    /**
     * 导出行程报表
     * 
     * @param exportRequest 导出请求
     * @return 报表数据
     */
    byte[] exportTripReport(Map<String, Object> exportRequest);

    /**
     * 导出成本报表
     * 
     * @param exportRequest 导出请求
     * @return 报表数据
     */
    byte[] exportCostReport(Map<String, Object> exportRequest);

    /**
     * 导出车辆报表
     * 
     * @param exportRequest 导出请求
     * @return 报表数据
     */
    byte[] exportVehicleReport(Map<String, Object> exportRequest);

    /**
     * 导出司机报表
     * 
     * @param exportRequest 导出请求
     * @return 报表数据
     */
    byte[] exportDriverReport(Map<String, Object> exportRequest);

    /**
     * 健康检查
     * 
     * @return 健康状态
     */
    Map<String, Object> healthCheck();

}