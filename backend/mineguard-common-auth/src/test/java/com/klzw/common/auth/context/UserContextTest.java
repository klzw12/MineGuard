package com.klzw.common.auth.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户上下文单元测试
 */
class UserContextTest {

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void setUserId_shouldSetUserId() {
        Long userId = 1L;

        UserContext.setUserId(userId);

        assertEquals(userId, UserContext.getUserId());
    }

    @Test
    void getUserId_shouldReturnNull_whenNotSet() {
        Long result = UserContext.getUserId();

        assertNull(result);
    }

    @Test
    void setUsername_shouldSetUsername() {
        String username = "testUser";

        UserContext.setUsername(username);

        assertEquals(username, UserContext.getUsername());
    }

    @Test
    void getUsername_shouldReturnNull_whenNotSet() {
        String result = UserContext.getUsername();

        assertNull(result);
    }

    @Test
    void clear_shouldRemoveUserId() {
        UserContext.setUserId(1L);

        UserContext.clear();

        assertNull(UserContext.getUserId());
    }

    @Test
    void clear_shouldRemoveUsername() {
        UserContext.setUsername("testUser");

        UserContext.clear();

        assertNull(UserContext.getUsername());
    }

    @Test
    void clear_shouldNotThrowException_whenCalledMultipleTimes() {
        UserContext.setUserId(1L);
        UserContext.setUsername("testUser");

        assertDoesNotThrow(() -> {
            UserContext.clear();
            UserContext.clear();
            UserContext.clear();
        });

        assertNull(UserContext.getUserId());
        assertNull(UserContext.getUsername());
    }

    @Test
    void threadIsolation_shouldIsolateDataBetweenThreads() throws InterruptedException {
        UserContext.setUserId(1L);
        UserContext.setUsername("mainThread");

        Thread workerThread = new Thread(() -> {
            UserContext.setUserId(2L);
            UserContext.setUsername("workerThread");

            assertEquals(2L, UserContext.getUserId());
            assertEquals("workerThread", UserContext.getUsername());
        });

        workerThread.start();
        workerThread.join();

        // 主线程的数据应该保持不变
        assertEquals(1L, UserContext.getUserId());
        assertEquals("mainThread", UserContext.getUsername());
    }

    @Test
    void setUserId_shouldOverwritePreviousValue() {
        UserContext.setUserId(1L);
        UserContext.setUserId(2L);

        assertEquals(2L, UserContext.getUserId());
    }

    @Test
    void setUsername_shouldOverwritePreviousValue() {
        UserContext.setUsername("user1");
        UserContext.setUsername("user2");

        assertEquals("user2", UserContext.getUsername());
    }
}