package com.klzw.common.web.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Web 配置属性
 * 通过 @ConfigurationProperties 暴露可配置项，由服务模块或 Nacos 提供实际值
 */
@ConfigurationProperties(prefix = "mineguard.web")
public class WebProperties {

    /**
     * CORS 配置
     */
    private final Cors cors = new Cors();

    /**
     * 文件上传配置
     */
    private final FileUpload fileUpload = new FileUpload();

    public Cors getCors() {
        return cors;
    }

    public FileUpload getFileUpload() {
        return fileUpload;
    }

    /**
     * CORS 配置
     */
    public static class Cors {
        /**
         * 允许的源
         */
        private String allowedOrigins;

        /**
         * 允许的方法
         */
        private String allowedMethods = "GET,POST,PUT,DELETE,OPTIONS";

        /**
         * 允许的头
         */
        private String allowedHeaders = "Content-Type,Authorization";

        /**
         * 是否允许凭证
         */
        private boolean allowCredentials = true;

        /**
         * 预检请求的有效期
         */
        private long maxAge = 3600;

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

    /**
     * 文件上传配置
     */
    public static class FileUpload {
        /**
         * 最大文件大小
         */
        private String maxFileSize = "50MB";

        /**
         * 最大请求大小
         */
        private String maxRequestSize = "100MB";

        /**
         * 文件上传临时目录
         */
        private String location = "${java.io.tmpdir}";

        public String getMaxFileSize() {
            return maxFileSize;
        }

        public void setMaxFileSize(String maxFileSize) {
            this.maxFileSize = maxFileSize;
        }

        public String getMaxRequestSize() {
            return maxRequestSize;
        }

        public void setMaxRequestSize(String maxRequestSize) {
            this.maxRequestSize = maxRequestSize;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }
    }
}