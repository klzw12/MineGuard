package com.klzw.common.redis.constant;

import java.util.concurrent.TimeUnit;

/**
 * Redis 常量
 * 包含键名格式、前缀和过期时间
 */
public class RedisConstants {
    
    // ==================== 键前缀 ====================
    
    /**
     * 缓存键前缀
     */
    public static final String PREFIX = "mineguard:";
    
    // ==================== 键名格式 ====================
    
    /**
     * 用户Token键
     * 格式: mineguard:user:{userId}:token:{token}
     */
    public static final String USER_TOKEN_KEY = PREFIX + "user:%d:token:%s";
    
    /**
     * 用户权限键
     * 格式: mineguard:user:{userId}:permission:{permissionId}
     */
    public static final String USER_PERMISSION_KEY = PREFIX + "user:%d:permission:%s";
    
    /**
     * 用户角色键
     * 格式: mineguard:user:{userId}:role:{roleId}
     */
    public static final String USER_ROLE_KEY = PREFIX + "user:%d:role:%s";
    
    /**
     * 车辆实时状态键
     * 格式: mineguard:vehicle:{vehicleId}:status:{statusId}
     */
    public static final String VEHICLE_STATUS_KEY = PREFIX + "vehicle:%d:status:%s";
    
    /**
     * 车辆位置键
     * 格式: mineguard:vehicle:{vehicleId}:location:{locationId}
     */
    public static final String VEHICLE_LOCATION_KEY = PREFIX + "vehicle:%d:location:%s";
    
    /**
     * 行程状态键
     * 格式: mineguard:trip:{tripId}:status:{statusId}
     */
    public static final String TRIP_STATUS_KEY = PREFIX + "trip:%d:status:%s";
    
    /**
     * 预警信息键
     * 格式: mineguard:warning:{warningId}:info:{infoId}
     */
    public static final String WARNING_INFO_KEY = PREFIX + "warning:%d:info:%s";
    
    /**
     * 分布式锁键
     * 格式: mineguard:lock:{lockName}
     */
    public static final String DISTRIBUTED_LOCK_KEY = PREFIX + "lock:%s";
    
    /**
     * 限流键
     * 格式: mineguard:rate:limit:{limitName}
     */
    public static final String RATE_LIMIT_KEY = PREFIX + "rate:limit:%s";
    
    /**
     * 缓存刷新锁键
     * 格式: mineguard:cache:refresh:{cacheName}
     */
    public static final String CACHE_REFRESH_LOCK_KEY = PREFIX + "cache:refresh:%s";
    
    // ==================== 过期时间（秒） ====================
    
    /**
     * 短缓存过期时间：10分钟
     */
    public static final long EXPIRE_SHORT = TimeUnit.MINUTES.toSeconds(10);
    
    /**
     * 默认缓存过期时间：1小时
     */
    public static final long EXPIRE_DEFAULT = TimeUnit.HOURS.toSeconds(1);
    
    /**
     * 长缓存过期时间：1天
     */
    public static final long EXPIRE_LONG = TimeUnit.DAYS.toSeconds(1);
    
    // ==================== 时间常量 ====================
    
    /**
     * 1分钟
     */
    public static final long MINUTE_1 = TimeUnit.MINUTES.toSeconds(1);
    
    /**
     * 5分钟
     */
    public static final long MINUTE_5 = TimeUnit.MINUTES.toSeconds(5);
    
    /**
     * 10分钟
     */
    public static final long MINUTE_10 = TimeUnit.MINUTES.toSeconds(10);
    
    /**
     * 30分钟
     */
    public static final long MINUTE_30 = TimeUnit.MINUTES.toSeconds(30);
    
    /**
     * 1小时
     */
    public static final long HOUR_1 = TimeUnit.HOURS.toSeconds(1);
    
    /**
     * 2小时
     */
    public static final long HOUR_2 = TimeUnit.HOURS.toSeconds(2);
    
    /**
     * 6小时
     */
    public static final long HOUR_6 = TimeUnit.HOURS.toSeconds(6);
    
    /**
     * 12小时
     */
    public static final long HOUR_12 = TimeUnit.HOURS.toSeconds(12);
    
    /**
     * 1天
     */
    public static final long DAY_1 = TimeUnit.DAYS.toSeconds(1);
    
    /**
     * 3天
     */
    public static final long DAY_3 = TimeUnit.DAYS.toSeconds(3);
    
    /**
     * 7天
     */
    public static final long DAY_7 = TimeUnit.DAYS.toSeconds(7);
    
    /**
     * 30天
     */
    public static final long DAY_30 = TimeUnit.DAYS.toSeconds(30);
}
