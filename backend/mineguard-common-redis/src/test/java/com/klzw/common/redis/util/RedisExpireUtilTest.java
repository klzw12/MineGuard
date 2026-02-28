package com.klzw.common.redis.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis 过期时间工具类单元测试
 */
@DisplayName("Redis过期时间工具类测试")
public class RedisExpireUtilTest {

    @Test
    @DisplayName("分钟级过期时间常量测试")
    void testMinuteConstants() {
        assertEquals(60L, RedisExpireUtil.MINUTE_1);
        assertEquals(300L, RedisExpireUtil.MINUTE_5);
        assertEquals(600L, RedisExpireUtil.MINUTE_10);
        assertEquals(1800L, RedisExpireUtil.MINUTE_30);
    }

    @Test
    @DisplayName("小时级过期时间常量测试")
    void testHourConstants() {
        assertEquals(3600L, RedisExpireUtil.HOUR_1);
        assertEquals(7200L, RedisExpireUtil.HOUR_2);
        assertEquals(21600L, RedisExpireUtil.HOUR_6);
        assertEquals(43200L, RedisExpireUtil.HOUR_12);
    }

    @Test
    @DisplayName("天级过期时间常量测试")
    void testDayConstants() {
        assertEquals(86400L, RedisExpireUtil.DAY_1);
        assertEquals(259200L, RedisExpireUtil.DAY_3);
        assertEquals(604800L, RedisExpireUtil.DAY_7);
        assertEquals(2592000L, RedisExpireUtil.DAY_30);
    }

    @Test
    @DisplayName("验证码过期时间测试")
    void testGetCaptchaExpire() {
        long expire = RedisExpireUtil.getCaptchaExpire();
        assertEquals(RedisExpireUtil.MINUTE_5, expire);
        assertEquals(300L, expire);
    }

    @Test
    @DisplayName("Token过期时间测试")
    void testGetTokenExpire() {
        long expire = RedisExpireUtil.getTokenExpire();
        assertEquals(RedisExpireUtil.DAY_1, expire);
        assertEquals(86400L, expire);
    }

    @Test
    @DisplayName("用户信息过期时间测试")
    void testGetUserInfoExpire() {
        long expire = RedisExpireUtil.getUserInfoExpire();
        assertEquals(RedisExpireUtil.HOUR_2, expire);
        assertEquals(7200L, expire);
    }

    @Test
    @DisplayName("车辆信息过期时间测试")
    void testGetVehicleInfoExpire() {
        long expire = RedisExpireUtil.getVehicleInfoExpire();
        assertEquals(RedisExpireUtil.HOUR_1, expire);
        assertEquals(3600L, expire);
    }

    @Test
    @DisplayName("行程信息过期时间测试")
    void testGetTripInfoExpire() {
        long expire = RedisExpireUtil.getTripInfoExpire();
        assertEquals(RedisExpireUtil.HOUR_6, expire);
        assertEquals(21600L, expire);
    }

    @Test
    @DisplayName("分布式锁过期时间测试")
    void testGetLockExpire() {
        long expire = RedisExpireUtil.getLockExpire();
        assertEquals(RedisExpireUtil.MINUTE_10, expire);
        assertEquals(600L, expire);
    }

    @Test
    @DisplayName("限流过期时间测试")
    void testGetRateLimitExpire() {
        long expire = RedisExpireUtil.getRateLimitExpire();
        assertEquals(RedisExpireUtil.MINUTE_1, expire);
        assertEquals(60L, expire);
    }

    @Test
    @DisplayName("时间关系验证")
    void testTimeRelationships() {
        assertTrue(RedisExpireUtil.MINUTE_1 < RedisExpireUtil.MINUTE_5);
        assertTrue(RedisExpireUtil.MINUTE_5 < RedisExpireUtil.MINUTE_10);
        assertTrue(RedisExpireUtil.MINUTE_10 < RedisExpireUtil.MINUTE_30);
        assertTrue(RedisExpireUtil.MINUTE_30 < RedisExpireUtil.HOUR_1);
        assertTrue(RedisExpireUtil.HOUR_1 < RedisExpireUtil.HOUR_2);
        assertTrue(RedisExpireUtil.HOUR_2 < RedisExpireUtil.HOUR_6);
        assertTrue(RedisExpireUtil.HOUR_6 < RedisExpireUtil.HOUR_12);
        assertTrue(RedisExpireUtil.HOUR_12 < RedisExpireUtil.DAY_1);
        assertTrue(RedisExpireUtil.DAY_1 < RedisExpireUtil.DAY_3);
        assertTrue(RedisExpireUtil.DAY_3 < RedisExpireUtil.DAY_7);
        assertTrue(RedisExpireUtil.DAY_7 < RedisExpireUtil.DAY_30);
    }
}
