package com.klzw.service.dispatch.entity;

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

    private String planName;

    private LocalDate planDate;

    private Integer planType;

    private Long routeId;

    private String startLocation;

    private Double startLongitude;

    private Double startLatitude;

    private String endLocation;

    private Double endLongitude;

    private Double endLatitude;

    private Integer plannedTrips;

    private Integer completedTrips;

    private BigDecimal plannedCargoWeight;

    private String startTimeSlot;

    private String endTimeSlot;

    private Integer status;

    private String remark;
}
