package com.klzw.service.trip.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "路线DTO")
public class RouteDTO {

    @Schema(description = "路线ID（更新时必填）")
    private Long id;

    @NotBlank(message = "路线名称不能为空")
    @Schema(description = "路线名称")
    private String routeName;

    @NotBlank(message = "起点不能为空")
    @Schema(description = "起点")
    private String startPoint;

    @NotBlank(message = "终点不能为空")
    @Schema(description = "终点")
    private String endPoint;

    @Schema(description = "起点经度")
    private Double startLongitude;

    @Schema(description = "起点纬度")
    private Double startLatitude;

    @Schema(description = "终点经度")
    private Double endLongitude;

    @Schema(description = "终点纬度")
    private Double endLatitude;

    @Schema(description = "距离（公里）")
    private BigDecimal distance;

    @Schema(description = "预计时间（分钟）")
    private Integer estimatedTime;

    @Schema(description = "路线类型：1-常规路线，2-备用路线")
    private Integer routeType;

    @Schema(description = "途经点（JSON格式）")
    private String waypoints;
}
