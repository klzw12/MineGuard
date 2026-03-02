package com.klzw.common.core.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 加密工具类
 * 提供常用的加密方法
 */
public class EncryptUtils {
    /**
     * MD5 加密
     * @param str 待加密字符串
     * @return MD5 加密后的字符串，如果str为空白则返回null
     * @throws RuntimeException 如果MD5加密失败
     */
    public static String md5(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 encryption failed", e);
        }
    }

    /**
     * SHA-256 加密
     * @param str 待加密字符串
     * @return SHA-256 加密后的字符串，如果str为空白则返回null
     * @throws RuntimeException 如果SHA-256加密失败
     */
    public static String sha256(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(str.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 encryption failed", e);
        }
    }
}
