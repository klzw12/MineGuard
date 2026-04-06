package com.klzw.service.dispatch.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "调度计划DTO")
public class DispatchPlanDTO {

    @Schema(description = "计划ID（更新时必填）")
    private Long id;

    @Schema(description = "计划名称")
    private String planName;

    @NotNull(message = "计划日期不能为空")
    @Schema(description = "计划日期")
    private LocalDate planDate;

    @NotNull(message = "计划类型不能为空")
    @Schema(description = "计划类型：1-运输计划，2-维修计划，3-巡检计划")
    private Integer planType;

    @Schema(description = "车辆ID（可选，智能调度自动分配）")
    private Long vehicleId;

    @Schema(description = "司机ID（可选，智能调度自动分配）")
    private Long driverId;

    @Schema(description = "路线ID")
    private Long routeId;

    @Schema(description = "计划运输次数")
    private Integer plannedTrips;

    @Schema(description = "计划运输吨位")
    private BigDecimal plannedCargoWeight;

    @Schema(description = "发车时段起")
    private String startTimeSlot;

    @Schema(description = "发车时段止")
    private String endTimeSlot;

    @Schema(description = "备注")
    private String remark;
}
