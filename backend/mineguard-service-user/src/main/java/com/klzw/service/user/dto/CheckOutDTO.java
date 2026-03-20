package com.klzw.service.user.dto;

import lombok.Data;

/**
 * 下班打卡 DTO
 */
@Data
public class CheckOutDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 备注
     */
    private String remark;
}
