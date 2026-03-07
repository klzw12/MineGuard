package com.klzw.service.user.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserVO {

    private String id;

    private String username;

    private String realName;

    private String phone;

    private String email;

    private String avatarUrl;

    private Integer status;

    private Integer userType;

    private List<String> roles;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
