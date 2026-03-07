package com.klzw.common.file.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "aliyun.oss")
public class AliyunOssProperties {
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String defaultBucket;
    private Buckets buckets;
    private int defaultExpireTime = 3600;
    private String defaultFolder = "";
    private boolean enabled = false;
    
    @Getter
    @Setter
    public static class Buckets {
        private String user;
        private String message;
        private String ai;
        private String vehicle;
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
