package com.klzw.service.statistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("cost_statistics")
public class CostStatistics {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private LocalDate statisticsDate;

    private BigDecimal fuelCost;

    private BigDecimal maintenanceCost;

    private BigDecimal laborCost;

    private BigDecimal otherCost;

    private BigDecimal totalCost;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}