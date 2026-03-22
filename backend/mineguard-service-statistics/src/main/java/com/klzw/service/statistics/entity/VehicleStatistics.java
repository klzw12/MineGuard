package com.klzw.service.statistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("vehicle_statistics")
public class VehicleStatistics {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long vehicleId;

    private LocalDate statisticsDate;

    private Integer tripCount;

    private BigDecimal totalDistance;

    private BigDecimal totalDuration;

    private BigDecimal cargoWeight;

    private BigDecimal fuelConsumption;

    private BigDecimal fuelCost;

    private Integer maintenanceCount;

    private BigDecimal maintenanceCost;

    private Integer warningCount;

    private Integer violationCount;

    private BigDecimal idleDuration;

    private BigDecimal idleDistance;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
