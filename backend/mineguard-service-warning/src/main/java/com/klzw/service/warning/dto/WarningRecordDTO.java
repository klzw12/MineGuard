package com.klzw.service.warning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "预警记录DTO")
public class WarningRecordDTO {

    @Schema(description = "预警ID（更新时必填）")
    private Long id;

    @Schema(description = "规则ID")
    private Long ruleId;

    @NotNull(message = "预警类型不能为空")
    @Schema(description = "预警类型")
    private Integer warningType;

    @NotNull(message = "预警级别不能为空")
    @Schema(description = "预警级别：1-低危，2-中危，3-高危")
    private Integer warningLevel;

    @NotNull(message = "车辆ID不能为空")
    @Schema(description = "车辆ID")
    private Long vehicleId;

    @Schema(description = "司机ID")
    private Long driverId;

    @Schema(description = "行程ID")
    private Long tripId;

    @Schema(description = "经度")
    private Double longitude;

    @Schema(description = "纬度")
    private Double latitude;

    @Schema(description = "速度")
    private BigDecimal speed;

    @NotBlank(message = "预警内容不能为空")
    @Schema(description = "预警内容")
    private String warningContent;

    @Schema(description = "预警时间")
    private String warningTime;
}
