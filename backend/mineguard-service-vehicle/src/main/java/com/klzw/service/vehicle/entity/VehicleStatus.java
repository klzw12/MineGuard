package com.klzw.service.vehicle.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.klzw.common.database.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vehicle_status")
public class VehicleStatus extends BaseEntity {
    
    private Long vehicleId;
    
    private Long tripId;
    
    private Integer status;
    
    private LocalDateTime statusTime;
    
    private Double longitude;
    
    private Double latitude;
    
    private Double speed;
    
    private Double direction;
    
    private Double altitude;
    
    private Double mileage;
    
    private Integer fuelLevel;
    
    private LocalDateTime reportTime;
    
}
