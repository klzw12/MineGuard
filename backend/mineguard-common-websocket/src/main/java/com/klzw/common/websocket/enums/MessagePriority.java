package com.klzw.common.websocket.enums;

import lombok.Getter;

@Getter
public enum MessagePriority {
    HIGH("HIGH", "高优先级"),
    MEDIUM("MEDIUM", "中优先级"),
    LOW("LOW", "低优先级");

    private final String code;
    private final String description;

    MessagePriority(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
