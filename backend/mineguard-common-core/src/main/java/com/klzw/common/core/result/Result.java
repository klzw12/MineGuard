package com.klzw.common.core.result;

import com.klzw.common.core.enums.ResultCodeEnum;
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
        return new Result<>(ResultCodeEnum.SUCCESS.getCode(), "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCodeEnum.SUCCESS.getCode(), "操作成功", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCodeEnum.SUCCESS.getCode(), message, data);
    }

    public static <T> Result<T> fail() {
        return new Result<>(ResultCodeEnum.FAIL.getCode(), "操作失败", null);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(ResultCodeEnum.FAIL.getCode(), message, null);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> error() {
        return new Result<>(ResultCodeEnum.INTERNAL_ERROR.getCode(), "系统内部错误", null);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(ResultCodeEnum.INTERNAL_ERROR.getCode(), message, null);
    }

    public boolean isSuccess() {
        return this.code == ResultCodeEnum.SUCCESS.getCode();
    }
}
