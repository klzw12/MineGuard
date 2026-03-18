package com.klzw.common.core.domain.dto;

import lombok.Data;

/**
 * 用户信息DTO
 */
@Data
public class UserInfo {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private Integer status;
    private Long roleId;
    private String roleCode;
    private String roleName;
}