package com.klzw.common.auth.exception;

import com.klzw.common.auth.constant.AuthResultCode;
import com.klzw.common.core.exception.BaseException;
import lombok.Getter;

/**
 * 认证异常类
 * <p>
 * 用于处理认证授权相关的异常，包括：
 * - Token过期/无效/缺失
 * - 权限不足
 * - 角色不足
 * - 用户不存在
 * - 密码错误
 * - 账户锁定/禁用
 * <p>
 * 错误码范围：800-899
 */
@Getter
public class AuthException extends BaseException {

    /**
     * 认证模块标识
     */
    private static final String MODULE = "auth";

    public AuthException(AuthResultCode resultCode) {
        super(resultCode.getCode(), resultCode.getMessage(), MODULE);
    }

    public AuthException(AuthResultCode resultCode, Throwable cause) {
        super(resultCode.getCode(), resultCode.getMessage(), MODULE, cause);
    }

    public AuthException(AuthResultCode resultCode, String message) {
        super(resultCode.getCode(), message, MODULE);
    }

    public AuthException(AuthResultCode resultCode, String message, Throwable cause) {
        super(resultCode.getCode(), message, MODULE, cause);
    }
}
