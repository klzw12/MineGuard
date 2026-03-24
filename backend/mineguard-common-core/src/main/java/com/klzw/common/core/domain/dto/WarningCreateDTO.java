package com.klzw.common.core.domain.dto;

import lombok.Data;

@Data
public class WarningCreateDTO {

    private Integer warningType;
    private Integer warningLevel;
    private Long vehicleId;
    private Long driverId;
    private Long tripId;
    private Double longitude;
    private Double latitude;
    private String warningContent;
}
