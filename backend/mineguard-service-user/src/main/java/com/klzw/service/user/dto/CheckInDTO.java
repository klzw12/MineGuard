package com.klzw.service.user.dto;

import lombok.Data;

/**
 * 上班打卡 DTO
 */
@Data
public class CheckInDTO {

    private Double latitude;

    private Double longitude;

    private String address;

    private String remark;
}
