package com.klzw.service.trip.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 行程统计响应 DTO
 */
@Data
public class TripStatisticsResponseDTO {
    
    /**
     * 行程数量
     */
    private Integer tripCount;
    
    /**
     * 总行驶距离 (公里)
     */
    private BigDecimal totalDistance;
    
    /**
     * 总行驶时长 (小时)
     */
    private BigDecimal totalDuration;
    
    /**
     * 完成行程数
     */
    private Integer completedTripCount;
    
    /**
     * 取消行程数
     */
    private Integer cancelledTripCount;
    
    /**
     * 平均速度 (km/h)
     */
    private BigDecimal averageSpeed;
    
    /**
     * 燃油消耗 (升)
     */
    private BigDecimal fuelConsumption;
    
    /**
     * 货物运输量 (吨)
     */
    private BigDecimal cargoWeight;
}
