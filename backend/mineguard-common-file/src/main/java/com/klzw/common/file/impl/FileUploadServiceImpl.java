package com.klzw.common.file.impl;

import com.klzw.common.file.constant.FileResultCode;
import com.klzw.common.file.enums.FileBusinessTypeEnum;
import com.klzw.common.file.exception.FileException;
import com.klzw.common.file.properties.FileStorageProperties;
import com.klzw.common.file.service.StorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadServiceImpl {

    private final StorageService storageService;
    private final FileStorageProperties fileStorageProperties;

    public FileUploadServiceImpl(StorageService storageService, FileStorageProperties fileStorageProperties) {
        this.storageService = storageService;
        this.fileStorageProperties = fileStorageProperties;
    }

    public String upload(MultipartFile file) throws IOException {
        return upload(file, FileBusinessTypeEnum.OTHER, null);
    }

    public String upload(MultipartFile file, FileBusinessTypeEnum businessType) throws IOException {
        return upload(file, businessType, null);
    }

    public String upload(MultipartFile file, FileBusinessTypeEnum businessType, String userId) throws IOException {
        validateFile(file);
        
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFileName = UUID.randomUUID().toString() + (fileExtension != null ? "." + fileExtension : "");
        
        String folder = businessType.getFolder();
        String filePath = userId != null ? 
                folder + "/" + userId + "/" + uniqueFileName :
                folder + "/" + uniqueFileName;
        
        try (InputStream inputStream = file.getInputStream()) {
            return storageService.upload(inputStream, filePath, file.getContentType());
        }
    }

    public String upload(MultipartFile file, int businessTypeCode, String userId) throws IOException {
        return upload(file, FileBusinessTypeEnum.fromCode(businessTypeCode), userId);
    }

    /**
     * 获取文件的签名URL
     * @param filePath 文件路径
     * @param expireSeconds 过期时间（秒）
     * @return 签名URL
     */
    public String getSignedUrl(String filePath, long expireSeconds) {
        return storageService.getUrl(filePath, expireSeconds);
    }
    
    /**
     * 获取文件的永久访问URL（需要OSS Bucket设置为公共读）
     * @param filePath 文件路径
     * @return 永久URL
     */
    public String getPermanentUrl(String filePath) {
        return storageService.getPermanentUrl(filePath);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileException(FileResultCode.FILE_OPERATION_FAILED, "文件不能为空");
        }
        
        if (file.getSize() > fileStorageProperties.getMaxFileSize()) {
            throw new FileException(FileResultCode.FILE_SIZE_EXCEEDED, "文件大小超出限制");
        }
        
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        if (extension != null) {
            List<String> allowedExtensions = Arrays.asList(
                    fileStorageProperties.getAllowedExtensions().split(",")
            );
            if (!allowedExtensions.contains(extension.toLowerCase())) {
                throw new FileException(FileResultCode.FILE_TYPE_NOT_ALLOWED, "文件类型不允许");
            }
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }
}
