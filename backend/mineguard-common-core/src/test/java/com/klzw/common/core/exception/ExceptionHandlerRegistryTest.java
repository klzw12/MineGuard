package com.klzw.common.core.exception;

import com.klzw.common.core.result.Result;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

/**
 * 异常处理注册中心测试类
 */
public class ExceptionHandlerRegistryTest {

    @Test
    @DisplayName("验证注册中心初始化时策略数量正确")
    public void testRegistryInitialization() {
        ExceptionHandlerRegistry registry = new ExceptionHandlerRegistry();
        // 初始化时注册了4个默认策略: MaxUploadSizeExceededExceptionHandlerStrategy, BusinessExceptionHandlerStrategy, SystemExceptionHandlerStrategy, DefaultExceptionHandlerStrategy
        assertEquals(4, registry.getStrategies().size());
    }

    @Test
    @DisplayName("验证注册自定义策略")
    public void testRegisterCustomStrategy() {
        ExceptionHandlerRegistry registry = new ExceptionHandlerRegistry();
        ExceptionHandlerStrategy customStrategy = new ExceptionHandlerStrategy() {
            @Override
            public boolean support(Throwable throwable) {
                return false;
            }

            @Override
            public Result<?> handle(Throwable throwable) {
                return null;
            }
        };

        registry.register(customStrategy);

        assertEquals(5, registry.getStrategies().size());
        assertSame(customStrategy, registry.getStrategies().get(0));
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
