package com.klzw.service.vehicle.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 实时状态DTO
 */
@Data
public class VehicleRealTimeStatusDTO {
    
    private Long vehicleId; // 车辆ID
    private Double longitude; // 经度
    private Double latitude; // 纬度
    private Double speed; // 速度（km/h）
    private Double direction; // 方向（度）
    private Double mileage; // 里程（km）
    private Integer fuelLevel; // 油量百分比
    private Integer status; // 状态：0-离线，1-在线，2-行驶中，3-故障，4-维修中
    private LocalDateTime reportTime; // 上报时间
    private String location; // 位置描述
    private String engineStatus; // 发动机状态
    private String batteryStatus; // 电池状态
    private String tirePressure; // 胎压
    private String temperature; // 温度
    
}