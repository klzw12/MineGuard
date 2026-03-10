package com.klzw.common.file.impl;

import com.klzw.common.file.service.StorageService;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Map;

@Service
public class FileDownloadServiceImpl {

    private final StorageService storageService;

    public FileDownloadServiceImpl(StorageService storageService) {
        this.storageService = storageService;
    }

    public InputStream download(String fileName) {
        return storageService.download(fileName);
    }

    public String getDownloadUrl(String fileName, long expireSeconds) {
        return storageService.getUrl(fileName, expireSeconds);
    }

    public Map<String, Object> getFileInfo(String fileName) {
        return storageService.getFileInfo(fileName);
    }

    public long getFileSize(String fileName) {
        Map<String, Object> info = storageService.getFileInfo(fileName);
        Object size = info.get("size");
        if (size instanceof Long) {
            return (Long) size;
        }
        return 0L;
    }

    public String getContentType(String fileName) {
        Map<String, Object> info = storageService.getFileInfo(fileName);
        Object contentType = info.get("contentType");
        return contentType != null ? contentType.toString() : "application/octet-stream";
    }
}
