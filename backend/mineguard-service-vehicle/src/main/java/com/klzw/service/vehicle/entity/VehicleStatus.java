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
    
    // TODO: 新方案：添加tripId字段，与vehicleId组成复合主键
    // 只有有行程时车辆才是在线状态，否则直接是离线状态
    private Long tripId;
    
    private Double longitude;
    
    private Double latitude;
    
    private Double speed;
    
    private Double direction;
    
    private Double mileage;
    
    private Integer fuelLevel;
    
    private Integer status;
    
    private LocalDateTime reportTime;
    
}
