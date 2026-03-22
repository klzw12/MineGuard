package com.klzw.service.user.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DriverVO {

    private Long id;

    private Long userId;

    private String driverName;

    private Integer gender;

    private String genderName;

    private String idCardMasked;

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

    private String statusName;

    private List<DriverVehicleVO> commonVehicles;

    private Integer score;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String remark;
}
