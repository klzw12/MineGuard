package com.klzw.common.core.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DriverVehicleInfo {

    private Long id;

    private Long driverId;

    private Long vehicleId;

    private String vehicleNo;

    private String vehicleType;

    private Integer useCount;

    private LocalDateTime lastUseTime;

    private Boolean isDefault;
}
