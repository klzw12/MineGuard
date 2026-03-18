package com.klzw.service.vehicle.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 车辆状态视图对象
 */
@Data
public class VehicleStatusVO {
    
    private String id;
    
    private String vehicleId; // 车辆ID
    
    private String vehicleNo; // 车牌号
    
    private Double longitude; // 经度
    
    private Double latitude; // 纬度
    
    private Double speed; // 速度（km/h）
    
    private Double direction; // 方向（度）
    
    private Double mileage; // 里程（km）
    
    private Integer fuelLevel; // 油量百分比
    
    private Integer status; // 状态
    
    private LocalDateTime reportTime; // 上报时间
    
    private LocalDateTime createTime;
    
}
