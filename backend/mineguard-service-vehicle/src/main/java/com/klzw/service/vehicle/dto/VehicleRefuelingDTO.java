package com.klzw.service.vehicle.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 车辆加油记录DTO
 */
@Data
public class VehicleRefuelingDTO {
    
    private Long vehicleId;
    
    private Long driverId;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate refuelingDate;
    
    private String refuelingStation;
    
    private String fuelType;
    
    private BigDecimal refuelingAmount;
    
    private BigDecimal unitPrice;
    
    private BigDecimal totalCost;
    
    private Integer currentMileage;
    
    private String remark;
    
}
