package com.klzw.service.user.dto;

import lombok.Data;

/**
 * 上班打卡 DTO
 */
@Data
public class CheckInDTO {

    /**
     * 司机ID
     */
    private Long driverId;

    /**
     * 备注
     */
    private String remark;
}
