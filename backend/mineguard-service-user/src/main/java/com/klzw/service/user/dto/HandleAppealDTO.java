package com.klzw.service.user.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 处理用户申诉DTO
 * <p>
 * 用于管理员处理用户申诉请求
 */
@Data
public class HandleAppealDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 处理状态：2-已通过（解除禁用），3-已驳回，4-已驳回并删除账号
     */
    @NotNull(message = "处理状态不能为空")
    private Integer status;

    /**
     * 管理员处理意见
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
