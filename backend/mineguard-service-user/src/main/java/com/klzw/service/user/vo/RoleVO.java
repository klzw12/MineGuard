package com.klzw.service.user.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoleVO {

    private String id;

    private String roleName;

    private String roleCode;

    private String description;

    private LocalDateTime createTime;
}
