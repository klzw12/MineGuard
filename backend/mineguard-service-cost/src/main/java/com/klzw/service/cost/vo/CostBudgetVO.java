package com.klzw.service.cost.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CostBudgetVO {

    private Long id;

    private String budgetNo;

    private String budgetName;

    private Integer budgetType;

    private String budgetTypeName;

    private Integer budgetYear;

    private Integer budgetMonth;

    private Integer budgetQuarter;

    private BigDecimal fuelBudget;

    private BigDecimal maintenanceBudget;

    private BigDecimal laborBudget;

    private BigDecimal insuranceBudget;

    private BigDecimal depreciationBudget;

    private BigDecimal managementBudget;

    private BigDecimal otherBudget;

    private BigDecimal totalBudget;

    private Integer status;

    private String statusName;

    private BigDecimal usedAmount;

    private BigDecimal remainingAmount;

    private LocalDateTime createTime;
}
