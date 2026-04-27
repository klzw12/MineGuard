package com.klzw.service.statistics.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VehicleTripStatsVO {
    
    private Long vehicleId;
    
    private String vehicleNo;
    
    private String period;
    
    private Integer tripCount;
    
    private BigDecimal totalDistance;
    
    private BigDecimal cargoWeight;
}
