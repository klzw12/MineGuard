package com.klzw.common.websocket.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
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

    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static MessagePriority fromCode(String code) {
        for (MessagePriority priority : values()) {
            if (priority.getCode().equals(code)) {
                return priority;
            }
        }
        return MEDIUM;
    }
}
