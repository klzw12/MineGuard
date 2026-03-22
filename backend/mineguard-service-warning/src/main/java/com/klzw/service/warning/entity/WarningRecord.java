package com.klzw.service.warning.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.klzw.common.database.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("warning_record")
public class WarningRecord extends BaseEntity {

    private String warningNo;

    private Long ruleId;

    private Integer warningType;

    private Integer warningLevel;

    private Long vehicleId;

    private Long driverId;

    private Long tripId;

    private Double longitude;

    private Double latitude;

    private BigDecimal speed;

    private String warningContent;

    private LocalDateTime warningTime;

    private Integer status;

    private Long handlerId;

    private LocalDateTime handleTime;

    private String handleResult;
}
