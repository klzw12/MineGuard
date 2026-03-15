package com.klzw.service.gateway.config;

import com.klzw.service.gateway.properties.GatewayProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.cors.reactive.CorsWebFilter;

import static org.mockito.Mockito.*;

/**
 * CORS配置测试
 * 测试优先级：中 - 配置类测试
 */
@ExtendWith(MockitoExtension.class)
class CorsConfigTest {

    @Mock
    private GatewayProperties gatewayProperties;

    @Mock
    private GatewayProperties.Cors cors;

    private CorsConfig corsConfig;

    @BeforeEach
    void setUp() {
        corsConfig = new CorsConfig(gatewayProperties);
    }

    @Test
    void testCorsWebFilter_WithAllowedOrigins() {
        // Given
        when(gatewayProperties.getCors()).thenReturn(cors);
        when(cors.getAllowedOrigins()).thenReturn("http://localhost:3000,http://localhost:8080");
        when(cors.getAllowedMethods()).thenReturn("GET,POST,PUT,DELETE");
        when(cors.getAllowedHeaders()).thenReturn("*");
        when(cors.isAllowCredentials()).thenReturn(true);
        when(cors.getMaxAge()).thenReturn(3600L);

        // When
        CorsWebFilter filter = corsConfig.corsWebFilter();

        // Then
        assert filter != null;
    }

    @Test
    void testCorsWebFilter_WithoutAllowedOrigins() {
        // Given
        when(gatewayProperties.getCors()).thenReturn(cors);
        when(cors.getAllowedOrigins()).thenReturn(null);
        when(cors.getAllowedMethods()).thenReturn("GET,POST,PUT,DELETE");
        when(cors.getAllowedHeaders()).thenReturn("*");
        when(cors.isAllowCredentials()).thenReturn(true);
        when(cors.getMaxAge()).thenReturn(3600L);

        // When
        CorsWebFilter filter = corsConfig.corsWebFilter();

        // Then
        assert filter != null;
    }

    @Test
    void testCorsWebFilter_EmptyAllowedOrigins() {
        // Given
        when(gatewayProperties.getCors()).thenReturn(cors);
        when(cors.getAllowedOrigins()).thenReturn("");
        when(cors.getAllowedMethods()).thenReturn("GET,POST,PUT,DELETE");
        when(cors.getAllowedHeaders()).thenReturn("*");
        when(cors.isAllowCredentials()).thenReturn(true);
        when(cors.getMaxAge()).thenReturn(3600L);

        // When
        CorsWebFilter filter = corsConfig.corsWebFilter();

        // Then
        assert filter != null;
    }
}
