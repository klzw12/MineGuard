package com.klzw.service.warning.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "预警记录VO")
public class WarningRecordVO {

    @Schema(description = "预警ID")
    private Long id;

    @Schema(description = "预警编号")
    private String warningNo;

    @Schema(description = "规则ID")
    private Long ruleId;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "预警类型")
    private Integer warningType;

    @Schema(description = "预警类型名称")
    private String warningTypeName;

    @Schema(description = "预警级别")
    private Integer warningLevel;

    @Schema(description = "预警级别名称")
    private String warningLevelName;

    @Schema(description = "车辆ID")
    private Long vehicleId;

    @Schema(description = "车牌号")
    private String vehicleNo;

    @Schema(description = "司机ID")
    private Long driverId;

    @Schema(description = "司机姓名")
    private String driverName;

    @Schema(description = "行程ID")
    private Long tripId;

    @Schema(description = "行程编号")
    private String tripNo;

    @Schema(description = "经度")
    private Double longitude;

    @Schema(description = "纬度")
    private Double latitude;

    @Schema(description = "速度")
    private BigDecimal speed;

    @Schema(description = "预警内容")
    private String warningContent;

    @Schema(description = "预警时间")
    private LocalDateTime warningTime;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "处理人ID")
    private Long handlerId;

    @Schema(description = "处理人姓名")
    private String handlerName;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "处理结果")
    private String handleResult;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
