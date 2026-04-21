package com.klzw.service.trip.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "行程VO")
public class TripVO {

    @Schema(description = "行程ID")
    private String id;

    @Schema(description = "行程编号")
    private String tripNo;

    @Schema(description = "车辆ID")
    private String vehicleId;

    @Schema(description = "车牌号")
    private String vehicleNo;

    @Schema(description = "司机ID")
    private String driverId;

    @Schema(description = "司机姓名")
    private String driverName;

    @Schema(description = "起点")
    private String startLocation;

    @Schema(description = "终点")
    private String endLocation;

    @Schema(description = "预计开始时间")
    private LocalDateTime estimatedStartTime;

    @Schema(description = "预计结束时间")
    private LocalDateTime estimatedEndTime;

    @Schema(description = "实际开始时间")
    private LocalDateTime actualStartTime;

    @Schema(description = "实际结束时间")
    private LocalDateTime actualEndTime;

    @Schema(description = "行程状态：0-待开始，1-已接单，2-进行中，3-已完成，4-已取消")
    private Integer status;

    @Schema(description = "行程类型：1-日常，2-应急，3-维修，4-其他")
    private Integer tripType;

    @Schema(description = "预计里程（km）")
    private Double estimatedMileage;

    @Schema(description = "实际里程（km）")
    private Double actualMileage;

    @Schema(description = "预计时长（分钟）")
    private Integer estimatedDuration;

    @Schema(description = "实际时长（分钟）")
    private Integer actualDuration;

    @Schema(description = "燃油消耗（升）")
    private Double fuelConsumption;

    @Schema(description = "平均速度（km/h）")
    private Double averageSpeed;

    @Schema(description = "货物重量（吨）")
    private BigDecimal cargoWeight;

    @Schema(description = "实际货物重量（吨）- 后核算填写")
    private BigDecimal actualCargoWeight;

    @Schema(description = "取消原因")
    private String cancellationReason;

    @Schema(description = "起点经度")
    private Double startLongitude;

    @Schema(description = "起点纬度")
    private Double startLatitude;

    @Schema(description = "终点经度")
    private Double endLongitude;

    @Schema(description = "终点纬度")
    private Double endLatitude;

    @Schema(description = "备注")
    private String remark;
    
    @Schema(description = "优先级: time, cost, distance")
    private String priority;
    
    @Schema(description = "截止日期")
    private LocalDateTime deadline;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    @Schema(description = "最后位置经度")
    private Double lastLongitude;
    
    @Schema(description = "最后位置纬度")
    private Double lastLatitude;
    
    @Schema(description = "最后位置记录时间")
    private LocalDateTime lastRecordTime;
    
    @Schema(description = "AI分析结果（JSON格式）")
    private String aiAnalysis;
    
    @Schema(description = "预计提成金额")
    private BigDecimal estimatedCommissionAmount;
}
