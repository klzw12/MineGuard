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

@Slf4j
@Service
public class RedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void set(String key, Object value, long expire, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForValue().set(key, value, expire, timeUnit);
        } catch (Exception e) {
            log.error("Redis缓存设置失败: key={}", key);
            throw new RedisException(RedisResultCode.CACHE_SET_FAILED, "缓存设置失败: " + key, e);
        }
    }

    public void set(String key, Object value) {
        set(key, value, 1, TimeUnit.HOURS);
    }

    public void setBatch(Map<String, Object> map, long expire, TimeUnit timeUnit) {
        if (map == null || map.isEmpty()) {
            return;
        }
        try {
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
            log.error("Redis缓存批量设置失败: keys数量={}", map.size());
            throw new RedisException(RedisResultCode.CACHE_SET_FAILED, "缓存批量设置失败", e);
        }
    }

    public void setBatch(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return;
        }
        setBatch(map, 1, TimeUnit.HOURS);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        try {
            return (T) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis缓存获取失败: key={}", key);
            throw new RedisException(RedisResultCode.CACHE_GET_FAILED, "缓存获取失败: " + key, e);
        }
    }

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
            log.error("Redis缓存批量获取失败: keys数量={}", keys.size());
            throw new RedisException(RedisResultCode.CACHE_GET_FAILED, "缓存批量获取失败", e);
        }
    }

    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Redis缓存删除失败: key={}", key);
            throw new RedisException(RedisResultCode.CACHE_DELETE_FAILED, "缓存删除失败: " + key, e);
        }
    }

    public Long deleteBatch(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0L;
        }
        try {
            return redisTemplate.delete(keys);
        } catch (Exception e) {
            log.error("Redis缓存批量删除失败: keys数量={}", keys.size());
            throw new RedisException(RedisResultCode.CACHE_DELETE_FAILED, "缓存批量删除失败", e);
        }
    }

    public Long deleteByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys == null || keys.isEmpty()) {
                return 0L;
            }
            return redisTemplate.delete(keys);
        } catch (Exception e) {
            log.error("Redis缓存按模式删除失败: pattern={}", pattern);
            throw new RedisException(RedisResultCode.CACHE_DELETE_FAILED, "缓存按模式删除失败: " + pattern, e);
        }
    }

    public void setBatchWithPipeline(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return;
        }
        try {
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
            log.error("Redis缓存Pipeline批量设置失败: keys数量={}", map.size());
            throw new RedisException(RedisResultCode.CACHE_SET_FAILED, "缓存Pipeline批量设置失败", e);
        }
    }

    @SuppressWarnings("deprecation")
    public Boolean exists(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Redis缓存存在性检查失败: key={}", key);
            throw new RedisException(RedisResultCode.CACHE_OPERATION_FAILED, "缓存存在性检查失败: " + key, e);
        }
    }

    public Map<String, Boolean> existsBatch(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            Map<String, Boolean> result = new HashMap<>(keys.size());
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
            log.error("Redis缓存批量存在性检查失败: keys数量={}", keys.size());
            throw new RedisException(RedisResultCode.CACHE_OPERATION_FAILED, "缓存批量存在性检查失败", e);
        }
    }

    public Boolean expire(String key, long expire, TimeUnit timeUnit) {
        try {
            return redisTemplate.expire(key, expire, timeUnit);
        } catch (Exception e) {
            log.error("Redis缓存过期时间设置失败: key={}", key);
            throw new RedisException(RedisResultCode.CACHE_EXPIRE_FAILED, "缓存过期时间设置失败: " + key, e);
        }
    }

    public int expireBatch(Collection<String> keys, long expire, TimeUnit timeUnit) {
        if (keys == null || keys.isEmpty()) {
            return 0;
        }
        try {
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
            log.error("Redis缓存过期时间批量设置失败: keys数量={}", keys.size());
            throw new RedisException(RedisResultCode.CACHE_EXPIRE_FAILED, "缓存过期时间批量设置失败", e);
        }
    }

    public Long getExpire(String key, TimeUnit timeUnit) {
        try {
            return redisTemplate.getExpire(key, timeUnit);
        } catch (Exception e) {
            log.error("Redis缓存过期时间获取失败: key={}", key);
            throw new RedisException(RedisResultCode.CACHE_OPERATION_FAILED, "缓存过期时间获取失败: " + key, e);
        }
    }

    public Long increment(String key) {
        try {
            return redisTemplate.opsForValue().increment(key);
        } catch (Exception e) {
            log.error("Redis缓存自增失败: key={}", key);
            throw new RedisException(RedisResultCode.CACHE_OPERATION_FAILED, "缓存自增失败: " + key, e);
        }
    }

    public Long increment(String key, long delta) {
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("Redis缓存自增失败: key={}, delta={}", key, delta);
            throw new RedisException(RedisResultCode.CACHE_OPERATION_FAILED, "缓存自增失败: " + key, e);
        }
    }

    public Long decrement(String key) {
        try {
            return redisTemplate.opsForValue().decrement(key);
        } catch (Exception e) {
            log.error("Redis缓存自减失败: key={}", key);
            throw new RedisException(RedisResultCode.CACHE_OPERATION_FAILED, "缓存自减失败: " + key, e);
        }
    }
}
