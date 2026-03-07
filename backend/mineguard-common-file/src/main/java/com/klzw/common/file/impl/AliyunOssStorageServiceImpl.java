package com.klzw.common.file.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.klzw.common.file.constant.FileResultCode;
import com.klzw.common.file.exception.FileException;
import com.klzw.common.file.properties.AliyunOssProperties;
import com.klzw.common.file.service.StorageService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AliyunOssStorageServiceImpl implements StorageService, DisposableBean {

    private final AliyunOssProperties aliyunOssProperties;
    private final OSS ossClient;

    public AliyunOssStorageServiceImpl(AliyunOssProperties aliyunOssProperties) {
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
            List<String> bucketNames = List.of(
                    aliyunOssProperties.getDefaultBucket(),
                    aliyunOssProperties.getBuckets() != null ? aliyunOssProperties.getBuckets().getUser() : null,
                    aliyunOssProperties.getBuckets() != null ? aliyunOssProperties.getBuckets().getMessage() : null,
                    aliyunOssProperties.getBuckets() != null ? aliyunOssProperties.getBuckets().getAi() : null,
                    aliyunOssProperties.getBuckets() != null ? aliyunOssProperties.getBuckets().getVehicle() : null
            );

            for (String bucketName : bucketNames) {
                if (bucketName != null && !bucketName.isEmpty() && !ossClient.doesBucketExist(bucketName)) {
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
            throw new FileException(FileResultCode.BUCKET_INIT_FAILED, "阿里云OSS桶初始化失败", e);
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
            String bucketName = aliyunOssProperties.getBucketName(module);
            // 确保桶存在
            ensureBucketExists(bucketName);

            // 上传文件
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            ossClient.putObject(bucketName, fileName, inputStream, metadata);

            return fileName;
        } catch (Exception e) {
            if (e instanceof FileException) {
                throw (FileException) e;
            }
            throw new FileException(FileResultCode.FILE_UPLOAD_FAILED, "阿里云OSS上传文件失败", e);
        }
    }

    @Override
    public String upload(String module, String folder, InputStream inputStream, String originalFileName, String contentType) {
        // 生成唯一文件名
        String fileExtension = getFileExtension(originalFileName);
        String uniqueFileName = UUID.randomUUID().toString() + (fileExtension != null ? "." + fileExtension : "");
        // 构建文件路径
        String filePath = folder + "/" + uniqueFileName;
        return upload(inputStream, filePath, contentType);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
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

            OSSObject ossObject = ossClient.getObject(bucketName, fileName);
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

            ossClient.deleteObject(bucketName, fileName);
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

            // 生成签名URL
            Date expiration = new Date(System.currentTimeMillis() + expire * 1000);
            return ossClient.generatePresignedUrl(bucketName, fileName, expiration).toString();
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

            ObjectMetadata metadata = ossClient.getObjectMetadata(bucketName, fileName);

            Map<String, Object> info = new HashMap<>();
            info.put("size", metadata.getContentLength());
            info.put("contentType", metadata.getContentType());
            info.put("etag", metadata.getETag());
            info.put("lastModified", metadata.getLastModified());
            return info;
        } catch (Exception e) {
            throw new FileException(FileResultCode.FILE_OPERATION_FAILED, "阿里云OSS获取文件信息失败", e);
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            // 检查默认桶是否存在
            String bucketName = aliyunOssProperties.getBucketName("user");
            return ossClient.doesBucketExist(bucketName);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void destroy() throws Exception {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }
}
