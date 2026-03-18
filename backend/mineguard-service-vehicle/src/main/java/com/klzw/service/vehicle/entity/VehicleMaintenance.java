package com.klzw.service.vehicle.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.klzw.common.database.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vehicle_maintenance")
public class VehicleMaintenance extends BaseEntity {
    
    private Long vehicleId;
    
    private Integer maintenanceType;
    
    private LocalDate maintenanceDate;
    
    private String maintenanceContent;
    
    private BigDecimal maintenanceCost;
    
    private Long repairmanId;
    
    private LocalDate nextMaintenanceDate;
    
    private BigDecimal mileage;
    
    private String remark;
    
}
