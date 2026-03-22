package com.klzw.service.statistics.service;

import com.klzw.service.statistics.dto.StatisticsQueryDTO;
import com.klzw.service.statistics.vo.FaultStatisticsVO;

import java.time.LocalDate;
import java.util.List;

public interface FaultStatisticsService {

    FaultStatisticsVO calculateFaultStatistics(Long vehicleId, String date);

    FaultStatisticsVO getFaultStatistics(Long vehicleId, LocalDate date);

    List<FaultStatisticsVO> getFaultStatisticsList(StatisticsQueryDTO queryDTO);

    FaultStatisticsVO getOverallFaultStatistics(LocalDate startDate, LocalDate endDate);

    List<FaultStatisticsVO.FaultTypeDistribution> getFaultTypeDistribution(LocalDate startDate, LocalDate endDate);

    List<FaultStatisticsVO.FaultTrendItem> getFaultTrend(LocalDate startDate, LocalDate endDate);

    void calculateDailyFaultStatistics(LocalDate date);
}
