package com.klzw.common.core.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VehicleInfo {
    private Long id;
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
    private String photoUrl;
    private String licenseFrontUrl;
    private String licenseBackUrl;
    private String owner;
    private BigDecimal cargoWeight;
    private Double startLongitude;
    private Double startLatitude;
}
