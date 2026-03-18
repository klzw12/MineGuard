package com.klzw.service.user.dto;

import lombok.Data;

/**
 * 下班打卡 DTO
 */
@Data
public class CheckOutDTO {

    /**
     * 司机ID
     */
    private Long driverId;

    /**
     * 备注
     */
    private String remark;
}
