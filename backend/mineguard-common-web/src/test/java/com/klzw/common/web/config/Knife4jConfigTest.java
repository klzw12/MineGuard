package com.klzw.common.web.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import io.swagger.v3.oas.models.OpenAPI;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Knife4jConfig 测试类
 */
@SpringJUnitConfig
@DisplayName("Knife4jConfig 测试")
public class Knife4jConfigTest {
    
    @Test
    @DisplayName("测试 OpenAPI 初始化")
    public void testOpenAPIInitialization() {
        Knife4jConfig knife4jConfig = new Knife4jConfig();
        OpenAPI openAPI = knife4jConfig.openAPI();
        assertNotNull(openAPI);
    }
    
}
