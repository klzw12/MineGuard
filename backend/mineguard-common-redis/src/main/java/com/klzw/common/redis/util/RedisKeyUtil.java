package com.klzw.common.redis.util;

import com.klzw.common.core.constant.RedisKeyConstants;

/**
 * Redis 键工具类
 * 基于 RedisKeyConstants 生成标准格式的 Redis 键
 */
public class RedisKeyUtil {

    /**
     * 生成登录用户Token键
     * @param token Token值
     * @return 登录用户Token键
     */
    public static String generateLoginTokenKey(String token) {
        return String.format(RedisKeyConstants.LOGIN_TOKEN_KEY, token);
    }

    /**
     * 生成用户权限键
     * @param userId 用户ID
     * @return 用户权限键
     */
    public static String generateUserPermissionKey(Long userId) {
        return String.format(RedisKeyConstants.USER_PERMISSION_KEY, userId);
    }

    /**
     * 生成用户角色键
     * @param userId 用户ID
     * @return 用户角色键
     */
    public static String generateUserRoleKey(Long userId) {
        return String.format(RedisKeyConstants.USER_ROLE_KEY, userId);
    }

    /**
     * 生成车辆实时状态键
     * @param vehicleId 车辆ID
     * @return 车辆实时状态键
     */
    public static String generateVehicleStatusKey(Long vehicleId) {
        return String.format(RedisKeyConstants.VEHICLE_STATUS_KEY, vehicleId);
    }

    /**
     * 生成车辆位置键
     * @param vehicleId 车辆ID
     * @return 车辆位置键
     */
    public static String generateVehicleLocationKey(Long vehicleId) {
        return String.format(RedisKeyConstants.VEHICLE_LOCATION_KEY, vehicleId);
    }

    /**
     * 生成行程状态键
     * @param tripId 行程ID
     * @return 行程状态键
     */
    public static String generateTripStatusKey(Long tripId) {
        return String.format(RedisKeyConstants.TRIP_STATUS_KEY, tripId);
    }

    /**
     * 生成预警信息键
     * @param warningId 预警ID
     * @return 预警信息键
     */
    public static String generateWarningInfoKey(Long warningId) {
        return String.format(RedisKeyConstants.WARNING_INFO_KEY, warningId);
    }

    /**
     * 生成分布式锁键
     * @param lockName 锁名称
     * @return 分布式锁键
     */
    public static String generateLockKey(String lockName) {
        return String.format(RedisKeyConstants.DISTRIBUTED_LOCK_KEY, lockName);
    }

    /**
     * 生成限流键
     * @param limitName 限流名称
     * @return 限流键
     */
    public static String generateRateLimitKey(String limitName) {
        return String.format(RedisKeyConstants.RATE_LIMIT_KEY, limitName);
    }

    /**
     * 生成缓存刷新锁键
     * @param cacheName 缓存名称
     * @return 缓存刷新锁键
     */
    public static String generateCacheRefreshLockKey(String cacheName) {
        return String.format(RedisKeyConstants.CACHE_REFRESH_LOCK_KEY, cacheName);
    }
}
