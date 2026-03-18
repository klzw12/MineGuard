package com.klzw.service.trip.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.klzw.common.database.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("trip")
public class Trip extends BaseEntity {

    private String tripNo;

    private Long vehicleId;

    private Long driverId;

    private Long routeId;

    private String startLocation;

    private String endLocation;

    private LocalDateTime estimatedStartTime;

    private LocalDateTime estimatedEndTime;

    private LocalDateTime actualStartTime;

    private LocalDateTime actualEndTime;

    private Integer status;

    private Integer tripType;

    private BigDecimal estimatedMileage;

    private BigDecimal actualMileage;

    private Integer estimatedDuration;

    private Integer actualDuration;

    private Double fuelConsumption;

    private Double averageSpeed;

    private String cancellationReason;

    private Double startLongitude;

    private Double startLatitude;

    private Double endLongitude;

    private Double endLatitude;

    private String remark;
}
