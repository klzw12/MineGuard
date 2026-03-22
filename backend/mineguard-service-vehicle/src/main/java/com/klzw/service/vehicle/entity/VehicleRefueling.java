package com.klzw.service.vehicle.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.klzw.common.database.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vehicle_refueling")
public class VehicleRefueling extends BaseEntity {
    
    private Long vehicleId;
    
    private Long driverId;
    
    private LocalDateTime refuelingDate;
    
    private String refuelingStation;
    
    private String fuelType;
    
    private BigDecimal refuelingAmount;
    
    private BigDecimal unitPrice;
    
    private BigDecimal totalCost;
    
    private Integer currentMileage;
    
}
