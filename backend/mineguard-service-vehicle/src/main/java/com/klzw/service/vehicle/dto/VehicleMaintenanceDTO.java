package com.klzw.service.vehicle.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 车辆保养记录DTO
 */
@Data
public class VehicleMaintenanceDTO {
    
    private Long vehicleId; // 车辆ID
    private Integer maintenanceType; // 保养类型
    private LocalDate maintenanceDate; // 保养日期
    private String maintenanceContent; // 保养内容
    private BigDecimal maintenanceCost; // 保养费用
    private Long repairmanId; // 维修员ID
    private LocalDate nextMaintenanceDate; // 下次保养日期
    private BigDecimal mileage; // 里程
    private String remark; // 备注
    
}