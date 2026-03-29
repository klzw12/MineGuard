package com.klzw.service.user.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 处理角色变更申请DTO
 */
@Data
public class HandleRoleChangeApplyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 处理状态：1-通过，2-拒绝
     */
    @NotNull(message = "处理状态不能为空")
    private Integer status;

    /**
     * 管理员意见
     */
    private String adminOpinion;

    /**
     * 处理人ID
     */
    @NotNull(message = "处理人ID不能为空")
    private Long handlerId;

    /**
     * 处理人姓名
     */
    @NotNull(message = "处理人姓名不能为空")
    private String handlerName;
}
