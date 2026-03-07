package com.klzw.common.web.interceptor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * AuthInterceptor 测试类
 */
@SpringJUnitConfig
@DisplayName("AuthInterceptor 测试")
public class AuthInterceptorTest {
    
    @Test
    @DisplayName("测试 AuthInterceptor 初始化")
    public void testAuthInterceptorInitialization() {
        AuthInterceptor authInterceptor = new AuthInterceptor();
        assertNotNull(authInterceptor);
    }
    
}
