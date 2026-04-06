package com.klzw.common.file.strategy;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectRequest;
import com.klzw.common.file.constant.FileResultCode;
import com.klzw.common.file.exception.FileException;
import com.klzw.common.file.properties.AliyunOssProperties;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

@Slf4j
@Component
public class AliyunOssStorageStrategy implements StorageStrategy, DisposableBean {

    private final AliyunOssProperties aliyunOssProperties;
    private final OSS ossClient;
    
    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;
    
    private static final String OSS_URL_CACHE_PREFIX = "oss:url:";
    private static final long URL_CACHE_EXPIRE_MINUTES = 30; // 缓存30分钟，比实际过期时间短一些

    public AliyunOssStorageStrategy(AliyunOssProperties aliyunOssProperties) {
        this.aliyunOssProperties = aliyunOssProperties;
        this.ossClient = new OSSClientBuilder().build(
                aliyunOssProperties.getEndpoint(),
                aliyunOssProperties.getAccessKeyId(),
                aliyunOssProperties.getAccessKeySecret()
        );
        initializeBuckets();
    }

    private void initializeBuckets() {
        try {
            List<String> bucketNames = new ArrayList<>();
            
            if (aliyunOssProperties.getDefaultBucket() != null && !aliyunOssProperties.getDefaultBucket().isEmpty()) {
                bucketNames.add(aliyunOssProperties.getDefaultBucket());
            }
            
            if (aliyunOssProperties.getBuckets() != null) {
                AliyunOssProperties.Buckets buckets = aliyunOssProperties.getBuckets();
                if (buckets.getUser() != null && !buckets.getUser().isEmpty()) {
                    bucketNames.add(buckets.getUser());
                }
                if (buckets.getMessage() != null && !buckets.getMessage().isEmpty()) {
                    bucketNames.add(buckets.getMessage());
                }
                if (buckets.getAi() != null && !buckets.getAi().isEmpty()) {
                    bucketNames.add(buckets.getAi());
                }
                if (buckets.getVehicle() != null && !buckets.getVehicle().isEmpty()) {
                    bucketNames.add(buckets.getVehicle());
                }
            }
            
            for (String bucketName : bucketNames) {
                if (!ossClient.doesBucketExist(bucketName)) {
                    ossClient.createBucket(bucketName);
                }
            }
        } catch (Exception e) {
            throw new FileException(FileResultCode.BUCKET_INIT_FAILED, "阿里云OSS桶初始化失败", e);
        }
    }

    private void ensureBucketExists(String bucketName) {
        try {
            if (!ossClient.doesBucketExist(bucketName)) {
                ossClient.createBucket(bucketName);
            }
        } catch (Exception e) {
            throw new FileException(FileResultCode.BUCKET_INIT_FAILED, "阿里云OSS桶初始化失败", e);
        }
    }
    
    private String extractModuleFromFilePath(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "user";
        }
        
        String lowerFileName = fileName.toLowerCase();
        
        // 用户模块：头像、身份证、驾驶证、资质证书等
        if (lowerFileName.startsWith("avatar/") || 
            lowerFileName.startsWith("id-card") || 
            lowerFileName.startsWith("idcard") ||
            lowerFileName.startsWith("driving-license") || 
            lowerFileName.startsWith("driving_license") ||
            lowerFileName.contains("qualification") ||
            lowerFileName.contains("cert") ||
            lowerFileName.contains("emergency")) {
            return "user";
        }
        
        // 消息模块：聊天图片等
        if (lowerFileName.startsWith("chat-image") || 
            lowerFileName.startsWith("chat") ||
            lowerFileName.startsWith("message")) {
            return "message";
        }
        
        // 车辆模块：车辆照片、行驶证、车牌照等
        if (lowerFileName.startsWith("vehicle-photo") || 
            lowerFileName.startsWith("vehicle_photo") ||
            lowerFileName.startsWith("vehicle-license") || 
            lowerFileName.startsWith("vehicle_license") ||
            lowerFileName.startsWith("vehicle") ||
            lowerFileName.contains("plate")) {
            return "vehicle";
        }
        
