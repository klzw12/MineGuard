package com.klzw.service.vehicle.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 车辆加油记录DTO
 */
@Data
public class VehicleRefuelingDTO {
    
    private Long vehicleId;
    
    private Long driverId;
    
    private LocalDateTime refuelingDate;
    
    private String refuelingStation;
    
    private String fuelType;
    
    private BigDecimal refuelingAmount;
    
    private BigDecimal unitPrice;
    
    private BigDecimal totalCost;
    
    private Integer currentMileage;
    
    private String remark;
    
}
