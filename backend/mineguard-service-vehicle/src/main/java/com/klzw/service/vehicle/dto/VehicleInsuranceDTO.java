package com.klzw.service.vehicle.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 车辆保险信息DTO
 */
@Data
public class VehicleInsuranceDTO {
    
    private Long vehicleId; // 车辆ID
    private String insuranceCompany; // 保险公司
    private String insuranceNumber; // 保险单号
    private Integer insuranceType; // 保险类型
    private BigDecimal insuranceAmount; // 保险金额
    private LocalDate startDate; // 开始日期
    private LocalDate expiryDate; // 过期日期
    private String remark; // 备注
    
}