package com.klzw.service.vehicle.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.klzw.common.database.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vehicle_insurance")
public class VehicleInsurance extends BaseEntity {
    
    private Long vehicleId;
    
    private String insuranceCompany;
    
    private String insuranceNumber;
    
    private Integer insuranceType;
    
    private BigDecimal insuranceAmount;
    
    private LocalDate startDate;
    
    private LocalDate expiryDate;
    
    private Integer status;
    
    private String remark;
    
}
