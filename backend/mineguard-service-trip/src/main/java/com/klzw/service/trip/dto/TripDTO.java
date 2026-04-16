package com.klzw.service.trip.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "行程DTO")
public class TripDTO {

    @Schema(description = "行程ID（更新时必填）")
    private Long id;

    @NotNull(message = "车辆ID不能为空")
    @Schema(description = "车辆ID")
    private Long vehicleId;

    @NotNull(message = "司机ID不能为空")
    @Schema(description = "司机ID")
    private Long driverId;

    @Schema(description = "路线ID")
    private Long routeId;

    @NotNull(message = "起点不能为空")
    @Schema(description = "起点")
    private String startLocation;

    @NotNull(message = "终点不能为空")
    @Schema(description = "终点")
    private String endLocation;

    @NotNull(message = "预计开始时间不能为空")
    @Schema(description = "预计开始时间")
    private LocalDateTime estimatedStartTime;

    @NotNull(message = "预计结束时间不能为空")
    @Schema(description = "预计结束时间")
    private LocalDateTime estimatedEndTime;

    @Schema(description = "行程类型：1-日常，2-应急，3-维修，4-其他")
    private Integer tripType;

    @Schema(description = "预计里程（km）")
    private Double estimatedMileage;

    @Schema(description = "预计时长（分钟）")
    private Integer estimatedDuration;

    @Schema(description = "起点经度")
    private Double startLongitude;

    @Schema(description = "起点纬度")
    private Double startLatitude;

    @Schema(description = "终点经度")
    private Double endLongitude;

    @Schema(description = "终点纬度")
    private Double endLatitude;

    @Schema(description = "取消原因")
    private String cancellationReason;

    @Schema(description = "备注")
    private String remark;
    
    @Schema(description = "优先级: time, cost, distance")
    private String priority;
    
    @Schema(description = "截止日期")
    private LocalDateTime deadline;
}
