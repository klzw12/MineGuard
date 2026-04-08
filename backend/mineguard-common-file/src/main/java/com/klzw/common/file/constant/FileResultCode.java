package com.klzw.common.file.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件存储模块错误码枚举
 * <p>
 * 错误码范围：1300-1399
 * <p>
 * 错误码说明：
 * - 1300-1302: 存储通用错误
 * - 1303-1306: 存储桶相关错误
 * - 1307-1314: 文件操作相关错误
 * - 1315-1316: URL相关错误
 * - 1317-1319: 配置/初始化相关错误
 */
@Getter
@AllArgsConstructor
public enum FileResultCode {

    STORAGE_CONNECTION_FAILED(1300, "存储连接失败"),
    STORAGE_TIMEOUT(1301, "存储操作超时"),
    STORAGE_DATA_ERROR(1302, "存储数据错误"),
    PARAM_ERROR(1399, "参数错误"),

    BUCKET_INIT_FAILED(1303, "存储桶初始化失败"),
    BUCKET_NOT_FOUND(1304, "存储桶不存在"),
    BUCKET_CREATE_FAILED(1305, "存储桶创建失败"),
    BUCKET_DELETE_FAILED(1306, "存储桶删除失败"),

    FILE_NOT_FOUND(1307, "文件不存在"),
    FILE_UPLOAD_FAILED(1308, "文件上传失败"),
    FILE_DOWNLOAD_FAILED(1309, "文件下载失败"),
    FILE_DELETE_FAILED(1310, "文件删除失败"),
    FILE_OPERATION_FAILED(1311, "文件操作失败"),
    FILE_SIZE_EXCEEDED(1312, "文件大小超出限制"),
    FILE_TYPE_NOT_ALLOWED(1313, "文件类型不允许"),
    FILE_PATH_INVALID(1314, "文件路径无效"),

    URL_GENERATE_FAILED(1315, "URL生成失败"),
    PRESIGNED_URL_FAILED(1316, "预签名URL生成失败"),

    STORAGE_CONFIG_ERROR(1317, "存储配置错误"),
    STORAGE_INIT_FAILED(1318, "存储初始化失败"),
    STORAGE_TYPE_NOT_SUPPORTED(1319, "不支持的存储类型");

    private final int code;
    private final String message;

    public static FileResultCode getByCode(int code) {
        for (FileResultCode value : values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        return null;
    }
}
