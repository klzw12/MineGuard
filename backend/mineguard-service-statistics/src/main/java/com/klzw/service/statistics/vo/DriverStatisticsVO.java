package com.klzw.service.statistics.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DriverStatisticsVO {

    private Long id;

    private Long userId;

    private String userName;

    private LocalDate statisticsDate;

    private LocalDate startDate;

    private LocalDate endDate;

    private String period;

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
}
