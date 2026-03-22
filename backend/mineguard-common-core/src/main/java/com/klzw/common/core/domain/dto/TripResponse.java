package com.klzw.common.core.domain.dto;

import lombok.Data;

@Data
public class TripResponse {
    private Long id;
    private Long vehicleId;
    private Long driverId;
    private String tripNo;
    private Integer status;
}