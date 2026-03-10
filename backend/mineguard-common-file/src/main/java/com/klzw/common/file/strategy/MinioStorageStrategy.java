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
        initializeBuckets();
    }

    private void initializeBuckets() {
        try {
            List<String> bucketNames = new ArrayList<>();
            
            if (minioProperties.getDefaultBucket() != null && !minioProperties.getDefaultBucket().isEmpty()) {
                bucketNames.add(minioProperties.getDefaultBucket());
            }
            
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
            
            for (String bucketName : bucketNames) {
                if (!minioClient.bucketExists(io.minio.BucketExistsArgs.builder().bucket(bucketName).build())) {
                    minioClient.makeBucket(io.minio.MakeBucketArgs.builder().bucket(bucketName).build());
                }
            }
        } catch (Exception e) {
            throw new FileException(FileResultCode.BUCKET_INIT_FAILED, "MinIO桶初始化失败", e);
        }
    }

    private void ensureBucketExists(String bucketName) {
        try {
            if (!minioClient.bucketExists(io.minio.BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(io.minio.MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            throw new FileException(FileResultCode.BUCKET_INIT_FAILED, "MinIO桶初始化失败", e);
        }
    }
    
    private void validateFilePath(String fileName) {
        String[] pathParts = fileName.split("/");
        if (pathParts.length < 3) {
            throw new FileException(FileResultCode.FILE_PATH_INVALID, "文件路径格式不正确，应为：业务类型/用户ID/文件名");
        }
        
        String businessType = pathParts[0];
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
            validateFilePath(fileName);
            
            String module = extractModuleFromFilePath(fileName);
            String bucketName = minioProperties.getBucketName(module);
            ensureBucketExists(bucketName);
            
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, -1, 10485760)
                            .contentType(contentType)
                            .build()
            );

            return fileName;
        } catch (Exception e) {
            if (e instanceof FileException) {
                throw (FileException) e;
            }
            throw new FileException(FileResultCode.FILE_UPLOAD_FAILED, "MinIO上传文件失败", e);
        }
    }

    @Override
    public InputStream download(String fileName) {
        try {
            String module = extractModuleFromFilePath(fileName);
            String bucketName = minioProperties.getBucketName(module);
            ensureBucketExists(bucketName);
            
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
        } catch (Exception e) {
            throw new FileException(FileResultCode.FILE_DOWNLOAD_FAILED, "MinIO下载文件失败", e);
        }
    }

    @Override
    public boolean delete(String fileName) {
        try {
            String module = extractModuleFromFilePath(fileName);
            String bucketName = minioProperties.getBucketName(module);
            ensureBucketExists(bucketName);
            
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            throw new FileException(FileResultCode.FILE_DELETE_FAILED, "MinIO删除文件失败", e);
        }
    }

    @Override
    public boolean exists(String fileName) {
        try {
            String module = extractModuleFromFilePath(fileName);
            String bucketName = minioProperties.getBucketName(module);
            
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getUrl(String fileName, long expireSeconds) {
        try {
            String module = extractModuleFromFilePath(fileName);
            String bucketName = minioProperties.getBucketName(module);
            ensureBucketExists(bucketName);
            
            return minioClient.getPresignedObjectUrl(
                    io.minio.GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(fileName)
                            .expiry((int) expireSeconds, TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            throw new FileException(FileResultCode.URL_GENERATE_FAILED, "MinIO获取文件URL失败", e);
        }
    }

    @Override
    public Map<String, Object> getFileInfo(String fileName) {
        try {
            String module = extractModuleFromFilePath(fileName);
            String bucketName = minioProperties.getBucketName(module);
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

    @Override
    public boolean isHealthy() {
        try {
            String bucketName = minioProperties.getBucketName("user");
            return minioClient.bucketExists(io.minio.BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            return false;
        }
    }
}
