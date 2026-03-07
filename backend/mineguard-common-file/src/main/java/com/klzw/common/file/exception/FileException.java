package com.klzw.common.file.exception;

import com.klzw.common.core.exception.BaseException;
import com.klzw.common.file.constant.FileResultCode;
import lombok.Getter;

/**
 * 文件存储模块业务异常
 * <p>
 * 用于处理文件存储相关的异常，包括：
 * - 存储连接异常
 * - 存储桶操作异常
 * - 文件上传/下载/删除异常
 * - URL生成异常
 * <p>
 * 错误码范围：1200-1299（统一使用FileResultCode定义）
 *
 * @see FileResultCode
 */
@Getter
public class FileException extends BaseException {

    /**
     * 文件模块标识
     */
    private static final String MODULE = "file";

    /**
     * 构造方法 - 使用FileResultCode
     *
     * @param resultCode 文件错误码枚举
     */
    public FileException(FileResultCode resultCode) {
        super(resultCode.getCode(), resultCode.getMessage(), MODULE);
    }

    /**
     * 构造方法 - 使用FileResultCode和自定义消息
     *
     * @param resultCode 文件错误码枚举
     * @param message    自定义错误消息
     */
    public FileException(FileResultCode resultCode, String message) {
        super(resultCode.getCode(), message, MODULE);
    }

    /**
     * 构造方法 - 使用FileResultCode和异常原因
     *
     * @param resultCode 文件错误码枚举
     * @param cause      异常原因
     */
    public FileException(FileResultCode resultCode, Throwable cause) {
        super(resultCode.getCode(), resultCode.getMessage(), MODULE, cause);
    }

    /**
     * 构造方法 - 使用FileResultCode、自定义消息和异常原因
     *
     * @param resultCode 文件错误码枚举
     * @param message    自定义错误消息
     * @param cause      异常原因
     */
    public FileException(FileResultCode resultCode, String message, Throwable cause) {
        super(resultCode.getCode(), message, MODULE, cause);
    }

    /**
     * 构造方法 - 使用错误码和消息（兼容旧代码）
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public FileException(int code, String message) {
        super(code, message, MODULE);
    }

    /**
     * 构造方法 - 使用错误码、消息和异常原因（兼容旧代码）
     *
     * @param code    错误码
     * @param message 错误消息
     * @param cause   异常原因
     */
    public FileException(int code, String message, Throwable cause) {
        super(code, message, MODULE, cause);
    }
}
