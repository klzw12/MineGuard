package com.klzw.common.mongodb.handler;

import com.klzw.common.core.result.Result;
import com.klzw.common.mongodb.exception.MongoDbException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MongoDbExceptionHandlerStrategy 单元测试
 */
@DisplayName("MongoDbExceptionHandlerStrategy异常处理策略测试")
class MongoDbExceptionHandlerStrategyTest {

    private MongoDbExceptionHandlerStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new MongoDbExceptionHandlerStrategy();
    }

    @Test
    @DisplayName("支持处理MongoDbException")
    void support_shouldReturnTrueForMongoDbException() {
        MongoDbException exception = new MongoDbException(1301, "MongoDB连接失败");
        
        assertTrue(strategy.support(exception));
    }

    @Test
    @DisplayName("不支持处理其他异常")
    void support_shouldReturnFalseForOtherException() {
        RuntimeException exception = new RuntimeException("其他异常");
        NullPointerException npe = new NullPointerException("空指针");
        
        assertFalse(strategy.support(exception));
        assertFalse(strategy.support(npe));
    }

    @Test
    @DisplayName("正确处理MongoDbException并返回Result")
    void handle_shouldReturnCorrectResult() {
        MongoDbException exception = new MongoDbException(1321, "文档不存在");
        
        Result<?> result = strategy.handle(exception);
        
        assertNotNull(result);
        assertEquals(1321, result.getCode());
        assertEquals("文档不存在", result.getMessage());
    }

    @Test
    @DisplayName("处理带原因的MongoDbException")
    void handle_shouldHandleExceptionWithCause() {
        RuntimeException cause = new RuntimeException("原始异常");
        MongoDbException exception = new MongoDbException(1302, "MongoDB连接超时", cause);
        
        Result<?> result = strategy.handle(exception);
        
        assertNotNull(result);
        assertEquals(1302, result.getCode());
        assertEquals("MongoDB连接超时", result.getMessage());
    }

    @Test
    @DisplayName("处理默认code的MongoDbException")
    void handle_shouldHandleExceptionWithDefaultCode() {
        MongoDbException exception = new MongoDbException("MongoDB操作失败");
        
        Result<?> result = strategy.handle(exception);
        
        assertNotNull(result);
        assertEquals(1300, result.getCode());
        assertEquals("MongoDB操作失败", result.getMessage());
    }

}
