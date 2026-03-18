package com.klzw.service.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.klzw.common.database.domain.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("driver")
public class Driver extends BaseEntity {

    private Long userId;

    private String driverName;

    private Integer gender;

    private Integer age;

    private String idCard;

    private String idCardFrontUrl;

    private String idCardBackUrl;

    private String drivingLicenseUrl;

    private Integer drivingYears;

    private String licenseType;

    private String belongingTeam;

    private String address;

    private String birthDate;

    private String firstIssueDate;

    private String validPeriod;

    private String licenseNumber;

    private Integer status;
}
