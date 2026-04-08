package com.klzw.service.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminVerifyDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private String realName;

    private String idCard;

    @jakarta.validation.constraints.NotBlank(message = "身份证正面照片不能为空")
    private String idCardFrontBase64;

    private String idCardBackBase64;

}
