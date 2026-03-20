package com.klzw.common.file.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "mineguard.file.storage.aliyun-oss")
public class AliyunOssProperties {
    private boolean enabled = false;
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String defaultBucket;
    private Buckets buckets;
    private int defaultExpireTime = 3600;
    private String defaultFolder = "";
    private String domain; // 自定义域名或CDN域名

    @Data
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
    
    /**
     * 获取永久访问URL（不签名）
     * 如果配置了自定义域名则使用自定义域名，否则使用OSS默认域名
     */
    public String getPermanentUrl(String bucketName, String filePath) {
        if (domain != null && !domain.isEmpty()) {
            return domain + "/" + filePath;
        }
        // 使用OSS默认域名格式: https://{bucket}.{endpoint}/{filePath}
        String cleanEndpoint = endpoint.replace("https://", "").replace("http://", "");
        return "https://" + bucketName + "." + cleanEndpoint + "/" + filePath;
    }
}
