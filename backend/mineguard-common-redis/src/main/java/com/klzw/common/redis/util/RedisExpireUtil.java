package com.klzw.common.redis.util;

import java.util.concurrent.TimeUnit;

/**
 * Redis 过期时间工具类
 */
public class RedisExpireUtil {

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

    /**
     * 获取验证码过期时间
     * @return 过期时间（秒）
     */
    public static long getCaptchaExpire() {
        return MINUTE_5;
    }

    /**
     * 获取Token过期时间
     * @return 过期时间（秒）
     */
    public static long getTokenExpire() {
        return DAY_1;
    }

    /**
     * 获取用户信息过期时间
     * @return 过期时间（秒）
     */
    public static long getUserInfoExpire() {
        return HOUR_2;
    }

    /**
     * 获取车辆信息过期时间
     * @return 过期时间（秒）
     */
    public static long getVehicleInfoExpire() {
        return HOUR_1;
    }

    /**
     * 获取行程信息过期时间
     * @return 过期时间（秒）
     */
    public static long getTripInfoExpire() {
        return HOUR_6;
    }

    /**
     * 获取分布式锁过期时间
     * @return 过期时间（秒）
     */
    public static long getLockExpire() {
        return MINUTE_10;
    }

    /**
     * 获取限流过期时间
     * @return 过期时间（秒）
     */
    public static long getRateLimitExpire() {
        return MINUTE_1;
    }
}
