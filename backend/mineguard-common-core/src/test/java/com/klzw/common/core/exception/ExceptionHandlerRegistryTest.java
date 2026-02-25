package com.klzw.common.core.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

/**
 * 异常处理注册中心测试类
 */
public class ExceptionHandlerRegistryTest {

    @Test
    @DisplayName("验证注册中心初始化时默认注册了所有策略")
    public void testRegistryInitialization() {
        ExceptionHandlerRegistry registry = new ExceptionHandlerRegistry();
        assertEquals(3, registry.getStrategies().size());

        // 验证策略顺序（新注册的策略优先级更高）
        assertTrue(registry.getStrategies().get(0) instanceof BusinessExceptionHandlerStrategy);
        assertTrue(registry.getStrategies().get(1) instanceof SystemExceptionHandlerStrategy);
        assertTrue(registry.getStrategies().get(2) instanceof DefaultExceptionHandlerStrategy);
    }

    @Test
    @DisplayName("验证能够注册自定义异常处理策略")
    public void testRegisterStrategy() {
        ExceptionHandlerRegistry registry = new ExceptionHandlerRegistry();
        int initialSize = registry.getStrategies().size();

        // 创建自定义异常处理策略
        ExceptionHandlerStrategy customStrategy = new ExceptionHandlerStrategy() {
            @Override
            public boolean support(Throwable throwable) {
                return throwable instanceof IllegalArgumentException;
            }

            @Override
            public com.klzw.common.core.result.Result<?> handle(Throwable throwable) {
                return com.klzw.common.core.result.Result.fail(400, "参数错误");
            }
        };

        // 注册自定义策略
        registry.register(customStrategy);

        // 验证策略已注册且位于列表首位（优先级最高）
        assertEquals(initialSize + 1, registry.getStrategies().size());
        assertSame(customStrategy, registry.getStrategies().getFirst());
    }

    @Test
    @DisplayName("验证能够获取到处理业务异常的策略")
    public void testGetStrategyForBusinessException() {
        ExceptionHandlerRegistry registry = new ExceptionHandlerRegistry();
        BusinessException exception = new BusinessException(400, "业务错误");
        ExceptionHandlerStrategy strategy = registry.getStrategy(exception);

        assertTrue(strategy instanceof BusinessExceptionHandlerStrategy);
    }

    @Test
    @DisplayName("验证能够获取到处理系统异常的策略")
    public void testGetStrategyForSystemException() {
        ExceptionHandlerRegistry registry = new ExceptionHandlerRegistry();
        SystemException exception = new SystemException(500, "系统错误");
        ExceptionHandlerStrategy strategy = registry.getStrategy(exception);

        assertTrue(strategy instanceof SystemExceptionHandlerStrategy);
    }

    @Test
    @DisplayName("验证能够获取到处理其他类型异常的默认策略")
    public void testGetStrategyForOtherException() {
        ExceptionHandlerRegistry registry = new ExceptionHandlerRegistry();
        IllegalArgumentException exception = new IllegalArgumentException("参数错误");
        ExceptionHandlerStrategy strategy = registry.getStrategy(exception);

        assertTrue(strategy instanceof DefaultExceptionHandlerStrategy);
    }
}
