package com.klzw.common.file.strategy;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectRequest;
import com.klzw.common.file.constant.FileResultCode;
import com.klzw.common.file.exception.FileException;
import com.klzw.common.file.properties.AliyunOssProperties;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

@Component
public class AliyunOssStorageStrategy implements StorageStrategy, DisposableBean {

    private final AliyunOssProperties aliyunOssProperties;
    private final OSS ossClient;

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
        String[] pathParts = fileName.split("/");
        if (pathParts.length >= 1) {
            String businessType = pathParts[0];
            switch (businessType) {
                case "avatar":
                case "id-card":
                    return "user";
                case "chat-image":
                    return "message";
                case "vehicle-photo":
                case "vehicle-license":
                case "driving-license":
                    return "vehicle";
                default:
                    return "ai";
            }
        }
        return "user";
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
            String module = extractModuleFromFilePath(fileName);
            String bucketName = aliyunOssProperties.getBucketName(module);
            ensureBucketExists(bucketName);
            
            // 如果expireSeconds <= 0，返回永久URL（需要Bucket设置为公共读）
            if (expireSeconds <= 0) {
                return generatePermanentUrl(bucketName, fileName);
            }
            
            Date expiration = new Date(System.currentTimeMillis() + expireSeconds * 1000);
            return ossClient.generatePresignedUrl(
                    bucketName,
                    fileName,
                    expiration
            ).toString();
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
