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
    
    private String vehicleNo;
    
    private Integer vehicleType;
    
    private String brand;
    
    private String model;
    
    private Integer status;
    
    private Integer fuelLevel;
    
    private String photoUrl;
    
    private String licenseFrontUrl;
    
    private String licenseBackUrl;
    
    private String owner;
    
    private String address;
    
    private String brandModel;
    
    private String vehicleModel;
    
    private String engineNumber;
    
    private String vin;
    
    private String useNature;
    
    private LocalDate registerDate;
    
    private LocalDate issueDate;
    
    private Integer seatingCapacity;
    
    private String totalMass;
    
    private String curbWeight;
    
    private String ratedLoad;
    
    private String dimensions;
    
    private String remarks;
    
    private String inspectionRecord;
    
    private String insuranceNo;
    private String insuranceCompany;
    private LocalDate insuranceStartDate;
    private LocalDate insuranceEndDate;
    private LocalDate insuranceExpiry;
    private String insuranceStatus;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
}
