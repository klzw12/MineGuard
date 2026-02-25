package com.klzw.common.core.constant;

public class RedisKeyConstants {
    // 登录用户token
    public static final String LOGIN_TOKEN_KEY = "mineguard:login:token:%s";
    
    // 用户权限
    public static final String USER_PERMISSION_KEY = "mineguard:user:permission:%s";
    
    // 用户角色
    public static final String USER_ROLE_KEY = "mineguard:user:role:%s";
    
    // 车辆实时状态
    public static final String VEHICLE_STATUS_KEY = "mineguard:vehicle:status:%s";
    
    // 车辆位置
    public static final String VEHICLE_LOCATION_KEY = "mineguard:vehicle:location:%s";
    
    // 行程状态
    public static final String TRIP_STATUS_KEY = "mineguard:trip:status:%s";
    
    // 预警信息
    public static final String WARNING_INFO_KEY = "mineguard:warning:info:%s";
    
    // 分布式锁
    public static final String DISTRIBUTED_LOCK_KEY = "mineguard:lock:%s";
    
    // 限流键
    public static final String RATE_LIMIT_KEY = "mineguard:rate:limit:%s";
    
    // 缓存刷新锁
    public static final String CACHE_REFRESH_LOCK_KEY = "mineguard:cache:refresh:%s";
}
