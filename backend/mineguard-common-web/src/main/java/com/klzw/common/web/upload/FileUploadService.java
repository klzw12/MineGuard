package com.klzw.common.web.upload;

import com.klzw.common.web.properties.WebProperties;
import com.klzw.common.web.constant.WebResultCode;
import com.klzw.common.web.exception.WebException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileUploadService {
    private final WebProperties webProperties;
    private Path uploadPath;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp",
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "txt", "csv", "json", "xml", "zip", "rar"
    );

    private static final List<String> DANGEROUS_EXTENSIONS = Arrays.asList(
            "exe", "bat", "cmd", "sh", "ps1", "vbs", "js", "jar", "class"
    );

    @Autowired
    public FileUploadService(WebProperties webProperties) {
        this.webProperties = webProperties;
        initUploadDirectory();
    }

    private void initUploadDirectory() {
        String location = webProperties.getFileUpload().getLocation();
        location = location.replace("${java.io.tmpdir}", System.getProperty("java.io.tmpdir"));
        this.uploadPath = Paths.get(location, "uploads");
        
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("创建上传目录: {}", uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("初始化上传目录失败: {}", e.getMessage(), e);
            throw new WebException(WebResultCode.FILE_UPLOAD_ERROR.getCode(), "初始化上传目录失败");
        }
    }

    public String uploadFile(MultipartFile file) {
        validateFile(file);
        
        try {
            String fileName = generateFileName(file.getOriginalFilename());
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);
            
            log.info("文件上传成功: {} -> {}", file.getOriginalFilename(), fileName);
            return fileName;
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new WebException(WebResultCode.FILE_UPLOAD_ERROR.getCode(), "文件上传失败: " + e.getMessage(), e);
        }
    }

    public List<String> uploadFiles(List<MultipartFile> files) {
        List<String> fileNames = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                fileNames.add(uploadFile(file));
            }
        }
        return fileNames;
    }

    public void downloadFile(String fileName, HttpServletResponse response) {
        validateFileName(fileName);
        
        Path filePath = uploadPath.resolve(fileName).normalize();
        
        if (!filePath.startsWith(uploadPath)) {
            throw new WebException(WebResultCode.FILE_NOT_FOUND.getCode(), "非法文件路径");
        }
        
        if (!Files.exists(filePath)) {
            throw new WebException(WebResultCode.FILE_NOT_FOUND.getCode(), "文件不存在");
        }

        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(filePath.toFile()));
             OutputStream outputStream = new BufferedOutputStream(response.getOutputStream())) {
            
            String originalFileName = fileName.substring(fileName.indexOf("_") + 1);
            String encodedFileName = URLEncoder.encode(originalFileName, StandardCharsets.UTF_8).replace("+", "%20");
            
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
            response.setContentLengthLong(Files.size(filePath));
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            outputStream.flush();
            log.info("文件下载成功: {}", fileName);
        } catch (IOException e) {
            log.error("文件下载失败: {}", e.getMessage(), e);
            throw new WebException(WebResultCode.FILE_UPLOAD_ERROR.getCode(), "文件下载失败: " + e.getMessage(), e);
        }
    }

    public void deleteFile(String fileName) {
        validateFileName(fileName);
        
        Path filePath = uploadPath.resolve(fileName).normalize();
        
        if (!filePath.startsWith(uploadPath)) {
            throw new WebException(WebResultCode.FILE_NOT_FOUND.getCode(), "非法文件路径");
        }
        
        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("文件删除成功: {}", fileName);
            }
        } catch (IOException e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            throw new WebException(WebResultCode.FILE_UPLOAD_ERROR.getCode(), "文件删除失败: " + e.getMessage(), e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new WebException(WebResultCode.PARAM_MISSING.getCode(), "文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new WebException(WebResultCode.PARAM_MISSING.getCode(), "文件名不能为空");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        
        if (DANGEROUS_EXTENSIONS.contains(extension)) {
            throw new WebException(WebResultCode.FILE_TYPE_NOT_ALLOWED.getCode(), "禁止上传可执行文件: " + extension);
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new WebException(WebResultCode.FILE_TYPE_NOT_ALLOWED.getCode(), "不支持的文件类型: " + extension);
        }

        long fileSize = file.getSize();
        long maxSize = parseSizeToBytes(webProperties.getFileUpload().getMaxFileSize());
        if (fileSize > maxSize) {
            throw new WebException(WebResultCode.FILE_SIZE_EXCEEDED.getCode(), 
                    "文件大小超过限制，最大允许: " + webProperties.getFileUpload().getMaxFileSize());
        }

        String contentType = file.getContentType();
        if (contentType == null || contentType.isEmpty()) {
            throw new WebException(WebResultCode.FILE_TYPE_NOT_ALLOWED.getCode(), "文件类型不能为空");
        }
    }

    private void validateFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new WebException(WebResultCode.PARAM_MISSING.getCode(), "文件名不能为空");
        }
        
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new WebException(WebResultCode.FILE_NOT_FOUND.getCode(), "非法文件名");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private long parseSizeToBytes(String size) {
        if (size == null || size.isEmpty()) {
            return 50 * 1024 * 1024L;
        }
        
        size = size.toUpperCase().trim();
        long multiplier = 1;
        
        if (size.endsWith("GB")) {
            multiplier = 1024L * 1024L * 1024L;
            size = size.substring(0, size.length() - 2);
        } else if (size.endsWith("MB")) {
            multiplier = 1024L * 1024L;
            size = size.substring(0, size.length() - 2);
        } else if (size.endsWith("KB")) {
            multiplier = 1024L;
            size = size.substring(0, size.length() - 2);
        }
        
        try {
            return Long.parseLong(size.trim()) * multiplier;
        } catch (NumberFormatException e) {
            return 50 * 1024 * 1024L;
        }
    }

    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        
        return timestamp + "_" + uuid + extension;
    }

    public File getUploadDir() {
        return uploadPath.toFile();
    }

    public long getDirSize() {
        try {
            return Files.walk(uploadPath)
                    .filter(Files::isRegularFile)
                    .mapToLong(p -> {
                        try {
                            return Files.size(p);
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .sum();
        } catch (IOException e) {
            log.error("计算目录大小失败: {}", e.getMessage(), e);
            return 0;
        }
    }
}
