package com.klzw.common.redis.service;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RedissonLockService 测试类
 */
@SpringBootTest
public class RedissonLockServiceTest {

    @Autowired
    private RedissonLockService redissonLockService;

    private static final String TEST_LOCK_KEY = "test:redisson:lock";
    private static final long TEST_WAIT_TIME = 1;
    private static final long TEST_LEASE_TIME = 30;
    private static final TimeUnit TEST_TIME_UNIT = TimeUnit.SECONDS;

    @Test
    public void testTryLockWithWaitTime() {
        // 测试带等待时间的获取锁
        boolean locked = redissonLockService.tryLock(TEST_LOCK_KEY, TEST_WAIT_TIME, TEST_LEASE_TIME, TEST_TIME_UNIT);
        assertTrue(locked);
        assertTrue(redissonLockService.isLocked(TEST_LOCK_KEY));
        assertTrue(redissonLockService.isHeldByCurrentThread(TEST_LOCK_KEY));
        
        // 释放锁
        redissonLockService.unlock(TEST_LOCK_KEY);
        assertFalse(redissonLockService.isLocked(TEST_LOCK_KEY));
        assertFalse(redissonLockService.isHeldByCurrentThread(TEST_LOCK_KEY));
    }

    @Test
    public void testTryLockWithoutParams() {
        // 测试无参数获取锁（默认等待时间：0，持有时间：30秒）
        boolean locked = redissonLockService.tryLock(TEST_LOCK_KEY);
        assertTrue(locked);
        assertTrue(redissonLockService.isLocked(TEST_LOCK_KEY));
        assertTrue(redissonLockService.isHeldByCurrentThread(TEST_LOCK_KEY));
        
        // 释放锁
        redissonLockService.unlock(TEST_LOCK_KEY);
        assertFalse(redissonLockService.isLocked(TEST_LOCK_KEY));
        assertFalse(redissonLockService.isHeldByCurrentThread(TEST_LOCK_KEY));
    }

    @Test
    public void testForceUnlock() {
        // 测试强制释放锁
        boolean locked = redissonLockService.tryLock(TEST_LOCK_KEY);
        assertTrue(locked);
        
        redissonLockService.forceUnlock(TEST_LOCK_KEY);
        assertFalse(redissonLockService.isLocked(TEST_LOCK_KEY));
        assertFalse(redissonLockService.isHeldByCurrentThread(TEST_LOCK_KEY));
    }

    @Test
    public void testUnlockNonHeldLock() {
        // 测试释放未持有的锁（应该不会抛出异常）
        redissonLockService.unlock(TEST_LOCK_KEY);
        // 验证锁不存在
        assertFalse(redissonLockService.isLocked(TEST_LOCK_KEY));
    }

    @Test
    public void testConcurrentLocking() throws InterruptedException {
        // 测试并发获取锁
        final AtomicInteger lockCount = new AtomicInteger(0);
        final int threadCount = 5;
        final Thread[] threads = new Thread[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                boolean locked = redissonLockService.tryLock(TEST_LOCK_KEY, TEST_WAIT_TIME, TEST_LEASE_TIME, TEST_TIME_UNIT);
                if (locked) {
                    try {
                        lockCount.incrementAndGet();
                        // 模拟业务操作
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        redissonLockService.unlock(TEST_LOCK_KEY);
                    }
                }
            });
            threads[i].start();
        }
        
        // 等待所有线程执行完成
        for (Thread thread : threads) {
            thread.join();
        }
        
        // 验证至少有一个线程获取到了锁
        assertTrue(lockCount.get() > 0);
        assertFalse(redissonLockService.isLocked(TEST_LOCK_KEY));
    }

    @Test
    public void testLockExpiration() throws InterruptedException {
        // 测试锁过期
        boolean locked = redissonLockService.tryLock(TEST_LOCK_KEY, 0, 1, TimeUnit.SECONDS);
        assertTrue(locked);
        
        // 等待锁过期
        Thread.sleep(1500);
        
        // 验证锁已经过期
        assertFalse(redissonLockService.isLocked(TEST_LOCK_KEY));
        assertFalse(redissonLockService.isHeldByCurrentThread(TEST_LOCK_KEY));
    }

    @Test
    public void testDifferentThreadLocking() throws InterruptedException {
        // 测试不同线程尝试获取同一把锁
        boolean locked = redissonLockService.tryLock(TEST_LOCK_KEY);
        assertTrue(locked);
        
        // 创建一个新线程尝试获取同一把锁
        final boolean[] otherThreadLocked = new boolean[1];
        Thread otherThread = new Thread(() -> {
            otherThreadLocked[0] = redissonLockService.tryLock(TEST_LOCK_KEY, 1, 1, TimeUnit.SECONDS);
        });
        otherThread.start();
        otherThread.join();
        
        // 验证新线程无法获取锁
        assertFalse(otherThreadLocked[0]);
        
        // 释放锁
        redissonLockService.unlock(TEST_LOCK_KEY);
    }

    // 集成测试
    @Test
    @Tag("integration")
    public void testIntegrationWithRealRedis() {
        // 测试与真实Redis的集成
        String integrationLockKey = "integration:redisson:lock";
        
        // 清理之前的锁
        redissonLockService.forceUnlock(integrationLockKey);
        
        // 测试获取锁
        boolean locked = redissonLockService.tryLock(integrationLockKey, TEST_WAIT_TIME, TEST_LEASE_TIME, TEST_TIME_UNIT);
        assertTrue(locked);
        assertTrue(redissonLockService.isLocked(integrationLockKey));
        assertTrue(redissonLockService.isHeldByCurrentThread(integrationLockKey));
        
        // 测试释放锁
        redissonLockService.unlock(integrationLockKey);
        assertFalse(redissonLockService.isLocked(integrationLockKey));
        assertFalse(redissonLockService.isHeldByCurrentThread(integrationLockKey));
    }
}
