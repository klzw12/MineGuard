package com.klzw.service.vehicle.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 车辆故障统计响应 DTO
 */
@Data
public class VehicleFaultStatisticsResponseDTO {
    
    /**
     * 统计日期范围开始
     */
    private LocalDate startDate;
    
    /**
     * 统计日期范围结束
     */
    private LocalDate endDate;
    
    /**
     * 故障总数
     */
    private Integer faultCount;
    
    /**
     * 轻微故障数
     */
    private Integer minorFaultCount;
    
    /**
     * 一般故障数
     */
    private Integer majorFaultCount;
    
    /**
     * 严重故障数
     */
    private Integer criticalFaultCount;
    
    /**
     * 总维修成本
     */
    private BigDecimal totalRepairCost;
    
    /**
     * 平均维修时长 (小时)
     */
    private BigDecimal avgRepairTime;
    
    /**
     * 已修复数量
     */
    private Integer repairedCount;
    
    /**
     * 待修复数量
     */
    private Integer pendingCount;
    
    /**
     * 故障类型分布
     */
    private List<FaultTypeDistribution> faultTypeDistribution;
    
    /**
     * 故障类型分布
     */
    @Data
    public static class FaultTypeDistribution {
        private String faultType;
        private Integer count;
        private BigDecimal percentage;
    }
}
