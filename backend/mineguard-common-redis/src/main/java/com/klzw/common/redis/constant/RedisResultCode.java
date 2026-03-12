package com.klzw.common.redis.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Redis模块错误码枚举
 * <p>
 * 错误码范围：900-999
 * <p>
 * 错误码说明：
 * - 900: Redis通用错误
 * - 901-909: 缓存操作相关错误
 * - 910-919: 分布式锁相关错误
 * - 920-929: 限流相关错误
 * - 930-939: 连接相关错误
 * - 940-949: 序列化相关错误
 */
@Getter
@AllArgsConstructor
public enum RedisResultCode {

    /**
     * Redis通用错误
     */
    REDIS_ERROR(900, "Redis操作失败"),

    /**
     * 缓存操作相关错误
     */
    CACHE_MISS(901, "缓存未命中"),
    CACHE_OPERATION_FAILED(902, "缓存操作失败"),
    CACHE_GET_FAILED(903, "缓存获取失败"),
    CACHE_SET_FAILED(904, "缓存设置失败"),
    CACHE_DELETE_FAILED(905, "缓存删除失败"),
    CACHE_EXPIRE_FAILED(906, "缓存过期设置失败"),
    CACHE_CLEAR_FAILED(907, "缓存清理失败"),

    /**
     * 分布式锁相关错误
     */
    LOCK_ACQUIRE_FAILED(910, "获取锁失败"),
    LOCK_RELEASE_FAILED(911, "释放锁失败"),
    LOCK_EXPIRED(912, "锁已过期"),
    LOCK_NOT_OWNER(913, "不是锁的所有者"),
    LOCK_TIMEOUT(914, "获取锁超时"),

    /**
     * 限流相关错误
     */
    RATE_LIMIT_EXCEEDED(920, "超过限流阈值"),
    RATE_LIMIT_CONFIG_ERROR(921, "限流配置错误"),
    RATE_LIMIT_RESET_FAILED(922, "限流重置失败"),

    /**
     * 连接相关错误
     */
    CONNECTION_ERROR(930, "连接错误"),
    CONNECTION_TIMEOUT(931, "连接超时"),
    CONNECTION_CLOSED(932, "连接已关闭"),
    CONNECTION_POOL_EXHAUSTED(933, "连接池耗尽"),

    /**
     * 序列化相关错误
     */
    SERIALIZATION_ERROR(940, "序列化错误"),
    DESERIALIZATION_ERROR(941, "反序列化错误"),
    SERIALIZER_NOT_FOUND(942, "序列化器未找到"),

    /**
     * 其他Redis错误
     */
    COMMAND_EXECUTION_ERROR(950, "命令执行错误"),
    KEY_NOT_FOUND(951, "键不存在"),
    INVALID_KEY(952, "无效的键"),
    INVALID_VALUE(953, "无效的值"),
    OPERATION_NOT_SUPPORTED(954, "不支持的操作"),
    PARAMETER_ERROR(955, "参数错误");

    private final int code;
    private final String message;
}