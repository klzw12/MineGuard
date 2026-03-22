package com.klzw.service.dispatch.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DispatchTaskVO {

    private Long id;

    private String taskNo;

    private Long planId;

    private String planName;

    private Long routeId;

    private Integer taskSequence;

    private Long vehicleId;

    private String vehicleNo;

    private Long executorId;

    private String executorName;

    private Long driverId;

    private String driverName;

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

    private LocalDateTime actualStartTime;

    private LocalDateTime actualEndTime;

    private Integer status;

    private String statusName;

    private String priority;

    private LocalDateTime pushTime;

    private LocalDateTime acceptTime;

    private String description;

    private String remark;

    private LocalDateTime createTime;
}
