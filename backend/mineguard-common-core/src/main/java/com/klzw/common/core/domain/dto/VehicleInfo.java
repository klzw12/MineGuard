package com.klzw.common.core.domain.dto;

import lombok.Data;

/**
 * 车辆信息DTO
 */
@Data
public class VehicleInfo {
    private Long id;
    private String licensePlate;
    private Long vehicleModelId;
    private Integer status;
    private Long currentDriverId;
}