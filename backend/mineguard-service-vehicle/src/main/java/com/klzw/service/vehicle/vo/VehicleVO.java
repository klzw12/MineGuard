package com.klzw.service.vehicle.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 车辆视图对象
 */
@Data
public class VehicleVO {
    
    private String id;
    
    private String vehicleNo; // 车牌号
    
    private Integer vehicleType; // 车辆类型（1-轿车，2-SUV，3-货车等）
    
    private String brand; // 品牌
    
    private String model; // 型号
    
    private String userId; // 所属用户ID
    
    private String userName; // 所属用户名称
    
    private Integer status; // 状态（0-离线，1-在线，2-行驶中，3-故障，4-维修中）
    
    private String photoUrl; // 车辆照片URL
    
    private String licenseFrontUrl; // 行驶证正面URL
    
    private String licenseBackUrl; // 行驶证反面URL
    
    private String owner; // 所有人
    
    private String address; // 住址
    
    private String brandModel; // 品牌型号
    
    private String vehicleModel; // 车辆型号
    
    private String engineNumber; // 发动机号
    
    private String vin; // 车辆识别代号（VIN）
    
    private String useNature; // 使用性质
    
    private LocalDate registerDate; // 注册日期
    
    private LocalDate issueDate; // 发证日期
    
    private Integer seatingCapacity; // 核定载人数
    
    private String totalMass; // 总质量
    
    private String curbWeight; // 整备质量
    
    private String ratedLoad; // 核定载质量
    
    private String dimensions; // 外廓尺寸
    
    private String remarks; // 备注
    
    private String inspectionRecord; // 检验记录
    
    private String insuranceNo; // 保险单号
    private String insuranceCompany; // 保险公司
    private LocalDate insuranceStartDate; // 保险开始日期
    private LocalDate insuranceEndDate; // 保险结束日期
    private LocalDate insuranceExpiry; // 保险过期日期
    private String insuranceStatus; // 保险状态
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
}
