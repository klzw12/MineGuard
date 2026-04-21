package com.klzw.common.core.domain.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 行程创建请求 DTO
 */
@Data
public class TripCreateRequest {
    
    /**
     * 车辆 ID
     */
    private Long vehicleId;
    
    /**
     * 司机 ID
     */
    private Long driverId;
    
    /**
     * 调度任务 ID
     */
    private Long dispatchTaskId;
    
    /**
     * 起点位置名称
     */
    private String startLocation;
    
    /**
     * 终点位置名称
     */
    private String endLocation;
    
    /**
     * 起点经度
     */
    private Double startLongitude;
    
    /**
     * 起点纬度
     */
    private Double startLatitude;
    
    /**
     * 终点经度
     */
    private Double endLongitude;
    
    /**
     * 终点纬度
     */
    private Double endLatitude;
    
    /**
     * 预计开始时间
     */
    private LocalDateTime estimatedStartTime;
    
    /**
     * 预计结束时间
     */
    private LocalDateTime estimatedEndTime;
    
    /**
     * 行程类型（1-日常，2-应急，3-维修，4-其他）
     */
    private Integer tripType;
    
    /**
     * 货物重量(吨)
     */
    private BigDecimal cargoWeight;
    
    /**
     * 预计提成金额
     */
    private BigDecimal estimatedCommissionAmount;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 优先级: time, cost, distance
     */
    private String priority;
    
    /**
     * 截止日期
     */
    private LocalDateTime deadline;
}
