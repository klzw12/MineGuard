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
     * @param objectName  对象名称
     * @param bucketName  存储桶名称
     * @param contentType 内容类型
     * @return 文件访问URL
     */
    String upload(InputStream inputStream, String objectName, String bucketName, String contentType);
    
    /**
     * 下载文件
     *
     * @param objectName 对象名称
     * @param bucketName 存储桶名称
     * @return 文件输入流
     */
    InputStream download(String objectName, String bucketName);
    
    /**
     * 删除文件
     *
     * @param objectName 对象名称
     * @param bucketName 存储桶名称
     * @return 是否删除成功
     */
    boolean delete(String objectName, String bucketName);
    
    /**
     * 检查文件是否存在
     *
     * @param objectName 对象名称
     * @param bucketName 存储桶名称
     * @return 是否存在
     */
    boolean exists(String objectName, String bucketName);
    
    /**
     * 获取文件访问URL
     *
     * @param objectName 对象名称
     * @param bucketName 存储桶名称
     * @param expireTime 过期时间（秒）
     * @return 访问URL
     */
    String getUrl(String objectName, String bucketName, int expireTime);
    
    /**
     * 获取文件元数据
     *
     * @param objectName 对象名称
     * @param bucketName 存储桶名称
     * @return 元数据Map
     */
    Map<String, String> getMetadata(String objectName, String bucketName);
}
