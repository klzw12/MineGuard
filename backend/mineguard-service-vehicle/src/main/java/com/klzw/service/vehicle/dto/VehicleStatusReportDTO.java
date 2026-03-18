package com.klzw.service.vehicle.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VehicleStatusReportDTO {
    private Long vehicleId;
    private Long tripId;
    private Double longitude;
    private Double latitude;
    private Double speed;
    private Double direction;
    private Double mileage;
    private Integer fuelLevel;
    private Integer specialStatus;
    private String remark;
    private LocalDateTime reportTime;
}
