package com.klzw.common.auth.util;

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

    private PasswordUtils passwordUtils;

    @BeforeEach
    void setUp() {
        passwordUtils = new PasswordUtils(passwordEncoder);
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
}