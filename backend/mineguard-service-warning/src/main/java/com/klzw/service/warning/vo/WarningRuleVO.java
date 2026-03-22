package com.klzw.service.warning.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "预警规则VO")
public class WarningRuleVO {

    @Schema(description = "规则ID")
    private Long id;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "规则编码")
    private String ruleCode;

    @Schema(description = "预警类型")
    private Integer warningType;

    @Schema(description = "预警类型名称")
    private String warningTypeName;

    @Schema(description = "预警级别")
    private Integer warningLevel;

    @Schema(description = "预警级别名称")
    private String warningLevelName;

    @Schema(description = "阈值")
    private String thresholdValue;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
