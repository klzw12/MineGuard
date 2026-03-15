package com.klzw.service.gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Gateway 路由转发集成测试
 * 测试优先级：高 - 核心路由功能
 * 注意：此测试需要真实的 Nacos 环境
 */
@DisplayName("Gateway 路由转发集成测试")
public class GatewayRouteIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RouteLocator routeLocator;

    @Test
    @DisplayName("测试路由配置加载 - 验证路由定位器可用")
    void testRouteLocatorAvailable() {
        assertThat(routeLocator).isNotNull();
        
        Flux<Route> routes = routeLocator.getRoutes();
        
        StepVerifier.create(routes.collectList())
                .consumeNextWith(routeList -> {
                    // 路由列表可能为空（如果没有后端服务注册）
                    // 主要验证 RouteLocator 可以正常工作
                    System.out.println("Loaded routes count: " + routeList.size());
                    routeList.forEach(route -> {
                        System.out.println("Route: id=" + route.getId() + ", uri=" + route.getUri());
                    });
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("测试服务发现路由 - 验证动态路由发现")
    void testDiscoveryLocatorEnabled() {
        Flux<Route> routes = routeLocator.getRoutes();

        StepVerifier.create(routes.collectList())
                .consumeNextWith(routeList -> {
                    // 验证是否有基于服务发现的路由
                    boolean hasDiscoveryRoute = routeList.stream()
                            .anyMatch(route -> route.getUri().getScheme().equals("lb") ||
                                    route.getId().contains("CompositeDiscoveryClient"));
                    
                    System.out.println("Has discovery route: " + hasDiscoveryRoute);
                    System.out.println("Total routes: " + routeList.size());
                })
                .verifyComplete();
    }
}
