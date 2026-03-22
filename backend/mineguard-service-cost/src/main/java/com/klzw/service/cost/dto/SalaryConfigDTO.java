package com.klzw.service.cost.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SalaryConfigDTO {

    private Long id;

    private String roleCode;

    private String roleName;

    private BigDecimal baseSalary;

    private BigDecimal dailySalary;

    private BigDecimal hourlySalary;

    private BigDecimal overtimeRate;

    private BigDecimal performanceBonus;

    private Integer status;

    private LocalDate effectiveDate;

    private LocalDate expiryDate;

    private String remark;
}
