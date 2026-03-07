package com.klzw.common.redis.service;

import com.klzw.common.redis.constant.RedisResultCode;
import com.klzw.common.redis.exception.RedisException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存服务
 * <p>
 * 提供缓存CRUD、批量操作、Pipeline优化等功能
 * <p>
 * 异常处理：所有操作异常均使用RedisResultCode定义的错误码
 *
 * @see RedisResultCode
 */
@Slf4j
@Service
public class RedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 缓存数据
     *
     * @param key      缓存键
     * @param value    缓存值
     * @param expire   过期时间
     * @param timeUnit 时间单位
     * @throws RedisException 缓存设置失败时抛出
     */
    public void set(String key, Object value, long expire, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForValue().set(key, value, expire, timeUnit);
        } catch (Exception e) {
            log.error("Redis缓存设置失败, key: {}, error: {}", key, e.getMessage(), e);
            throw new RedisException(RedisResultCode.CACHE_SET_FAILED, "缓存设置失败: " + key, e);
        }
    }

    /**
     * 缓存数据（默认过期时间：1小时）
     *
     * @param key   缓存键
     * @param value 缓存值
     * @throws RedisException 缓存设置失败时抛出
     */
    public void set(String key, Object value) {
        set(key, value, 1, TimeUnit.HOURS);
    }

    /**
     * 批量设置缓存
     *
     * @param map      键值对映射
     * @param expire   过期时间
     * @param timeUnit 时间单位
     * @throws RedisException 缓存批量设置失败时抛出
     */
    public void setBatch(Map<String, Object> map, long expire, TimeUnit timeUnit) {
        if (map == null || map.isEmpty()) {
            return;
        }
        try {
            // 使用pipeline批量操作，减少网络请求
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                RedisSerializer<String> stringSerializer = new StringRedisSerializer();
                @SuppressWarnings("unchecked")
                RedisSerializer<Object> valueSerializer = (RedisSerializer<Object>) redisTemplate.getValueSerializer();
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    byte[] keyBytes = stringSerializer.serialize(entry.getKey());
                    byte[] valueBytes = valueSerializer.serialize(entry.getValue());
                    connection.set(keyBytes, valueBytes);
                    connection.expire(keyBytes, timeUnit.toMillis(expire));
                }
                return null;
            });
        } catch (Exception e) {
            log.error("Redis缓存批量设置失败, keys数量: {}, error: {}", map.size(), e.getMessage(), e);
            throw new RedisException(RedisResultCode.CACHE_SET_FAILED, "缓存批量设置失败", e);
        }
    }

    /**
     * 批量设置缓存（默认过期时间：1小时）
     *
     * @param map 键值对映射
     */
    public void setBatch(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return;
        }
        setBatch(map, 1, TimeUnit.HOURS);
    }

    /**
     * 获取缓存数据
     *
     * @param key 缓存键
     * @param <T> 泛型
     * @return 缓存值，不存在返回null
     * @throws RedisException 缓存获取失败时抛出
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        try {
            return (T) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis缓存获取失败, key: {}, error: {}", key, e.getMessage(), e);
            throw new RedisException(RedisResultCode.CACHE_GET_FAILED, "缓存获取失败: " + key, e);
        }
    }

    /**
     * 批量获取缓存数据
     *
     * @param keys 缓存键列表
     * @param <T>  泛型
     * @return 键值对映射
     * @throws RedisException 缓存批量获取失败时抛出
     */
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getBatch(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            List<Object> values = redisTemplate.opsForValue().multiGet(keys);
            Map<String, T> result = new HashMap<>(keys.size());
            Iterator<String> keyIterator = keys.iterator();
            Iterator<Object> valueIterator = values.iterator();
            while (keyIterator.hasNext() && valueIterator.hasNext()) {
                String key = keyIterator.next();
                Object value = valueIterator.next();
                if (value != null) {
                    result.put(key, (T) value);
                }
            }
            return result;
        } catch (Exception e) {
            log.error("Redis缓存批量获取失败, keys数量: {}, error: {}", keys.size(), e.getMessage(), e);
            throw new RedisException(RedisResultCode.CACHE_GET_FAILED, "缓存批量获取失败", e);
        }
    }

    /**
     * 删除缓存
     *
     * @param key 缓存键
     * @throws RedisException 缓存删除失败时抛出
     */
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Redis缓存删除失败, key: {}, error: {}", key, e.getMessage(), e);
            throw new RedisException(RedisResultCode.CACHE_DELETE_FAILED, "缓存删除失败: " + key, e);
        }
    }

    /**
     * 批量删除缓存
     *
     * @param keys 缓存键列表
     * @return 删除的数量
     * @throws RedisException 缓存批量删除失败时抛出
     */
    public Long deleteBatch(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0L;
        }
        try {
            return redisTemplate.delete(keys);
        } catch (Exception e) {
            log.error("Redis缓存批量删除失败, keys数量: {}, error: {}", keys.size(), e.getMessage(), e);
            throw new RedisException(RedisResultCode.CACHE_DELETE_FAILED, "缓存批量删除失败", e);
        }
    }

    /**
     * 根据模式删除缓存
     *
     * @param pattern 键模式，如 "user:*"
     * @return 删除的数量
     * @throws RedisException 缓存删除失败时抛出
     */
    public Long deleteByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys == null || keys.isEmpty()) {
                return 0L;
            }
            return redisTemplate.delete(keys);
        } catch (Exception e) {
            log.error("Redis缓存按模式删除失败, pattern: {}, error: {}", pattern, e.getMessage(), e);
            throw new RedisException(RedisResultCode.CACHE_DELETE_FAILED, "缓存按模式删除失败: " + pattern, e);
        }
    }

    /**
     * 批量设置缓存（使用pipeline优化）
     *
     * @param map 键值对映射
     * @throws RedisException 缓存批量设置失败时抛出
     */
    public void setBatchWithPipeline(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return;
        }
        try {
            // 使用pipeline批量操作，减少网络请求
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                RedisSerializer<String> stringSerializer = new StringRedisSerializer();
                @SuppressWarnings("unchecked")
                RedisSerializer<Object> valueSerializer = (RedisSerializer<Object>) redisTemplate.getValueSerializer();
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    byte[] keyBytes = stringSerializer.serialize(entry.getKey());
                    byte[] valueBytes = valueSerializer.serialize(entry.getValue());
                    connection.set(keyBytes, valueBytes);
                    connection.expire(keyBytes, TimeUnit.HOURS.toMillis(1));
                }
                return null;
            });
        } catch (Exception e) {
            log.error("Redis缓存Pipeline批量设置失败, keys数量: {}, error: {}", map.size(), e.getMessage(), e);
            throw new RedisException(RedisResultCode.CACHE_SET_FAILED, "缓存Pipeline批量设置失败", e);
        }
    }

    /**
     * 检查缓存是否存在
     *
     * @param key 缓存键
     * @return 是否存在
     * @throws RedisException 缓存检查失败时抛出
     */
    public Boolean exists(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Redis缓存存在性检查失败, key: {}, error: {}", key, e.getMessage(), e);
            throw new RedisException(RedisResultCode.CACHE_OPERATION_FAILED, "缓存存在性检查失败: " + key, e);
        }
    }

    /**
     * 批量检查缓存是否存在
     *
     * @param keys 缓存键列表
     * @return 键存在性映射
     * @throws RedisException 缓存批量检查失败时抛出
     */
    public Map<String, Boolean> existsBatch(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            Map<String, Boolean> result = new HashMap<>(keys.size());
            // 使用pipeline批量操作，减少网络请求
            List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                RedisSerializer<String> stringSerializer = new StringRedisSerializer();
                for (String key : keys) {
                    connection.exists(stringSerializer.serialize(key));
                }
                return null;
            });
            Iterator<String> keyIterator = keys.iterator();
            Iterator<Object> valueIterator = results.iterator();
            while (keyIterator.hasNext() && valueIterator.hasNext()) {
                result.put(keyIterator.next(), (Boolean) valueIterator.next());
            }
            return result;
        } catch (Exception e) {
            log.error("Redis缓存批量存在性检查失败, keys数量: {}, error: {}", keys.size(), e.getMessage(), e);
            throw new RedisException(RedisResultCode.CACHE_OPERATION_FAILED, "缓存批量存在性检查失败", e);
        }
    }

    /**
     * 设置缓存过期时间
     *
     * @param key      缓存键
     * @param expire   过期时间
     * @param timeUnit 时间单位
     * @return 是否设置成功
     * @throws RedisException 过期时间设置失败时抛出
     */
    public Boolean expire(String key, long expire, TimeUnit timeUnit) {
        try {
            return redisTemplate.expire(key, expire, timeUnit);
        } catch (Exception e) {
            log.error("Redis缓存过期时间设置失败, key: {}, error: {}", key, e.getMessage(), e);
            throw new RedisException(RedisResultCode.CACHE_EXPIRE_FAILED, "缓存过期时间设置失败: " + key, e);
        }
    }

    /**
     * 批量设置缓存过期时间
     *
     * @param keys     缓存键列表
     * @param expire   过期时间
     * @param timeUnit 时间单位
     * @return 设置成功的键数量
     * @throws RedisException 过期时间批量设置失败时抛出
     */
    public int expireBatch(Collection<String> keys, long expire, TimeUnit timeUnit) {
        if (keys == null || keys.isEmpty()) {
            return 0;
        }
        try {
            // 使用pipeline批量操作，减少网络请求
            List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                RedisSerializer<String> stringSerializer = new StringRedisSerializer();
                for (String key : keys) {
                    connection.expire(stringSerializer.serialize(key), timeUnit.toMillis(expire));
                }
                return null;
            });
            int successCount = 0;
            for (Object result : results) {
                if (Boolean.TRUE.equals(result)) {
                    successCount++;
                }
            }
            return successCount;
        } catch (Exception e) {
            log.error("Redis缓存过期时间批量设置失败, keys数量: {}, error: {}", keys.size(), e.getMessage(), e);
            throw new RedisException(RedisResultCode.CACHE_EXPIRE_FAILED, "缓存过期时间批量设置失败", e);
        }
    }

    /**
     * 获取缓存剩余过期时间
     *
     * @param key      缓存键
     * @param timeUnit 时间单位
     * @return 剩余过期时间
     * @throws RedisException 获取过期时间失败时抛出
     */
    public Long getExpire(String key, TimeUnit timeUnit) {
        try {
            return redisTemplate.getExpire(key, timeUnit);
        } catch (Exception e) {
            log.error("Redis缓存过期时间获取失败, key: {}, error: {}", key, e.getMessage(), e);
            throw new RedisException(RedisResultCode.CACHE_OPERATION_FAILED, "缓存过期时间获取失败: " + key, e);
        }
    }

    /**
     * 自增
     *
     * @param key 缓存键
     * @return 自增后的值
     * @throws RedisException 自增操作失败时抛出
     */
    public Long increment(String key) {
        try {
            return redisTemplate.opsForValue().increment(key);
        } catch (Exception e) {
            log.error("Redis缓存自增失败, key: {}, error: {}", key, e.getMessage(), e);
            throw new RedisException(RedisResultCode.CACHE_OPERATION_FAILED, "缓存自增失败: " + key, e);
        }
    }

    /**
     * 自增指定值
     *
     * @param key   缓存键
     * @param delta 增量值
     * @return 自增后的值
     * @throws RedisException 自增操作失败时抛出
     */
    public Long increment(String key, long delta) {
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("Redis缓存自增失败, key: {}, delta: {}, error: {}", key, delta, e.getMessage(), e);
            throw new RedisException(RedisResultCode.CACHE_OPERATION_FAILED, "缓存自增失败: " + key, e);
        }
    }

    /**
     * 自减
     *
     * @param key 缓存键
     * @return 自减后的值
     * @throws RedisException 自减操作失败时抛出
     */
    public Long decrement(String key) {
        try {
            return redisTemplate.opsForValue().decrement(key);
        } catch (Exception e) {
            log.error("Redis缓存自减失败, key: {}, error: {}", key, e.getMessage(), e);
            throw new RedisException(RedisResultCode.CACHE_OPERATION_FAILED, "缓存自减失败: " + key, e);
        }
    }
}
