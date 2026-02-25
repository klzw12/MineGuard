package com.klzw.common.core.result;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS, "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS, "操作成功", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS, message, data);
    }

    public static <T> Result<T> fail() {
        return new Result<>(ResultCode.FAIL, "操作失败", null);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(ResultCode.FAIL, message, null);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> error() {
        return new Result<>(ResultCode.INTERNAL_ERROR, "系统内部错误", null);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(ResultCode.INTERNAL_ERROR, message, null);
    }
}
