package com.klzw.common.auth.util;

import com.klzw.common.auth.exception.AuthException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AES工具类单元测试
 */
class AESUtilTest {

    private AESUtil aesUtil;

    @BeforeEach
    void setUp() {
        aesUtil = new AESUtil();
    }

    @Test
    void encrypt_shouldReturnEncryptedText() {
        String plainText = "110101199001011234";

        String encrypted = aesUtil.encrypt(plainText);

        assertNotNull(encrypted);
        assertNotEquals(plainText, encrypted);
        assertTrue(encrypted.length() > 0);
    }

    @Test
    void decrypt_shouldReturnOriginalText() {
        String plainText = "110101199001011234";

        String encrypted = aesUtil.encrypt(plainText);
        String decrypted = aesUtil.decrypt(encrypted);

        assertEquals(plainText, decrypted);
    }

    @Test
    void encrypt_shouldReturnDifferentCiphertext_forSamePlaintext() {
        String plainText = "110101199001011234";

        String encrypted1 = aesUtil.encrypt(plainText);
        String encrypted2 = aesUtil.encrypt(plainText);

        assertNotEquals(encrypted1, encrypted2, "每次加密结果应该不同（因为使用了随机IV）");
    }

    @Test
    void decrypt_shouldWork_forMultipleEncryptions() {
        String plainText = "110101199001011234";

        String encrypted1 = aesUtil.encrypt(plainText);
        String encrypted2 = aesUtil.encrypt(plainText);

        assertEquals(plainText, aesUtil.decrypt(encrypted1));
        assertEquals(plainText, aesUtil.decrypt(encrypted2));
    }

    @Test
    void encrypt_shouldHandleLongText() {
        String plainText = "这是一段很长的文本，用于测试AES加密是否能够正确处理较长的字符串内容。This is a long text to test AES encryption.";

        String encrypted = aesUtil.encrypt(plainText);
        String decrypted = aesUtil.decrypt(encrypted);

        assertEquals(plainText, decrypted);
    }

    @Test
    void encrypt_shouldHandleSpecialCharacters() {
        String plainText = "特殊字符!@#$%^&*()_+-=[]{}|;':\",./<>?~`";

        String encrypted = aesUtil.encrypt(plainText);
        String decrypted = aesUtil.decrypt(encrypted);

        assertEquals(plainText, decrypted);
    }

    @Test
    void encrypt_shouldHandleChineseCharacters() {
        String plainText = "中文测试，包含各种汉字：矿用卡车司机";

        String encrypted = aesUtil.encrypt(plainText);
        String decrypted = aesUtil.decrypt(encrypted);

        assertEquals(plainText, decrypted);
    }

    @Test
    void encrypt_shouldThrowException_whenTextIsNull() {
        assertThrows(AuthException.class, () -> aesUtil.encrypt(null));
    }

    @Test
    void encrypt_shouldThrowException_whenTextIsEmpty() {
        assertThrows(AuthException.class, () -> aesUtil.encrypt(""));
    }

    @Test
    void encrypt_shouldThrowException_whenTextIsBlank() {
        assertThrows(AuthException.class, () -> aesUtil.encrypt("   "));
    }

    @Test
    void decrypt_shouldThrowException_whenTextIsNull() {
        assertThrows(AuthException.class, () -> aesUtil.decrypt(null));
    }

    @Test
    void decrypt_shouldThrowException_whenTextIsEmpty() {
        assertThrows(AuthException.class, () -> aesUtil.decrypt(""));
    }

    @Test
    void decrypt_shouldThrowException_whenTextIsInvalid() {
        assertThrows(AuthException.class, () -> aesUtil.decrypt("invalid_encrypted_text"));
    }

    @Test
    void decrypt_shouldThrowException_whenTextIsTooShort() {
        String shortText = "abc";
        assertThrows(AuthException.class, () -> aesUtil.decrypt(shortText));
    }
}
