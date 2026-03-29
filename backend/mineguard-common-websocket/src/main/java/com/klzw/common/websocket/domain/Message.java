package com.klzw.common.websocket.domain;

import com.klzw.common.websocket.enums.MessagePriority;
import com.klzw.common.websocket.enums.MessageTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message implements Serializable {
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

    public static class MessageBuilder {
        private String messageId;
        private MessageTypeEnum messageType;
        private String sender;
        private String receiver;
        private Long timestamp;
        private MessagePriority priority = MessagePriority.MEDIUM;
        private Boolean requireAck = false;
        private Map<String, Object> content;

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
            if (this.timestamp == null) {
                this.timestamp = System.currentTimeMillis();
            }
            return new Message(messageId, messageType, sender, receiver, timestamp, priority, requireAck, content);
        }
    }
}
