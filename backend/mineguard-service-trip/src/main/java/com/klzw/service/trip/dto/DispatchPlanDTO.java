package com.klzw.service.trip.dto;

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

    @NotNull(message = "计划日期不能为空")
    @Schema(description = "计划日期")
    private LocalDate planDate;

    @NotNull(message = "车辆ID不能为空")
    @Schema(description = "车辆ID")
    private Long vehicleId;

    @NotNull(message = "司机ID不能为空")
    @Schema(description = "司机ID")
    private Long driverId;

    @Schema(description = "路线ID")
    private Long routeId;

    @Schema(description = "计划运输次数")
    private Integer plannedTrips;

    @Schema(description = "计划运输吨位")
    private BigDecimal plannedCargoWeight;

    @Schema(description = "发车时段")
    private String startTimeSlot;

    @Schema(description = "备注")
    private String remark;
}
