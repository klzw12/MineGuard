package com.klzw.common.file.impl;

import com.klzw.common.file.service.StorageService;
import com.klzw.common.file.exception.FileException;
import com.klzw.common.file.constant.FileResultCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

/**
 * 双写存储服务实现类
 * 同时将数据写入MinIO和阿里云OSS，实现数据冗余和高可用
 */
@Service("dualWriteStorageService")
@Slf4j
@EnableScheduling
public class DualWriteStorageServiceImpl implements StorageService, InitializingBean {

    private final MinioStorageServiceImpl minioStorageService;
    private final AliyunOssStorageServiceImpl aliyunOssStorageService;

    /**
     * 构造函数注入依赖
     */
    public DualWriteStorageServiceImpl(MinioStorageServiceImpl minioStorageService, AliyunOssStorageServiceImpl aliyunOssStorageService) {
        this.minioStorageService = minioStorageService;
        this.aliyunOssStorageService = aliyunOssStorageService;
    }

    // 存储服务健康状态
    @Getter
    private boolean minioHealthy = true;

    @Getter
    private boolean ossHealthy = true;
    
    // 健康检查缓存相关
    private long lastHealthCheckTime = 0;
    private static final long HEALTH_CHECK_INTERVAL = 60000; // 健康检查间隔，单位：毫秒，默认1分钟
    
    // 当前主存储服务类型
    private enum PrimaryStorageType {
        MINIO, OSS
    }
    
    // 当前主存储服务
    private PrimaryStorageType primaryStorage = PrimaryStorageType.MINIO;

    /**
     * 初始化存储服务
     */
    @Override
    public void afterPropertiesSet() {
        log.info("初始化双写存储服务");
        
        // 检查主存储服务是否健康，不健康则切换
        checkAndSwitchPrimaryStorage();
        
        log.info("双写存储服务初始化完成，当前主存储服务: {}", primaryStorage);
    }
    
    /**
     * 检查并切换主存储服务
     */
    private void checkAndSwitchPrimaryStorage() {
        // 先检查两个存储服务的健康状态
        checkStorageHealth();
        
        // 如果当前主存储服务不健康，切换到健康的存储服务
        if ((primaryStorage == PrimaryStorageType.MINIO && !minioHealthy) ||
            (primaryStorage == PrimaryStorageType.OSS && !ossHealthy)) {
            
            // 切换到健康的存储服务
            if (ossHealthy) {
                primaryStorage = PrimaryStorageType.OSS;
                log.info("主存储服务 切换到OSS");
            } else if (minioHealthy) {
                primaryStorage = PrimaryStorageType.MINIO;
                log.info("主存储服务 切换到MinIO");
            } else {
                log.error("所有存储服务都不健康，无法切换主存储服务");
            }
        }
    }
    
    /**
     * 检查存储服务健康状态
     */
    private void checkStorageHealth() {
        // 检查是否需要执行健康检查（基于缓存时间）
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastHealthCheckTime < HEALTH_CHECK_INTERVAL) {
            // 未到检查间隔，直接返回
            return;
        }
        
        // 检查MinIO健康状态
        try {
            minioHealthy = minioStorageService.isHealthy();
            if (!minioHealthy) {
                log.warn("MinIO存储服务健康检查失败");
            }
        } catch (Exception e) {
            minioHealthy = false;
            log.warn("MinIO存储服务健康检查异常: {}", e.getMessage());
        }
        
        // 检查OSS健康状态
        try {
            ossHealthy = aliyunOssStorageService.isHealthy();
            if (!ossHealthy) {
                log.warn("OSS存储服务健康检查失败");
            }
        } catch (Exception e) {
            ossHealthy = false;
            log.warn("OSS存储服务健康检查异常: {}", e.getMessage());
        }
        
