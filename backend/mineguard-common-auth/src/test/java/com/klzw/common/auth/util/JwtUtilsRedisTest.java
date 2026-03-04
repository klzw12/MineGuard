package com.klzw.common.auth.util;

import com.klzw.common.auth.config.JwtConfig;
import com.klzw.common.redis.service.RedisCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtUtilsRedisTest {

    @Mock
    private JwtConfig jwtConfig;

    @Mock
    private RedisCacheService redisCacheService;

    @InjectMocks
    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(jwtConfig.getSecret()).thenReturn("testSecretKeyForJwtTokenGeneration12345678901234567890123456789012345678901234567890");
        when(jwtConfig.getExpiration()).thenReturn(3600000L); // 1 hour
        when(jwtConfig.getPrefix()).thenReturn("Bearer ");
        when(jwtConfig.getHeader()).thenReturn("Authorization");
    }

    @Test
    void testAddToBlacklist() {
        // 生成一个token
        String token = jwtUtils.generateToken(1L, "test-user");
        assertNotNull(token);

        // 测试添加到黑名单
        jwtUtils.addToBlacklist(token);

        // 验证Redis操作
        verify(redisCacheService, times(1)).set(anyString(), eq("1"), anyLong(), any());
    }

    @Test
    void testIsInBlacklist() {
        String token = jwtUtils.generateToken(1L, "test-user");
        String blacklistKey = "auth:token:blacklist:" + token;

        // 测试token不在黑名单中
        when(redisCacheService.exists(blacklistKey)).thenReturn(false);
        assertFalse(jwtUtils.isInBlacklist(token));

        // 测试token在黑名单中
        when(redisCacheService.exists(blacklistKey)).thenReturn(true);
        assertTrue(jwtUtils.isInBlacklist(token));
    }

    @Test
    void testValidateTokenWithBlacklist() {
        String token = jwtUtils.generateToken(1L, "test-user");
        String blacklistKey = "auth:token:blacklist:" + token;

        // 测试token有效且不在黑名单中
        when(redisCacheService.exists(blacklistKey)).thenReturn(false);
        assertTrue(jwtUtils.validateToken(token));

        // 测试token在黑名单中
        when(redisCacheService.exists(blacklistKey)).thenReturn(true);
        assertFalse(jwtUtils.validateToken(token));
    }
}
