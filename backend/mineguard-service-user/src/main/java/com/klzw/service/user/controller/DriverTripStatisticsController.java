package com.klzw.service.user.controller;

import com.klzw.common.core.domain.dto.DriverTripStatistics;
import com.klzw.service.user.service.DriverTripStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/driver/trip-statistics")
@RequiredArgsConstructor
public class DriverTripStatisticsController {

    private final DriverTripStatisticsService driverTripStatisticsService;

    @GetMapping("/{driverId}")
    public DriverTripStatistics getByDriverId(
            @PathVariable Long driverId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return driverTripStatisticsService.getStatisticsByDriverId(driverId, startDate, endDate);
    }

    @GetMapping("/range")
    public List<DriverTripStatistics> getByDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return driverTripStatisticsService.getStatisticsByDateRange(startDate, endDate);
    }

    @GetMapping("/{driverId}/monthly/{yearMonth}")
    public DriverTripStatistics getMonthlyStatistics(
            @PathVariable Long driverId,
            @PathVariable String yearMonth) {
        return driverTripStatisticsService.getMonthlyStatistics(driverId, yearMonth);
    }
}
