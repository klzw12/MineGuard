package com.klzw.common.redis.constant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RedisResultCode 单元测试
 * <p>
 * 验证错误码枚举的正确性和完整性
 */
@DisplayName("Redis错误码枚举单元测试")
class RedisResultCodeTest {

    @Test
    @DisplayName("测试错误码数量")
    void testEnumSize() {
        // 验证所有错误码都被定义
        RedisResultCode[] codes = RedisResultCode.values();
        assertEquals(28, codes.length, "RedisResultCode应该包含28个错误码");
    }

    @ParameterizedTest
    @EnumSource(RedisResultCode.class)
    @DisplayName("测试所有错误码都有有效的code值")
    void testAllCodesHaveValidCode(RedisResultCode resultCode) {
        // 验证错误码在有效范围内 (900-999)
        int code = resultCode.getCode();
        assertTrue(code >= 900 && code <= 999,
                "错误码 " + resultCode.name() + " 的值 " + code + " 不在有效范围(900-999)内");
    }

    @ParameterizedTest
    @EnumSource(RedisResultCode.class)
    @DisplayName("测试所有错误码都有非空message")
    void testAllCodesHaveNonEmptyMessage(RedisResultCode resultCode) {
        // 验证所有错误码都有非空的错误消息
        String message = resultCode.getMessage();
        assertNotNull(message, "错误码 " + resultCode.name() + " 的消息不能为null");
        assertFalse(message.trim().isEmpty(), "错误码 " + resultCode.name() + " 的消息不能为空");
    }

    @ParameterizedTest
    @CsvSource({
            "REDIS_ERROR, 900, Redis操作失败",
            "CACHE_MISS, 901, 缓存未命中",
            "CACHE_OPERATION_FAILED, 902, 缓存操作失败",
            "CACHE_GET_FAILED, 903, 缓存获取失败",
            "CACHE_SET_FAILED, 904, 缓存设置失败",
            "CACHE_DELETE_FAILED, 905, 缓存删除失败",
            "CACHE_EXPIRE_FAILED, 906, 缓存过期设置失败",
            "CACHE_CLEAR_FAILED, 907, 缓存清理失败",
            "LOCK_ACQUIRE_FAILED, 910, 获取锁失败",
            "LOCK_RELEASE_FAILED, 911, 释放锁失败",
            "LOCK_EXPIRED, 912, 锁已过期",
            "LOCK_NOT_OWNER, 913, 不是锁的所有者",
            "LOCK_TIMEOUT, 914, 获取锁超时",
            "RATE_LIMIT_EXCEEDED, 920, 超过限流阈值",
            "RATE_LIMIT_CONFIG_ERROR, 921, 限流配置错误",
            "RATE_LIMIT_RESET_FAILED, 922, 限流重置失败",
            "CONNECTION_ERROR, 930, 连接错误",
            "CONNECTION_TIMEOUT, 931, 连接超时",
            "CONNECTION_CLOSED, 932, 连接已关闭",
            "CONNECTION_POOL_EXHAUSTED, 933, 连接池耗尽",
            "SERIALIZATION_ERROR, 940, 序列化错误",
            "DESERIALIZATION_ERROR, 941, 反序列化错误",
            "SERIALIZER_NOT_FOUND, 942, 序列化器未找到",
            "COMMAND_EXECUTION_ERROR, 950, 命令执行错误",
            "KEY_NOT_FOUND, 951, 键不存在",
            "INVALID_KEY, 952, 无效的键",
            "INVALID_VALUE, 953, 无效的值",
            "OPERATION_NOT_SUPPORTED, 954, 不支持的操作"
    })
    @DisplayName("测试特定错误码的值")
    void testSpecificErrorCode(String enumName, int expectedCode, String expectedMessage) {
        RedisResultCode resultCode = RedisResultCode.valueOf(enumName);
        assertEquals(expectedCode, resultCode.getCode(),
                enumName + " 的错误码值应该为 " + expectedCode);
        assertEquals(expectedMessage, resultCode.getMessage(),
                enumName + " 的错误消息应该为 '" + expectedMessage + "'");
    }

