package com.klzw.common.redis.service;

import com.klzw.common.redis.constant.RedisResultCode;
import com.klzw.common.redis.exception.RedisException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedissonLockService {

    private final RedissonClient redissonClient;

    public RedissonLockService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, timeUnit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public void tryLockOrThrow(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) {
        if (!tryLock(lockKey, waitTime, leaseTime, timeUnit)) {
            throw new RedisException(RedisResultCode.LOCK_ACQUIRE_FAILED, "获取锁失败: " + lockKey);
        }
    }

    public boolean tryLock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.tryLock();
    }

    public void tryLockOrThrow(String lockKey) {
        if (!tryLock(lockKey)) {
            throw new RedisException(RedisResultCode.LOCK_ACQUIRE_FAILED, "获取锁失败: " + lockKey);
        }
    }

    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isLocked() && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    public void unlockOrThrow(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        if (!lock.isLocked()) {
            throw new RedisException(RedisResultCode.LOCK_EXPIRED, "锁已过期: " + lockKey);
        }
        if (!lock.isHeldByCurrentThread()) {
            throw new RedisException(RedisResultCode.LOCK_NOT_OWNER, "不是锁的所有者: " + lockKey);
        }
        lock.unlock();
    }

    public void forceUnlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isLocked()) {
            lock.forceUnlock();
        }
    }

    public boolean isLocked(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isLocked();
    }

    public boolean isHeldByCurrentThread(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isHeldByCurrentThread();
    }
}
