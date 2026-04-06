package com.klzw.service.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.klzw.common.database.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("driver_vehicle")
public class DriverVehicle extends BaseEntity {

    private Long driverId;

    private Long vehicleId;

    private Integer useCount;

    private LocalDateTime lastUseTime;

    private Integer isDefault;

}
