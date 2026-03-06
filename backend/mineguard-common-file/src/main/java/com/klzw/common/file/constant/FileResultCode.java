package com.klzw.common.file.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件存储模块错误码枚举
 * <p>
 * 错误码范围：1200-1299
 * <p>
 * 错误码说明：
 * - 1200-1202: 存储通用错误
 * - 1203-1206: 存储桶相关错误
 * - 1207-1214: 文件操作相关错误
 * - 1215-1216: URL相关错误
 * - 1217-1218: 配置/初始化相关错误
 */
@Getter
@AllArgsConstructor
public enum FileResultCode {

    /**
     * 存储通用错误
     */
    STORAGE_CONNECTION_FAILED(1200, "存储连接失败"),
    STORAGE_TIMEOUT(1201, "存储操作超时"),
    STORAGE_DATA_ERROR(1202, "存储数据错误"),

    /**
     * 存储桶相关错误
     */
    BUCKET_INIT_FAILED(1203, "存储桶初始化失败"),
    BUCKET_NOT_FOUND(1204, "存储桶不存在"),
    BUCKET_CREATE_FAILED(1205, "存储桶创建失败"),
    BUCKET_DELETE_FAILED(1206, "存储桶删除失败"),

    /**
     * 文件操作相关错误
     */
    FILE_NOT_FOUND(1207, "文件不存在"),
    FILE_UPLOAD_FAILED(1208, "文件上传失败"),
    FILE_DOWNLOAD_FAILED(1209, "文件下载失败"),
    FILE_DELETE_FAILED(1210, "文件删除失败"),
    FILE_OPERATION_FAILED(1211, "文件操作失败"),
    FILE_SIZE_EXCEEDED(1212, "文件大小超出限制"),
    FILE_TYPE_NOT_ALLOWED(1213, "文件类型不允许"),
    FILE_PATH_INVALID(1214, "文件路径无效"),

    /**
     * URL相关错误
     */
    URL_GENERATE_FAILED(1215, "URL生成失败"),
    PRESIGNED_URL_FAILED(1216, "预签名URL生成失败"),

    /**
     * 配置/初始化相关错误
     */
    STORAGE_CONFIG_ERROR(1217, "存储配置错误"),
    STORAGE_INIT_FAILED(1218, "存储初始化失败"),
    STORAGE_TYPE_NOT_SUPPORTED(1219, "不支持的存储类型");

    private final int code;
    private final String message;

    /**
     * 根据错误码获取枚举
     *
     * @param code 错误码
     * @return 对应的枚举，如果未找到返回null
     */
    public static FileResultCode getByCode(int code) {
        for (FileResultCode value : values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        return null;
    }
}
