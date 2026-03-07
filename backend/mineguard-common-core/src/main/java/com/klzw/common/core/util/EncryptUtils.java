package com.klzw.common.core.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 加密工具类
 * 提供常用的加密方法
 */
public class EncryptUtils {
    
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/ECB/PKCS5Padding";

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

    /**
     * AES 加密
     * @param plainText 明文
     * @param key 密钥（必须是16位）
     * @return Base64编码的密文
     * @throws RuntimeException 如果加密失败
     */
    public static String encrypt(String plainText, String key) {
        if (StringUtils.isBlank(plainText) || StringUtils.isBlank(key)) {
            return null;
        }
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("AES encryption failed", e);
        }
    }

    /**
     * AES 解密
     * @param encryptedText Base64编码的密文
     * @param key 密钥（必须是16位）
     * @return 明文
     * @throws RuntimeException 如果解密失败
     */
    public static String decrypt(String encryptedText, String key) {
        if (StringUtils.isBlank(encryptedText) || StringUtils.isBlank(key)) {
            return null;
        }
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES decryption failed", e);
        }
    }
}
