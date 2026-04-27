package com.klzw.service.statistics.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class VehicleStatisticsVO {

    private Long id;

    private Long vehicleId;

    private String vehicleNo;

    private LocalDate statisticsDate;

    private LocalDate startDate;

    private LocalDate endDate;

    private String period;

    private Integer tripCount;

    private BigDecimal totalDistance;

    private BigDecimal totalDuration;

    private BigDecimal cargoWeight;

    private BigDecimal fuelConsumption;

    private BigDecimal fuelCost;

    private Integer maintenanceCount;

    private BigDecimal maintenanceCost;

    private Integer warningCount;

    private Integer violationCount;

    private BigDecimal idleDuration;

    private BigDecimal idleDistance;

    private BigDecimal utilizationRate;
}
