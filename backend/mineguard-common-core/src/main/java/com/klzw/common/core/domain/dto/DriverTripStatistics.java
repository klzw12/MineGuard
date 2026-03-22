package com.klzw.common.core.domain.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DriverTripStatistics {

    private Long driverId;

    private String driverName;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer totalTrips;

    private Integer successTrips;

    private Integer failedTrips;

    private Double successRate;

    private Double totalDistance;

    private Double totalDuration;

    private Integer totalLoad;

    private Integer warningCount;

    private Integer violationCount;
}
