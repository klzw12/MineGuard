package com.klzw.common.auth.util;

import com.klzw.common.auth.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT工具类单元测试
 */
@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    private JwtProperties jwtProperties;
    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret("testSecretKeyForJwtTokenGeneration12345678901234567890");
        jwtProperties.setExpiration(3600000L); // 1小时
        jwtProperties.setEnableBlacklist(false);
        
        jwtUtils = new JwtUtils(jwtProperties, redisTemplate);
    }

    @Test
    void generateToken_shouldReturnValidToken() {
        Long userId = 1L;
        String username = "testuser";
        String role = "ROLE_USER";

        String token = jwtUtils.generateToken(userId, username, role);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void parseToken_shouldReturnClaimsSet() {
        Long userId = 1L;
        String username = "testuser";
        String role = "ROLE_USER";

        String token = jwtUtils.generateToken(userId, username, role);
        assertNotNull(token);

        // 验证token可以解析
        assertDoesNotThrow(() -> jwtUtils.parseToken(token));
    }

    @Test
    void getUserIdFromToken_shouldReturnCorrectUserId() {
        Long userId = 123L;
        String username = "testuser";

        String token = jwtUtils.generateToken(userId, username);
        Long extractedUserId = jwtUtils.getUserIdFromToken(token);

        assertEquals(userId, extractedUserId);
    }

    @Test
    void getUsernameFromToken_shouldReturnCorrectUsername() {
        Long userId = 1L;
        String username = "testuser123";

        String token = jwtUtils.generateToken(userId, username);
        String extractedUsername = jwtUtils.getUsernameFromToken(token);

        assertEquals(username, extractedUsername);
    }

    @Test
    void getRoleFromToken_shouldReturnCorrectRole() {
        Long userId = 1L;
        String username = "testuser";
        String role = "ROLE_ADMIN";

        String token = jwtUtils.generateToken(userId, username, role);
        String extractedRole = jwtUtils.getRoleFromToken(token);

        assertNotNull(extractedRole);
        assertEquals(role, extractedRole);
    }

    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        Long userId = 1L;
        String username = "testuser";

        String token = jwtUtils.generateToken(userId, username);
        boolean isValid = jwtUtils.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void getTokenFromHeader_shouldExtractToken() {
        String prefix = "Bearer ";
        String token = "test-token-123";
        String header = prefix + token;
        
        jwtProperties.setPrefix(prefix);
        String extractedToken = jwtUtils.getTokenFromHeader(header);

        assertEquals(token, extractedToken);
    }

    @Test
    void getTokenFromHeader_shouldReturnNullForInvalidHeader() {
        String invalidHeader = "Invalid header";
        String extractedToken = jwtUtils.getTokenFromHeader(invalidHeader);

        assertNull(extractedToken);
    }

    @Test
    void getTokenFromHeader_shouldReturnNullForNullHeader() {
        String extractedToken = jwtUtils.getTokenFromHeader(null);
        assertNull(extractedToken);
    }
}
