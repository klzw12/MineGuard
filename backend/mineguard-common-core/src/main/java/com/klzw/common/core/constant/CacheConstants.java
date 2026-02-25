package com.klzw.common.core.constant;

public class CacheConstants {
    // 缓存前缀
    public static final String CACHE_PREFIX = "mineguard:";
    
    // 用户缓存前缀
    public static final String USER_CACHE_PREFIX = CACHE_PREFIX + "user:";
    
    // 车辆缓存前缀
    public static final String VEHICLE_CACHE_PREFIX = CACHE_PREFIX + "vehicle:";
    
    // 行程缓存前缀
    public static final String TRIP_CACHE_PREFIX = CACHE_PREFIX + "trip:";
    
    // 预警缓存前缀
    public static final String WARNING_CACHE_PREFIX = CACHE_PREFIX + "warning:";
    
    // 统计缓存前缀
    public static final String STATISTICS_CACHE_PREFIX = CACHE_PREFIX + "statistics:";
    
    // 字典缓存前缀
    public static final String DICT_CACHE_PREFIX = CACHE_PREFIX + "dict:";
    
    // 权限缓存前缀
    public static final String PERMISSION_CACHE_PREFIX = CACHE_PREFIX + "permission:";
    
    // 角色缓存前缀
    public static final String ROLE_CACHE_PREFIX = CACHE_PREFIX + "role:";
    
    // 缓存过期时间（秒） 1h
    public static final int CACHE_EXPIRE_TIME = 3600;
    
    // 长缓存过期时间（秒）1d
    public static final int CACHE_LONG_EXPIRE_TIME = 86400;
    
    // 短缓存过期时间（秒）10m
    public static final int CACHE_SHORT_EXPIRE_TIME = 600;
}
