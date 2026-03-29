package com.klzw.service.vehicle.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 车辆保养记录DTO
 */
@Data
public class VehicleMaintenanceDTO {
    
    private Long vehicleId;
    private Integer maintenanceType;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate maintenanceDate;
    
    private String maintenanceShop;
    private String maintenanceContent;
    private BigDecimal maintenanceCost;
    private String remark;
    
}