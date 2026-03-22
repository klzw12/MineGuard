package com.klzw.service.statistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("transport_statistics")
public class TransportStatistics {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private LocalDate statisticsDate;

    private BigDecimal totalCargoWeight;

    private Integer totalTrips;

    private Integer totalVehicles;

    private Integer totalDrivers;

    private BigDecimal avgCargoPerTrip;

    private BigDecimal avgTripsPerVehicle;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
