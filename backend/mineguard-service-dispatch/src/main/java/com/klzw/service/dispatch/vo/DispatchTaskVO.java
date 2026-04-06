package com.klzw.service.dispatch.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DispatchTaskVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String taskNo;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long planId;

    private String planName;

    private Integer taskType;

    private String taskTypeName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long routeId;

    private Integer taskSequence;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long vehicleId;

    private String vehicleNo;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long executorId;

    private String executorName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long driverId;

    private String driverName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long tripId;

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
