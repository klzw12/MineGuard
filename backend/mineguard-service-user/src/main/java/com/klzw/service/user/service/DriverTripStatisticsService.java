package com.klzw.service.user.service;

import com.klzw.common.core.domain.dto.DriverTripStatistics;

import java.time.LocalDate;
import java.util.List;

public interface DriverTripStatisticsService {

    DriverTripStatistics getStatisticsByDriverId(Long driverId, LocalDate startDate, LocalDate endDate);

    List<DriverTripStatistics> getStatisticsByDateRange(LocalDate startDate, LocalDate endDate);

    DriverTripStatistics getMonthlyStatistics(Long driverId, String yearMonth);

    void updateStatisticsAfterTripComplete(Long driverId, Long tripId, boolean success);

    void recordTripFailure(Long driverId, Long tripId, String reason);
}
