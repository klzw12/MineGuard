package com.klzw.service.user.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {

    private String id;

    private String username;

    private String realName;

    private String phone;

    private String email;

    private String avatarUrl;

    private Integer status;

    private String roleId;

    private String roleCode;

    private String roleName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String token;

    private String refreshToken;

    private Long expiresIn;

    private String belongingTeam;
}
