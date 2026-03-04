package com.klzw.common.auth.util;

import com.klzw.common.auth.config.JwtConfig;
import com.klzw.common.auth.constant.AuthResultCode;
import com.klzw.common.auth.domain.JwtToken;
import com.klzw.common.auth.exception.AuthException;
import com.klzw.common.redis.service.RedisCacheService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JWT 工具类单元测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtUtilsTest {

    @Mock
    private JwtConfig jwtConfig;

    @Mock
    private RedisCacheService redisCacheService;

    private JwtUtils jwtUtils;

    private static final String TEST_SECRET = "testSecretKeyForJwtTokenGeneration12345678901234567890123456789012345678901234567890";
    private static final Long TEST_EXPIRATION = 86400000L;
    private static final String TEST_PREFIX = "Bearer ";
    private static final String TEST_HEADER = "Authorization";

    @BeforeEach
    void setUp() {
        when(jwtConfig.getSecret()).thenReturn(TEST_SECRET);
        when(jwtConfig.getExpiration()).thenReturn(TEST_EXPIRATION);
        when(jwtConfig.getPrefix()).thenReturn(TEST_PREFIX);
        when(jwtConfig.getHeader()).thenReturn(TEST_HEADER);
        jwtUtils = new JwtUtils(jwtConfig, redisCacheService);
    }

    @Test
    void generateToken_shouldGenerateValidToken() {
        Long userId = 1L;
        String username = "testUser";

        String token = jwtUtils.generateToken(userId, username);

        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void parseToken_shouldParseValidToken() {
        Long userId = 1L;
        String username = "testUser";
        String token = jwtUtils.generateToken(userId, username);

        Claims claims = jwtUtils.parseToken(token);

        assertNotNull(claims);
        assertEquals(username, claims.getSubject());
        assertEquals(userId, claims.get("userId", Long.class));
    }

    @Test
    void parseToken_shouldThrowException_whenTokenExpired() {
        when(jwtConfig.getExpiration()).thenReturn(-1000L);
        Long userId = 1L;
        String username = "testUser";
        String token = jwtUtils.generateToken(userId, username);

        AuthException exception = assertThrows(AuthException.class, () -> {
            jwtUtils.parseToken(token);
        });

        assertEquals(AuthResultCode.TOKEN_EXPIRED.getCode(), exception.getCode());
    }

    @Test
    void parseToken_shouldThrowException_whenTokenInvalid() {
        String invalidToken = "invalid.token.here";

        AuthException exception = assertThrows(AuthException.class, () -> {
            jwtUtils.parseToken(invalidToken);
        });

        assertEquals(AuthResultCode.TOKEN_INVALID.getCode(), exception.getCode());
    }

    @Test
    void getUserIdFromToken_shouldReturnUserId() {
        Long userId = 1L;
        String username = "testUser";
        String token = jwtUtils.generateToken(userId, username);

        Long result = jwtUtils.getUserIdFromToken(token);

        assertEquals(userId, result);
    }

    @Test
    void getUsernameFromToken_shouldReturnUsername() {
        Long userId = 1L;
        String username = "testUser";
        String token = jwtUtils.generateToken(userId, username);

        String result = jwtUtils.getUsernameFromToken(token);

        assertEquals(username, result);
    }

    @Test
    void validateToken_shouldReturnTrue_whenTokenValid() {
        Long userId = 1L;
        String username = "testUser";
        String token = jwtUtils.generateToken(userId, username);

        when(redisCacheService.exists(anyString())).thenReturn(false);
        boolean result = jwtUtils.validateToken(token);

        assertTrue(result);
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenInvalid() {
        String invalidToken = "invalid.token.here";

        boolean result = jwtUtils.validateToken(invalidToken);

        assertFalse(result);
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenInBlacklist() {
        Long userId = 1L;
        String username = "testUser";
        String token = jwtUtils.generateToken(userId, username);

        when(redisCacheService.exists(anyString())).thenReturn(true);
        boolean result = jwtUtils.validateToken(token);

        assertFalse(result);
    }

    @Test
    void getTokenFromHeader_shouldExtractToken() {
        String token = "testToken123";
        String authHeader = TEST_PREFIX + token;

        String result = jwtUtils.getTokenFromHeader(authHeader);

        assertEquals(token, result);
    }

    @Test
    void getTokenFromHeader_shouldReturnNull_whenHeaderNull() {
        String result = jwtUtils.getTokenFromHeader(null);

        assertNull(result);
    }

    @Test
    void getTokenFromHeader_shouldReturnNull_whenHeaderNotStartWithPrefix() {
        String authHeader = "Basic testToken123";

        String result = jwtUtils.getTokenFromHeader(authHeader);

        assertNull(result);
    }

    @Test
    void getTokenInfo_shouldReturnTokenInfo() {
        Long userId = 1L;
        String username = "testUser";
        String token = jwtUtils.generateToken(userId, username);

        JwtToken tokenInfo = jwtUtils.getTokenInfo(token);

        assertNotNull(tokenInfo);
        assertEquals(token, tokenInfo.getToken());
        assertEquals(userId, tokenInfo.getUserId());
        assertEquals(username, tokenInfo.getUsername());
        assertNotNull(tokenInfo.getIssuedAt());
        assertNotNull(tokenInfo.getExpiration());
    }

    @Test
    void addToBlacklist_shouldAddTokenToRedis() {
        Long userId = 1L;
        String username = "testUser";
        String token = jwtUtils.generateToken(userId, username);

        jwtUtils.addToBlacklist(token);

        verify(redisCacheService, times(1)).set(anyString(), eq("1"), anyLong(), any());
    }

    @Test
    void isInBlacklist_shouldReturnTrue_whenTokenInRedis() {
        String token = "testToken123";
        String blacklistKey = "auth:token:blacklist:" + token;

        when(redisCacheService.exists(blacklistKey)).thenReturn(true);
        boolean result = jwtUtils.isInBlacklist(token);

        assertTrue(result);
    }

    @Test
    void isInBlacklist_shouldReturnFalse_whenTokenNotInRedis() {
        String token = "testToken123";
        String blacklistKey = "auth:token:blacklist:" + token;

        when(redisCacheService.exists(blacklistKey)).thenReturn(false);
        boolean result = jwtUtils.isInBlacklist(token);

        assertFalse(result);
    }
}