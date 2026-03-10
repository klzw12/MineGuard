package com.klzw.common.file.impl;

import com.klzw.common.file.service.StorageService;
import com.klzw.common.file.strategy.AliyunOssStorageStrategy;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

@Service
public class AliyunOssStorageServiceImpl implements StorageService {

    private final AliyunOssStorageStrategy aliyunOssStorageStrategy;

    public AliyunOssStorageServiceImpl(AliyunOssStorageStrategy aliyunOssStorageStrategy) {
        this.aliyunOssStorageStrategy = aliyunOssStorageStrategy;
    }

    @Override
    public String upload(InputStream inputStream, String fileName, String contentType) {
        return aliyunOssStorageStrategy.upload(inputStream, fileName, contentType);
    }

    @Override
    public String upload(String module, String folder, InputStream inputStream, String originalFileName, String contentType) {
        // 生成唯一文件名
        String fileExtension = getFileExtension(originalFileName);
        String uniqueFileName = UUID.randomUUID().toString() + (fileExtension != null ? "." + fileExtension : "");
        // 构建文件路径
        String filePath = folder + "/" + uniqueFileName;
        return aliyunOssStorageStrategy.upload(inputStream, filePath, contentType);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    @Override
    public InputStream download(String fileName) {
        return aliyunOssStorageStrategy.download(fileName);
    }

    @Override
    public boolean delete(String fileName) {
        return aliyunOssStorageStrategy.delete(fileName);
    }

    @Override
    public String getUrl(String fileName, long expire) {
        return aliyunOssStorageStrategy.getUrl(fileName, expire);
    }

    @Override
    public Map<String, Object> getFileInfo(String fileName) {
        return aliyunOssStorageStrategy.getFileInfo(fileName);
    }

    @Override
    public boolean isHealthy() {
        return aliyunOssStorageStrategy.isHealthy();
    }
}
