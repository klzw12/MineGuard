package com.klzw.common.websocket.util;

import com.klzw.common.websocket.domain.Message;
import com.klzw.common.websocket.enums.MessagePriority;
import com.klzw.common.websocket.enums.MessageTypeEnum;

import java.util.Map;
import java.util.UUID;

public class MessageBuilder {
    private String messageId;
    private MessageTypeEnum messageType;
    private String sender;
    private String receiver;
    private Long timestamp;
    private MessagePriority priority;
    private Boolean requireAck;
    private Map<String, Object> content;

    public static MessageBuilder builder() {
        return new MessageBuilder();
    }

    public MessageBuilder messageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    public MessageBuilder messageType(MessageTypeEnum messageType) {
        this.messageType = messageType;
        this.priority = messageType.getPriority();
        return this;
    }

    public MessageBuilder sender(String sender) {
        this.sender = sender;
        return this;
    }

    public MessageBuilder receiver(String receiver) {
        this.receiver = receiver;
        return this;
    }

    public MessageBuilder timestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public MessageBuilder priority(MessagePriority priority) {
        this.priority = priority;
        return this;
    }

    public MessageBuilder requireAck(Boolean requireAck) {
        this.requireAck = requireAck;
        return this;
    }

    public MessageBuilder content(Map<String, Object> content) {
        this.content = content;
        return this;
    }

    public Message build() {
        if (this.messageId == null) {
            this.messageId = generateMessageId();
        }
        if (this.timestamp == null) {
            this.timestamp = System.currentTimeMillis();
        }
        if (this.priority == null) {
            this.priority = MessagePriority.MEDIUM;
        }
        if (this.requireAck == null) {
            this.requireAck = false;
        }

        return Message.builder()
                .messageId(this.messageId)
                .messageType(this.messageType)
                .sender(this.sender)
                .receiver(this.receiver)
                .timestamp(this.timestamp)
                .priority(this.priority)
                .requireAck(this.requireAck)
                .content(this.content)
                .build();
    }

    private String generateMessageId() {
        return "MSG_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
