package com.klzw.common.redis.service;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redisson 锁服务
 */
@Service
public class RedissonLockService {


    private final RedissonClient redissonClient;

    public RedissonLockService(RedissonClient redissonClient)
    {
        this.redissonClient = redissonClient;
    }

    /**
     * 尝试获取锁
     * @param lockKey 锁键
     * @param waitTime 等待时间
     * @param leaseTime 持有时间
     * @param timeUnit 时间单位
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, timeUnit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 尝试获取锁（默认等待时间：0，持有时间：30秒）
     * @param lockKey 锁键
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.tryLock();
    }

    /**
     * 释放锁
     * @param lockKey 锁键
     */
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isLocked() && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    /**
     * 强制释放锁
     * @param lockKey 锁键
     */
    public void forceUnlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isLocked()) {
            lock.forceUnlock();
        }
    }

    /**
     * 检查锁是否存在
     * @param lockKey 锁键
     * @return 是否存在
     */
    public boolean isLocked(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isLocked();
    }

    /**
     * 检查当前线程是否持有锁
     * @param lockKey 锁键
     * @return 是否持有
     */
    public boolean isHeldByCurrentThread(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isHeldByCurrentThread();
    }
}
