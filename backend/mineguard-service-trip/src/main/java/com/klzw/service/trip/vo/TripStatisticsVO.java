package com.klzw.service.trip.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Duration;

@Data
@Schema(description = "行程统计VO")
public class TripStatisticsVO {

    @Schema(description = "行程ID")
    private Long tripId;

    @Schema(description = "行程时长（分钟）")
    private Long durationMinutes;

    @Schema(description = "预计里程（km）")
    private BigDecimal estimatedDistance;

    @Schema(description = "实际里程（km）")
    private BigDecimal actualDistance;

    @Schema(description = "平均速度（km/h）")
    private Double averageSpeed;
}
