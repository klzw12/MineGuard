package com.klzw.common.redis.service;

import com.klzw.common.redis.AbstractRedisIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis 分布式锁集成测试
 */
@DisplayName("Redis分布式锁集成测试")
@Tag("integration")
public class RedisDistributedLockIntegrationTest extends AbstractRedisIntegrationTest {

    @Autowired
    private RedisDistributedLock redisDistributedLock;

    private static final String TEST_LOCK_KEY = "test:lock:distributed";

    @BeforeEach
    void setUp() {
        redisDistributedLock.forceUnlock(TEST_LOCK_KEY);
    }

    @AfterEach
    void tearDown() {
        redisDistributedLock.forceUnlock(TEST_LOCK_KEY);
    }

    @Test
    @DisplayName("获取和释放锁")
    void testTryLockAndUnlock() {
        boolean acquired = redisDistributedLock.tryLock(TEST_LOCK_KEY, 10, TimeUnit.SECONDS);
        
        assertTrue(acquired);
        assertTrue(redisDistributedLock.isLocked(TEST_LOCK_KEY));
        assertTrue(redisDistributedLock.isHeldByCurrentThread(TEST_LOCK_KEY));
        
        boolean released = redisDistributedLock.unlock(TEST_LOCK_KEY);
        assertTrue(released);
        assertFalse(redisDistributedLock.isLocked(TEST_LOCK_KEY));
    }

    @Test
    @DisplayName("锁的可重入性")
    void testReentrantLock() {
        boolean acquired1 = redisDistributedLock.tryLock(TEST_LOCK_KEY, 10, TimeUnit.SECONDS);
        assertTrue(acquired1);
        
        boolean acquired2 = redisDistributedLock.tryLock(TEST_LOCK_KEY, 10, TimeUnit.SECONDS);
        assertTrue(acquired2);
        
        boolean released1 = redisDistributedLock.unlock(TEST_LOCK_KEY);
        assertTrue(released1);
        assertTrue(redisDistributedLock.isLocked(TEST_LOCK_KEY));
        
        boolean released2 = redisDistributedLock.unlock(TEST_LOCK_KEY);
        assertTrue(released2);
        assertFalse(redisDistributedLock.isLocked(TEST_LOCK_KEY));
    }

    @Test
    @DisplayName("锁超时自动释放")
    void testLockTimeout() throws InterruptedException {
        boolean acquired = redisDistributedLock.tryLock(TEST_LOCK_KEY, 1, TimeUnit.SECONDS);
        assertTrue(acquired);
        
        Thread.sleep(1100);
        
        assertFalse(redisDistributedLock.isLocked(TEST_LOCK_KEY));
    }

    @Test
    @DisplayName("强制释放锁")
    void testForceUnlock() {
        boolean acquired = redisDistributedLock.tryLock(TEST_LOCK_KEY, 10, TimeUnit.SECONDS);
        assertTrue(acquired);
        
        redisDistributedLock.forceUnlock(TEST_LOCK_KEY);
        
        assertFalse(redisDistributedLock.isLocked(TEST_LOCK_KEY));
        assertFalse(redisDistributedLock.isHeldByCurrentThread(TEST_LOCK_KEY));
    }

    @Test
    @DisplayName("释放未持有的锁")
    void testUnlockNotHeld() {
        boolean released = redisDistributedLock.unlock(TEST_LOCK_KEY);
        assertFalse(released);
    }

    @Test
    @DisplayName("检查锁状态")
    void testLockStatus() {
        assertFalse(redisDistributedLock.isLocked(TEST_LOCK_KEY));
        assertFalse(redisDistributedLock.isHeldByCurrentThread(TEST_LOCK_KEY));
        
        redisDistributedLock.tryLock(TEST_LOCK_KEY, 10, TimeUnit.SECONDS);
        
        assertTrue(redisDistributedLock.isLocked(TEST_LOCK_KEY));
        assertTrue(redisDistributedLock.isHeldByCurrentThread(TEST_LOCK_KEY));
    }

    @Test
    @DisplayName("多线程锁竞争")
    void testMultiThreadCompetition() throws InterruptedException {
        Thread thread1 = new Thread(() -> {
            boolean acquired = redisDistributedLock.tryLock(TEST_LOCK_KEY, 5, TimeUnit.SECONDS);
            if (acquired) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    redisDistributedLock.unlock(TEST_LOCK_KEY);
                }
            }
        });
        
        Thread thread2 = new Thread(() -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            boolean acquired = redisDistributedLock.tryLock(TEST_LOCK_KEY, 5, TimeUnit.SECONDS);
            if (acquired) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    redisDistributedLock.unlock(TEST_LOCK_KEY);
                }
            }
        });
        
        thread1.start();
        thread2.start();
        
        thread1.join();
        thread2.join();
        
        assertFalse(redisDistributedLock.isLocked(TEST_LOCK_KEY));
    }
}
