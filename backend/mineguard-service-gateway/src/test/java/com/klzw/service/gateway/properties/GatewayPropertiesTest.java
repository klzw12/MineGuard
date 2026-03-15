package com.klzw.service.gateway.properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * 网关配置属性测试
 * 测试优先级：高 - 核心配置组件
 */
class GatewayPropertiesTest {

    private GatewayProperties gatewayProperties;

    @BeforeEach
    void setUp() {
        gatewayProperties = new GatewayProperties();
    }

    @Test
    void testDefaultValues() {
        // 测试默认值
        assert gatewayProperties.isEnableRateLimit() == true;
        assert gatewayProperties.isEnableCircuitBreaker() == true;
        assert gatewayProperties.getRateLimitCount() == 1000;
        assert gatewayProperties.getRateLimitTime() == 60;
        assert gatewayProperties.getCors() != null;
        assert gatewayProperties.getIgnoreAuth() != null;
    }

    @Test
    void testSettersAndGetters() {
        // 测试setter和getter
        gatewayProperties.setEnableRateLimit(false);
        gatewayProperties.setEnableCircuitBreaker(false);
        gatewayProperties.setRateLimitCount(500);
        gatewayProperties.setRateLimitTime(30);

        assert gatewayProperties.isEnableRateLimit() == false;
        assert gatewayProperties.isEnableCircuitBreaker() == false;
        assert gatewayProperties.getRateLimitCount() == 500;
        assert gatewayProperties.getRateLimitTime() == 30;
    }

    @Test
    void testCorsDefaultValues() {
        // 测试Cors默认值
        GatewayProperties.Cors cors = gatewayProperties.getCors();
        assert cors != null;
        assert cors.getAllowedOrigins() == null;
        assert cors.getAllowedMethods().equals("GET,POST,PUT,DELETE,OPTIONS");
        assert cors.getAllowedHeaders().equals("*");
        assert cors.isAllowCredentials() == true;
        assert cors.getMaxAge() == 3600L;
    }

    @Test
    void testCorsSettersAndGetters() {
        // 测试Cors setter和getter
        GatewayProperties.Cors cors = gatewayProperties.getCors();
        cors.setAllowedOrigins("http://localhost:3000");
        cors.setAllowedMethods("GET,POST");
        cors.setAllowedHeaders("Authorization,Content-Type");
        cors.setAllowCredentials(false);
        cors.setMaxAge(7200L);

        assert cors.getAllowedOrigins().equals("http://localhost:3000");
        assert cors.getAllowedMethods().equals("GET,POST");
        assert cors.getAllowedHeaders().equals("Authorization,Content-Type");
        assert cors.isAllowCredentials() == false;
        assert cors.getMaxAge() == 7200L;
    }

    @Test
    void testIgnoreAuthIsIgnored() {
        // 测试路径忽略认证
        GatewayProperties.IgnoreAuth ignoreAuth = gatewayProperties.getIgnoreAuth();
        
        // 空路径列表
        assert ignoreAuth.isIgnored("/api/test") == false;
        
        // 添加忽略路径
        ignoreAuth.setPaths(Arrays.asList("/api/auth", "/api/public"));
        assert ignoreAuth.isIgnored("/api/auth/login") == true;
        assert ignoreAuth.isIgnored("/api/public/data") == true;
        assert ignoreAuth.isIgnored("/api/protected") == false;
    }

    @Test
    void testIgnoreAuthWithNullPaths() {
        // 测试空路径列表
        GatewayProperties.IgnoreAuth ignoreAuth = gatewayProperties.getIgnoreAuth();
        ignoreAuth.setPaths(null);
        assert ignoreAuth.isIgnored("/api/test") == false;
    }
}
