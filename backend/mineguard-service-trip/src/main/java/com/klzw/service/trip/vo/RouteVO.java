package com.klzw.service.trip.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "路线VO")
public class RouteVO {

    @Schema(description = "路线ID")
    private String id;

    @Schema(description = "路线名称")
    private String routeName;

    @Schema(description = "起点")
    private String startLocation;

    @Schema(description = "终点")
    private String endLocation;

    @Schema(description = "起点经度")
    private Double startLongitude;

    @Schema(description = "起点纬度")
    private Double startLatitude;

    @Schema(description = "终点经度")
    private Double endLongitude;

    @Schema(description = "终点纬度")
    private Double endLatitude;

    @Schema(description = "距离（km）")
    private Double distance;

    @Schema(description = "预计时长（分钟）")
    private Integer estimatedDuration;

    @Schema(description = "途经点（JSON格式）")
    private String waypoints;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
