package com.klzw.common.redis.util;

import com.klzw.common.redis.constant.RedisConstants;

/**
 * Redis 键工具类
 * 基于 RedisConstants 生成标准格式的 Redis 键
 */
public class RedisKeyUtil {

    /**
     * 生成用户Token键
     *
     * @param userId 用户ID
     * @param token  Token值
     * @return 登录用户Token键
     */
    public static String generateUserTokenKey(Long userId, String token) {
        return String.format(RedisConstants.USER_TOKEN_KEY, userId, token);
    }

    /**
     * 生成用户权限键
     *
     * @param userId       用户ID
     * @param permissionId 权限ID
     * @return 用户权限键
     */
    public static String generateUserPermissionKey(Long userId, String permissionId) {
        return String.format(RedisConstants.USER_PERMISSION_KEY, userId, permissionId);
    }

    /**
     * 生成用户角色键
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 用户角色键
     */
    public static String generateUserRoleKey(Long userId, String roleId) {
        return String.format(RedisConstants.USER_ROLE_KEY, userId, roleId);
    }

    /**
     * 生成车辆实时状态键
     *
     * @param vehicleId 车辆ID
     * @param statusId  状态ID
     * @return 车辆实时状态键
     */
    public static String generateVehicleStatusKey(Long vehicleId, String statusId) {
        return String.format(RedisConstants.VEHICLE_STATUS_KEY, vehicleId, statusId);
    }

    /**
     * 生成车辆位置键
     *
     * @param vehicleId  车辆ID
     * @param locationId 位置ID
     * @return 车辆位置键
     */
    public static String generateVehicleLocationKey(Long vehicleId, String locationId) {
        return String.format(RedisConstants.VEHICLE_LOCATION_KEY, vehicleId, locationId);
    }

    /**
     * 生成行程状态键
     *
     * @param tripId   行程ID
     * @param statusId 状态ID
     * @return 行程状态键
     */
    public static String generateTripStatusKey(Long tripId, String statusId) {
        return String.format(RedisConstants.TRIP_STATUS_KEY, tripId, statusId);
    }

    /**
     * 生成预警信息键
     *
     * @param warningId 预警ID
     * @param infoId    信息ID
     * @return 预警信息键
     */
    public static String generateWarningInfoKey(Long warningId, String infoId) {
        return String.format(RedisConstants.WARNING_INFO_KEY, warningId, infoId);
    }

    /**
     * 生成分布式锁键
     *
     * @param lockName 锁名称
     * @return 分布式锁键
     */
    public static String generateLockKey(String lockName) {
        return String.format(RedisConstants.DISTRIBUTED_LOCK_KEY, lockName);
    }

    /**
     * 生成限流键
     *
     * @param limitName 限流名称
     * @return 限流键
     */
    public static String generateRateLimitKey(String limitName) {
        return String.format(RedisConstants.RATE_LIMIT_KEY, limitName);
    }

    /**
     * 生成缓存刷新锁键
     *
     * @param cacheName 缓存名称
     * @return 缓存刷新锁键
     */
    public static String generateCacheRefreshLockKey(String cacheName) {
        return String.format(RedisConstants.CACHE_REFRESH_LOCK_KEY, cacheName);
    }
}
