package com.klzw.service.statistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("fault_statistics")
public class FaultStatistics {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long vehicleId;

    private LocalDate statisticsDate;

    private Integer faultCount;

    private Integer minorFaultCount;

    private Integer majorFaultCount;

    private Integer criticalFaultCount;

    private BigDecimal totalRepairCost;

    private BigDecimal avgRepairTime;

    private String topFaultType;

    private Integer topFaultCount;

    private Integer repairedCount;

    private Integer pendingCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
