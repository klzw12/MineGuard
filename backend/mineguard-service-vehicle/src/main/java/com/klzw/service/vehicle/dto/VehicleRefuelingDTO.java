package com.klzw.service.vehicle.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 车辆加油记录DTO
 */
@Data
public class VehicleRefuelingDTO {
    
    private Long vehicleId; // 车辆ID
    private Long driverId; // 司机ID
    private LocalDateTime refuelingDate; // 加油日期
    private String fuelType; // 燃油类型
    private BigDecimal fuelAmount; // 加油量
    private BigDecimal fuelPrice; // 燃油价格
    private BigDecimal totalCost; // 总费用
    private BigDecimal mileage; // 里程
    private String gasStation; // 加油站
    private String remark; // 备注
    
}