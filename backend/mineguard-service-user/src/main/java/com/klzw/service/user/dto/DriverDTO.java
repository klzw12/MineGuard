package com.klzw.service.user.dto;

import lombok.Data;

@Data
public class DriverDTO {

    private Long id;

    private Long userId;

    private String driverName;

    private Integer gender;

    private String idCard;

    private String idCardFrontUrl;

    private String idCardBackUrl;

    private String drivingLicenseUrl;

    private String licenseType;

    private String belongingTeam;

    private String address;

    private String birthDate;

    private String firstIssueDate;

    private Integer drivingYears;

    private String validPeriod;

    private String licenseNumber;

    private Integer status;

    private Long vehicleId;

    private String remark;
}
