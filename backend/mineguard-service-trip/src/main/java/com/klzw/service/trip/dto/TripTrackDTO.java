package com.klzw.service.trip.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "轨迹点DTO")
public class TripTrackDTO {

    @Schema(description = "行程ID")
    private Long tripId;

    @Schema(description = "车辆ID")
    private Long vehicleId;

    @Schema(description = "司机ID")
    private Long driverId;

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
    private Long recordTime;
}
