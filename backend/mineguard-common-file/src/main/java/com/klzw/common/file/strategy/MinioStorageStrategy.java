package com.klzw.common.file.strategy;

import com.klzw.common.file.constant.FileResultCode;
import com.klzw.common.file.exception.FileException;
import com.klzw.common.file.properties.MinioProperties;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.http.Method;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class MinioStorageStrategy implements StorageStrategy, DisposableBean {

    private final MinioProperties minioProperties;
    private final MinioClient minioClient;

    public MinioStorageStrategy(MinioProperties minioProperties) {
        this.minioProperties = minioProperties;
        this.minioClient = MinioClient.builder()
                .endpoint(minioProperties.getUrl())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
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
            if (minioProperties.getDefaultBucket() != null && !minioProperties.getDefaultBucket().isEmpty()) {
                bucketNames.add(minioProperties.getDefaultBucket());
            }
            
            // 添加配置的功能桶
            if (minioProperties.getBuckets() != null) {
                MinioProperties.Buckets buckets = minioProperties.getBuckets();
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
                if (!minioClient.bucketExists(io.minio.BucketExistsArgs.builder().bucket(bucketName).build())) {
                    minioClient.makeBucket(io.minio.MakeBucketArgs.builder().bucket(bucketName).build());
                }
            }
        } catch (Exception e) {
            throw new FileException(FileResultCode.STORAGE_BUCKET_INIT_FAILED, "MinIO桶初始化失败", e);
        }
    }

    /**
     * 确保桶存在
     */
    private void ensureBucketExists(String bucketName) {
        try {
            if (!minioClient.bucketExists(io.minio.BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(io.minio.MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            throw new FileException(FileResultCode.STORAGE_BUCKET_INIT_FAILED, "MinIO桶初始化失败", e);
        }
    }
    
    /**
     * 验证文件路径是否符合目录约束
     * 格式：业务类型/用户ID/文件名
     * @param fileName 文件名（包含路径）
     */
    private void validateFilePath(String fileName) {
        String[] pathParts = fileName.split("/");
        if (pathParts.length < 3) {
            throw new FileException(FileResultCode.FILE_PATH_INVALID, "文件路径格式不正确，应为：业务类型/用户ID/文件名");
        }
        
        String businessType = pathParts[0];
        // 验证业务类型是否有效
        List<String> validBusinessTypes = List.of(
                "avatar", "id-card", "driving-license", "vehicle-license", 
                "vehicle-photo", "chat-image", "other"
        );
        if (!validBusinessTypes.contains(businessType)) {
            throw new FileException(FileResultCode.FILE_PATH_INVALID, "无效的业务类型目录");
        }
        
        String userId = pathParts[1];
        if (userId == null || userId.trim().isEmpty()) {
            throw new FileException(FileResultCode.FILE_PATH_INVALID, "用户ID不能为空");
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
            // 验证文件路径
            validateFilePath(fileName);
            
            // 提取模块名称
            String module = extractModuleFromFilePath(fileName);
            // 获取对应的桶名称
            String bucketName = minioProperties.getBucketName(module);
            // 确保桶存在
            ensureBucketExists(bucketName);
            
            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, -1, 10485760) // 10MB
                            .contentType(contentType)
                            .build()
            );

            return fileName;
        } catch (Exception e) {
            if (e instanceof FileException) {
                throw (FileException) e;
            }
            throw new FileException(FileResultCode.FILE_UPLOAD_ERROR, "MinIO上传文件失败", e);
        }
    }

    @Override
    public InputStream download(String fileName) {
        try {
            // 提取模块名称
            String module = extractModuleFromFilePath(fileName);
            // 获取对应的桶名称
            String bucketName = minioProperties.getBucketName(module);
            // 确保桶存在
            ensureBucketExists(bucketName);
            
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
        } catch (Exception e) {
            throw new FileException(FileResultCode.FILE_DOWNLOAD_ERROR, "MinIO下载文件失败", e);
        }
    }

    @Override
    public boolean delete(String fileName) {
        try {
            // 提取模块名称
            String module = extractModuleFromFilePath(fileName);
            // 获取对应的桶名称
            String bucketName = minioProperties.getBucketName(module);
            // 确保桶存在
            ensureBucketExists(bucketName);
            
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            throw new FileException(FileResultCode.FILE_DELETE_ERROR, "MinIO删除文件失败", e);
        }
    }

    @Override
    public String getUrl(String fileName, long expire) {
        try {
            // 提取模块名称
            String module = extractModuleFromFilePath(fileName);
            // 获取对应的桶名称
            String bucketName = minioProperties.getBucketName(module);
            // 确保桶存在
            ensureBucketExists(bucketName);
            
            return minioClient.getPresignedObjectUrl(
                    io.minio.GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(fileName)
                            .expiry((int) expire, TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            throw new FileException(FileResultCode.URL_GENERATE_ERROR, "MinIO获取文件URL失败", e);
        }
    }

    @Override
    public Map<String, Object> getFileInfo(String fileName) {
        try {
            // 提取模块名称
            String module = extractModuleFromFilePath(fileName);
            // 获取对应的桶名称
            String bucketName = minioProperties.getBucketName(module);
            // 确保桶存在
            ensureBucketExists(bucketName);
            
            var stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );

            Map<String, Object> info = new HashMap<>();
            info.put("size", stat.size());
            info.put("contentType", stat.contentType());
            info.put("etag", stat.etag());
            info.put("lastModified", stat.lastModified());
            return info;
        } catch (Exception e) {
            throw new FileException(FileResultCode.FILE_OPERATION_FAILED, "MinIO获取文件信息失败", e);
        }
    }

    @Override
    public void destroy() throws Exception {
        if (minioClient != null) {
            minioClient.close();
        }
    }

    /**
     * 健康检查
     * @return 存储服务是否健康
     */
    public boolean isHealthy() {
        try {
            // 检查默认桶是否存在
            String bucketName = minioProperties.getBucketName("user");
            return minioClient.bucketExists(io.minio.BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            return false;
        }
    }
}