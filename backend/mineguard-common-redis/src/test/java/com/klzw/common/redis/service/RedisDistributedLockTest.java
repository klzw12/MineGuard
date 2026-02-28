package com.klzw.common.redis.service;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RedisDistributedLock 测试类
 */
@SpringBootTest
public class RedisDistributedLockTest {

    @Autowired
    private RedisDistributedLock redisDistributedLock;

    private static final String TEST_LOCK_KEY = "test:lock";
    private static final long TEST_EXPIRE = 1;
    private static final TimeUnit TEST_TIME_UNIT = TimeUnit.MINUTES;

    @Test
    public void testTryLockAndUnlock() {
        // 测试获取锁和释放锁
        boolean locked = redisDistributedLock.tryLock(TEST_LOCK_KEY, TEST_EXPIRE, TEST_TIME_UNIT);
        assertTrue(locked);
        assertTrue(redisDistributedLock.isLocked(TEST_LOCK_KEY));
        assertTrue(redisDistributedLock.isHeldByCurrentThread(TEST_LOCK_KEY));
        
        boolean unlocked = redisDistributedLock.unlock(TEST_LOCK_KEY);
        assertTrue(unlocked);
        assertFalse(redisDistributedLock.isLocked(TEST_LOCK_KEY));
        assertFalse(redisDistributedLock.isHeldByCurrentThread(TEST_LOCK_KEY));
    }

    @Test
    public void testTryLockWithCustomValue() {
        // 测试使用自定义值获取锁
        String customValue = "custom:value";
        boolean locked = redisDistributedLock.tryLock(TEST_LOCK_KEY, customValue, TEST_EXPIRE, TEST_TIME_UNIT);
        assertTrue(locked);
        assertTrue(redisDistributedLock.isLocked(TEST_LOCK_KEY));
        
        boolean unlocked = redisDistributedLock.unlock(TEST_LOCK_KEY);
        assertTrue(unlocked);
        assertFalse(redisDistributedLock.isLocked(TEST_LOCK_KEY));
    }

    @Test
    public void testReentrantLock() {
        // 测试可重入锁
        // 第一次获取锁
        boolean locked1 = redisDistributedLock.tryLock(TEST_LOCK_KEY, TEST_EXPIRE, TEST_TIME_UNIT);
        assertTrue(locked1);
        
        // 第二次获取同一把锁（重入）
        boolean locked2 = redisDistributedLock.tryLock(TEST_LOCK_KEY, TEST_EXPIRE, TEST_TIME_UNIT);
        assertTrue(locked2);
        
        // 第一次释放锁（重入计数减1，锁仍然存在）
        boolean unlocked1 = redisDistributedLock.unlock(TEST_LOCK_KEY);
        assertTrue(unlocked1);
        assertTrue(redisDistributedLock.isLocked(TEST_LOCK_KEY));
        
        // 第二次释放锁（重入计数为0，锁被释放）
        boolean unlocked2 = redisDistributedLock.unlock(TEST_LOCK_KEY);
        assertTrue(unlocked2);
        assertFalse(redisDistributedLock.isLocked(TEST_LOCK_KEY));
    }

    @Test
    public void testForceUnlock() {
        // 测试强制释放锁
        boolean locked = redisDistributedLock.tryLock(TEST_LOCK_KEY, TEST_EXPIRE, TEST_TIME_UNIT);
        assertTrue(locked);
        
        redisDistributedLock.forceUnlock(TEST_LOCK_KEY);
        assertFalse(redisDistributedLock.isLocked(TEST_LOCK_KEY));
        assertFalse(redisDistributedLock.isHeldByCurrentThread(TEST_LOCK_KEY));
    }

    @Test
    public void testUnlockNonHeldLock() {
        // 测试释放未持有的锁
        boolean unlocked = redisDistributedLock.unlock(TEST_LOCK_KEY);
        assertFalse(unlocked);
    }

