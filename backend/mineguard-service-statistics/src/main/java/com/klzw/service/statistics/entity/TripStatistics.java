package com.klzw.service.statistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("trip_statistics")
public class TripStatistics {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private LocalDate statisticsDate;

    private Integer tripCount;

    private BigDecimal totalDistance;

    private BigDecimal totalDuration;

    private Integer completedTripCount;

    private Integer cancelledTripCount;

    private BigDecimal averageSpeed;

    private BigDecimal fuelConsumption;

    private BigDecimal cargoWeight;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
