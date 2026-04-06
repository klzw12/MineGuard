package com.klzw.common.core.domain.dto;

import lombok.Data;

/**
 * 车辆信息DTO
 */
@Data
public class VehicleInfo {
    private Long id;
    private String licensePlate;
    private String vehicleNo;
    private Integer vehicleType;
    private String brand;
    private String model;
    private String ratedLoad;
    private Integer fuelLevel;
    private Integer status;
    private Long currentDriverId;
    private Integer score;
    private String reason;
}
