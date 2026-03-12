package com.klzw.common.web.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import io.swagger.v3.oas.models.OpenAPI;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringJUnitConfig
@DisplayName("OpenApiConfig 测试")
public class OpenApiConfigTest {
    
    @Test
    @DisplayName("测试 OpenAPI 初始化")
    public void testOpenAPIInitialization() {
        OpenApiConfig openApiConfig = new OpenApiConfig();
        OpenAPI openAPI = openApiConfig.openAPI();
        assertNotNull(openAPI);
    }    
}
