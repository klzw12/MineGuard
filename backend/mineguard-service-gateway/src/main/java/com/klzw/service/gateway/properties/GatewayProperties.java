package com.klzw.service.gateway.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mineguard.gateway")
public class GatewayProperties {

    private final Cors cors = new Cors();
    private final IgnoreAuth ignoreAuth = new IgnoreAuth();
    @Setter
    private boolean enableRateLimit = true;
    @Setter
    private boolean enableCircuitBreaker = true;
    @Setter
    private int rateLimitCount = 1000;
    @Setter
    private int rateLimitTime = 60;

    public Cors getCors() {
        return cors;
    }

    public IgnoreAuth getIgnoreAuth() {
        return ignoreAuth;
    }

    public boolean isEnableRateLimit() {
        return enableRateLimit;
    }

    public boolean isEnableCircuitBreaker() {
        return enableCircuitBreaker;
    }

    public int getRateLimitCount() {
        return rateLimitCount;
    }

    public int getRateLimitTime() {
        return rateLimitTime;
    }

    @Getter
    public static class Cors {
        private String allowedOrigins;
        private String allowedMethods = "GET,POST,PUT,DELETE,OPTIONS";
        private String allowedHeaders = "*";
        private boolean allowCredentials = true;
        private long maxAge = 3600L;

        public void setAllowedOrigins(String allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }

        public void setAllowedMethods(String allowedMethods) {
            this.allowedMethods = allowedMethods;
        }

        public void setAllowedHeaders(String allowedHeaders) {
            this.allowedHeaders = allowedHeaders;
        }

        public void setAllowCredentials(boolean allowCredentials) {
            this.allowCredentials = allowCredentials;
        }

        public void setMaxAge(long maxAge) {
            this.maxAge = maxAge;
        }
    }

    @Getter
    public static class IgnoreAuth {
        private java.util.List<String> paths = new java.util.ArrayList<>();

        public void setPaths(java.util.List<String> paths) {
            this.paths = paths;
        }

        public boolean isIgnored(String path) {
            if (paths == null || paths.isEmpty()) {
                return false;
            }
            return paths.stream().anyMatch(path::startsWith);
        }
    }
}
