package com.klzw.service.cost.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CostBudgetDTO {

    private Long id;

    private String budgetName;

    private Integer budgetType;

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

    private String remark;
}
