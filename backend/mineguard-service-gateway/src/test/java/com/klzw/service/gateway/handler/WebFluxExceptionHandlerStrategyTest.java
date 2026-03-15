package com.klzw.service.gateway.handler;

import com.klzw.common.core.exception.ExceptionHandlerRegistry;
import com.klzw.common.core.exception.ExceptionHandlerStrategy;
import com.klzw.common.core.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * WebFlux异常处理策略测试
 * 测试优先级：高 - 核心错误处理组件
 */
@ExtendWith(MockitoExtension.class)
class WebFluxExceptionHandlerStrategyTest {

    @Mock
    private ExceptionHandlerRegistry registry;

    @Mock
    private ExceptionHandlerStrategy strategy;

    private WebFluxExceptionHandlerStrategy webFluxExceptionHandlerStrategy;

    @BeforeEach
    void setUp() {
        webFluxExceptionHandlerStrategy = new WebFluxExceptionHandlerStrategy(registry);
    }

    @Test
    void testHandle_Exception_ReturnResult() {
        // Given
        RuntimeException exception = new RuntimeException("Test exception");
        
        when(registry.getStrategy(exception)).thenReturn(strategy);
        when(strategy.handle(exception)).thenReturn(Result.fail(500, "Internal server error"));

        // When
        Result<?> result = webFluxExceptionHandlerStrategy.handle(exception);

        // Then
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("Internal server error", result.getMessage());
        verify(registry).getStrategy(exception);
        verify(strategy).handle(exception);
    }
}
