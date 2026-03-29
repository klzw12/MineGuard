package com.klzw.service.cost.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalaryRecordDTO {

    private Long id;

    private Long driverId;

    private String driverName;

    private String vehicleNo;

    private String period;

    private BigDecimal baseSalary;

    private BigDecimal bonus;

    private BigDecimal deduction;

    private BigDecimal totalSalary;

    private Integer status;

    private String remark;
}
