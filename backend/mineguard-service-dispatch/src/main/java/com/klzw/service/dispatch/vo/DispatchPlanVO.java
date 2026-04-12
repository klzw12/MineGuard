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

    @Schema(description = "计划名称")
    private String planName;

    @Schema(description = "计划日期")
    private LocalDate planDate;

    @Schema(description = "计划类型：0-日常调度，1-临时调度，2-紧急调度")
    private Integer planType;

    @Schema(description = "路线ID")
    private String routeId;

    @Schema(description = "路线名称")
    private String routeName;

    @Schema(description = "起点位置")
    private String startLocation;

    @Schema(description = "终点位置")
    private String endLocation;

    @Schema(description = "计划运输次数")
    private Integer plannedTrips;

    @Schema(description = "已完成运输次数")
    private Integer completedTrips;

    @Schema(description = "计划运输吨位")
    private BigDecimal plannedCargoWeight;

    @Schema(description = "发车时段起")
    private String startTimeSlot;

    @Schema(description = "发车时段止")
    private String endTimeSlot;

    @Schema(description = "计划状态：0-待分配，1-执行中，2-已完成，3-已取消")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
