package com.klzw.common.core.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

/**
 * 加密工具类测试
 */
public class EncryptUtilsTest {

    @Test
    @DisplayName("测试 MD5 加密")
    public void testMd5() {
        String original = "test123";
        String encrypted = EncryptUtils.md5(original);
        assertNotNull(encrypted);
        assertEquals(32, encrypted.length());
        
        // 测试相同字符串加密结果相同
        String encrypted2 = EncryptUtils.md5(original);
        assertEquals(encrypted, encrypted2);
    }

    @Test
    @DisplayName("测试 MD5 加密空值")
    public void testMd5WithNull() {
        assertNull(EncryptUtils.md5(null));
        assertNull(EncryptUtils.md5(""));
        assertNull(EncryptUtils.md5("   "));
    }

    @Test
    @DisplayName("测试 SHA-256 加密")
    public void testSha256() {
        String original = "test123";
        String encrypted = EncryptUtils.sha256(original);
        assertNotNull(encrypted);
        assertEquals(64, encrypted.length());
        
        // 测试相同字符串加密结果相同
        String encrypted2 = EncryptUtils.sha256(original);
        assertEquals(encrypted, encrypted2);
    }

    @Test
    @DisplayName("测试 SHA-256 加密空值")
    public void testSha256WithNull() {
        assertNull(EncryptUtils.sha256(null));
        assertNull(EncryptUtils.sha256(""));
        assertNull(EncryptUtils.sha256("   "));
    }
}