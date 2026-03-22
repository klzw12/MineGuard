package com.klzw.common.core.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 成本统计响应 DTO
 */
@Data
public class CostStatisticsResponseDTO {
    
    /**
     * 总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 记录数量
     */
    private Integer recordCount;
    
    /**
     * 按类型分组的金额
     */
    private Map<String, BigDecimal> typeAmountMap;
    
    /**
     * 燃油成本
     */
    private BigDecimal fuelCost;
    
    /**
     * 维修成本
     */
    private BigDecimal maintenanceCost;
    
    /**
     * 人工成本
     */
    private BigDecimal laborCost;
    
    /**
     * 保险成本
     */
    private BigDecimal insuranceCost;
    
    /**
     * 折旧成本
     */
    private BigDecimal depreciationCost;
    
    /**
     * 管理成本
     */
    private BigDecimal managementCost;
    
    /**
     * 其他成本
     */
    private BigDecimal otherCost;
}
