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
@TableName("cost_budget")
public class CostBudget {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String budgetNo;

    private String budgetName;

    private Integer budgetType;

    private Integer budgetYear;

    private Integer budgetMonth;

    private Integer budgetQuarter;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal fuelBudget;

    private BigDecimal maintenanceBudget;

    private BigDecimal laborBudget;

    private BigDecimal insuranceBudget;

    private BigDecimal depreciationBudget;

    private BigDecimal managementBudget;

    private BigDecimal otherBudget;

    private BigDecimal totalBudget;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createBy;

    private Long updateBy;

    @TableLogic
    private Integer deleted;

    private String remark;
}
