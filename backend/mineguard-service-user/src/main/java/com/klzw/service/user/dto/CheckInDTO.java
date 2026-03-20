package com.klzw.service.user.dto;

import lombok.Data;

/**
 * 上班打卡 DTO
 */
@Data
public class CheckInDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 备注
     */
    private String remark;
}
