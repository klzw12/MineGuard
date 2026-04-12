package com.klzw.service.vehicle.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.klzw.common.database.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 车辆状态实体
 * 注意：实时状态（速度、方向等）从Redis获取，此表只存储持久化状态
 */
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
    
    private Double mileage;
    
    private Integer fuelLevel;
    
    private LocalDateTime reportTime;
    
}
