package com.klzw.common.websocket.constant;

import lombok.Getter;

@Getter
public enum WebSocketResultCode {
    CONNECTION_FAILED(1500, "WebSocket连接失败"),
    CONNECTION_TIMEOUT(1501, "WebSocket连接超时"),
    CONNECTION_CLOSED(1502, "WebSocket连接已关闭"),
    AUTHENTICATION_FAILED(1503, "WebSocket认证失败"),
    TOKEN_INVALID(1504, "Token无效或已过期"),
    TOKEN_MISSING(1505, "Token缺失"),
    MESSAGE_SEND_FAILED(1506, "消息发送失败"),
    MESSAGE_FORMAT_INVALID(1507, "消息格式无效"),
    MESSAGE_TYPE_UNKNOWN(1508, "未知的消息类型"),
    SUBSCRIBE_FAILED(1509, "订阅失败"),
    UNSUBSCRIBE_FAILED(1510, "取消订阅失败"),
    USER_NOT_ONLINE(1511, "用户不在线"),
    USER_ALREADY_ONLINE(1512, "用户已在线"),
    SESSION_NOT_FOUND(1513, "会话不存在"),
    HEARTBEAT_TIMEOUT(1514, "心跳超时"),
    RATE_LIMIT_EXCEEDED(1515, "消息频率超限"),
    TOPIC_NOT_FOUND(1516, "主题不存在"),
    PERMISSION_DENIED(1517, "权限不足"),
    ENCRYPTION_ERROR(1518, "消息加密/解密失败"),
    INTERNAL_ERROR(1519, "WebSocket内部错误");

    private final int code;
    private final String message;

    WebSocketResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
