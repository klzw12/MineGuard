package com.klzw.service.dispatch.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.klzw.common.database.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dispatch_task_inspection")
public class InspectionTask extends BaseEntity {

    private String taskNo;

    private Long planId;

    private Long vehicleId;

    private Long executorId;

    private Integer inspectionType;

    private String inspectionArea;

    private String inspectionPoints;

    private String startLocation;

    private Double startLongitude;

    private Double startLatitude;

    private String endLocation;

    private Double endLongitude;

    private Double endLatitude;

    private LocalDateTime scheduledStartTime;

    private LocalDateTime scheduledEndTime;

    private LocalDateTime actualStartTime;

    private LocalDateTime actualEndTime;

    private String inspectionResult;

    private String issuesFound;

    private Integer status;

    private String priority;

    private LocalDateTime pushTime;

    private LocalDateTime acceptTime;

    private String description;

    private String remark;
}
