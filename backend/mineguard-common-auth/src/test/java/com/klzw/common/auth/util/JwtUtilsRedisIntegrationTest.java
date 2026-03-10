package com.klzw.common.auth.util;

import com.klzw.common.core.config.DotenvInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT 工具类 Redis 集成测试
 */
@SpringBootTest(classes = com.klzw.common.auth.TestAuthApplication.class)
@ContextConfiguration(initializers = DotenvInitializer.class)
@ActiveProfiles("test")
@Tag("integration")
class JwtUtilsRedisIntegrationTest {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private String testToken;

    @BeforeEach
    void setUp() {
        testToken = jwtUtils.generateToken(1L, "test-user");
    }

    @AfterEach
    void tearDown() {
        if (testToken != null) {
            String blacklistKey = jwtUtils.getJwtProperties().getBlacklistPrefix() + testToken;
            redisTemplate.delete(blacklistKey);
        }
    }

    @Test
    void addToBlacklist_shouldAddTokenToRedis() {
        assertFalse(jwtUtils.isInBlacklist(testToken));

        jwtUtils.addToBlacklist(testToken);

        assertTrue(jwtUtils.isInBlacklist(testToken));
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenInBlacklist() {
        assertTrue(jwtUtils.validateToken(testToken));

        jwtUtils.addToBlacklist(testToken);

        assertFalse(jwtUtils.validateToken(testToken));
    }
}
