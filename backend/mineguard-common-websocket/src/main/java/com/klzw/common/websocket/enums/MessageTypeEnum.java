package com.klzw.common.websocket.enums;

import lombok.Getter;

@Getter
public enum MessageTypeEnum {
    VEHICLE_STATUS("vehicle_status", "车辆状态变更", MessagePriority.MEDIUM),
    WARNING_NOTIFICATION("warning_notification", "预警通知", MessagePriority.HIGH),
    DISPATCH_COMMAND("dispatch_command", "调度指令", MessagePriority.HIGH),
    TRIP_UPDATE("trip_update", "行程状态更新", MessagePriority.MEDIUM),
    SYSTEM_NOTICE("system_notice", "系统公告", MessagePriority.LOW),
    CHAT_MESSAGE("chat_message", "聊天消息", MessagePriority.LOW),
    HEARTBEAT("heartbeat", "心跳消息", MessagePriority.HIGH),
    AUTH("auth", "认证消息", MessagePriority.HIGH),
    SUBSCRIBE("subscribe", "订阅消息", MessagePriority.HIGH),
    ACK("ack", "确认消息", MessagePriority.HIGH),
    ERROR("error", "错误消息", MessagePriority.HIGH),
    ONLINE_STATUS("online_status", "在线状态", MessagePriority.MEDIUM),
    NOTIFICATION("notification", "通知消息", MessagePriority.MEDIUM);

    private final String code;
    private final String description;
    private final MessagePriority priority;

    MessageTypeEnum(String code, String description, MessagePriority priority) {
        this.code = code;
        this.description = description;
        this.priority = priority;
    }

    public static MessageTypeEnum fromCode(String code) {
        for (MessageTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
