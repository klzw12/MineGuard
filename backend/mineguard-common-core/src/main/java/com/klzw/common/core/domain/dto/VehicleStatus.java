package com.klzw.common.core.domain.dto;

import lombok.Data;

/**
 * 车辆状态DTO
 */
@Data
public class VehicleStatus {
    private Long vehicleId;
    private Integer status;
    private Double longitude;
    private Double latitude;
}