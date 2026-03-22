package com.klzw.service.statistics.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TripStatisticsVO {

    private Long id;

    private LocalDate statisticsDate;

    private Integer tripCount;

    private BigDecimal totalDistance;

    private BigDecimal totalDuration;

    private Integer completedTripCount;

    private Integer cancelledTripCount;

    private BigDecimal averageSpeed;

    private BigDecimal fuelConsumption;

    private BigDecimal cargoWeight;
}
