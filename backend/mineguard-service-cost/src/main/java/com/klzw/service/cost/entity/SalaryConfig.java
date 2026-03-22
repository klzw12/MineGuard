package com.klzw.service.cost.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("salary_config")
public class SalaryConfig {

    @TableId(type = IdType.ASSIGN_ID)
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

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createBy;

    private Long updateBy;

    @TableLogic
    private Integer deleted;

    private String remark;
}
