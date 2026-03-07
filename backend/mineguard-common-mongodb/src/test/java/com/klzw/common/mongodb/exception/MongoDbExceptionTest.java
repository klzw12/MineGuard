package com.klzw.common.mongodb.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MongoDbException 单元测试
 */
@DisplayName("MongoDbException异常测试")
class MongoDbExceptionTest {

    @Test
    @DisplayName("使用code和message创建异常")
    void create_withCodeAndMessage() {
        MongoDbException exception = new MongoDbException(1301, "MongoDB连接失败");
        
        assertEquals(1301, exception.getCode());
        assertEquals("MongoDB连接失败", exception.getMessage());
        assertEquals("mongodb", exception.getModule());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("使用code、message和cause创建异常")
    void create_withCodeMessageAndCause() {
        RuntimeException cause = new RuntimeException("原始异常");
        MongoDbException exception = new MongoDbException(1302, "MongoDB连接超时", cause);
        
        assertEquals(1302, exception.getCode());
        assertEquals("MongoDB连接超时", exception.getMessage());
        assertEquals("mongodb", exception.getModule());
        assertSame(cause, exception.getCause());
    }

    @Test
    @DisplayName("仅使用message创建异常（默认code=1300）")
    void create_withMessageOnly() {
        MongoDbException exception = new MongoDbException("MongoDB操作失败");
        
        assertEquals(1300, exception.getCode());
        assertEquals("MongoDB操作失败", exception.getMessage());
        assertEquals("mongodb", exception.getModule());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("使用message和cause创建异常（默认code=1300）")
    void create_withMessageAndCause() {
        RuntimeException cause = new RuntimeException("原始异常");
        MongoDbException exception = new MongoDbException("MongoDB操作失败", cause);
        
        assertEquals(1300, exception.getCode());
        assertEquals("MongoDB操作失败", exception.getMessage());
        assertEquals("mongodb", exception.getModule());
        assertSame(cause, exception.getCause());
    }

    @Test
    @DisplayName("异常可以被抛出和捕获")
    void exception_canBeThrownAndCaught() {
        assertThrows(MongoDbException.class, () -> {
            throw new MongoDbException(1301, "测试异常");
        });
    }

    @Test
    @DisplayName("异常链应正确传递")
    void exceptionChain_shouldBeCorrect() {
        Exception cause = new IllegalArgumentException("非法参数");
        MongoDbException exception = new MongoDbException(1320, "文档操作失败", cause);
        
        assertSame(cause, exception.getCause());
        assertEquals("非法参数", exception.getCause().getMessage());
    }

}
