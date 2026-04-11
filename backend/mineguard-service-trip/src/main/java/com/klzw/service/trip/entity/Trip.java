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

    private Long dispatchTaskId;

    private Long vehicleId;

    private Long driverId;

    private String startLocation;

    private Double startLongitude;

    private Double startLatitude;

    private String endLocation;

    private Double endLongitude;

    private Double endLatitude;

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

    private BigDecimal cargoWeight;
    
    // private BigDecimal estimatedCommissionAmount;  // 预计提成金额（调度时设定）
    
    private String cancellationReason;

    private String remark;
}
