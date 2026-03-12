package com.klzw.common.redis.service;

import com.klzw.common.redis.constant.RedisResultCode;
import com.klzw.common.redis.exception.RedisException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class RedisDistributedLock {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisDistributedLock(@Autowired RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private final Lock localLock = new ReentrantLock();
    private final ThreadLocal<LockInfo> threadLocalLockInfo = new ThreadLocal<>();

    public boolean tryLock(String lockKey, long expireTime, TimeUnit timeUnit) {
        String lockValue = generateLockValue();
        return tryLock(lockKey, lockValue, expireTime, timeUnit);
    }

    public void tryLockOrThrow(String lockKey, long expireTime, TimeUnit timeUnit) {
        if (!tryLock(lockKey, expireTime, timeUnit)) {
            throw new RedisException(RedisResultCode.LOCK_ACQUIRE_FAILED, "获取锁失败: " + lockKey);
        }
    }

    public boolean tryLock(String lockKey, Object value, long expireTime, TimeUnit timeUnit) {
        localLock.lock();
        try {
            LockInfo currentLockInfo = threadLocalLockInfo.get();
            if (currentLockInfo != null && currentLockInfo.getLockKey().equals(lockKey)) {
                currentLockInfo.incrementCount();
                return true;
            }

            boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, value, expireTime, timeUnit);
            if (acquired) {
                threadLocalLockInfo.set(new LockInfo(lockKey, value.toString(), 1));
            }
            return acquired;
        } finally {
            localLock.unlock();
        }
    }

    public void tryLockOrThrow(String lockKey, Object value, long expireTime, TimeUnit timeUnit) {
        if (!tryLock(lockKey, value, expireTime, timeUnit)) {
            throw new RedisException(RedisResultCode.LOCK_ACQUIRE_FAILED, "获取锁失败: " + lockKey);
        }
    }

    public boolean unlock(String lockKey) {
        localLock.lock();
        try {
            LockInfo currentLockInfo = threadLocalLockInfo.get();
            if (currentLockInfo == null || !currentLockInfo.getLockKey().equals(lockKey)) {
                return false;
            }

            int count = currentLockInfo.decrementCount();
            if (count > 0) {
                return true;
            }

            Object currentValue = redisTemplate.opsForValue().get(lockKey);
            if (currentValue != null && currentValue.toString().equals(currentLockInfo.getLockValue())) {
                redisTemplate.delete(lockKey);
                threadLocalLockInfo.remove();
                return true;
            }
            return false;
        } finally {
            localLock.unlock();
        }
    }

    public void unlockOrThrow(String lockKey) {
        localLock.lock();
        try {
            LockInfo currentLockInfo = threadLocalLockInfo.get();
            if (currentLockInfo == null || !currentLockInfo.getLockKey().equals(lockKey)) {
                throw new RedisException(RedisResultCode.LOCK_NOT_OWNER, "不是锁的所有者: " + lockKey);
            }

            int count = currentLockInfo.decrementCount();
            if (count > 0) {
                return;
            }

            Object currentValue = redisTemplate.opsForValue().get(lockKey);
            if (currentValue == null) {
                threadLocalLockInfo.remove();
                throw new RedisException(RedisResultCode.LOCK_EXPIRED, "锁已过期: " + lockKey);
            }
            if (!currentValue.toString().equals(currentLockInfo.getLockValue())) {
                threadLocalLockInfo.remove();
                throw new RedisException(RedisResultCode.LOCK_NOT_OWNER, "不是锁的所有者: " + lockKey);
            }
            redisTemplate.delete(lockKey);
            threadLocalLockInfo.remove();
        } finally {
            localLock.unlock();
        }
    }

    public void forceUnlock(String lockKey) {
        redisTemplate.delete(lockKey);
        LockInfo currentLockInfo = threadLocalLockInfo.get();
        if (currentLockInfo != null && currentLockInfo.getLockKey().equals(lockKey)) {
            threadLocalLockInfo.remove();
        }
    }

    public boolean isLocked(String lockKey) {
        return redisTemplate.hasKey(lockKey);
    }

    public boolean isHeldByCurrentThread(String lockKey) {
        LockInfo currentLockInfo = threadLocalLockInfo.get();
        return currentLockInfo != null && currentLockInfo.getLockKey().equals(lockKey);
    }

    private String generateLockValue() {
        return UUID.randomUUID().toString();
    }

    @Getter
    private static class LockInfo {
        private final String lockKey;
        private final String lockValue;
        private int count;

        public LockInfo(String lockKey, String lockValue, int count) {
            this.lockKey = lockKey;
            this.lockValue = lockValue;
            this.count = count;
        }

        public void incrementCount() {
            count++;
        }

        public int decrementCount() {
            return --count;
        }
    }
}
