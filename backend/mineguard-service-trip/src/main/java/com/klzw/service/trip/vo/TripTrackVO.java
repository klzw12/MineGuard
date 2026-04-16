package com.klzw.service.trip.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "轨迹点VO")
public class TripTrackVO {

    @Schema(description = "轨迹点ID")
    private String id;

    @Schema(description = "行程ID")
    private String tripId;

    @Schema(description = "车辆ID")
    private String vehicleId;

    @Schema(description = "经度")
    private Double longitude;

    @Schema(description = "纬度")
    private Double latitude;

    @Schema(description = "速度（km/h）")
    private Double speed;

    @Schema(description = "方向（度）")
    private Double direction;

    @Schema(description = "海拔（米）")
    private Double altitude;

    @Schema(description = "时间戳")
    private LocalDateTime timestamp;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "轨迹点序号")
    private Integer pointIndex;
}
