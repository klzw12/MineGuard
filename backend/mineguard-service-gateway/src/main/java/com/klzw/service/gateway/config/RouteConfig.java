package com.klzw.service.gateway.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway 路由配置
 * 使用 @ConfigurationProperties 读取路由列表，动态构建路由
 * 路由配置需要在应用启动时就存在，因此直接写在 yml 中
 */
@Configuration
@ConfigurationProperties(prefix = "mineguard.gateway")
public class RouteConfig {

    private List<RouteDefinition> routes = new ArrayList<>();

    public List<RouteDefinition> getRoutes() {
        return routes;
    }

    public void setRoutes(List<RouteDefinition> routes) {
        this.routes = routes;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        RouteLocatorBuilder.Builder routesBuilder = builder.routes();

        if (routes != null && !routes.isEmpty()) {
            for (RouteDefinition route : routes) {
                routesBuilder.route(route.getId(), r -> {
                    if (route.getStripPrefix() > 0) {
                        return r.path(route.getPath())
                                .filters(f -> f.stripPrefix(route.getStripPrefix()))
                                .uri(route.getUri());
                    }
                    return r.path(route.getPath()).uri(route.getUri());
                });
            }
        }

        return routesBuilder.build();
    }

    public static class RouteDefinition {
        private String id;
        private String path;
        private String uri;
        private int stripPrefix = 0;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public int getStripPrefix() {
            return stripPrefix;
        }

        public void setStripPrefix(int stripPrefix) {
            this.stripPrefix = stripPrefix;
        }
    }
}
