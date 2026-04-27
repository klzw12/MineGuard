package com.klzw.service.statistics.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CostStatisticsVO {

    private Long id;

    private LocalDate statisticsMonth;

    private BigDecimal fuelCost;

    private BigDecimal maintenanceCost;

    private BigDecimal laborCost;

    private BigDecimal insuranceCost;

    private BigDecimal depreciationCost;

    private BigDecimal managementCost;

    private BigDecimal otherCost;

    private BigDecimal totalCost;
}
