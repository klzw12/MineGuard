package com.klzw.common.file.impl;

import com.klzw.common.file.service.StorageService;
import com.klzw.common.file.strategy.MinioStorageStrategy;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

@Service
public class MinioStorageServiceImpl implements StorageService {

    private final MinioStorageStrategy minioStorageStrategy;

    public MinioStorageServiceImpl(MinioStorageStrategy minioStorageStrategy) {
        this.minioStorageStrategy = minioStorageStrategy;
    }

    @Override
    public String upload(InputStream inputStream, String fileName, String contentType) {
        return minioStorageStrategy.upload(inputStream, fileName, contentType);
    }

    @Override
    public String upload(String module, String folder, InputStream inputStream, String originalFileName, String contentType) {
        // 生成唯一文件名
        String fileExtension = getFileExtension(originalFileName);
        String uniqueFileName = UUID.randomUUID().toString() + (fileExtension != null ? "." + fileExtension : "");
        // 构建文件路径
        String filePath = folder + "/" + uniqueFileName;
        return minioStorageStrategy.upload(inputStream, filePath, contentType);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    @Override
    public InputStream download(String fileName) {
        return minioStorageStrategy.download(fileName);
    }

    @Override
    public boolean delete(String fileName) {
        return minioStorageStrategy.delete(fileName);
    }

    @Override
    public String getUrl(String fileName, long expire) {
        return minioStorageStrategy.getUrl(fileName, expire);
    }

    @Override
    public Map<String, Object> getFileInfo(String fileName) {
        return minioStorageStrategy.getFileInfo(fileName);
    }

    @Override
    public boolean isHealthy() {
        return minioStorageStrategy.isHealthy();
    }
}