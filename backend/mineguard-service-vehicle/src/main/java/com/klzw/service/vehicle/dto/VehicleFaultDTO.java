package com.klzw.service.vehicle.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 车辆故障记录DTO
 */
@Data
public class VehicleFaultDTO {
    
    private Long vehicleId;
    private String faultType;
    private String faultDescription;
    private Integer severity;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String locationAddress;
    
}