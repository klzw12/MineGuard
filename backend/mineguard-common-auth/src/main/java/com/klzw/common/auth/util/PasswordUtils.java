package com.klzw.common.auth.util;

import com.klzw.common.auth.constant.AuthResultCode;
import com.klzw.common.auth.exception.AuthException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordUtils {

    private final PasswordEncoder passwordEncoder;
    private final AESUtil aesUtil;

    public PasswordUtils(PasswordEncoder passwordEncoder, AESUtil aesUtil) {
        this.passwordEncoder = passwordEncoder;
        this.aesUtil = aesUtil;
    }

    public String encode(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new AuthException(AuthResultCode.PARAMETER_ERROR, "密码不能为空");
        }
        return passwordEncoder.encode(password);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new AuthException(AuthResultCode.PARAMETER_ERROR, "原始密码不能为空");
        }
        if (encodedPassword == null || encodedPassword.trim().isEmpty()) {
            throw new AuthException(AuthResultCode.PARAMETER_ERROR, "加密密码不能为空");
        }
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public String generateRandomPassword(int length) {
        if (length < 8) {
            throw new AuthException(AuthResultCode.PARAMETER_ERROR, "密码长度不能小于8位");
        }
        if (length > 128) {
            throw new AuthException(AuthResultCode.PARAMETER_ERROR, "密码长度不能超过128位");
        }
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            password.append(chars.charAt(index));
        }
        return password.toString();
    }

    public String encodeIdCard(String idCard) {
        if (idCard == null || idCard.trim().isEmpty()) {
            throw new AuthException(AuthResultCode.PARAMETER_ERROR, "身份证号不能为空");
        }
        return aesUtil.encrypt(idCard);
    }
}