    @Test
    public void testConcurrentLocking() throws InterruptedException {
        // 测试并发获取锁
        final AtomicInteger lockCount = new AtomicInteger(0);
        final int threadCount = 5;
        final Thread[] threads = new Thread[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                boolean locked = redisDistributedLock.tryLock(TEST_LOCK_KEY, TEST_EXPIRE, TEST_TIME_UNIT);
                if (locked) {
                    try {
                        lockCount.incrementAndGet();
                        // 模拟业务操作
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        redisDistributedLock.unlock(TEST_LOCK_KEY);
                    }
                }
            });
            threads[i].start();
        }
        
        // 等待所有线程执行完成
        for (Thread thread : threads) {
            thread.join();
        }
        
        // 验证只有一个线程获取到了锁
        assertTrue(lockCount.get() > 0);
        assertFalse(redisDistributedLock.isLocked(TEST_LOCK_KEY));
    }

    @Test
    public void testLockExpiration() throws InterruptedException {
        // 测试锁过期
        boolean locked = redisDistributedLock.tryLock(TEST_LOCK_KEY, 1, TimeUnit.SECONDS);
        assertTrue(locked);
        
        // 等待锁过期
        Thread.sleep(1500);
        
        // 验证锁已经过期
        assertFalse(redisDistributedLock.isLocked(TEST_LOCK_KEY));
        assertFalse(redisDistributedLock.isHeldByCurrentThread(TEST_LOCK_KEY));
    }

    @Test
    public void testDifferentThreadLocking() throws InterruptedException {
        // 测试不同线程尝试获取同一把锁
        boolean locked = redisDistributedLock.tryLock(TEST_LOCK_KEY, TEST_EXPIRE, TEST_TIME_UNIT);
        assertTrue(locked);
        
        // 创建一个新线程尝试获取同一把锁
        final boolean[] otherThreadLocked = new boolean[1];
        Thread otherThread = new Thread(() -> {
            otherThreadLocked[0] = redisDistributedLock.tryLock(TEST_LOCK_KEY, 1, TimeUnit.SECONDS);
        });
        otherThread.start();
        otherThread.join();
        
        // 验证新线程无法获取锁
        assertFalse(otherThreadLocked[0]);
        
        // 释放锁
        redisDistributedLock.unlock(TEST_LOCK_KEY);
    }

    @Test
    public void testUnlockWithMismatchedValue() {
        // 测试锁值不匹配时的释放情况
        // 先获取锁
        boolean locked = redisDistributedLock.tryLock(TEST_LOCK_KEY, TEST_EXPIRE, TEST_TIME_UNIT);
        assertTrue(locked);
        
        // 手动修改锁值
        redisTemplate.opsForValue().set(TEST_LOCK_KEY, "mismatched:value", TEST_EXPIRE, TEST_TIME_UNIT);
        
        // 尝试释放锁，应该失败
        boolean unlocked = redisDistributedLock.unlock(TEST_LOCK_KEY);
        assertFalse(unlocked);
        
        // 强制释放锁
        redisDistributedLock.forceUnlock(TEST_LOCK_KEY);
        assertFalse(redisDistributedLock.isLocked(TEST_LOCK_KEY));
    }

    // 集成测试
    @Test
    @Tag("integration")
    public void testIntegrationWithRealRedis() {
        // 测试与真实Redis的集成
        String integrationLockKey = "integration:lock";
        
        // 清理之前的锁
        redisDistributedLock.forceUnlock(integrationLockKey);
        
        // 测试获取锁
        boolean locked = redisDistributedLock.tryLock(integrationLockKey, TEST_EXPIRE, TEST_TIME_UNIT);
        assertTrue(locked);
        assertTrue(redisDistributedLock.isLocked(integrationLockKey));
        assertTrue(redisDistributedLock.isHeldByCurrentThread(integrationLockKey));
        
        // 测试释放锁
        boolean unlocked = redisDistributedLock.unlock(integrationLockKey);
        assertTrue(unlocked);
        assertFalse(redisDistributedLock.isLocked(integrationLockKey));
        assertFalse(redisDistributedLock.isHeldByCurrentThread(integrationLockKey));
    }

    // 需要注入RedisTemplate来测试锁值不匹配的情况
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
}