        // 更新最后检查时间
        lastHealthCheckTime = currentTime;
    }
    
    /**
     * 健康检查
     * @return 存储服务是否健康
     */
    public boolean isHealthy() {
        // 至少一个存储服务健康就算健康
        checkStorageHealth();
        return minioHealthy || ossHealthy;
    }

    /**
     * 获取当前主存储服务类型
     * @return 主存储服务类型
     */
    public String getPrimaryStorage() {
        return primaryStorage.name();
    }
    
    /**
     * 强制切换主存储服务
     * @param primaryStorage 主存储服务类型
     */
    public void setPrimaryStorage(String primaryStorage) {
        try {
            this.primaryStorage = PrimaryStorageType.valueOf(primaryStorage.toUpperCase());
            log.info("主存储服务强制切换到: {}", this.primaryStorage);
        } catch (IllegalArgumentException e) {
            log.error("无效的主存储服务类型: {}", primaryStorage);
        }
    }

    /**
     * 上传文件
     * 同时上传到MinIO和OSS
     * @param inputStream 文件输入流
     * @param fileName 文件名
     * @param contentType 内容类型
     * @return 文件路径
     */
    @Override
    public String upload(InputStream inputStream, String fileName, String contentType) {
        log.info("开始双写上传文件，文件名: {}, 当前主存储: {}", fileName, primaryStorage);
        
        // 使用ByteArrayOutputStream缓存输入流内容
        byte[] fileBytes = null;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024 * 1024]; // 1MB buffer
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            bos.flush();
            fileBytes = bos.toByteArray();
        } catch (Exception e) {
            log.error("读取输入流失败: {}", e.getMessage());
            throw new FileException(FileResultCode.FILE_UPLOAD_ERROR, "读取输入流失败", e);
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
                log.warn("关闭输入流失败: {}", e.getMessage());
            }
        }
        
        // 先上传到主存储服务
        String primaryFilePath = null;
        boolean primaryUploadSuccess = false;
        
        try (ByteArrayInputStream primaryBais = new ByteArrayInputStream(fileBytes)) {
            if (primaryStorage == PrimaryStorageType.MINIO) {
                primaryFilePath = minioStorageService.upload(primaryBais, fileName, contentType);
                minioHealthy = true;
            } else {
                primaryFilePath = aliyunOssStorageService.upload(primaryBais, fileName, contentType);
                ossHealthy = true;
            }
            primaryUploadSuccess = true;
            log.info("主存储服务文件上传成功: {}", primaryFilePath);
        } catch (Exception e) {
            log.error("主存储服务文件上传失败: {}", e.getMessage());
            // 更新主存储服务健康状态
            if (primaryStorage == PrimaryStorageType.MINIO) {
                minioHealthy = false;
            } else {
                ossHealthy = false;
            }
            // 切换主存储服务
            checkAndSwitchPrimaryStorage();
        }

        // 再上传到备用存储服务
        String backupFilePath = null;
        boolean backupUploadSuccess = false;
        
        try (ByteArrayInputStream backupBais = new ByteArrayInputStream(fileBytes)) {
            if (primaryStorage == PrimaryStorageType.MINIO) {
                // 主存储是MinIO，备用是OSS
                backupFilePath = aliyunOssStorageService.upload(backupBais, fileName, contentType);
                ossHealthy = true;
            } else {
                // 主存储是OSS，备用是MinIO
                backupFilePath = minioStorageService.upload(backupBais, fileName, contentType);
                minioHealthy = true;
            }
            backupUploadSuccess = true;
            log.info("备用存储服务文件上传成功: {}", backupFilePath);
        } catch (Exception e) {
            log.error("备用存储服务文件上传失败: {}", e.getMessage());
            // 更新备用存储服务健康状态
            if (primaryStorage == PrimaryStorageType.MINIO) {
                ossHealthy = false;
            } else {
                minioHealthy = false;
            }
        }

        // 检查上传结果
        if (!primaryUploadSuccess && !backupUploadSuccess) {
            // 两者都失败，抛出异常
            throw new FileException(FileResultCode.FILE_UPLOAD_ERROR, "双写文件上传失败");
        }

        // 返回主存储的文件路径作为统一返回值，如果主存储失败则返回备用存储的
        String resultFilePath = primaryUploadSuccess ? primaryFilePath : backupFilePath;
        log.info("双写文件上传完成，结果: {}", resultFilePath);
        return resultFilePath;
    }

    @Override
    public String upload(String module, String folder, InputStream inputStream, String originalFileName, String contentType) {
        log.info("开始双写上传文件，模块: {}, 文件夹: {}, 文件名: {}, 当前主存储: {}", module, folder, originalFileName, primaryStorage);
        
        // 使用ByteArrayOutputStream缓存输入流内容
        byte[] fileBytes = null;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024 * 1024]; // 1MB buffer
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            bos.flush();
            fileBytes = bos.toByteArray();
        } catch (Exception e) {
            log.error("读取输入流失败: {}", e.getMessage());
            throw new FileException(FileResultCode.FILE_UPLOAD_ERROR, "读取输入流失败", e);
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
                log.warn("关闭输入流失败: {}", e.getMessage());
            }
        }
        
        // 先上传到主存储服务
        String primaryFilePath = null;
        boolean primaryUploadSuccess = false;
        
        try (ByteArrayInputStream primaryBais = new ByteArrayInputStream(fileBytes)) {
            if (primaryStorage == PrimaryStorageType.MINIO) {
                primaryFilePath = minioStorageService.upload(module, folder, primaryBais, originalFileName, contentType);
                minioHealthy = true;
            } else {
                primaryFilePath = aliyunOssStorageService.upload(module, folder, primaryBais, originalFileName, contentType);
                ossHealthy = true;
            }
            primaryUploadSuccess = true;
            log.info("主存储服务文件上传成功: {}", primaryFilePath);
        } catch (Exception e) {
            log.error("主存储服务文件上传失败: {}", e.getMessage());
            // 更新主存储服务健康状态
            if (primaryStorage == PrimaryStorageType.MINIO) {
                minioHealthy = false;
            } else {
                ossHealthy = false;
            }
            // 切换主存储服务
            checkAndSwitchPrimaryStorage();
        }

        // 再上传到备用存储服务
        String backupFilePath = null;
        boolean backupUploadSuccess = false;
        
        try (ByteArrayInputStream backupBais = new ByteArrayInputStream(fileBytes)) {
            if (primaryStorage == PrimaryStorageType.MINIO) {
                // 主存储是MinIO，备用是OSS
                backupFilePath = aliyunOssStorageService.upload(module, folder, backupBais, originalFileName, contentType);
                ossHealthy = true;
            } else {
                // 主存储是OSS，备用是MinIO
                backupFilePath = minioStorageService.upload(module, folder, backupBais, originalFileName, contentType);
                minioHealthy = true;
            }
            backupUploadSuccess = true;
            log.info("备用存储服务文件上传成功: {}", backupFilePath);
        } catch (Exception e) {
            log.error("备用存储服务文件上传失败: {}", e.getMessage());
            // 更新备用存储服务健康状态
            if (primaryStorage == PrimaryStorageType.MINIO) {
                ossHealthy = false;
            } else {
                minioHealthy = false;
            }
        }

        // 检查上传结果
        if (!primaryUploadSuccess && !backupUploadSuccess) {
            // 两者都失败，抛出异常
            throw new FileException(FileResultCode.FILE_UPLOAD_ERROR, "双写文件上传失败");
        }

        // 返回主存储的文件路径作为统一返回值，如果主存储失败则返回备用存储的
        String resultFilePath = primaryUploadSuccess ? primaryFilePath : backupFilePath;
        log.info("双写文件上传完成，结果: {}", resultFilePath);
        return resultFilePath;
    }

    /**
     * 下载文件
     * 优先从主存储服务下载，失败则从备用存储服务下载
     * @param fileName 文件名
     * @return 文件输入流
     */
    @Override
    public InputStream download(String fileName) {
        log.info("开始下载文件，文件名: {}, 当前主存储: {}", fileName, primaryStorage);
        
        return executeWithFallback(
            () -> {
                if (primaryStorage == PrimaryStorageType.MINIO) {
                    return minioStorageService.download(fileName);
                } else {
                    return aliyunOssStorageService.download(fileName);
                }
            },
            () -> {
                if (primaryStorage == PrimaryStorageType.MINIO) {
                    return aliyunOssStorageService.download(fileName);
                } else {
                    return minioStorageService.download(fileName);
                }
            },
            "下载文件",
            FileResultCode.FILE_DOWNLOAD_ERROR
        );
    }
    
    /**
     * 执行存储操作，失败时从备用存储服务执行
     * @param primaryOperation 主存储服务操作
     * @param backupOperation 备用存储服务操作
     * @param operationName 操作名称
     * @param errorCode 错误码
     * @param <T> 操作返回类型
     * @return 操作结果
     */
    private <T> T executeWithFallback(
            Operation<T> primaryOperation,
            Operation<T> backupOperation,
            String operationName,
            FileResultCode errorCode) {
        // 优先从主存储服务执行
        try {
            T result = primaryOperation.execute();
            // 更新主存储服务健康状态
            if (primaryStorage == PrimaryStorageType.MINIO) {
                minioHealthy = true;
            } else {
                ossHealthy = true;
            }
            log.info("从主存储服务{}成功", operationName);
            return result;
        } catch (Exception e) {
            log.error("从主存储服务{}失败，尝试从备用存储服务{}", operationName, operationName, e);
            // 更新主存储服务健康状态
            if (primaryStorage == PrimaryStorageType.MINIO) {
                minioHealthy = false;
            } else {
                ossHealthy = false;
            }
            // 切换主存储服务
            checkAndSwitchPrimaryStorage();
        }

        // 主存储服务执行失败，从备用存储服务执行
        try {
            T result = backupOperation.execute();
            // 更新备用存储服务健康状态
            if (primaryStorage == PrimaryStorageType.MINIO) {
                ossHealthy = true;
            } else {
                minioHealthy = true;
            }
            log.info("从备用存储服务{}成功", operationName);
            return result;
        } catch (Exception e) {
            log.error("从备用存储服务{}也失败", operationName, e);
            // 更新备用存储服务健康状态
            if (primaryStorage == PrimaryStorageType.MINIO) {
                ossHealthy = false;
            } else {
                minioHealthy = false;
            }
            throw new FileException(errorCode, "双写文件" + operationName + "失败");
        }
    }
    
    /**
     * 函数式接口，用于封装存储操作
     * @param <T> 操作返回类型
     */
    private interface Operation<T> {
        T execute() throws Exception;
    }
    
    /**
     * 定期健康检查
     * 每5分钟执行一次，确保主存储服务始终健康
     */
    @Scheduled(fixedRate = 300000) // 5分钟执行一次
    public void scheduledHealthCheck() {
        log.info("开始定期健康检查");
        
        // 强制执行健康检查，不使用缓存
        forceHealthCheck();
        
        // 检查并切换主存储服务
        checkAndSwitchPrimaryStorage();
        
        log.info("定期健康检查完成，当前主存储服务: {}, MinIO健康状态: {}, OSS健康状态: {}", 
                primaryStorage, minioHealthy, ossHealthy);
    }
    
    /**
     * 强制执行健康检查，不使用缓存
     */
    private void forceHealthCheck() {
        // 检查MinIO健康状态
        try {
            minioHealthy = minioStorageService.isHealthy();
            if (!minioHealthy) {
                log.warn("MinIO存储服务健康检查失败");
            }
        } catch (Exception e) {
            minioHealthy = false;
            log.warn("MinIO存储服务健康检查异常: {}", e.getMessage());
        }
        
        // 检查OSS健康状态
        try {
            ossHealthy = aliyunOssStorageService.isHealthy();
            if (!ossHealthy) {
                log.warn("OSS存储服务健康检查失败");
            }
        } catch (Exception e) {
            ossHealthy = false;
            log.warn("OSS存储服务健康检查异常: {}", e.getMessage());
        }
        
        // 更新最后检查时间
        lastHealthCheckTime = System.currentTimeMillis();
    }

    /**
     * 删除文件
     * 同时从MinIO和OSS删除
     * @param fileName 文件名
     * @return 是否删除成功
     */
    @Override
    public boolean delete(String fileName) {
        log.info("开始双写删除文件，文件名: {}", fileName);
        
        boolean minioDeleted = false;
        boolean ossDeleted = false;

        // 从MinIO 删除
        try {
            minioDeleted = minioStorageService.delete(fileName);
            log.info("从MinIO删除文件成功: {}", fileName);
        } catch (Exception e) {
            log.error("从MinIO删除文件失败: {}", fileName, e);
            minioHealthy = false;
        }

        // 从OSS 删除
        try {
            ossDeleted = aliyunOssStorageService.delete(fileName);
            log.info("从OSS删除文件成功: {}", fileName);
        } catch (Exception e) {
            log.error("从OSS删除文件失败: {}", fileName, e);
            ossHealthy = false;
        }

        // 实现补偿机制：如果有一个删除失败，尝试重试
        if (!minioDeleted && ossHealthy) {
            log.info("MinIO删除失败，尝试重试: {}", fileName);
            try {
                minioDeleted = minioStorageService.delete(fileName);
                if (minioDeleted) {
                    log.info("MinIO删除重试成功: {}", fileName);
                    minioHealthy = true;
                }
            } catch (Exception e) {
                log.error("MinIO删除重试失败: {}", fileName, e);
            }
        }

        if (!ossDeleted && minioHealthy) {
            log.info("OSS删除失败，尝试重试: {}", fileName);
            try {
                ossDeleted = aliyunOssStorageService.delete(fileName);
                if (ossDeleted) {
                    log.info("OSS删除重试成功: {}", fileName);
                    ossHealthy = true;
                }
            } catch (Exception e) {
                log.error("OSS删除重试失败: {}", fileName, e);
            }
        }

        // 只要有一个删除成功，就返回成功
        boolean result = minioDeleted || ossDeleted;
        log.info("双写删除文件完成，结果: {}", result);
        return result;
    }

    /**
     * 获取文件URL
     * 优先从主存储服务获取，失败则从备用存储服务获取
     * @param fileName 文件名
     * @param expire 过期时间（秒）
     * @return 文件URL
     */
    @Override
    public String getUrl(String fileName, long expire) {
        log.info("开始获取文件URL，文件名: {}, 当前主存储: {}", fileName, primaryStorage);
        
        return executeWithFallback(
            () -> {
                if (primaryStorage == PrimaryStorageType.MINIO) {
                    return minioStorageService.getUrl(fileName, expire);
                } else {
                    return aliyunOssStorageService.getUrl(fileName, expire);
                }
            },
            () -> {
                if (primaryStorage == PrimaryStorageType.MINIO) {
                    return aliyunOssStorageService.getUrl(fileName, expire);
                } else {
                    return minioStorageService.getUrl(fileName, expire);
                }
            },
            "获取文件URL",
            FileResultCode.URL_GENERATE_ERROR
        );
    }

    /**
     * 获取文件信息
     * 优先从主存储服务获取，失败则从备用存储服务获取
     * @param fileName 文件名
     * @return 文件信息
     */
    @Override
    public Map<String, Object> getFileInfo(String fileName) {
        log.info("开始获取文件信息，文件名: {}, 当前主存储: {}", fileName, primaryStorage);
        
        return executeWithFallback(
            () -> {
                if (primaryStorage == PrimaryStorageType.MINIO) {
                    return minioStorageService.getFileInfo(fileName);
                } else {
                    return aliyunOssStorageService.getFileInfo(fileName);
                }
            },
            () -> {
                if (primaryStorage == PrimaryStorageType.MINIO) {
                    return aliyunOssStorageService.getFileInfo(fileName);
                } else {
                    return minioStorageService.getFileInfo(fileName);
                }
            },
            "获取文件信息",
            FileResultCode.FILE_OPERATION_FAILED
        );
    }
}
