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
    @DisplayName("生成用户Token键")
    void testGenerateUserTokenKey() {
        Long userid = 1233332223L;
        String token = "abc123";
        String key = RedisKeyUtil.generateUserTokenKey(userid,token);
        assertEquals("mineguard:user:1233332223:token:abc123", key);
    }

    @Test
    @DisplayName("生成用户权限键")
    void testGenerateUserPermissionKey() {
        Long userId = 1001L;
        String permissionId = "perm_001";
        String key = RedisKeyUtil.generateUserPermissionKey(userId, permissionId);
        assertEquals("mineguard:user:1001:permission:perm_001", key);
    }

    @Test
    @DisplayName("生成用户角色键")
    void testGenerateUserRoleKey() {
        Long userId = 1001L;
        String roleId = "role_001";
        String key = RedisKeyUtil.generateUserRoleKey(userId, roleId);
        assertEquals("mineguard:user:1001:role:role_001", key);
    }

    @Test
    @DisplayName("生成车辆实时状态键")
    void testGenerateVehicleStatusKey() {
        Long vehicleId = 2001L;
        String statusId = "status_001";
        String key = RedisKeyUtil.generateVehicleStatusKey(vehicleId, statusId);
        assertEquals("mineguard:vehicle:2001:status:status_001", key);
    }

    @Test
    @DisplayName("生成车辆位置键")
    void testGenerateVehicleLocationKey() {
        Long vehicleId = 2001L;
        String locationId = "loc_001";
        String key = RedisKeyUtil.generateVehicleLocationKey(vehicleId, locationId);
        assertEquals("mineguard:vehicle:2001:location:loc_001", key);
    }

    @Test
    @DisplayName("生成行程状态键")
    void testGenerateTripStatusKey() {
        Long tripId = 3001L;
        String statusId = "status_001";
        String key = RedisKeyUtil.generateTripStatusKey(tripId, statusId);
        assertEquals("mineguard:trip:3001:status:status_001", key);
    }

    @Test
    @DisplayName("生成预警信息键")
    void testGenerateWarningInfoKey() {
        Long warningId = 4001L;
        String infoId = "info_001";
        String key = RedisKeyUtil.generateWarningInfoKey(warningId, infoId);
        assertEquals("mineguard:warning:4001:info:info_001", key);
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
        assertNotNull(RedisKeyUtil.generateUserTokenKey(0L,""));
        assertNotNull(RedisKeyUtil.generateUserTokenKey(null,null));
        assertNotNull(RedisKeyUtil.generateLockKey(""));
        assertNotNull(RedisKeyUtil.generateLockKey(null));
    }
}
