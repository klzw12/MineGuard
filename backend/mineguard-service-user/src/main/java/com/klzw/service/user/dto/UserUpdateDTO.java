package com.klzw.service.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDTO {

    @Size(min = 2, max = 20, message = "用户名长度为2-20个字符")
    private String username;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String avatarUrl;
}
