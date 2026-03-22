package com.klzw.service.dispatch.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "调度计划VO")
public class DispatchPlanVO {

    @Schema(description = "计划ID")
    private String id;

    @Schema(description = "计划编号")
    private String planNo;

    @Schema(description = "计划日期")
    private LocalDate planDate;

    @Schema(description = "车辆ID")
    private String vehicleId;

    @Schema(description = "车辆编号")
    private String vehicleNo;

    @Schema(description = "司机ID")
    private String driverId;

    @Schema(description = "司机姓名")
    private String driverName;

    @Schema(description = "路线ID")
    private String routeId;

    @Schema(description = "路线名称")
    private String routeName;

    @Schema(description = "计划运输次数")
    private Integer plannedTrips;

    @Schema(description = "实际运输次数")
    private Integer actualTrips;

    @Schema(description = "计划运输吨位")
    private BigDecimal plannedCargoWeight;

    @Schema(description = "实际运输吨位")
    private BigDecimal actualCargoWeight;

    @Schema(description = "发车时段")
    private String startTimeSlot;

    @Schema(description = "计划状态：1-待执行，2-执行中，3-已完成，4-已取消")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
