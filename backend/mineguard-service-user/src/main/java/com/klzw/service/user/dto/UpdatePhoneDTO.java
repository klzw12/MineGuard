package com.klzw.service.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdatePhoneDTO {

    @NotBlank(message = "新手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String newPhone;

    @NotBlank(message = "短信验证码不能为空")
    @Pattern(regexp = "^\\d{4,6}$", message = "短信验证码必须为4-6位数字")
    private String smsCode;
}
