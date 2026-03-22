package com.klzw.service.statistics.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransportStatisticsVO {

    private Long id;

    private LocalDate statisticsDate;

    private BigDecimal totalCargoWeight;

    private Integer totalTrips;

    private Integer totalVehicles;

    private Integer totalDrivers;

    private BigDecimal avgCargoPerTrip;

    private BigDecimal avgTripsPerVehicle;
}
