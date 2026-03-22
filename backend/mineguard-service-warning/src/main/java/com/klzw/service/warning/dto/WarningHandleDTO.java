package com.klzw.service.warning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "预警处理DTO")
public class WarningHandleDTO {

    @Schema(description = "处理结果")
    private String handleResult;

    @Schema(description = "处理状态：2-已解决，3-已忽略")
    private Integer status;
}
