package com.klzw.common.auth.util;

import com.klzw.common.redis.service.RedisCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 权限工具类单元测试
 */

class PermissionUtilsTest {

    private RedisCacheService redisCacheService;

    @BeforeEach
    void setUp() {
        // 清除之前的提供者设置
        PermissionUtils.setPermissionProvider(null);
        PermissionUtils.setRoleProvider(null);
        
        // 初始化RedisCacheService mock
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
    void hasPermission_shouldReturnTrue_whenUserHasAllRequiredPermissions() {
        Long userId = 1L;
        String[] requiredPermissions = {"user:read", "user:write"};

        // 设置权限提供者
        PermissionUtils.setPermissionProvider(uid -> 
            Arrays.asList("user:read", "user:write", "user:delete")
        );

        // 模拟Redis缓存未命中
        when(redisCacheService.get(anyString())).thenReturn(null);

        boolean result = PermissionUtils.hasPermission(userId, requiredPermissions, true);

        assertTrue(result);
        // 验证缓存被设置
        verify(redisCacheService, times(1)).set(anyString(), anyList(), anyLong(), any());
    }

    @Test
    void hasPermission_shouldReturnFalse_whenUserMissingSomePermissions() {
        Long userId = 1L;
        String[] requiredPermissions = {"user:read", "user:write", "user:delete"};

        // 设置权限提供者
        PermissionUtils.setPermissionProvider(uid -> 
            Arrays.asList("user:read", "user:write")
        );

        // 模拟Redis缓存未命中
        when(redisCacheService.get(anyString())).thenReturn(null);

        boolean result = PermissionUtils.hasPermission(userId, requiredPermissions, true);

        assertFalse(result);
        // 验证缓存被设置
        verify(redisCacheService, times(1)).set(anyString(), anyList(), anyLong(), any());
    }

    @Test
    void hasPermission_shouldReturnTrue_whenUserHasAnyRequiredPermission() {
        Long userId = 1L;
        String[] requiredPermissions = {"user:read", "admin:read"};

        // 设置权限提供者
        PermissionUtils.setPermissionProvider(uid -> 
            Arrays.asList("user:read", "user:write")
        );

        // 模拟Redis缓存未命中
        when(redisCacheService.get(anyString())).thenReturn(null);

        boolean result = PermissionUtils.hasPermission(userId, requiredPermissions, false);

        assertTrue(result);
        // 验证缓存被设置
        verify(redisCacheService, times(1)).set(anyString(), anyList(), anyLong(), any());
    }

    @Test
    void hasPermission_shouldReturnFalse_whenUserHasNoRequiredPermissions() {
        Long userId = 1L;
        String[] requiredPermissions = {"admin:read", "admin:write"};

        // 设置权限提供者
        PermissionUtils.setPermissionProvider(uid -> 
            Arrays.asList("user:read", "user:write")
        );

        // 模拟Redis缓存未命中
        when(redisCacheService.get(anyString())).thenReturn(null);

        boolean result = PermissionUtils.hasPermission(userId, requiredPermissions, false);

        assertFalse(result);
        // 验证缓存被设置
        verify(redisCacheService, times(1)).set(anyString(), anyList(), anyLong(), any());
    }

    @Test
    void hasPermission_shouldUseDefaultPermissions_whenNoProviderSet() {
        Long userId = 1L;
        String[] requiredPermissions = {"user:read"};

        // 模拟Redis缓存未命中
        when(redisCacheService.get(anyString())).thenReturn(null);

        boolean result = PermissionUtils.hasPermission(userId, requiredPermissions, true);

        assertTrue(result);
        // 验证缓存被设置
        verify(redisCacheService, times(1)).set(anyString(), anyList(), anyLong(), any());
    }

    @Test
    void hasPermission_shouldUseCachedPermissions_whenCacheHit() {
        Long userId = 1L;
        String[] requiredPermissions = {"user:read", "user:write"};
        List<String> cachedPermissions = Arrays.asList("user:read", "user:write", "user:delete");

        // 模拟Redis缓存命中
        when(redisCacheService.get("auth:permission:" + userId)).thenReturn(cachedPermissions);

        boolean result = PermissionUtils.hasPermission(userId, requiredPermissions, true);

        assertTrue(result);
        // 验证缓存未被重新设置
        verify(redisCacheService, never()).set(anyString(), anyList(), anyLong(), any());
    }

    @Test
    void hasRole_shouldReturnTrue_whenUserHasAllRequiredRoles() {
        Long userId = 1L;
        String[] requiredRoles = {"ROLE_USER", "ROLE_ADMIN"};

        // 设置角色提供者
        PermissionUtils.setRoleProvider(uid -> 
            Arrays.asList("ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER_ADMIN")
        );

        // 模拟Redis缓存未命中
        when(redisCacheService.get(anyString())).thenReturn(null);

        boolean result = PermissionUtils.hasRole(userId, requiredRoles, true);

        assertTrue(result);
        // 验证缓存被设置
        verify(redisCacheService, times(1)).set(anyString(), anyList(), anyLong(), any());
    }

    @Test
    void hasRole_shouldReturnFalse_whenUserMissingSomeRoles() {
        Long userId = 1L;
        String[] requiredRoles = {"ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER_ADMIN"};

        // 设置角色提供者
        PermissionUtils.setRoleProvider(uid -> 
            Arrays.asList("ROLE_USER", "ROLE_ADMIN")
        );

        // 模拟Redis缓存未命中
        when(redisCacheService.get(anyString())).thenReturn(null);

        boolean result = PermissionUtils.hasRole(userId, requiredRoles, true);

        assertFalse(result);
        // 验证缓存被设置
        verify(redisCacheService, times(1)).set(anyString(), anyList(), anyLong(), any());
    }

    @Test
    void hasRole_shouldReturnTrue_whenUserHasAnyRequiredRole() {
        Long userId = 1L;
        String[] requiredRoles = {"ROLE_ADMIN", "ROLE_SUPER_ADMIN"};

        // 设置角色提供者
        PermissionUtils.setRoleProvider(uid -> 
            Arrays.asList("ROLE_USER", "ROLE_ADMIN")
        );

        // 模拟Redis缓存未命中
        when(redisCacheService.get(anyString())).thenReturn(null);

        boolean result = PermissionUtils.hasRole(userId, requiredRoles, false);

        assertTrue(result);
        // 验证缓存被设置
        verify(redisCacheService, times(1)).set(anyString(), anyList(), anyLong(), any());
    }

    @Test
    void hasRole_shouldReturnFalse_whenUserHasNoRequiredRoles() {
        Long userId = 1L;
        String[] requiredRoles = {"ROLE_ADMIN", "ROLE_SUPER_ADMIN"};

        // 设置角色提供者
        PermissionUtils.setRoleProvider(uid -> 
            Arrays.asList("ROLE_USER")
        );

        // 模拟Redis缓存未命中
        when(redisCacheService.get(anyString())).thenReturn(null);

        boolean result = PermissionUtils.hasRole(userId, requiredRoles, false);

        assertFalse(result);
        // 验证缓存被设置
        verify(redisCacheService, times(1)).set(anyString(), anyList(), anyLong(), any());
    }

    @Test
    void hasRole_shouldUseDefaultRoles_whenNoProviderSet() {
        Long userId = 1L;
        String[] requiredRoles = {"ROLE_USER"};

        // 模拟Redis缓存未命中
        when(redisCacheService.get(anyString())).thenReturn(null);

        boolean result = PermissionUtils.hasRole(userId, requiredRoles, true);

        assertTrue(result);
        // 验证缓存被设置
        verify(redisCacheService, times(1)).set(anyString(), anyList(), anyLong(), any());
    }

    @Test
    void hasRole_shouldUseCachedRoles_whenCacheHit() {
        Long userId = 1L;
        String[] requiredRoles = {"ROLE_USER", "ROLE_ADMIN"};
        List<String> cachedRoles = Arrays.asList("ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER_ADMIN");

        // 模拟Redis缓存命中
        when(redisCacheService.get("auth:role:" + userId)).thenReturn(cachedRoles);

        boolean result = PermissionUtils.hasRole(userId, requiredRoles, true);

        assertTrue(result);
        // 验证缓存未被重新设置
        verify(redisCacheService, never()).set(anyString(), anyList(), anyLong(), any());
    }

    @Test
    void setPermissionProvider_shouldUpdatePermissionProvider() {
        Function<Long, List<String>> customProvider = uid -> Arrays.asList("custom:permission");
        
        PermissionUtils.setPermissionProvider(customProvider);
        
        // 模拟Redis缓存未命中
        when(redisCacheService.get(anyString())).thenReturn(null);
        
        // 验证新的提供者被使用
        Long userId = 1L;
        String[] requiredPermissions = {"custom:permission"};
        boolean result = PermissionUtils.hasPermission(userId, requiredPermissions, true);
        
        assertTrue(result);
    }

    @Test
    void setRoleProvider_shouldUpdateRoleProvider() {
        Function<Long, List<String>> customProvider = uid -> Arrays.asList("ROLE_CUSTOM");
        
        PermissionUtils.setRoleProvider(customProvider);
        
        // 模拟Redis缓存未命中
        when(redisCacheService.get(anyString())).thenReturn(null);
        
        // 验证新的提供者被使用
        Long userId = 1L;
        String[] requiredRoles = {"ROLE_CUSTOM"};
        boolean result = PermissionUtils.hasRole(userId, requiredRoles, true);
        
        assertTrue(result);
    }

    @Test
    void clearPermissionCache_shouldDeleteCacheFromRedis() {
        Long userId = 1L;
        String permissionCacheKey = "auth:permission:" + userId;
        String roleCacheKey = "auth:role:" + userId;

        PermissionUtils.clearPermissionCache(userId);

        // 验证缓存被删除
        verify(redisCacheService, times(1)).delete(permissionCacheKey);
        verify(redisCacheService, times(1)).delete(roleCacheKey);
    }
}