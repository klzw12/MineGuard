package com.klzw.service.warning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "预警规则DTO")
public class WarningRuleDTO {

    @Schema(description = "规则ID（更新时必填）")
    private Long id;

    @NotBlank(message = "规则名称不能为空")
    @Schema(description = "规则名称")
    private String ruleName;

    @NotBlank(message = "规则编码不能为空")
    @Schema(description = "规则编码")
    private String ruleCode;

    @NotNull(message = "预警类型不能为空")
    @Schema(description = "预警类型")
    private Integer warningType;

    @NotNull(message = "预警级别不能为空")
    @Schema(description = "预警级别：1-低危，2-中危，3-高危")
    private Integer warningLevel;

    @Schema(description = "阈值")
    private String thresholdValue;

    @Schema(description = "描述")
    private String description;
}
