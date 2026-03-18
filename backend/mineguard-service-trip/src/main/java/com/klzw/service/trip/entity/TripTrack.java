package com.klzw.service.trip.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("trip_track")
public class TripTrack {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long tripId;

    private Long vehicleId;

    private Double longitude;

    private Double latitude;

    private BigDecimal speed;

    private BigDecimal direction;

    private BigDecimal altitude;

    private LocalDateTime recordTime;

    private LocalDateTime createTime;
}
