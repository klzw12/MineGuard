package com.klzw.common.file.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "mineguard.file.storage.minio")
public class MinioProperties {
    private boolean enabled = false;
    private String url;
    private String accessKey;
    private String secretKey;
    private String defaultBucket;
    private Buckets buckets;
    private int defaultExpireTime = 3600;
    private String defaultFolder = "";

    @Data
    public static class Buckets {
        private String user;
        private String message;
        private String ai;
        private String vehicle;
    }

    public String getEndpoint() {
        return url;
    }

    public String getBucketName(String module) {
        if (buckets == null) {
            return defaultBucket;
        }
        switch (module) {
            case "user":
                return buckets.getUser() != null ? buckets.getUser() : defaultBucket;
            case "message":
                return buckets.getMessage() != null ? buckets.getMessage() : defaultBucket;
            case "ai":
                return buckets.getAi() != null ? buckets.getAi() : defaultBucket;
            case "vehicle":
                return buckets.getVehicle() != null ? buckets.getVehicle() : defaultBucket;
            default:
                return defaultBucket;
        }
    }
}
