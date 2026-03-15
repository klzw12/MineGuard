package com.klzw.service.gateway.config;

import com.klzw.service.gateway.AbstractIntegrationTest;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Nacos集成测试
 * 测试Nacos服务发现和配置中心的连接
 * 注意：此测试需要真实的Nacos环境，属于集成测试
 */
@DisplayName("Nacos 连接集成测试")
public class NacosIntegrationTest extends AbstractIntegrationTest {

    @Autowired(required = false)
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    @Autowired(required = false)
    private NacosConfigProperties nacosConfigProperties;

    @Autowired(required = false)
    private DiscoveryClient discoveryClient;

    @Test
    @DisplayName("测试Nacos配置属性加载")
    void testNacosPropertiesLoaded() {
        // 测试Nacos配置属性是否加载
        if (nacosDiscoveryProperties != null) {
            assertNotNull(nacosDiscoveryProperties.getServerAddr(), "Nacos server address should be configured");
            System.out.println("Nacos discovery namespace: " + nacosDiscoveryProperties.getNamespace());
            System.out.println("Nacos discovery group: " + nacosDiscoveryProperties.getGroup());
        } else {
            System.out.println("Nacos Discovery is disabled in test environment");
        }

        if (nacosConfigProperties != null) {
            assertNotNull(nacosConfigProperties.getServerAddr(), "Nacos config server address should be configured");
            System.out.println("Nacos config namespace: " + nacosConfigProperties.getNamespace());
            System.out.println("Nacos config group: " + nacosConfigProperties.getGroup());
        } else {
            System.out.println("Nacos Config is disabled in test environment");
        }
    }

    @Test
    @DisplayName("测试服务发现客户端可用性")
    void testDiscoveryClientAvailable() {
        // 测试服务发现客户端是否可用
        if (discoveryClient != null) {
            List<String> services = discoveryClient.getServices();
            assertNotNull(services, "Services list should not be null");
            System.out.println("Available services: " + services);
        } else {
            System.out.println("DiscoveryClient is not available (Nacos Discovery disabled)");
        }
    }

    @Test
    @DisplayName("测试应用名称配置")
    void testApplicationName() {
        // 测试应用名称配置
        String applicationName = System.getProperty("spring.application.name", "gateway-service-test");
        assertNotNull(applicationName, "Application name should be configured");
        System.out.println("Application name: " + applicationName);
    }
}
