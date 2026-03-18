package com.klzw.service.warning.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("warning_record")
public class WarningRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

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

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
