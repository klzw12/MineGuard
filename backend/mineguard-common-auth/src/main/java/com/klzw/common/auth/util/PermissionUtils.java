package com.klzw.common.auth.util;

import com.klzw.common.redis.service.RedisCacheService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.concurrent.TimeUnit;

/**
 * 权限工具类
 */
@Component
public class PermissionUtils {


    @Setter
    private static Function<Long, List<String>> permissionProvider;
    @Setter
    private static Function<Long, List<String>> roleProvider;

    private static RedisCacheService redisCacheService;

    @Autowired
    public void setRedisCacheService(RedisCacheService redisCacheService) {
        PermissionUtils.redisCacheService = redisCacheService;
    }

    /**
     * 检查用户是否有权限
     * @param userId 用户ID
     * @param permissions 权限列表
     * @param requireAll 是否需要所有权限
     * @return 是否有权限
     */
    public static boolean hasPermission(Long userId, String[] permissions, boolean requireAll) {
        List<String> userPermissions = getUserPermissions(userId);
        
        if (requireAll) {
            return new HashSet<>(userPermissions).containsAll(Arrays.asList(permissions));
        } else {
            return Arrays.stream(permissions).anyMatch(userPermissions::contains);
        }
    }

    /**
     * 检查用户是否有角色
     * @param userId 用户ID
     * @param roles 角色列表
     * @param requireAll 是否需要所有角色
     * @return 是否有角色
     */
    public static boolean hasRole(Long userId, String[] roles, boolean requireAll) {
        List<String> userRoles = getUserRoles(userId);
        
        if (requireAll) {
            return new HashSet<>(userRoles).containsAll(Arrays.asList(roles));
        } else {
            return Arrays.stream(roles).anyMatch(userRoles::contains);
        }
    }

    /**
     * 生成权限缓存key
     * @param userId 用户ID
     * @return 缓存key
     */
    private static String generatePermissionCacheKey(Long userId) {
        return "auth:permission:" + userId;
    }

    /**
     * 生成角色缓存key
     * @param userId 用户ID
     * @return 缓存key
     */
    private static String generateRoleCacheKey(Long userId) {
        return "auth:role:" + userId;
    }

    /**
     * 获取用户权限列表
     * @param userId 用户ID
     * @return 权限列表
     */
    private static List<String> getUserPermissions(Long userId) {
        // 尝试从缓存获取
        if (redisCacheService != null) {
            String cacheKey = generatePermissionCacheKey(userId);
            List<String> permissions = redisCacheService.get(cacheKey);
            if (permissions != null) {
                return permissions;
            }
        }

        // 从提供者获取
        List<String> permissions;
        if (permissionProvider != null) {
            permissions = permissionProvider.apply(userId);
        } else {
            // 默认权限，实际应用中应该由业务模块提供
            permissions = List.of("user:read");
        }

        // 缓存权限
        if (redisCacheService != null) {
            String cacheKey = generatePermissionCacheKey(userId);
            redisCacheService.set(cacheKey, permissions, 30, TimeUnit.MINUTES);
        }

        return permissions;
    }

    /**
     * 获取用户角色列表
     * @param userId 用户ID
     * @return 角色列表
     */
    private static List<String> getUserRoles(Long userId) {
        // 尝试从缓存获取
        if (redisCacheService != null) {
            String cacheKey = generateRoleCacheKey(userId);
            List<String> roles = redisCacheService.get(cacheKey);
            if (roles != null) {
                return roles;
            }
        }

        // 从提供者获取
        List<String> roles;
        if (roleProvider != null) {
            roles = roleProvider.apply(userId);
        } else {
            // 默认角色，实际应用中应该由业务模块提供
            roles = List.of("ROLE_USER");
        }

        // 缓存角色
        if (redisCacheService != null) {
            String cacheKey = generateRoleCacheKey(userId);
            redisCacheService.set(cacheKey, roles, 30, TimeUnit.MINUTES);
        }

        return roles;
    }

    /**
     * 清除用户权限缓存
     * @param userId 用户ID
     */
    public static void clearPermissionCache(Long userId) {
        if (redisCacheService != null) {
            String permissionCacheKey = generatePermissionCacheKey(userId);
            String roleCacheKey = generateRoleCacheKey(userId);
            redisCacheService.delete(permissionCacheKey);
            redisCacheService.delete(roleCacheKey);
        }
    }
}
