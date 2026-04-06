package com.klzw.service.dispatch.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "路线模板VO")
public class RouteTemplateVO {

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "路线ID")
    private Long id;

    @Schema(description = "路线名称")
    private String routeName;

    @Schema(description = "起点名称")
    private String startLocation;

    @Schema(description = "起点经度")
    private Double startLongitude;

    @Schema(description = "起点纬度")
    private Double startLatitude;

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

    @Schema(description = "状态：1-启用，2-禁用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
