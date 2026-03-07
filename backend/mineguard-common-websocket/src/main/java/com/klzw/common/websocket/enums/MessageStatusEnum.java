package com.klzw.common.websocket.enums;

import lombok.Getter;

@Getter
public enum MessageStatusEnum {
    PENDING("PENDING", "待发送"),
    SENT("SENT", "已发送"),
    DELIVERED("DELIVERED", "已送达"),
    READ("READ", "已读"),
    FAILED("FAILED", "发送失败");

    private final String code;
    private final String description;

    MessageStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
