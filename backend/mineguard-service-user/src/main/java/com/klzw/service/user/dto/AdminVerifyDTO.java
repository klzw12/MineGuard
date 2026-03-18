package com.klzw.service.user.dto;

import lombok.Data;

@Data
public class AdminVerifyDTO {

    private Long userId;

    private String realName;

    private String phone;

    private String idCardFrontBase64;

    private String idCardBackBase64;
}
