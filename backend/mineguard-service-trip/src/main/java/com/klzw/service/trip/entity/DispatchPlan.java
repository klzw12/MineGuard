package com.klzw.service.trip.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.klzw.common.database.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dispatch_plan")
public class DispatchPlan extends BaseEntity {

    private String planNo;

    private LocalDate planDate;

    private Long vehicleId;

    private Long driverId;

    private Long routeId;

    private Integer plannedTrips;

    private BigDecimal plannedCargoWeight;

    private String startTimeSlot;

    private Integer status;

    private String remark;
}
