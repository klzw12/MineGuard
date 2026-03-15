package com.klzw.common.auth.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 认证模块错误码枚举
 * <p>
 * 错误码范围：800-899
 * <p>
 * 错误码说明：
 * - 800: 认证通用错误
 * - 801-809: Token相关错误
 * - 810-819: 权限相关错误
 * - 820-829: 角色相关错误
 * - 830-839: 用户相关错误
 * - 840-849: 账户相关错误
 */
@Getter
@AllArgsConstructor
public enum AuthResultCode {

    /**
     * 认证通用错误
     */
    AUTH_ERROR(800, "认证操作失败"),
    PARAMETER_ERROR(801, "参数错误"),

    /**
     * Token相关错误
     */
    TOKEN_EXPIRED(802, "Token已过期"),
    TOKEN_INVALID(803, "Token无效"),
    TOKEN_MISSING(804, "Token缺失"),
    TOKEN_SIGNATURE_ERROR(805, "Token签名错误"),
    TOKEN_PARSE_ERROR(806, "Token解析错误"),
    TOKEN_REVOKED(807, "Token已被撤销"),

    /**
     * 权限相关错误
     */
    PERMISSION_DENIED(810, "权限不足"),
    PERMISSION_NOT_FOUND(811, "权限不存在"),
    PERMISSION_EXPIRED(812, "权限已过期"),
    PERMISSION_DISABLED(813, "权限已禁用"),

    /**
     * 角色相关错误
     */
    ROLE_DENIED(820, "角色不足"),
    ROLE_NOT_FOUND(821, "角色不存在"),
    ROLE_DISABLED(822, "角色已禁用"),
    ROLE_EXPIRED(823, "角色已过期"),

    /**
     * 用户相关错误
     */
    USER_NOT_FOUND(830, "用户不存在"),
    PASSWORD_ERROR(831, "密码错误"),
    PASSWORD_EXPIRED(832, "密码已过期"),
    PASSWORD_TOO_WEAK(833, "密码强度不足"),
    USERNAME_TAKEN(834, "用户名已被使用"),
    EMAIL_TAKEN(835, "邮箱已被使用"),
    PHONE_TAKEN(836, "手机号已被使用"),

    /**
     * 账户相关错误
     */
    ACCOUNT_LOCKED(840, "账户已锁定"),
    ACCOUNT_DISABLED(841, "账户已禁用"),
    ACCOUNT_EXPIRED(842, "账户已过期"),
    ACCOUNT_NOT_ACTIVATED(843, "账户未激活"),
    ACCOUNT_TOO_MANY_ATTEMPTS(844, "账户尝试次数过多"),

    /**
     * 其他认证错误
     */
    LOGIN_FAILED(850, "登录失败"),
    LOGOUT_FAILED(851, "登出失败"),
    REGISTER_FAILED(852, "注册失败"),
    VERIFICATION_FAILED(853, "验证失败"),
    SESSION_EXPIRED(854, "会话已过期"),
    
    /**
     * 短信相关错误
     */
    SMS_SEND_FAILED(860, "短信发送失败"),
    SMS_CODE_ERROR(861, "验证码错误"),
    SMS_CODE_EXPIRED(862, "验证码已过期"),
    SMS_SEND_FREQUENCY_ERROR(863, "短信发送频率过高");


    private final int code;
    private final String message;
}
