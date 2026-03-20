package com.klzw.service.user.exception;

import com.klzw.common.core.exception.BaseException;
import com.klzw.service.user.constant.UserResultCode;
import lombok.Getter;

/**
 * 用户模块异常类
 * <p>
 * 用于处理用户模块相关的异常，包括：
 * - 用户不存在
 * - 用户名/手机号/邮箱已被注册
 * - 用户已被禁用
 * - 角色分配失败
 * - 人员资格认证失败
 * <p>
 * 错误码范围：2000-2099
 */
@Getter
public class UserException extends BaseException {

    /**
     * 用户模块标识
     */
    private static final String MODULE = "user";

    public UserException(int code, String message) {
        super(code, message, MODULE);
    }

    public UserException(UserResultCode resultCode) {
        super(resultCode.getCode(), resultCode.getMessage(), MODULE);
    }

    public UserException(UserResultCode resultCode, String message) {
        super(resultCode.getCode(), message, MODULE);
    }

    public UserException(UserResultCode resultCode, Throwable cause) {
        super(resultCode.getCode(), resultCode.getMessage(), MODULE, cause);
    }

    public UserException(int code, String message, Throwable cause) {
        super(code, message, MODULE, cause);
    }
}
