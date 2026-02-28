package com.klzw.common.redis.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存服务
 */
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
     */
    public void set(String key, Object value, long expire, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, expire, timeUnit);
    }

    /**
     * 缓存数据（默认过期时间：1小时）
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value, 1, TimeUnit.HOURS);
    }

    /**
     * 批量设置缓存
     *
     * @param map      键值对映射
     * @param expire   过期时间
     * @param timeUnit 时间单位
     */
    public void setBatch(Map<String, Object> map, long expire, TimeUnit timeUnit) {
        if (map == null || map.isEmpty()) {
            return;
        }
        redisTemplate.opsForValue().multiSet(map);
        // 批量设置过期时间
        for (String key : map.keySet()) {
            redisTemplate.expire(key, expire, timeUnit);
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
     * @return 缓存值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    /**
     * 批量获取缓存数据
     *
     * @param keys 缓存键列表
     * @param <T>  泛型
     * @return 键值对映射
     */
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getBatch(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyMap();
        }
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
    }

    /**
     * 删除缓存
     *
     * @param key 缓存键
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 批量删除缓存
     *
     * @param keys 缓存键列表
     * @return 删除的数量
     */
    public Long deleteBatch(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0L;
        }
        return redisTemplate.delete(keys);
    }

    /**
     * 检查缓存是否存在
     *
     * @param key 缓存键
     * @return 是否存在
     */
    public Boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 批量检查缓存是否存在
     *
     * @param keys 缓存键列表
     * @return 键存在性映射
     */
    public Map<String, Boolean> existsBatch(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Boolean> result = new HashMap<>(keys.size());
        for (String key : keys) {
            result.put(key, redisTemplate.hasKey(key));
        }
        return result;
    }

    /**
     * 设置缓存过期时间
     *
     * @param key      缓存键
     * @param expire   过期时间
     * @param timeUnit 时间单位
     * @return 是否设置成功
     */
    public Boolean expire(String key, long expire, TimeUnit timeUnit) {
        return redisTemplate.expire(key, expire, timeUnit);
    }

    /**
     * 批量设置缓存过期时间
     *
     * @param keys     缓存键列表
     * @param expire   过期时间
     * @param timeUnit 时间单位
     * @return 设置成功的键数量
     */
    public int expireBatch(Collection<String> keys, long expire, TimeUnit timeUnit) {
        if (keys == null || keys.isEmpty()) {
            return 0;
        }
        int successCount = 0;
        for (String key : keys) {
            if (redisTemplate.expire(key, expire, timeUnit)) {
                successCount++;
            }
        }
        return successCount;
    }

    /**
     * 获取缓存剩余过期时间
     *
     * @param key      缓存键
     * @param timeUnit 时间单位
     * @return 剩余过期时间
     */
    public Long getExpire(String key, TimeUnit timeUnit) {
        return redisTemplate.getExpire(key, timeUnit);
    }

    /**
     * 自增
     *
     * @param key 缓存键
     * @return 自增后的值
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * 自增指定值
     *
     * @param key   缓存键
     * @param delta 增量值
     * @return 自增后的值
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 自减
     *
     * @param key 缓存键
     * @return 自减后的值
     */
    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    /**
     * 自减指定值
     *
     * @param key   缓存键
     * @param delta 减量值
     * @return 自减后的值
     */
    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    /**
     * 哈希设置
     *
     * @param key     缓存键
     * @param hashKey 哈希键
     * @param value   缓存值
     */
    public void hSet(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    /**
     * 哈希获取
     *
     * @param key     缓存键
     * @param hashKey 哈希键
     * @param <T>     泛型
     * @return 缓存值
     */
    @SuppressWarnings("unchecked")
    public <T> T hGet(String key, String hashKey) {
        return (T) redisTemplate.opsForHash().get(key, hashKey);
    }

    /**
     * 哈希批量设置
     *
     * @param key 缓存键
     * @param map 哈希键值对映射
     */
    public void hSetBatch(String key, Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return;
        }
        redisTemplate.opsForHash().putAll(key, map);
    }

    /**
     * 哈希批量获取
     *
     * @param key      缓存键
     * @param hashKeys 哈希键列表
     * @param <T>      泛型
     * @return 哈希键值对映射
     */
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> hGetBatch(String key, Collection<String> hashKeys) {
        if (hashKeys == null || hashKeys.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Object> values = redisTemplate.opsForHash().multiGet(key, new ArrayList<>(hashKeys));
        Map<String, T> result = new HashMap<>(hashKeys.size());
        Iterator<String> keyIterator = hashKeys.iterator();
        Iterator<Object> valueIterator = values.iterator();
        while (keyIterator.hasNext() && valueIterator.hasNext()) {
            String hashKey = keyIterator.next();
            Object value = valueIterator.next();
            if (value != null) {
                result.put(hashKey, (T) value);
            }
        }
        return result;
    }

    /**
     * 哈希删除
     *
     * @param key      缓存键
     * @param hashKeys 哈希键列表
     * @return 删除的数量
     */
    public Long hDelete(String key, Object... hashKeys) {
        return redisTemplate.opsForHash().delete(key, hashKeys);
    }
}

