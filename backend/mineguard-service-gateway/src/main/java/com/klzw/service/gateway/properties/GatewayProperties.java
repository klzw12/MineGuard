package com.klzw.service.gateway.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mineguard.gateway")
public class GatewayProperties {

    private final Cors cors = new Cors();
    private final IgnoreAuth ignoreAuth = new IgnoreAuth();
    private boolean enableRateLimit = true;
    private boolean enableCircuitBreaker = true;
    private int rateLimitCount = 1000;
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

    public void setEnableRateLimit(boolean enableRateLimit) {
        this.enableRateLimit = enableRateLimit;
    }

    public boolean isEnableCircuitBreaker() {
        return enableCircuitBreaker;
    }

    public void setEnableCircuitBreaker(boolean enableCircuitBreaker) {
        this.enableCircuitBreaker = enableCircuitBreaker;
    }

    public int getRateLimitCount() {
        return rateLimitCount;
    }

    public void setRateLimitCount(int rateLimitCount) {
        this.rateLimitCount = rateLimitCount;
    }

    public int getRateLimitTime() {
        return rateLimitTime;
    }

    public void setRateLimitTime(int rateLimitTime) {
        this.rateLimitTime = rateLimitTime;
    }

    public static class Cors {
        private String allowedOrigins;
        private String allowedMethods = "GET,POST,PUT,DELETE,OPTIONS";
        private String allowedHeaders = "*";
        private boolean allowCredentials = true;
        private long maxAge = 3600L;

        public String getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(String allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }

        public String getAllowedMethods() {
            return allowedMethods;
        }

        public void setAllowedMethods(String allowedMethods) {
            this.allowedMethods = allowedMethods;
        }

        public String getAllowedHeaders() {
            return allowedHeaders;
        }

        public void setAllowedHeaders(String allowedHeaders) {
            this.allowedHeaders = allowedHeaders;
        }

        public boolean isAllowCredentials() {
            return allowCredentials;
        }

        public void setAllowCredentials(boolean allowCredentials) {
            this.allowCredentials = allowCredentials;
        }

        public long getMaxAge() {
            return maxAge;
        }

        public void setMaxAge(long maxAge) {
            this.maxAge = maxAge;
        }
    }

    public static class IgnoreAuth {
        private java.util.List<String> paths = new java.util.ArrayList<>();

        public java.util.List<String> getPaths() {
            return paths;
        }

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
