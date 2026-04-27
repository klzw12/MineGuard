package com.klzw.service.statistics.service;

import com.klzw.service.statistics.dto.StatisticsQueryDTO;
import com.klzw.service.statistics.vo.*;

import java.time.LocalDate;
import java.util.List;

public interface StatisticsService {

    TripStatisticsVO calculateTripStatistics(String date);

    CostStatisticsVO calculateCostStatistics(String date);

    VehicleStatisticsVO calculateVehicleStatistics(Long vehicleId, String startDate, String endDate);

    DriverStatisticsVO calculateDriverStatistics(Long userId, String startDate, String endDate);

    TransportStatisticsVO calculateTransportStatistics(String date);

    void calculateMonthlyTripStatistics(LocalDate startDate, LocalDate endDate);

    void calculateMonthlyCostStatistics(LocalDate startDate, LocalDate endDate);

    List<TripStatisticsVO> getTripStatistics(StatisticsQueryDTO queryDTO);

    List<CostStatisticsVO> getCostStatistics(StatisticsQueryDTO queryDTO);

    List<DriverStatisticsVO> getDriverStatistics(StatisticsQueryDTO queryDTO);

    List<TransportStatisticsVO> getTransportStatistics(StatisticsQueryDTO queryDTO);

    OverallStatisticsVO getOverallStatistics(StatisticsQueryDTO queryDTO);
    
    FaultStatisticsVO getFaultStatistics(StatisticsQueryDTO queryDTO);
    
    FaultStatisticsVO getFaultOverallStatistics(StatisticsQueryDTO queryDTO);
    
    void calculateFaultStatistics(Long vehicleId, String startDate, String endDate);
    
    void calculateAllDriverStatistics(String startDate, String endDate);
    
    void calculateAllFaultStatistics(String startDate, String endDate);
    
    void calculateAllVehicleStatistics(String startDate, String endDate);
    
    List<VehicleTripStatsVO> getVehicleTripStats(String dimension, String startDate, String endDate);
    
    List<DriverStatisticsVO> getDriverStatsAggregated(String dimension, String startDate, String endDate);
    
    void backfillStatistics(String date);
}
