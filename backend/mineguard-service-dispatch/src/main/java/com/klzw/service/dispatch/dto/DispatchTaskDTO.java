package com.klzw.service.dispatch.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DispatchTaskDTO {

    private Long id;

    private Long planId;

    private Long routeId;

    private Integer taskSequence;

    private Long vehicleId;

    private Long executorId;

    private String startLocation;

    private Double startLongitude;

    private Double startLatitude;

    private String endLocation;

    private Double endLongitude;

    private Double endLatitude;

    private BigDecimal cargoWeight;

    private String cargoType;

    private LocalDateTime scheduledStartTime;

    private LocalDateTime scheduledEndTime;

    private String priority;

    private String description;

    private String remark;
}
