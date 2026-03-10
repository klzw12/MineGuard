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

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 权限工具类 Redis 集成测试
 */
@SpringBootTest(classes = com.klzw.common.auth.TestAuthApplication.class)
@ContextConfiguration(initializers = DotenvInitializer.class)
@ActiveProfiles("test")
@Tag("integration")
class PermissionUtilsRedisIntegrationTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private Long testUserId = 1L;

    @BeforeEach
    void setUp() {
        PermissionUtils.setPermissionProvider(null);
        PermissionUtils.setRoleProvider(null);
        
        clearCache();
    }

    @AfterEach
    void tearDown() {
        clearCache();
    }

    private void clearCache() {
        String permissionCacheKey = "auth:permission:" + testUserId;
        String roleCacheKey = "auth:role:" + testUserId;
        redisTemplate.delete(permissionCacheKey);
        redisTemplate.delete(roleCacheKey);
    }

    @Test
    void hasPermission_shouldCachePermissions() {
        PermissionUtils.setPermissionProvider(uid -> 
            Arrays.asList("user:read", "user:write", "user:delete")
        );

        String[] requiredPermissions = {"user:read", "user:write"};

        boolean result1 = PermissionUtils.hasPermission(testUserId, requiredPermissions, true);
        assertTrue(result1);

        String permissionCacheKey = "auth:permission:" + testUserId;
        assertNotNull(redisTemplate.opsForValue().get(permissionCacheKey));

        boolean result2 = PermissionUtils.hasPermission(testUserId, requiredPermissions, true);
        assertTrue(result2);
    }

    @Test
    void hasRole_shouldCacheRoles() {
        PermissionUtils.setRoleProvider(uid -> 
            Arrays.asList("ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER_ADMIN")
        );

        String[] requiredRoles = {"ROLE_USER", "ROLE_ADMIN"};

        boolean result1 = PermissionUtils.hasRole(testUserId, requiredRoles, true);
        assertTrue(result1);

        String roleCacheKey = "auth:role:" + testUserId;
        assertNotNull(redisTemplate.opsForValue().get(roleCacheKey));

        boolean result2 = PermissionUtils.hasRole(testUserId, requiredRoles, true);
        assertTrue(result2);
    }

    @Test
    void clearPermissionCache_shouldDeleteCache() {
        PermissionUtils.setPermissionProvider(uid -> 
            Arrays.asList("user:read", "user:write")
        );
        PermissionUtils.setRoleProvider(uid -> 
            Arrays.asList("ROLE_USER", "ROLE_ADMIN")
        );

        PermissionUtils.hasPermission(testUserId, new String[]{"user:read"}, true);
        PermissionUtils.hasRole(testUserId, new String[]{"ROLE_USER"}, true);

        String permissionCacheKey = "auth:permission:" + testUserId;
        String roleCacheKey = "auth:role:" + testUserId;
        assertNotNull(redisTemplate.opsForValue().get(permissionCacheKey));
        assertNotNull(redisTemplate.opsForValue().get(roleCacheKey));

        PermissionUtils.clearPermissionCache(testUserId);

        assertNull(redisTemplate.opsForValue().get(permissionCacheKey));
        assertNull(redisTemplate.opsForValue().get(roleCacheKey));
    }
}
