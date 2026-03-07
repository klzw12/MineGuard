package com.klzw.common.auth.util;

import com.klzw.common.redis.service.RedisCacheService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT 工具类 Redis 集成测试
 */
@SpringBootTest(classes = com.klzw.common.auth.TestAuthApplication.class)
@ActiveProfiles("test")
@Tag("integration")
class JwtUtilsRedisIntegrationTest {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisCacheService redisCacheService;

    private String testToken;

    @BeforeEach
    void setUp() {
        // 生成测试token
        testToken = jwtUtils.generateToken(1L, "test-user");
    }

    @AfterEach
    void tearDown() {
        // 清理Redis中的测试数据
        String blacklistKey = "auth:token:blacklist:" + testToken;
        redisCacheService.delete(blacklistKey);
    }

    @Test
    void addToBlacklist_shouldAddTokenToRedis() {
        // 验证token不在黑名单中
        assertFalse(jwtUtils.isInBlacklist(testToken));

        // 添加到黑名单
        jwtUtils.addToBlacklist(testToken);

        // 验证token在黑名单中
        assertTrue(jwtUtils.isInBlacklist(testToken));
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenInBlacklist() {
        // 验证token有效
        assertTrue(jwtUtils.validateToken(testToken));

        // 添加到黑名单
        jwtUtils.addToBlacklist(testToken);

        // 验证token无效
        assertFalse(jwtUtils.validateToken(testToken));
    }
}
