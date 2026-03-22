package com.klzw.common.core.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DriverInfo {

    private Long id;

    private Long userId;

    private String driverName;

    private Integer gender;

    private String genderName;

    private String idCardMasked;

    private String licenseType;

    private String belongingTeam;

    private Integer drivingYears;

    private Integer status;

    private String statusName;

    private Integer score;

    private List<DriverVehicleInfo> commonVehicles;

    private LocalDateTime createTime;
}