    @Test
    @DisplayName("测试错误码范围分组 - 通用错误")
    void testErrorCodeRange_General() {
        assertEquals(900, RedisResultCode.REDIS_ERROR.getCode());
    }

    @Test
    @DisplayName("测试错误码范围分组 - 缓存错误(901-909)")
    void testErrorCodeRange_Cache() {
        // 验证缓存相关错误码在901-909范围内
        assertTrue(RedisResultCode.CACHE_MISS.getCode() >= 901 && RedisResultCode.CACHE_MISS.getCode() <= 909);
        assertTrue(RedisResultCode.CACHE_OPERATION_FAILED.getCode() >= 901 && RedisResultCode.CACHE_OPERATION_FAILED.getCode() <= 909);
        assertTrue(RedisResultCode.CACHE_GET_FAILED.getCode() >= 901 && RedisResultCode.CACHE_GET_FAILED.getCode() <= 909);
        assertTrue(RedisResultCode.CACHE_SET_FAILED.getCode() >= 901 && RedisResultCode.CACHE_SET_FAILED.getCode() <= 909);
        assertTrue(RedisResultCode.CACHE_DELETE_FAILED.getCode() >= 901 && RedisResultCode.CACHE_DELETE_FAILED.getCode() <= 909);
        assertTrue(RedisResultCode.CACHE_EXPIRE_FAILED.getCode() >= 901 && RedisResultCode.CACHE_EXPIRE_FAILED.getCode() <= 909);
        assertTrue(RedisResultCode.CACHE_CLEAR_FAILED.getCode() >= 901 && RedisResultCode.CACHE_CLEAR_FAILED.getCode() <= 909);
    }

    @Test
    @DisplayName("测试错误码范围分组 - 分布式锁错误(910-919)")
    void testErrorCodeRange_Lock() {
        // 验证分布式锁相关错误码在910-919范围内
        assertTrue(RedisResultCode.LOCK_ACQUIRE_FAILED.getCode() >= 910 && RedisResultCode.LOCK_ACQUIRE_FAILED.getCode() <= 919);
        assertTrue(RedisResultCode.LOCK_RELEASE_FAILED.getCode() >= 910 && RedisResultCode.LOCK_RELEASE_FAILED.getCode() <= 919);
        assertTrue(RedisResultCode.LOCK_EXPIRED.getCode() >= 910 && RedisResultCode.LOCK_EXPIRED.getCode() <= 919);
        assertTrue(RedisResultCode.LOCK_NOT_OWNER.getCode() >= 910 && RedisResultCode.LOCK_NOT_OWNER.getCode() <= 919);
        assertTrue(RedisResultCode.LOCK_TIMEOUT.getCode() >= 910 && RedisResultCode.LOCK_TIMEOUT.getCode() <= 919);
    }

    @Test
    @DisplayName("测试错误码范围分组 - 限流错误(920-929)")
    void testErrorCodeRange_RateLimit() {
        // 验证限流相关错误码在920-929范围内
        assertTrue(RedisResultCode.RATE_LIMIT_EXCEEDED.getCode() >= 920 && RedisResultCode.RATE_LIMIT_EXCEEDED.getCode() <= 929);
        assertTrue(RedisResultCode.RATE_LIMIT_CONFIG_ERROR.getCode() >= 920 && RedisResultCode.RATE_LIMIT_CONFIG_ERROR.getCode() <= 929);
        assertTrue(RedisResultCode.RATE_LIMIT_RESET_FAILED.getCode() >= 920 && RedisResultCode.RATE_LIMIT_RESET_FAILED.getCode() <= 929);
    }

    @Test
    @DisplayName("测试错误码范围分组 - 连接错误(930-939)")
    void testErrorCodeRange_Connection() {
        // 验证连接相关错误码在930-939范围内
        assertTrue(RedisResultCode.CONNECTION_ERROR.getCode() >= 930 && RedisResultCode.CONNECTION_ERROR.getCode() <= 939);
        assertTrue(RedisResultCode.CONNECTION_TIMEOUT.getCode() >= 930 && RedisResultCode.CONNECTION_TIMEOUT.getCode() <= 939);
        assertTrue(RedisResultCode.CONNECTION_CLOSED.getCode() >= 930 && RedisResultCode.CONNECTION_CLOSED.getCode() <= 939);
        assertTrue(RedisResultCode.CONNECTION_POOL_EXHAUSTED.getCode() >= 930 && RedisResultCode.CONNECTION_POOL_EXHAUSTED.getCode() <= 939);
    }

