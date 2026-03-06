package com.klzw.common.file.service;

import java.io.InputStream;
import java.util.Map;

public interface StorageService {
    /**
     * 上传文件
     * @param inputStream 文件输入流
     * @param fileName 文件名
     * @param contentType 内容类型
     * @return 文件路径
     */
    String upload(InputStream inputStream, String fileName, String contentType);

    /**
     * 上传文件（指定模块和文件夹）
     * @param module 模块名称
     * @param folder 文件夹路径
     * @param inputStream 文件输入流
     * @param originalFileName 原始文件名
     * @param contentType 内容类型
     * @return 文件路径
     */
    String upload(String module, String folder, InputStream inputStream, String originalFileName, String contentType);

    /**
     * 下载文件
     * @param fileName 文件名
     * @return 文件输入流
     */
    InputStream download(String fileName);

    /**
     * 删除文件
     * @param fileName 文件名
     * @return 是否删除成功
     */
    boolean delete(String fileName);

    /**
     * 获取文件URL
     * @param fileName 文件名
     * @param expire 过期时间（秒）
     * @return 文件URL
     */
    String getUrl(String fileName, long expire);

    /**
     * 获取文件信息
     * @param fileName 文件名
     * @return 文件信息
     */
    Map<String, Object> getFileInfo(String fileName);

    /**
     * 健康检查
     * @return 存储服务是否健康
     */
    boolean isHealthy();
}