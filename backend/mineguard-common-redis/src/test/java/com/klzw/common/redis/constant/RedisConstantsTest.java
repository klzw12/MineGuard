package com.klzw.common.redis.constant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis 常量类单元测试
 */
@DisplayName("Redis常量类测试")
public class RedisConstantsTest {

    @Test
    @DisplayName("键前缀测试")
    void testPrefix() {
        assertEquals("mineguard:", RedisConstants.PREFIX);
    }

    @Test
    @DisplayName("键名格式测试")
    void testKeyFormats() {
        assertTrue(RedisConstants.USER_TOKEN_KEY.startsWith(RedisConstants.PREFIX));
        assertTrue(RedisConstants.USER_PERMISSION_KEY.startsWith(RedisConstants.PREFIX));
        assertTrue(RedisConstants.USER_ROLE_KEY.startsWith(RedisConstants.PREFIX));
        assertTrue(RedisConstants.VEHICLE_STATUS_KEY.startsWith(RedisConstants.PREFIX));
        assertTrue(RedisConstants.VEHICLE_LOCATION_KEY.startsWith(RedisConstants.PREFIX));
        assertTrue(RedisConstants.TRIP_STATUS_KEY.startsWith(RedisConstants.PREFIX));
        assertTrue(RedisConstants.WARNING_INFO_KEY.startsWith(RedisConstants.PREFIX));
        assertTrue(RedisConstants.DISTRIBUTED_LOCK_KEY.startsWith(RedisConstants.PREFIX));
        assertTrue(RedisConstants.RATE_LIMIT_KEY.startsWith(RedisConstants.PREFIX));
        assertTrue(RedisConstants.CACHE_REFRESH_LOCK_KEY.startsWith(RedisConstants.PREFIX));
    }

    @Test
    @DisplayName("过期时间常量测试")
    void testExpireConstants() {
        assertEquals(600L, RedisConstants.EXPIRE_SHORT);
        assertEquals(3600L, RedisConstants.EXPIRE_DEFAULT);
        assertEquals(86400L, RedisConstants.EXPIRE_LONG);
    }

    @Test
    @DisplayName("分钟级过期时间常量测试")
    void testMinuteConstants() {
        assertEquals(60L, RedisConstants.MINUTE_1);
        assertEquals(300L, RedisConstants.MINUTE_5);
        assertEquals(600L, RedisConstants.MINUTE_10);
        assertEquals(1800L, RedisConstants.MINUTE_30);
    }

    @Test
    @DisplayName("小时级过期时间常量测试")
    void testHourConstants() {
        assertEquals(3600L, RedisConstants.HOUR_1);
        assertEquals(7200L, RedisConstants.HOUR_2);
        assertEquals(21600L, RedisConstants.HOUR_6);
        assertEquals(43200L, RedisConstants.HOUR_12);
    }

    @Test
    @DisplayName("天级过期时间常量测试")
    void testDayConstants() {
        assertEquals(86400L, RedisConstants.DAY_1);
        assertEquals(259200L, RedisConstants.DAY_3);
        assertEquals(604800L, RedisConstants.DAY_7);
        assertEquals(2592000L, RedisConstants.DAY_30);
    }

    @Test
    @DisplayName("时间关系验证")
    void testTimeRelationships() {
        assertTrue(RedisConstants.MINUTE_1 < RedisConstants.MINUTE_5);
        assertTrue(RedisConstants.MINUTE_5 < RedisConstants.MINUTE_10);
        assertTrue(RedisConstants.MINUTE_10 < RedisConstants.MINUTE_30);
        assertTrue(RedisConstants.MINUTE_30 < RedisConstants.HOUR_1);
        assertTrue(RedisConstants.HOUR_1 < RedisConstants.HOUR_2);
        assertTrue(RedisConstants.HOUR_2 < RedisConstants.HOUR_6);
        assertTrue(RedisConstants.HOUR_6 < RedisConstants.HOUR_12);
        assertTrue(RedisConstants.HOUR_12 < RedisConstants.DAY_1);
        assertTrue(RedisConstants.DAY_1 < RedisConstants.DAY_3);
        assertTrue(RedisConstants.DAY_3 < RedisConstants.DAY_7);
        assertTrue(RedisConstants.DAY_7 < RedisConstants.DAY_30);
    }

    @Test
    @DisplayName("过期时间与标准常量关系验证")
    void testExpireRelationships() {
        assertEquals(RedisConstants.MINUTE_10, RedisConstants.EXPIRE_SHORT);
        assertEquals(RedisConstants.HOUR_1, RedisConstants.EXPIRE_DEFAULT);
        assertEquals(RedisConstants.DAY_1, RedisConstants.EXPIRE_LONG);
    }
}
