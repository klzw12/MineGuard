package com.klzw.common.web.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * WebMvcConfig 测试类
 */
@SpringJUnitConfig
@DisplayName("WebMvcConfig 测试")
public class WebMvcConfigTest {
    
    @Test
    @DisplayName("测试 WebMvcConfig 初始化")
    public void testWebMvcConfigInitialization() {
        WebMvcConfig webMvcConfig = new WebMvcConfig();
        assertNotNull(webMvcConfig);
    }
    
}
