package com.klzw.service.statistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("driver_statistics")
public class DriverStatistics {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private String userName;

    private LocalDate statisticsDate;

    private Integer attendanceDays;

    private BigDecimal attendanceHours;

    private Integer tripCount;

    private BigDecimal totalDistance;

    private BigDecimal cargoWeight;

    private Integer lateCount;

    private Integer earlyLeaveCount;

    private Integer warningCount;

    private Integer violationCount;

    private Integer overSpeedCount;

    private Integer routeDeviationCount;

    private BigDecimal performanceScore;

    private BigDecimal fuelCost;

    private BigDecimal tollCost;

    private BigDecimal commissionAmount;

    private BigDecimal totalCost;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
