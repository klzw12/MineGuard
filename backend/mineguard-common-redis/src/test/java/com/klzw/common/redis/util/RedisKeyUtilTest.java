package com.klzw.common.redis.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis 键工具类单元测试
 */
@DisplayName("Redis键工具类测试")
public class RedisKeyUtilTest {

    @Test
    @DisplayName("生成登录Token键")
    void testGenerateLoginTokenKey() {
        String token = "abc123";
        String key = RedisKeyUtil.generateLoginTokenKey(token);
        assertEquals("mineguard:login:token:abc123", key);
    }

    @Test
    @DisplayName("生成用户权限键")
    void testGenerateUserPermissionKey() {
        Long userId = 1001L;
        String key = RedisKeyUtil.generateUserPermissionKey(userId);
        assertEquals("mineguard:user:permission:1001", key);
    }

    @Test
    @DisplayName("生成用户角色键")
    void testGenerateUserRoleKey() {
        Long userId = 1001L;
        String key = RedisKeyUtil.generateUserRoleKey(userId);
        assertEquals("mineguard:user:role:1001", key);
    }

    @Test
    @DisplayName("生成车辆实时状态键")
    void testGenerateVehicleStatusKey() {
        Long vehicleId = 2001L;
        String key = RedisKeyUtil.generateVehicleStatusKey(vehicleId);
        assertEquals("mineguard:vehicle:status:2001", key);
    }

    @Test
    @DisplayName("生成车辆位置键")
    void testGenerateVehicleLocationKey() {
        Long vehicleId = 2001L;
        String key = RedisKeyUtil.generateVehicleLocationKey(vehicleId);
        assertEquals("mineguard:vehicle:location:2001", key);
    }

    @Test
    @DisplayName("生成行程状态键")
    void testGenerateTripStatusKey() {
        Long tripId = 3001L;
        String key = RedisKeyUtil.generateTripStatusKey(tripId);
        assertEquals("mineguard:trip:status:3001", key);
    }

    @Test
    @DisplayName("生成预警信息键")
    void testGenerateWarningInfoKey() {
        Long warningId = 4001L;
        String key = RedisKeyUtil.generateWarningInfoKey(warningId);
        assertEquals("mineguard:warning:info:4001", key);
    }

    @Test
    @DisplayName("生成分布式锁键")
    void testGenerateLockKey() {
        String lockName = "order_create";
        String key = RedisKeyUtil.generateLockKey(lockName);
        assertEquals("mineguard:lock:order_create", key);
    }

    @Test
    @DisplayName("生成限流键")
    void testGenerateRateLimitKey() {
        String limitName = "api_request";
        String key = RedisKeyUtil.generateRateLimitKey(limitName);
        assertEquals("mineguard:rate:limit:api_request", key);
    }

    @Test
    @DisplayName("生成缓存刷新锁键")
    void testGenerateCacheRefreshLockKey() {
        String cacheName = "user_cache";
        String key = RedisKeyUtil.generateCacheRefreshLockKey(cacheName);
        assertEquals("mineguard:cache:refresh:user_cache", key);
    }

    @Test
    @DisplayName("空值处理测试")
    void testNullAndEmptyValues() {
        assertNotNull(RedisKeyUtil.generateLoginTokenKey(""));
        assertNotNull(RedisKeyUtil.generateLoginTokenKey(null));
        assertNotNull(RedisKeyUtil.generateLockKey(""));
        assertNotNull(RedisKeyUtil.generateLockKey(null));
    }
}
