package com.klzw.common.auth.util;

import com.klzw.common.redis.service.RedisCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

class PermissionUtilsRedisTest {

    private RedisCacheService redisCacheService;

    @BeforeEach
    void setUp() {
        redisCacheService = mock(RedisCacheService.class);
        // 使用反射设置静态字段
        try {
            java.lang.reflect.Field field = PermissionUtils.class.getDeclaredField("redisCacheService");
            field.setAccessible(true);
            field.set(null, redisCacheService);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testGetUserPermissionsWithCache() {
        Long userId = 1L;
        String cacheKey = "auth:permission:" + userId;
        List<String> expectedPermissions = Arrays.asList("user:read", "user:write");

        // 测试缓存命中
        when(redisCacheService.get(cacheKey)).thenReturn(expectedPermissions);
        PermissionUtils.hasPermission(userId, new String[] {"user:read"}, true);

        // 验证Redis操作
        verify(redisCacheService, times(1)).get(cacheKey);
    }

    @Test
    void testGetUserRolesWithCache() {
        Long userId = 1L;
        String cacheKey = "auth:role:" + userId;
        List<String> expectedRoles = Arrays.asList("ROLE_USER", "ROLE_ADMIN");

        // 测试缓存命中
        when(redisCacheService.get(cacheKey)).thenReturn(expectedRoles);
        PermissionUtils.hasRole(userId, new String[] {"ROLE_USER"}, true);

        // 验证Redis操作
        verify(redisCacheService, times(1)).get(cacheKey);
    }

    @Test
    void testClearPermissionCache() {
        Long userId = 1L;
        String permissionCacheKey = "auth:permission:" + userId;
        String roleCacheKey = "auth:role:" + userId;

        // 测试清除缓存
        PermissionUtils.clearPermissionCache(userId);

        // 验证Redis操作
        verify(redisCacheService, times(1)).delete(permissionCacheKey);
        verify(redisCacheService, times(1)).delete(roleCacheKey);
    }
}
