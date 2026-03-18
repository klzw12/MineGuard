package com.klzw.service.user.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 用户申诉DTO
 * <p>
 * 用于提交用户申诉请求
 */
@Data
public class UserAppealDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 申诉原因
     */
    @NotBlank(message = "申诉原因不能为空")
    private String appealReason;
}
