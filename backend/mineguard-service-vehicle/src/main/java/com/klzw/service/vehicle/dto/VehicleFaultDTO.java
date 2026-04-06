package com.klzw.service.vehicle.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VehicleFaultDTO {
    
    private Long vehicleId;
    private String faultType;
    private String faultDescription;
    private Integer severity;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String locationAddress;
    private Long reporterId;
}