package com.klzw.service.dispatch.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.klzw.common.database.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dispatch_task_maintenance")
public class MaintenanceTask extends BaseEntity {

    private String taskNo;

    private Long planId;

    private Long vehicleId;

    private Long repairmanVehicleId;

    private Long executorId;

    private Integer faultType;

    private Integer faultLevel;

    private String faultDescription;

    private String faultLocation;

    private Double faultLongitude;

    private Double faultLatitude;

    private String repairLocation;

    private Double repairLongitude;

    private Double repairLatitude;

    private LocalDateTime scheduledStartTime;

    private LocalDateTime scheduledEndTime;

    private LocalDateTime actualStartTime;

    private LocalDateTime actualEndTime;

    private String repairResult;

    private BigDecimal repairCost;

    private Integer status;

    private String priority;

    private LocalDateTime pushTime;

    private LocalDateTime acceptTime;

    private String description;

    private String remark;
}
