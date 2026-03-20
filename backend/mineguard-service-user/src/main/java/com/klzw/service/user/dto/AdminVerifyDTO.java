package com.klzw.service.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminVerifyDTO {

    @NotBlank(message = "用户ID不能为空")
    private String userId;

    private String realName;

    private String idCard;

    @NotBlank(message = "身份证正面照片不能为空")
    private String idCardFrontBase64;

    private String idCardBackBase64;

}
