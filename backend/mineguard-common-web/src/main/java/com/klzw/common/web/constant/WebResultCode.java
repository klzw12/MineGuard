package com.klzw.common.web.constant;

public enum WebResultCode {
    PARAM_MISSING(701, "参数缺失"),
    PARAM_TYPE_ERROR(702, "参数类型错误"),
    PARAM_VALUE_ERROR(703, "参数值错误"),
    PARAM_FORMAT_ERROR(704, "参数格式错误"),
    PARAM_VALIDATION_ERROR(705, "参数验证错误"),
    FILE_UPLOAD_ERROR(706, "文件上传错误"),
    FILE_SIZE_EXCEEDED(707, "文件大小超限"),
    FILE_TYPE_NOT_ALLOWED(708, "文件类型不允许"),
    FILE_NOT_FOUND(709, "文件不存在"),
    REQUEST_TIMEOUT(710, "请求超时"),
    NETWORK_ERROR(711, "网络错误"),
    SERVER_ERROR(712, "服务器内部错误"),
    SERVICE_UNAVAILABLE(713, "服务不可用"),
    GATEWAY_ERROR(714, "网关错误"),
    METHOD_NOT_ALLOWED(721, "方法不允许"),
    UNSUPPORTED_MEDIA_TYPE(722, "不支持的媒体类型"),
    TOO_MANY_REQUESTS(724, "请求过于频繁"),
    CORS_ERROR(727, "跨域请求错误");

    private final int code;
    private final String message;

    WebResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
