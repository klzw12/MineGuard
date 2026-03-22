package com.klzw.service.cost.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
public class CostStatisticsVO {

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal totalAmount;

    private Integer recordCount;

    private Map<Integer, BigDecimal> typeAmountMap;

    private Map<Integer, String> typeNames;

    private BigDecimal fuelCost;

    private BigDecimal maintenanceCost;

    private BigDecimal laborCost;

    private BigDecimal insuranceCost;

    private BigDecimal depreciationCost;

    private BigDecimal managementCost;

    private BigDecimal otherCost;
}
