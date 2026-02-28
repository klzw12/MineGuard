package com.klzw.common.redis.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Redis 分布式锁服务
 */
@Service
public class RedisDistributedLock {


    private final RedisTemplate<String, Object> redisTemplate;

    public RedisDistributedLock(@Autowired RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 本地重入锁，用于实现可重入性
    private final Lock localLock = new ReentrantLock();
    // 线程本地存储，存储当前线程持有的锁信息
    private final ThreadLocal<LockInfo> threadLocalLockInfo = new ThreadLocal<>();

    /**
     * 尝试获取锁
     *
     * @param lockKey    锁键
     * @param expireTime 过期时间
     * @param timeUnit   时间单位
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, long expireTime, TimeUnit timeUnit) {
        // 生成唯一的锁所有者ID
        String lockValue = generateLockValue();
        return tryLock(lockKey, lockValue, expireTime, timeUnit);
    }

    /**
     * 尝试获取锁
     *
     * @param lockKey    锁键
     * @param value      锁值
     * @param expireTime 过期时间
     * @param timeUnit   时间单位
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, Object value, long expireTime, TimeUnit timeUnit) {
        localLock.lock();
        try {
            // 检查是否已经持有该锁
            LockInfo currentLockInfo = threadLocalLockInfo.get();
            if (currentLockInfo != null && currentLockInfo.getLockKey().equals(lockKey)) {
                // 已持有锁，增加重入计数
                currentLockInfo.incrementCount();
                return true;
            }

            // 尝试获取锁
            boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, value, expireTime, timeUnit);
            if (acquired) {
                // 记录锁信息
                threadLocalLockInfo.set(new LockInfo(lockKey, value.toString(), 1));
            }
            return acquired;
        } finally {
            localLock.unlock();
        }
    }

    /**
     * 释放锁
     *
     * @param lockKey 锁键
     * @return 是否释放成功
     */
    public boolean unlock(String lockKey) {
        localLock.lock();
        try {
            LockInfo currentLockInfo = threadLocalLockInfo.get();
            if (currentLockInfo == null || !currentLockInfo.getLockKey().equals(lockKey)) {
                // 没有持有该锁，无法释放
                return false;
            }

            // 减少重入计数
            int count = currentLockInfo.decrementCount();
            if (count > 0) {
                // 还有重入，不释放锁
                return true;
            }

            // 检查锁是否存在且所有者匹配
            Object currentValue = redisTemplate.opsForValue().get(lockKey);
            if (currentValue != null && currentValue.toString().equals(currentLockInfo.getLockValue())) {
                // 所有者匹配，释放锁
                redisTemplate.delete(lockKey);
                threadLocalLockInfo.remove();
                return true;
            }
            return false;
        } finally {
            localLock.unlock();
        }
    }

    /**
     * 强制释放锁（不检查所有者）
     *
     * @param lockKey 锁键
     */
    public void forceUnlock(String lockKey) {
        redisTemplate.delete(lockKey);
        LockInfo currentLockInfo = threadLocalLockInfo.get();
        if (currentLockInfo != null && currentLockInfo.getLockKey().equals(lockKey)) {
            threadLocalLockInfo.remove();
        }
    }

    /**
     * 检查锁是否存在
     *
     * @param lockKey 锁键
     * @return 是否存在
     */
    public boolean isLocked(String lockKey) {
        return redisTemplate.hasKey(lockKey);
    }

    /**
     * 检查锁是否被当前线程持有
     *
     * @param lockKey 锁键
     * @return 是否被当前线程持有
     */
    public boolean isHeldByCurrentThread(String lockKey) {
        LockInfo currentLockInfo = threadLocalLockInfo.get();
        return currentLockInfo != null && currentLockInfo.getLockKey().equals(lockKey);
    }

    /**
     * 生成唯一的锁值
     *
     * @return 锁值
     */
    private String generateLockValue() {
        return UUID.randomUUID().toString();
    }

    /**
     * 锁信息类
     */
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

