package com.klzw.service.user.vo;

import lombok.Data;

import java.util.List;

@Data
public class LoginVO {

    private String accessToken;

    private String refreshToken;

    private String tokenType;

    private Long expiresIn;

    private UserVO user;
}
