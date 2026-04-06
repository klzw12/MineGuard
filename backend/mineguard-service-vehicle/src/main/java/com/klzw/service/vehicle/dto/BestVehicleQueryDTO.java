package com.klzw.service.vehicle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "最佳车辆查询条件DTO")
public class BestVehicleQueryDTO {

    @Schema(description = "货物重量(吨)")
    private BigDecimal cargoWeight;

    @Schema(description = "起点经度")
    private Double startLongitude;

    @Schema(description = "起点纬度")
    private Double startLatitude;

    @Schema(description = "车辆类型(可选)")
    private Integer vehicleType;

    @Schema(description = "排除的车辆ID列表(已分配的车辆)")
    private java.util.List<Long> excludeVehicleIds;

    @Schema(description = "计划执行时间，用于过滤该时间段已有行程的车辆")
    private String scheduledTime;
}
