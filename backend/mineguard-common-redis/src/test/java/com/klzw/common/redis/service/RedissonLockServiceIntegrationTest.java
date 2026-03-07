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
 * Redisson 锁服务集成测试
 */
@DisplayName("Redisson锁服务集成测试")
@Tag("integration")
public class RedissonLockServiceIntegrationTest extends AbstractRedisIntegrationTest {

    @Autowired
    private RedissonLockService redissonLockService;

    private static final String TEST_LOCK_KEY = "test:lock:redisson";

    @BeforeEach
    void setUp() {
        redissonLockService.forceUnlock(TEST_LOCK_KEY);
    }

    @AfterEach
    void tearDown() {
        redissonLockService.forceUnlock(TEST_LOCK_KEY);
    }

    @Test
    @DisplayName("获取和释放锁")
    void testTryLockAndUnlock() {
        boolean acquired = redissonLockService.tryLock(TEST_LOCK_KEY);
        
        assertTrue(acquired);
        assertTrue(redissonLockService.isLocked(TEST_LOCK_KEY));
        assertTrue(redissonLockService.isHeldByCurrentThread(TEST_LOCK_KEY));
        
        redissonLockService.unlock(TEST_LOCK_KEY);
        assertFalse(redissonLockService.isLocked(TEST_LOCK_KEY));
    }

    @Test
    @DisplayName("带等待时间的锁获取")
    void testTryLockWithWaitTime() {
        boolean acquired = redissonLockService.tryLock(TEST_LOCK_KEY, 1, 10, TimeUnit.SECONDS);
        
        assertTrue(acquired);
        assertTrue(redissonLockService.isLocked(TEST_LOCK_KEY));
        
        redissonLockService.unlock(TEST_LOCK_KEY);
        assertFalse(redissonLockService.isLocked(TEST_LOCK_KEY));
    }

    @Test
    @DisplayName("锁的可重入性")
    void testReentrantLock() {
        boolean acquired1 = redissonLockService.tryLock(TEST_LOCK_KEY);
        assertTrue(acquired1);
        
        boolean acquired2 = redissonLockService.tryLock(TEST_LOCK_KEY);
        assertTrue(acquired2);
        
        redissonLockService.unlock(TEST_LOCK_KEY);
        assertTrue(redissonLockService.isLocked(TEST_LOCK_KEY));
        
        redissonLockService.unlock(TEST_LOCK_KEY);
        assertFalse(redissonLockService.isLocked(TEST_LOCK_KEY));
    }

    @Test
    @DisplayName("强制释放锁")
    void testForceUnlock() {
        boolean acquired = redissonLockService.tryLock(TEST_LOCK_KEY);
        assertTrue(acquired);
        
        redissonLockService.forceUnlock(TEST_LOCK_KEY);
        
        assertFalse(redissonLockService.isLocked(TEST_LOCK_KEY));
    }

    @Test
    @DisplayName("检查锁状态")
    void testLockStatus() {
        assertFalse(redissonLockService.isLocked(TEST_LOCK_KEY));
        assertFalse(redissonLockService.isHeldByCurrentThread(TEST_LOCK_KEY));
        
        redissonLockService.tryLock(TEST_LOCK_KEY);
        
        assertTrue(redissonLockService.isLocked(TEST_LOCK_KEY));
        assertTrue(redissonLockService.isHeldByCurrentThread(TEST_LOCK_KEY));
    }

    @Test
    @DisplayName("释放未持有的锁")
    void testUnlockNotHeld() {
        assertDoesNotThrow(() -> redissonLockService.unlock(TEST_LOCK_KEY));
    }

    @Test
    @DisplayName("多线程锁竞争")
    void testMultiThreadCompetition() throws InterruptedException {
        Thread thread1 = new Thread(() -> {
            boolean acquired = redissonLockService.tryLock(TEST_LOCK_KEY, 5, 10, TimeUnit.SECONDS);
            if (acquired) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    redissonLockService.unlock(TEST_LOCK_KEY);
                }
            }
        });
        
        Thread thread2 = new Thread(() -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            boolean acquired = redissonLockService.tryLock(TEST_LOCK_KEY, 5, 10, TimeUnit.SECONDS);
            if (acquired) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    redissonLockService.unlock(TEST_LOCK_KEY);
                }
            }
        });
        
        thread1.start();
        thread2.start();
        
        thread1.join();
        thread2.join();
        
        assertFalse(redissonLockService.isLocked(TEST_LOCK_KEY));
    }
}
