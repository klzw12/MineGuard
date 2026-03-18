package com.klzw.service.vehicle.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 车辆故障记录DTO
 */
@Data
public class VehicleFaultDTO {
    
    private Long vehicleId; // 车辆ID
    private String faultType; // 故障类型
    private String faultDescription; // 故障描述
    private LocalDateTime faultDate; // 故障日期
    private Integer severity; // 严重程度
    private Long repairmanId; // 维修员ID
    private String repairContent; // 维修内容
    private Double repairCost; // 维修费用
    private Integer status; // 状态：1-未处理，2-处理中，3-已处理
    
}