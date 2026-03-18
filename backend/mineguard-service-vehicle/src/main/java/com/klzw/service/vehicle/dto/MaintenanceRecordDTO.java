package com.klzw.service.vehicle.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 维修记录DTO
 */
@Data
public class MaintenanceRecordDTO {
    
    private Long vehicleId; // 车辆ID
    private String maintenanceType; // 维修类型
    private String maintenanceContent; // 维修内容
    private String maintenanceResult; // 维修结果
    private LocalDateTime maintenanceDate; // 维修日期
    private LocalDateTime completionDate; // 完成日期
    private String maintenanceCompany; // 维修公司
    private String maintenancePerson; // 维修人员
    private String cost; // 维修费用
    private String partsReplaced; // 更换部件
    private String notes; // 备注
    private Integer status; // 状态：0-待维修，1-维修中，2-已完成
    
}