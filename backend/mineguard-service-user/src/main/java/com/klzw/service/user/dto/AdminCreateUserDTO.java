package com.klzw.service.user.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 管理员创建用户DTO
 * <p>
 * 用于管理员手动录入用户
 */
@Data
public class AdminCreateUserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 手机号（可选）
     */
    private String phone;

    /**
     * 邮箱（可选）
     */
    private String email;

    /**
     * 角色ID（可选，只能分配管理员或调度员角色）
     */
    private Long roleId;
}
