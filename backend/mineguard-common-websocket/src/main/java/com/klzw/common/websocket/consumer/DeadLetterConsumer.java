package com.klzw.common.websocket.consumer;

import com.klzw.common.core.util.JsonUtils;
import com.klzw.common.mq.constant.MqConstants;
import com.klzw.common.websocket.domain.MessageHistory;
import com.klzw.common.websocket.enums.MessageStatusEnum;
import com.klzw.common.websocket.repository.MessageHistoryRepository;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class DeadLetterConsumer {

    private final MessageHistoryRepository messageHistoryRepository;

    public DeadLetterConsumer(MessageHistoryRepository messageHistoryRepository) {
        this.messageHistoryRepository = messageHistoryRepository;
    }

    @RabbitListener(queues = MqConstants.DEAD_LETTER_QUEUE)
    public void handleDeadLetter(Message message, Channel channel,
                                 @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            byte[] bodyBytes = message.getBody();
            String contentType = message.getMessageProperties().getContentType();
            String originalExchange = message.getMessageProperties().getReceivedExchange();
            String originalRoutingKey = message.getMessageProperties().getReceivedRoutingKey();
            Integer retryCount = (Integer) message.getMessageProperties().getHeaders().get("retryCount");
            Map<String, Object> headers = message.getMessageProperties().getHeaders();
            
            log.warn("收到死信消息: originalExchange={}, originalRoutingKey={}, retryCount={}, contentType={}, headers={}", 
                originalExchange, originalRoutingKey, retryCount, contentType, headers);
            
            saveFailedMessage(message, bodyBytes, contentType, originalExchange, originalRoutingKey, retryCount, headers);
            
            channel.basicAck(deliveryTag, false);
            log.info("死信消息处理完成，已保存到失败记录");
            
        } catch (Exception e) {
            log.error("处理死信消息失败: error={}", e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
    
    private void saveFailedMessage(Message amqpMessage, byte[] bodyBytes, String contentType, 
                                   String originalExchange, String originalRoutingKey, 
                                   Integer retryCount, Map<String, Object> headers) {
        try {
            MessageHistory history = MessageHistory.builder()
                    .messageId(amqpMessage.getMessageProperties().getMessageId())
                    .messageType("DEAD_LETTER")
                    .sender("SYSTEM")
                    .receiver(extractReceiver(bodyBytes, contentType, originalRoutingKey, headers))
                    .status(MessageStatusEnum.FAILED.getCode())
                    .createTime(LocalDateTime.now())
                    .retryCount(retryCount != null ? retryCount : 0)
                    .content(buildDeadLetterContent(bodyBytes, contentType, originalExchange, originalRoutingKey, retryCount, headers))
                    .build();
            
            messageHistoryRepository.save(history);
            log.info("死信消息已保存到历史记录: messageId={}", history.getMessageId());
            
        } catch (Exception e) {
            log.error("保存死信消息到历史记录失败: error={}", e.getMessage(), e);
        }
    }
    
    private Object buildDeadLetterContent(byte[] bodyBytes, String contentType, 
                                          String originalExchange, String originalRoutingKey, 
                                          Integer retryCount, Map<String, Object> headers) {
        Map<String, Object> content = new HashMap<>();
        content.put("contentType", contentType);
        content.put("originalExchange", originalExchange != null ? originalExchange : "unknown");
        content.put("originalRoutingKey", originalRoutingKey != null ? originalRoutingKey : "unknown");
        content.put("retryCount", retryCount != null ? retryCount : 0);
        content.put("failedAt", LocalDateTime.now().toString());
        content.put("headers", headers);
        
        try {
            if (contentType != null && contentType.contains("json")) {
                String body = new String(bodyBytes, StandardCharsets.UTF_8);
                @SuppressWarnings("unchecked")
                Map<String, Object> originalContent = JsonUtils.fromJson(body, Map.class);
                content.put("originalContent", originalContent);
                content.put("rawBody", body);
            } else if (contentType != null && contentType.contains("serialized-object")) {
                content.put("rawBody", "[Java serialized object - base64 encoded]");
                content.put("base64Body", Base64.getEncoder().encodeToString(bodyBytes));
            } else {
                try {
                    String body = new String(bodyBytes, StandardCharsets.UTF_8);
                    content.put("rawBody", body);
                } catch (Exception e) {
                    content.put("rawBody", "[Binary data - " + bodyBytes.length + " bytes]");
                    content.put("base64Body", Base64.getEncoder().encodeToString(bodyBytes));
                }
            }
        } catch (Exception e) {
            content.put("rawBody", "[Error parsing body: " + e.getMessage() + "]");
        }
        
        return content;
    }
    
    private String extractReceiver(byte[] bodyBytes, String contentType, String routingKey, Map<String, Object> headers) {
        if (routingKey != null && routingKey.startsWith("offline.")) {
            return routingKey.substring("offline.".length());
        }
        
        try {
            if (contentType != null && contentType.contains("json")) {
                String body = new String(bodyBytes, StandardCharsets.UTF_8);
                @SuppressWarnings("unchecked")
                Map<String, Object> data = JsonUtils.fromJson(body, Map.class);
                if (data.containsKey("executorId")) {
                    return String.valueOf(data.get("executorId"));
                }
                if (data.containsKey("receiver")) {
                    return String.valueOf(data.get("receiver"));
                }
                if (data.containsKey("userId")) {
                    return String.valueOf(data.get("userId"));
                }
            }
        } catch (Exception ignored) {
        }
        
        return "UNKNOWN";
    }
}