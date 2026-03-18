
package com.klzw.service.warning.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WarningTrackDTO {

    private Long vehicleId;

    private Long driverId;

    private Long tripId;

    private Double longitude;

    private Double latitude;

    private BigDecimal speed;

    private String direction;

    private Double mileage;

    private Integer fuelLevel;

    private String specialStatus;

    private Boolean isReported = false;
}