        // AI报告模块（默认）
        return "ai";
    }

    @Override
    public String upload(InputStream inputStream, String fileName, String contentType) {
        try {
            String module = extractModuleFromFilePath(fileName);
            String bucketName = aliyunOssProperties.getBucketName(module);
            ensureBucketExists(bucketName);
            
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName,
                    fileName,
                    inputStream
            );
            // 创建并设置metadata
            com.aliyun.oss.model.ObjectMetadata metadata = new com.aliyun.oss.model.ObjectMetadata();
            metadata.setContentType(contentType);
            putObjectRequest.setMetadata(metadata);
            ossClient.putObject(putObjectRequest);

            return fileName;
        } catch (Exception e) {
            throw new FileException(FileResultCode.FILE_UPLOAD_FAILED, "阿里云OSS上传文件失败", e);
        }
    }

    @Override
    public InputStream download(String fileName) {
        try {
            String module = extractModuleFromFilePath(fileName);
            String bucketName = aliyunOssProperties.getBucketName(module);
            ensureBucketExists(bucketName);
            
            OSSObject ossObject = ossClient.getObject(
                    bucketName,
                    fileName
            );
            return ossObject.getObjectContent();
        } catch (Exception e) {
            throw new FileException(FileResultCode.FILE_DOWNLOAD_FAILED, "阿里云OSS下载文件失败", e);
        }
    }

    @Override
    public boolean delete(String fileName) {
        try {
            String module = extractModuleFromFilePath(fileName);
            String bucketName = aliyunOssProperties.getBucketName(module);
            ensureBucketExists(bucketName);
            
            ossClient.deleteObject(
                    bucketName,
                    fileName
            );
            return true;
        } catch (Exception e) {
            throw new FileException(FileResultCode.FILE_DELETE_FAILED, "阿里云OSS删除文件失败", e);
        }
    }

    @Override
    public boolean exists(String fileName) {
        try {
            String module = extractModuleFromFilePath(fileName);
            String bucketName = aliyunOssProperties.getBucketName(module);
            return ossClient.doesObjectExist(bucketName, fileName);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getUrl(String fileName, long expireSeconds) {
        try {
            String cacheKey = OSS_URL_CACHE_PREFIX + fileName;
            
            // 1. 先从Redis缓存获取（如果Redis可用）
            if (stringRedisTemplate != null) {
                try {
                    String cachedUrl = stringRedisTemplate.opsForValue().get(cacheKey);
                    if (cachedUrl != null && !cachedUrl.isEmpty()) {
                        return cachedUrl;
                    }
                } catch (Exception e) {
                    log.warn("从Redis缓存获取URL失败，将直接生成: {}", e.getMessage());
                }
            }
            
            // 2. 缓存未命中或不可用，直接调用OSS生成URL
            String module = extractModuleFromFilePath(fileName);
            String bucketName = aliyunOssProperties.getBucketName(module);
            ensureBucketExists(bucketName);
            
            String url;
            // 如果expireSeconds <= 0，返回永久URL（需要Bucket设置为公共读）
            if (expireSeconds <= 0) {
                url = generatePermanentUrl(bucketName, fileName);
            } else {
                Date expiration = new Date(System.currentTimeMillis() + expireSeconds * 1000);
                url = ossClient.generatePresignedUrl(
                        bucketName,
                        fileName,
                        expiration
                ).toString();
            }
            
            // 3. 将URL和过期时间写入Redis缓存（如果Redis可用）
            if (stringRedisTemplate != null) {
                try {
                    long cacheExpireSeconds = Math.min(expireSeconds, URL_CACHE_EXPIRE_MINUTES * 60);
                    if (cacheExpireSeconds > 0) {
                        stringRedisTemplate.opsForValue().set(cacheKey, url, Duration.ofSeconds(cacheExpireSeconds));
                    } else {
                        // 永久URL缓存更长时间
                        stringRedisTemplate.opsForValue().set(cacheKey, url, Duration.ofHours(24));
                    }
                } catch (Exception e) {
                    log.warn("写入Redis缓存失败: {}", e.getMessage());
                }
            }
            
            return url;
        } catch (Exception e) {
            throw new FileException(FileResultCode.URL_GENERATE_FAILED, "阿里云OSS获取文件URL失败", e);
        }
    }

    /**
     * 生成永久访问URL（需要Bucket设置为公共读）
     */
    private String generatePermanentUrl(String bucketName, String fileName) {
        String endpoint = aliyunOssProperties.getEndpoint();
        // 移除协议前缀
        if (endpoint.startsWith("https://")) {
            endpoint = endpoint.substring(8);
        } else if (endpoint.startsWith("http://")) {
            endpoint = endpoint.substring(7);
        }
        return "https://" + bucketName + "." + endpoint + "/" + fileName;
    }

    @Override
    public String getPermanentUrl(String fileName) {
        String module = extractModuleFromFilePath(fileName);
        String bucketName = aliyunOssProperties.getBucketName(module);
        return generatePermanentUrl(bucketName, fileName);
    }

    @Override
    public Map<String, Object> getFileInfo(String fileName) {
        try {
            String module = extractModuleFromFilePath(fileName);
            String bucketName = aliyunOssProperties.getBucketName(module);
            ensureBucketExists(bucketName);
            
            OSSObject ossObject = ossClient.getObject(
                    bucketName,
                    fileName
            );

            Map<String, Object> info = new HashMap<>();
            info.put("size", ossObject.getObjectMetadata().getContentLength());
            info.put("contentType", ossObject.getObjectMetadata().getContentType());
            info.put("etag", ossObject.getObjectMetadata().getETag());
            info.put("lastModified", ossObject.getObjectMetadata().getLastModified());
            return info;
        } catch (Exception e) {
            throw new FileException(FileResultCode.FILE_OPERATION_FAILED, "阿里云OSS获取文件信息失败", e);
        }
    }

    @Override
    public void destroy() throws Exception {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            String bucketName = aliyunOssProperties.getBucketName("user");
            return ossClient.doesBucketExist(bucketName);
        } catch (Exception e) {
            return false;
        }
    }
}
