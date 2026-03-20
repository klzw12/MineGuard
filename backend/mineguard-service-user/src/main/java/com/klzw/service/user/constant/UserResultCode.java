package com.klzw.service.user.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户模块错误码枚举
 * <p>
 * 错误码范围：2000-2099
 * <p>
 * 错误码说明：
 * - 2000: 用户通用错误
 * - 2001-2009: 用户基本信息相关错误
 * - 2010-2019: 用户认证相关错误
 * - 2020-2029: 角色相关错误
 * - 2030-2039: 权限相关错误
 * - 2040-2049: 人员资格认证相关错误
 */
@Getter
@AllArgsConstructor
public enum UserResultCode {

    /**
     * 用户通用错误
     */
    USER_ERROR(2000, "用户操作失败"),
    PARAM_ERROR(2001, "参数错误"),

    /**
     * 用户基本信息相关错误
     */
    USER_NOT_FOUND(2001, "用户不存在"),
    USERNAME_EXISTS(2002, "用户名已存在"),
    PHONE_EXISTS(2003, "手机号已被注册"),
    EMAIL_EXISTS(2004, "邮箱已被注册"),
    USER_DISABLED(2005, "用户已被禁用"),
    USER_TYPE_ERROR(2006, "用户类型错误"),

    /**
     * 用户认证相关错误
     */
    PASSWORD_ERROR(2010, "密码错误"),
    PASSWORD_INVALID(2011, "密码无效"),
    OLD_PASSWORD_ERROR(2012, "原密码错误"),
    TOKEN_ERROR(2013, "Token错误"),
    TOKEN_EXPIRED(2014, "Token已过期"),
    SMS_SEND_FAILED(2015, "短信发送失败"),
    SMS_VERIFY_FAILED(2016, "短信验证失败"),

    /**
     * 角色相关错误
     */
    ROLE_NOT_FOUND(2020, "角色不存在"),
    ROLE_DISABLED(2021, "角色已禁用"),
    ROLE_ASSIGN_FAILED(2022, "角色分配失败"),
    USER_ROLE_NOT_FOUND(2023, "用户角色不存在"),

    /**
     * 权限相关错误
     */
    PERMISSION_DENIED(2030, "权限不足"),
    PERMISSION_NOT_FOUND(2031, "权限不存在"),

    /**
     * 人员资格认证相关错误
     */
    ID_CARD_EXISTS(2040, "身份证号已被注册"),
    ID_CARD_INVALID(2041, "身份证号无效"),
    OCR_FAILED(2042, "OCR识别失败"),
    CERTIFICATE_EXPIRED(2043, "证书已过期"),
    CERTIFICATE_INVALID(2044, "证书无效"),
    QUALIFICATION_NOT_VERIFIED(2045, "资格未认证"),
    QUALIFICATION_VERIFY_FAILED(2046, "资格认证失败"),

    /**
     * 其他错误
     */
    DATA_ERROR(2050, "数据错误"),
    OPERATION_FAILED(2051, "操作失败");

    private final int code;
    private final String message;
}
