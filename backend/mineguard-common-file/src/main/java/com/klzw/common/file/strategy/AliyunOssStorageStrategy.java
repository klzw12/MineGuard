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
        // 初始化时检查并创建桶
        initializeBuckets();
    }

    /**
     * 初始化桶，确保所有配置的桶存在
     */
    private void initializeBuckets() {
        try {
            List<String> bucketNames = new ArrayList<>();
            
            // 添加默认桶
            if (aliyunOssProperties.getDefaultBucket() != null && !aliyunOssProperties.getDefaultBucket().isEmpty()) {
                bucketNames.add(aliyunOssProperties.getDefaultBucket());
            }
            
            // 添加配置的功能桶
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
            
            // 检查并创建每个桶
            for (String bucketName : bucketNames) {
                if (!ossClient.doesBucketExist(bucketName)) {
                    ossClient.createBucket(bucketName);
                }
            }
        } catch (Exception e) {
            throw new FileException(FileResultCode.BUCKET_INIT_FAILED, "阿里云OSS桶初始化失败", e);
        }
    }

    /**
     * 确保桶存在
     */
    private void ensureBucketExists(String bucketName) {
        try {
            if (!ossClient.doesBucketExist(bucketName)) {
                ossClient.createBucket(bucketName);
            }
        } catch (Exception e) {
            throw new FileException(FileResultCode.STORAGE_BUCKET_INIT_FAILED, "阿里云OSS桶初始化失败", e);
        }
    }
    
    /**
     * 从文件路径中提取模块名称
     * @param fileName 文件名（包含路径）
     * @return 模块名称
     */
    private String extractModuleFromFilePath(String fileName) {
        // 根据文件路径的业务类型推断模块
        String[] pathParts = fileName.split("/");
        if (pathParts.length >= 1) {
            String businessType = pathParts[0];
            switch (businessType) {
                case "avatar", "id-card":
                    return "user";
                case "chat-image":
                    return "message";
                case "vehicle-photo", "vehicle-license", "driving-license":
                    return "vehicle";
                default:
                    return "ai";
            }
        }
        return "user"; // 默认返回用户模块
    }

    @Override
    public String upload(InputStream inputStream, String fileName, String contentType) {
        try {
            // 提取模块名称
            String module = extractModuleFromFilePath(fileName);
            // 获取对应的桶名称
            String bucketName = aliyunOssProperties.getBucketName(module);
            // 确保桶存在
            ensureBucketExists(bucketName);
            
            // 上传文件
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName,
                    fileName,
                    inputStream
            );
            // 设置内容类型
            putObjectRequest.getMetadata().setContentType(contentType);
            ossClient.putObject(putObjectRequest);

            return fileName;
        } catch (Exception e) {
            throw new FileException(FileResultCode.FILE_UPLOAD_FAILED, "阿里云OSS上传文件失败", e);
        }
    }

    @Override
    public InputStream download(String fileName) {
        try {
            // 提取模块名称
            String module = extractModuleFromFilePath(fileName);
            // 获取对应的桶名称
            String bucketName = aliyunOssProperties.getBucketName(module);
            // 确保桶存在
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
            // 提取模块名称
            String module = extractModuleFromFilePath(fileName);
            // 获取对应的桶名称
            String bucketName = aliyunOssProperties.getBucketName(module);
            // 确保桶存在
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
    public String getUrl(String fileName, long expire) {
        try {
            // 提取模块名称
            String module = extractModuleFromFilePath(fileName);
            // 获取对应的桶名称
            String bucketName = aliyunOssProperties.getBucketName(module);
            // 确保桶存在
            ensureBucketExists(bucketName);
            
            Date expiration = new Date(System.currentTimeMillis() + expire * 1000);
            return ossClient.generatePresignedUrl(
                    bucketName,
                    fileName,
                    expiration
            ).toString();
        } catch (Exception e) {
            throw new FileException(FileResultCode.URL_GENERATE_FAILED, "阿里云OSS获取文件URL失败", e);
        }
    }

    @Override
    public Map<String, Object> getFileInfo(String fileName) {
        try {
            // 提取模块名称
            String module = extractModuleFromFilePath(fileName);
            // 获取对应的桶名称
            String bucketName = aliyunOssProperties.getBucketName(module);
            // 确保桶存在
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

    /**
     * 健康检查
     * @return 存储服务是否健康
     */
    public boolean isHealthy() {
        try {
            // 检查默认桶是否存在
            String bucketName = aliyunOssProperties.getBucketName("user");
            return ossClient.doesBucketExist(bucketName);
        } catch (Exception e) {
            return false;
        }
    }
}