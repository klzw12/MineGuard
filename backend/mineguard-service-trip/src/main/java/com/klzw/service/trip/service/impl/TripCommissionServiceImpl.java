package com.klzw.service.trip.service.impl;

import com.klzw.service.trip.entity.Trip;
import com.klzw.service.trip.mapper.TripMapper;
import com.klzw.service.trip.service.TripCommissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Trip 任务提成服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TripCommissionServiceImpl implements TripCommissionService {

    private final TripMapper tripMapper;
    
    // 提成配置
    private static final double MIN_COMMISSION_RATE = 0.0;  // 最低提成比例（0%）
    private static final double MAX_COMMISSION_RATE = 1.0;  // 最高提成比例（100%）

    @Override
    public double calculateTripCommission(Long tripId, int pythonScore, double estimatedAmount) {
        // 验证 Trip 是否存在
        Trip trip = tripMapper.selectById(tripId);
        if (trip == null) {
            throw new RuntimeException("行程不存在");
        }
        
        // 计算提成比例（Python 得分百分比）
        double commissionRate = Math.max(MIN_COMMISSION_RATE, 
                               Math.min(MAX_COMMISSION_RATE, pythonScore / 100.0));
        
        // 计算提成金额
        double commission = estimatedAmount * commissionRate;
        
        log.info("Trip 提成计算：行程 ID={}, Python 得分={}, 提成比例={}%, 预计金额={}元，提成={}元",
                tripId, pythonScore, commissionRate * 100, estimatedAmount, commission);
        
        return commission;
    }
}
