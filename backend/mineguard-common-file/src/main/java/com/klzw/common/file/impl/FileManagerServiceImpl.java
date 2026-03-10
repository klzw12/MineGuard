package com.klzw.common.file.impl;

import com.klzw.common.file.service.StorageService;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Map;

@Service
public class FileManagerServiceImpl {

    private final StorageService storageService;

    public FileManagerServiceImpl(StorageService storageService) {
        this.storageService = storageService;
    }

    public InputStream download(String fileName) {
        return storageService.download(fileName);
    }

    public boolean delete(String fileName) {
        return storageService.delete(fileName);
    }

    public String getUrl(String fileName, long expireSeconds) {
        return storageService.getUrl(fileName, expireSeconds);
    }

    public Map<String, Object> getFileInfo(String fileName) {
        return storageService.getFileInfo(fileName);
    }

    public boolean exists(String fileName) {
        try {
            storageService.getFileInfo(fileName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
