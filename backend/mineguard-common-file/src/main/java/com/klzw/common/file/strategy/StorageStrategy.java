package com.klzw.common.file.strategy;

import java.io.InputStream;
import java.util.Map;

/**
 * 存储策略接口
 * 定义文件存储的基本操作
 */
public interface StorageStrategy {
    
    /**
     * 上传文件
     *
     * @param inputStream 文件输入流
     * @param fileName    文件名（包含路径）
     * @param contentType 内容类型
     * @return 文件访问路径
     */
    String upload(InputStream inputStream, String fileName, String contentType);
    
    /**
     * 下载文件
     *
     * @param fileName 文件名（包含路径）
     * @return 文件输入流
     */
    InputStream download(String fileName);
    
    /**
     * 删除文件
     *
     * @param fileName 文件名（包含路径）
     * @return 是否删除成功
     */
    boolean delete(String fileName);
    
    /**
     * 检查文件是否存在
     *
     * @param fileName 文件名（包含路径）
     * @return 是否存在
     */
    boolean exists(String fileName);
    
    /**
     * 获取文件访问URL
     *
     * @param fileName 文件名（包含路径）
     * @param expireSeconds 过期时间（秒）
     * @return 访问URL
     */
    String getUrl(String fileName, long expireSeconds);
    
    /**
     * 获取文件永久访问URL（需要Bucket设置为公共读）
     *
     * @param fileName 文件名（包含路径）
     * @return 永久访问URL
     */
    String getPermanentUrl(String fileName);
    
    /**
     * 获取文件信息
     *
     * @param fileName 文件名（包含路径）
     * @return 文件信息Map
     */
    Map<String, Object> getFileInfo(String fileName);

    /**
     * 检查存储服务是否健康
     *
     * @return 健康状态
     */
    boolean isHealthy();
}
