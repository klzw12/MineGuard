package com.klzw.service.statistics.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class OverallStatisticsVO {

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer totalTripCount;

    private BigDecimal totalDistance;

    private BigDecimal totalDuration;

    private Integer totalCompletedTripCount;

    private Integer totalCancelledTripCount;

    private BigDecimal totalFuelCost;

    private BigDecimal totalMaintenanceCost;

    private BigDecimal totalLaborCost;

    private BigDecimal totalOtherCost;

    private BigDecimal totalCost;

    private BigDecimal totalCargoWeight;

    private Integer totalVehicles;

    private Integer totalDrivers;

    private List<TripStatisticsVO> tripStatisticsList;

    private List<CostStatisticsVO> costStatisticsList;
}
