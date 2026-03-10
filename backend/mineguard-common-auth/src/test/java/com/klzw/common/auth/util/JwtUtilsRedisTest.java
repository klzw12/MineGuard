package com.klzw.common.auth.util;

import com.klzw.common.auth.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtUtilsRedisTest {

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private JwtUtils jwtUtils;

    private static final String TEST_SECRET = "testSecretKeyForJwtTokenGeneration12345678901234567890123456789012345678901234567890";
    private static final String TEST_BLACKLIST_PREFIX = "jwt:blacklist:";

    @BeforeEach
    void setUp() {
        when(jwtProperties.getSecret()).thenReturn(TEST_SECRET);
        when(jwtProperties.getExpiration()).thenReturn(3600000L);
        when(jwtProperties.getPrefix()).thenReturn("Bearer ");
        when(jwtProperties.getHeader()).thenReturn("Authorization");
        when(jwtProperties.getEnableBlacklist()).thenReturn(true);
        when(jwtProperties.getBlacklistPrefix()).thenReturn(TEST_BLACKLIST_PREFIX);
        when(jwtProperties.getBlacklistExpire()).thenReturn(86400L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        jwtUtils = new JwtUtils(jwtProperties, redisTemplate);
    }

    @Test
    void testAddToBlacklist() {
        String token = jwtUtils.generateToken(1L, "test-user");
        assertNotNull(token);

        jwtUtils.addToBlacklist(token);

        verify(valueOperations, times(1)).set(anyString(), eq("1"), anyLong(), any(TimeUnit.class));
    }

    @Test
    void testIsInBlacklist() {
        String token = jwtUtils.generateToken(1L, "test-user");
        String blacklistKey = TEST_BLACKLIST_PREFIX + token;

        when(redisTemplate.hasKey(blacklistKey)).thenReturn(false);
        assertFalse(jwtUtils.isInBlacklist(token));

        when(redisTemplate.hasKey(blacklistKey)).thenReturn(true);
        assertTrue(jwtUtils.isInBlacklist(token));
    }

    @Test
    void testValidateTokenWithBlacklist() {
        String token = jwtUtils.generateToken(1L, "test-user");
        String blacklistKey = TEST_BLACKLIST_PREFIX + token;

        when(redisTemplate.hasKey(blacklistKey)).thenReturn(false);
        assertTrue(jwtUtils.validateToken(token));

        when(redisTemplate.hasKey(blacklistKey)).thenReturn(true);
        assertFalse(jwtUtils.validateToken(token));
    }
}
