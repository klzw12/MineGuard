package com.klzw.service.vehicle.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.klzw.common.database.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vehicle_maintenance")
public class VehicleMaintenance extends BaseEntity {
    
    private Long vehicleId;
    
    private Integer maintenanceType;
    
    private LocalDateTime maintenanceDate;
    
    private String maintenanceShop;
    
    private BigDecimal maintenanceCost;
    
    private String maintenanceContent;
    
    private String maintenanceResult;
    
    private Integer mileage;
    
    private LocalDate nextMaintenanceDate;
    
    private Integer nextMaintenanceMileage;
    
    private String remark;
    
}
