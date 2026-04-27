package com.klzw.service.statistics.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class FaultStatisticsVO {

    private Long id;

    private LocalDate statisticsDate;

    private LocalDate startDate;

    private LocalDate endDate;

    private String period;

    private Long vehicleId;

    private String vehicleNo;

    private Integer faultCount;

    private Integer minorFaultCount;

    private Integer majorFaultCount;

    private Integer criticalFaultCount;

    private BigDecimal totalRepairCost;

    private BigDecimal avgRepairTime;

    private String topFaultType;

    private Integer topFaultCount;

    private Integer repairedCount;

    private Integer pendingCount;

    private Integer totalFaultCount;

    private Integer totalVehicleCount;

    private BigDecimal totalCost;

    private List<FaultTypeDistribution> faultTypeDistribution;

    private List<FaultTrendItem> faultTrend;

    private Map<String, Integer> faultLevelDistribution;

    @Data
    public static class FaultTypeDistribution {
        private String faultType;
        private Integer count;
        private BigDecimal percentage;
    }

    @Data
    public static class FaultTrendItem {
        private LocalDate date;
        private Integer faultCount;
        private BigDecimal repairCost;
    }
}
