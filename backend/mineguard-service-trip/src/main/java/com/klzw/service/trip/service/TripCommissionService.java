package com.klzw.service.trip.service;

/**
 * Trip 任务提成服务
 */
public interface TripCommissionService {
    
    /**
     * 计算 Trip 任务提成
     * 公式：提成 = 预计金额 × (Python 得分 / 100)
     * 
     * @param tripId 行程 ID
     * @param pythonScore Python 计算的驾驶行为评分（0-100）
     * @param estimatedAmount 预计金额（调度时设定）
     * @return 提成金额（元）
     */
    double calculateTripCommission(Long tripId, int pythonScore, double estimatedAmount);
}
