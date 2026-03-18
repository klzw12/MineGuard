package com.klzw.common.auth.util;

import com.klzw.common.auth.exception.AuthException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * 密码工具类单元测试
 */
@ExtendWith(MockitoExtension.class)
class PasswordUtilsTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AESUtil aesUtil;

    private PasswordUtils passwordUtils;

    @BeforeEach
    void setUp() {
        passwordUtils = new PasswordUtils(passwordEncoder, aesUtil);
    }

    @Test
    void encode_shouldReturnEncodedPassword() {
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword123";
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        String result = passwordUtils.encode(rawPassword);

        assertEquals(encodedPassword, result);
    }

    @Test
    void matches_shouldReturnTrue_whenPasswordsMatch() {
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword123";
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        boolean result = passwordUtils.matches(rawPassword, encodedPassword);

        assertTrue(result);
    }

    @Test
    void matches_shouldReturnFalse_whenPasswordsNotMatch() {
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword123";
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        boolean result = passwordUtils.matches(rawPassword, encodedPassword);

        assertFalse(result);
    }

    @Test
    void generateRandomPassword_shouldGeneratePasswordWithSpecifiedLength() {
        int length = 10;

        String password = passwordUtils.generateRandomPassword(length);

        assertNotNull(password);
        assertEquals(length, password.length());
    }

    @Test
    void generateRandomPassword_shouldGenerateDifferentPasswords() {
        int length = 10;

        String password1 = passwordUtils.generateRandomPassword(length);
        String password2 = passwordUtils.generateRandomPassword(length);

        assertNotNull(password1);
        assertNotNull(password2);
        assertEquals(length, password1.length());
        assertEquals(length, password2.length());
        // 生成的密码应该不同（概率极低会相同）
        assertNotEquals(password1, password2);
    }

    @Test
    void generateRandomPassword_shouldContainValidCharacters() {
        int length = 20;
        String validChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";

        String password = passwordUtils.generateRandomPassword(length);

        for (char c : password.toCharArray()) {
            assertTrue(validChars.indexOf(c) >= 0, "Password contains invalid character: " + c);
        }
    }

    @Test
    void encode_shouldThrowException_whenPasswordIsNull() {
        assertThrows(AuthException.class, () -> passwordUtils.encode(null));
    }

    @Test
    void encode_shouldThrowException_whenPasswordIsEmpty() {
        assertThrows(AuthException.class, () -> passwordUtils.encode(""));
    }

    @Test
    void encode_shouldThrowException_whenPasswordIsBlank() {
        assertThrows(AuthException.class, () -> passwordUtils.encode("   "));
    }

    @Test
    void matches_shouldThrowException_whenRawPasswordIsNull() {
        String encodedPassword = "encodedPassword123";
        assertThrows(AuthException.class, () -> passwordUtils.matches(null, encodedPassword));
    }

    @Test
    void matches_shouldThrowException_whenRawPasswordIsEmpty() {
        String encodedPassword = "encodedPassword123";
        assertThrows(AuthException.class, () -> passwordUtils.matches("", encodedPassword));
    }

    @Test
    void matches_shouldThrowException_whenEncodedPasswordIsNull() {
        String rawPassword = "password123";
        assertThrows(AuthException.class, () -> passwordUtils.matches(rawPassword, null));
    }

    @Test
    void matches_shouldThrowException_whenEncodedPasswordIsEmpty() {
        String rawPassword = "password123";
        assertThrows(AuthException.class, () -> passwordUtils.matches(rawPassword, ""));
    }

    @Test
    void generateRandomPassword_shouldThrowException_whenLengthLessThan8() {
        assertThrows(AuthException.class, () -> passwordUtils.generateRandomPassword(7));
        assertThrows(AuthException.class, () -> passwordUtils.generateRandomPassword(0));
        assertThrows(AuthException.class, () -> passwordUtils.generateRandomPassword(-1));
    }

    @Test
    void generateRandomPassword_shouldThrowException_whenLengthGreaterThan128() {
        assertThrows(AuthException.class, () -> passwordUtils.generateRandomPassword(129));
        assertThrows(AuthException.class, () -> passwordUtils.generateRandomPassword(1000));
    }

    @Test
    void encodeIdCard_shouldReturnEncryptedIdCard() {
        String idCard = "110101199001011234";
        String encryptedIdCard = "encryptedIdCard123";
        when(aesUtil.encrypt(idCard)).thenReturn(encryptedIdCard);

        String result = passwordUtils.encodeIdCard(idCard);

        assertEquals(encryptedIdCard, result);
    }

    @Test
    void encodeIdCard_shouldThrowException_whenIdCardIsNull() {
        assertThrows(AuthException.class, () -> passwordUtils.encodeIdCard(null));
    }

    @Test
    void encodeIdCard_shouldThrowException_whenIdCardIsEmpty() {
        assertThrows(AuthException.class, () -> passwordUtils.encodeIdCard(""));
    }

    @Test
    void encodeIdCard_shouldThrowException_whenIdCardIsBlank() {
        assertThrows(AuthException.class, () -> passwordUtils.encodeIdCard("   "));
    }
}