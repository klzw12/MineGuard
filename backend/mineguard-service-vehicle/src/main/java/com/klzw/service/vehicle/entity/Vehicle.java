package com.klzw.service.vehicle.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.klzw.common.database.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vehicle")
public class Vehicle extends BaseEntity {
    
    private String vehicleNo;
    
    private Integer vehicleType;
    
    private String brand;
    
    private String model;
    
    private Long userId;
    
    private Integer status;
    
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
}
