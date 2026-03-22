package com.klzw.service.user.dto;

import lombok.Data;

/**
 * 下班打卡 DTO
 */
@Data
public class CheckOutDTO {

    private Double latitude;

    private Double longitude;

    private String address;

    private String remark;
}
