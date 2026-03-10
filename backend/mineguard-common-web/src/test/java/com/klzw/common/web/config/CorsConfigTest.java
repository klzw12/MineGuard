package com.klzw.common.web.config;

import com.klzw.common.web.properties.WebProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.web.filter.CorsFilter;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("WebAutoConfiguration CORS 测试")
public class CorsConfigTest {
    
    @Test
    @DisplayName("测试 CorsFilter 初始化")
    public void testCorsFilterInitialization() {
        WebProperties webProperties = new WebProperties();
        WebProperties.Cors cors = webProperties.getCors();
        cors.setAllowedOrigins("*");
        cors.setAllowedMethods("GET,POST,PUT,DELETE,OPTIONS");
        cors.setAllowedHeaders("*");
        cors.setAllowCredentials(true);
        cors.setMaxAge(3600L);
        
        WebAutoConfiguration webAutoConfiguration = new WebAutoConfiguration(webProperties);
        CorsFilter corsFilter = webAutoConfiguration.corsFilter();
        assertNotNull(corsFilter);
    }
    
}
