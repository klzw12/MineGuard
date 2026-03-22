package com.klzw.service.vehicle.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "最佳车辆推荐VO")
public class BestVehicleVO {

    @Schema(description = "车辆ID")
    private Long vehicleId;

    @Schema(description = "车牌号")
    private String vehicleNo;

    @Schema(description = "车辆类型")
    private Integer vehicleType;

    @Schema(description = "品牌")
    private String brand;

    @Schema(description = "型号")
    private String model;

    @Schema(description = "额定载重(吨)")
    private String ratedLoad;

    @Schema(description = "当前油量(%)")
    private Integer fuelLevel;

    @Schema(description = "车辆状态")
    private Integer status;

    @Schema(description = "司机ID")
    private Long driverId;

    @Schema(description = "司机姓名")
    private String driverName;

    @Schema(description = "推荐得分(越高越好)")
    private Integer score;

    @Schema(description = "推荐理由")
    private String reason;
}
