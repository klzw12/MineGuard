package com.klzw.service.dispatch.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "路线模板DTO")
public class RouteTemplateDTO {

    @Schema(description = "路线ID（更新时必填）")
    private Long id;

    @NotBlank(message = "路线名称不能为空")
    @Schema(description = "路线名称")
    private String routeName;

    @NotBlank(message = "起点不能为空")
    @Schema(description = "起点名称")
    private String startLocation;

    @Schema(description = "起点经度")
    private Double startLongitude;

    @Schema(description = "起点纬度")
    private Double startLatitude;

    @NotBlank(message = "终点不能为空")
    @Schema(description = "终点名称")
    private String endLocation;

    @Schema(description = "终点经度")
    private Double endLongitude;

    @Schema(description = "终点纬度")
    private Double endLatitude;

    @Schema(description = "参考距离（公里）")
    private BigDecimal distance;

    @Schema(description = "参考时长（分钟）")
    private Integer estimatedDuration;
}
