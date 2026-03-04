package com.klzw.common.auth.util;

import com.klzw.common.redis.service.RedisCacheService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 权限工具类 Redis 集成测试
 */
@SpringBootTest(classes = com.klzw.common.auth.TestAuthApplication.class)
@ActiveProfiles("test")
@Tag("integration")
class PermissionUtilsRedisIntegrationTest {

    @Autowired
    private RedisCacheService redisCacheService;

    private Long testUserId = 1L;

    @BeforeEach
    void setUp() {
        // 清除之前的提供者设置
        PermissionUtils.setPermissionProvider(null);
        PermissionUtils.setRoleProvider(null);
        
        // 清理Redis中的测试数据
        clearCache();
    }

    @AfterEach
    void tearDown() {
        // 清理Redis中的测试数据
        clearCache();
    }

    private void clearCache() {
        String permissionCacheKey = "auth:permission:" + testUserId;
        String roleCacheKey = "auth:role:" + testUserId;
        redisCacheService.delete(permissionCacheKey);
        redisCacheService.delete(roleCacheKey);
    }

    @Test
    void hasPermission_shouldCachePermissions() {
        // 设置权限提供者
        PermissionUtils.setPermissionProvider(uid -> 
            Arrays.asList("user:read", "user:write", "user:delete")
        );

        String[] requiredPermissions = {"user:read", "user:write"};

        // 第一次调用，缓存未命中
        boolean result1 = PermissionUtils.hasPermission(testUserId, requiredPermissions, true);
        assertTrue(result1);

        // 验证缓存被设置
        String permissionCacheKey = "auth:permission:" + testUserId;
        assertNotNull(redisCacheService.get(permissionCacheKey));

        // 第二次调用，缓存命中
        boolean result2 = PermissionUtils.hasPermission(testUserId, requiredPermissions, true);
        assertTrue(result2);
    }

    @Test
    void hasRole_shouldCacheRoles() {
        // 设置角色提供者
        PermissionUtils.setRoleProvider(uid -> 
            Arrays.asList("ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER_ADMIN")
        );

        String[] requiredRoles = {"ROLE_USER", "ROLE_ADMIN"};

        // 第一次调用，缓存未命中
        boolean result1 = PermissionUtils.hasRole(testUserId, requiredRoles, true);
        assertTrue(result1);

        // 验证缓存被设置
        String roleCacheKey = "auth:role:" + testUserId;
        assertNotNull(redisCacheService.get(roleCacheKey));

        // 第二次调用，缓存命中
        boolean result2 = PermissionUtils.hasRole(testUserId, requiredRoles, true);
        assertTrue(result2);
    }

    @Test
    void clearPermissionCache_shouldDeleteCache() {
        // 设置权限和角色提供者
        PermissionUtils.setPermissionProvider(uid -> 
            Arrays.asList("user:read", "user:write")
        );
        PermissionUtils.setRoleProvider(uid -> 
            Arrays.asList("ROLE_USER", "ROLE_ADMIN")
        );

        // 调用方法，设置缓存
        PermissionUtils.hasPermission(testUserId, new String[]{"user:read"}, true);
        PermissionUtils.hasRole(testUserId, new String[]{"ROLE_USER"}, true);

        // 验证缓存存在
        String permissionCacheKey = "auth:permission:" + testUserId;
        String roleCacheKey = "auth:role:" + testUserId;
        assertNotNull(redisCacheService.get(permissionCacheKey));
        assertNotNull(redisCacheService.get(roleCacheKey));

        // 清除缓存
        PermissionUtils.clearPermissionCache(testUserId);

        // 验证缓存被删除
        assertNull(redisCacheService.get(permissionCacheKey));
        assertNull(redisCacheService.get(roleCacheKey));
    }
}
