package com.klzw.service.user.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户模块错误码枚举
 * <p>
 * 错误码范围：2100-2199
 */
@Getter
@AllArgsConstructor
public enum UserResultCode {

    USER_ERROR(2100, "用户操作失败"),

    USER_NOT_FOUND(2101, "用户不存在"),
    USERNAME_EXISTS(2102, "用户名已存在"),
    PHONE_EXISTS(2103, "手机号已被注册"),
    EMAIL_EXISTS(2104, "邮箱已被注册"),
    USER_DISABLED(2105, "用户已被禁用"),
    USER_TYPE_ERROR(2106, "用户类型错误"),

    PASSWORD_ERROR(2110, "密码错误"),
    PASSWORD_INVALID(2111, "密码无效"),
    OLD_PASSWORD_ERROR(2112, "原密码错误"),
    SMS_SEND_FAILED(2115, "短信发送失败"),
    SMS_VERIFY_FAILED(2116, "短信验证失败"),

    ROLE_NOT_FOUND(2120, "角色不存在"),
    ROLE_DISABLED(2121, "角色已禁用"),
    ROLE_ASSIGN_FAILED(2122, "角色分配失败"),
    USER_ROLE_NOT_FOUND(2123, "用户角色不存在"),

    PERMISSION_DENIED(2130, "权限不足"),
    PERMISSION_NOT_FOUND(2131, "权限不存在"),

    ID_CARD_EXISTS(2140, "身份证号已被注册"),
    ID_CARD_INVALID(2141, "身份证号无效"),
    OCR_FAILED(2142, "OCR识别失败"),
    CERTIFICATE_EXPIRED(2143, "证书已过期"),
    CERTIFICATE_INVALID(2144, "证书无效"),
    QUALIFICATION_NOT_VERIFIED(2145, "资格未认证"),
    QUALIFICATION_VERIFY_FAILED(2146, "资格认证失败"),

    DATA_ERROR(2150, "数据错误"),
    OPERATION_FAILED(2151, "操作失败");

    private final int code;
    private final String message;
}
