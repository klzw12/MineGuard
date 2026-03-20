package com.klzw.service.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SmsScene {
    
    REGISTER("REGISTER", "注册", "SMS_100001"),
    LOGIN("LOGIN", "登录", "SMS_100001"),
    RESET_PASSWORD("RESET_PASSWORD", "找回密码", "SMS_100002"),
    UPDATE_PHONE("UPDATE_PHONE", "更换手机号", "SMS_100003"),
    BIND_PHONE("BIND_PHONE", "绑定手机号", "SMS_100004"),
    VERIFY_PHONE("VERIFY_PHONE", "验证手机号", "SMS_100005");
    
    private final String code;
    private final String description;
    private final String templateCode;
    
    public static SmsScene fromCode(String code) {
        for (SmsScene scene : values()) {
            if (scene.getCode().equals(code)) {
                return scene;
            }
        }
        return REGISTER;
    }
}
