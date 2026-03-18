package com.klzw.service.vehicle.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 保险信息DTO
 */
@Data
public class InsuranceInfoDTO {
    
    private String insuranceCompany; // 保险公司
    private String policyNo; // 保单号
    private LocalDate startDate; // 开始日期
    private LocalDate endDate; // 结束日期
    private String insuranceType; // 保险类型
    private String insuredName; // 被保险人
    private String contactPhone; // 联系电话
    private String insuranceAmount; // 保险金额
    private String insuranceStatus; // 保险状态：有效、过期
    
}