package com.klzw.service.vehicle.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 车辆保险信息DTO
 */
@Data
public class VehicleInsuranceDTO {
    
    private Long vehicleId;
    private String insuranceCompany;
    private String insuranceNumber;
    private Integer insuranceType;
    private BigDecimal insuranceAmount;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    
    private String remark;
    
}