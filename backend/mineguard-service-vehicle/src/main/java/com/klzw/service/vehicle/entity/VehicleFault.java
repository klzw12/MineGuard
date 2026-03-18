package com.klzw.service.vehicle.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.klzw.common.database.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vehicle_fault")
public class VehicleFault extends BaseEntity {
    
    private Long vehicleId;
    
    private String faultType;
    
    private String faultDescription;
    
    private LocalDateTime faultDate;
    
    private Integer severity;
    
    private Integer status;
    
    private Long repairmanId;
    
    private LocalDateTime repairDate;
    
    private BigDecimal repairCost;
    
    private String repairContent;
    
}
