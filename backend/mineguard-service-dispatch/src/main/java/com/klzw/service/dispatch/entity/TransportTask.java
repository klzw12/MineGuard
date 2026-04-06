package com.klzw.service.dispatch.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.klzw.common.database.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dispatch_task_transport")
public class TransportTask extends BaseEntity {

    private String taskNo;

    private Long planId;

    private Long tripId;

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

    private BigDecimal estimatedCommissionAmount;

    private String cargoType;

    private LocalDateTime scheduledStartTime;

    private LocalDateTime scheduledEndTime;

    private LocalDateTime actualStartTime;

    private LocalDateTime actualEndTime;

    private Integer status;

    private String priority;

    private LocalDateTime pushTime;

    private LocalDateTime acceptTime;

    private String description;

    private String remark;
}
