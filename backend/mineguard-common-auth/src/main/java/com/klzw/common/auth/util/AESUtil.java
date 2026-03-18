package com.klzw.common.auth.util;

import com.klzw.common.auth.constant.AuthResultCode;
import com.klzw.common.auth.exception.AuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Component
public class AESUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;

    private final SecretKey secretKey;

    public AESUtil() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(KEY_SIZE, new SecureRandom());
            this.secretKey = keyGenerator.generateKey();
            log.info("AES密钥已生成，长度：{}位", KEY_SIZE);
        } catch (Exception e) {
            throw new AuthException(AuthResultCode.SYSTEM_ERROR, "AES密钥生成失败");
        }
    }

    public String encrypt(String plainText) {
        if (plainText == null || plainText.trim().isEmpty()) {
            throw new AuthException(AuthResultCode.PARAMETER_ERROR, "加密内容不能为空");
        }
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
            
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            
            byte[] encryptedBytesWithIv = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, encryptedBytesWithIv, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, encryptedBytesWithIv, iv.length, encryptedBytes.length);
            
            return Base64.getEncoder().encodeToString(encryptedBytesWithIv);
        } catch (Exception e) {
            log.error("AES加密失败", e);
            throw new AuthException(AuthResultCode.SYSTEM_ERROR, "加密失败");
        }
    }

    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.trim().isEmpty()) {
            throw new AuthException(AuthResultCode.PARAMETER_ERROR, "解密内容不能为空");
        }
        try {
            byte[] encryptedBytesWithIv = Base64.getDecoder().decode(encryptedText);
            
            if (encryptedBytesWithIv.length < GCM_IV_LENGTH) {
                throw new AuthException(AuthResultCode.SYSTEM_ERROR, "加密数据格式错误");
            }
            
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encryptedBytes = new byte[encryptedBytesWithIv.length - GCM_IV_LENGTH];
            
            System.arraycopy(encryptedBytesWithIv, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedBytesWithIv, GCM_IV_LENGTH, encryptedBytes, 0, encryptedBytes.length);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
            
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES解密失败", e);
            throw new AuthException(AuthResultCode.SYSTEM_ERROR, "解密失败");
        }
    }
}