package com.klzw.service.trip.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "结束行程DTO")
public class TripEndDTO {

    @Schema(description = "终点经度")
    private Double endLongitude;

    @Schema(description = "终点纬度")
    private Double endLatitude;
    
    @Schema(description = "过路费（元）")
    private BigDecimal tolls;
    
    @Schema(description = "收费路段距离（米）")
    private Integer tollDistance;
}
