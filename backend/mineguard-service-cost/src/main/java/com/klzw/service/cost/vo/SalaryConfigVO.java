package com.klzw.service.cost.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SalaryConfigVO {

    private Long id;

    private String roleCode;

    private String roleName;

    private BigDecimal baseSalary;

    private BigDecimal dailySalary;

    private BigDecimal hourlySalary;

    private BigDecimal overtimeRate;

    private BigDecimal performanceBonus;

    private Integer status;

    private String statusName;

    private LocalDate effectiveDate;

    private LocalDate expiryDate;

    private LocalDateTime createTime;
}