    @Test
    @DisplayName("测试错误码范围分组 - 序列化错误(940-949)")
    void testErrorCodeRange_Serialization() {
        // 验证序列化相关错误码在940-949范围内
        assertTrue(RedisResultCode.SERIALIZATION_ERROR.getCode() >= 940 && RedisResultCode.SERIALIZATION_ERROR.getCode() <= 949);
        assertTrue(RedisResultCode.DESERIALIZATION_ERROR.getCode() >= 940 && RedisResultCode.DESERIALIZATION_ERROR.getCode() <= 949);
        assertTrue(RedisResultCode.SERIALIZER_NOT_FOUND.getCode() >= 940 && RedisResultCode.SERIALIZER_NOT_FOUND.getCode() <= 949);
    }

    @Test
    @DisplayName("测试错误码范围分组 - 其他错误(950-999)")
    void testErrorCodeRange_Other() {
        // 验证其他错误码在950-999范围内
        assertTrue(RedisResultCode.COMMAND_EXECUTION_ERROR.getCode() >= 950 && RedisResultCode.COMMAND_EXECUTION_ERROR.getCode() <= 999);
        assertTrue(RedisResultCode.KEY_NOT_FOUND.getCode() >= 950 && RedisResultCode.KEY_NOT_FOUND.getCode() <= 999);
        assertTrue(RedisResultCode.INVALID_KEY.getCode() >= 950 && RedisResultCode.INVALID_KEY.getCode() <= 999);
        assertTrue(RedisResultCode.INVALID_VALUE.getCode() >= 950 && RedisResultCode.INVALID_VALUE.getCode() <= 999);
        assertTrue(RedisResultCode.OPERATION_NOT_SUPPORTED.getCode() >= 950 && RedisResultCode.OPERATION_NOT_SUPPORTED.getCode() <= 999);
    }

    @Test
    @DisplayName("测试错误码唯一性")
    void testErrorCodeUniqueness() {
        // 验证所有错误码的值都是唯一的
        java.util.Set<Integer> codeSet = new java.util.HashSet<>();
        for (RedisResultCode resultCode : RedisResultCode.values()) {
            assertTrue(codeSet.add(resultCode.getCode()),
                    "错误码 " + resultCode.name() + " 的值 " + resultCode.getCode() + " 重复");
        }
    }

    @Test
    @DisplayName("测试valueOf方法")
    void testValueOf() {
        // 测试根据名称获取枚举值
        assertEquals(RedisResultCode.REDIS_ERROR, RedisResultCode.valueOf("REDIS_ERROR"));
        assertEquals(RedisResultCode.CACHE_GET_FAILED, RedisResultCode.valueOf("CACHE_GET_FAILED"));
        assertEquals(RedisResultCode.LOCK_ACQUIRE_FAILED, RedisResultCode.valueOf("LOCK_ACQUIRE_FAILED"));
        assertEquals(RedisResultCode.RATE_LIMIT_EXCEEDED, RedisResultCode.valueOf("RATE_LIMIT_EXCEEDED"));
    }

    @Test
    @DisplayName("测试values方法")
    void testValues() {
        // 测试获取所有枚举值
        RedisResultCode[] values = RedisResultCode.values();
        assertNotNull(values);
        assertTrue(values.length > 0);
        // 验证包含特定的错误码
        assertTrue(java.util.Arrays.asList(values).contains(RedisResultCode.REDIS_ERROR));
        assertTrue(java.util.Arrays.asList(values).contains(RedisResultCode.CACHE_MISS));
        assertTrue(java.util.Arrays.asList(values).contains(RedisResultCode.LOCK_ACQUIRE_FAILED));
    }
}
