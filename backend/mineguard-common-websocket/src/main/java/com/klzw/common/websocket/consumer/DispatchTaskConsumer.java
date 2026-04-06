package com.klzw.common.websocket.consumer;

import com.klzw.common.core.util.JsonUtils;
import com.klzw.common.mq.constant.MqConstants;
import com.klzw.common.websocket.domain.Message;
import com.klzw.common.websocket.enums.MessageTypeEnum;
import com.klzw.common.websocket.manager.OnlineUserManager;
import com.klzw.common.websocket.service.MessageHistoryService;
import com.klzw.common.websocket.service.MessagePushService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class DispatchTaskConsumer {

    private final MessagePushService messagePushService;
    private final OnlineUserManager onlineUserManager;
    private final MessageHistoryService messageHistoryService;

    public DispatchTaskConsumer(MessagePushService messagePushService,
                                OnlineUserManager onlineUserManager,
                                MessageHistoryService messageHistoryService) {
        this.messagePushService = messagePushService;
        this.onlineUserManager = onlineUserManager;
        this.messageHistoryService = messageHistoryService;
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = MqConstants.DISPATCH_TASK_QUEUE, durable = "true"),
                    exchange = @Exchange(name = MqConstants.DISPATCH_TASK_EXCHANGE, type = ExchangeTypes.DIRECT, durable = "true"),
                    key = MqConstants.DISPATCH_TASK_ROUTING_KEY
            ),
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void handleDispatchTask(Object message, Channel channel) throws IOException {
        long deliveryTag = 0;
        String messageBody = null;
        
        if (message instanceof org.springframework.amqp.core.Message) {
            org.springframework.amqp.core.Message amqpMessage = (org.springframework.amqp.core.Message) message;
            deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();
            messageBody = new String(amqpMessage.getBody());
        } else if (message instanceof Map) {
            // 如果是 Map 类型，直接使用（可能是 Java 序列化的消息）
            Map<?, ?> messageMap = (Map<?, ?>) message;
            deliveryTag = 1; // 模拟 deliveryTag
            messageBody = JsonUtils.toJson(messageMap);
        } else {
            // 其他类型，转换为字符串
            deliveryTag = 1; // 模拟 deliveryTag
            messageBody = message.toString();
        }

        try {
            log.info("收到调度任务消息: {}", messageBody);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> taskData = JsonUtils.fromJson(messageBody, Map.class);

            Long taskId = taskData.get("taskId") != null ? Long.valueOf(taskData.get("taskId").toString()) : null;
            String taskNo = (String) taskData.get("taskNo");
            Long executorId = taskData.get("executorId") != null ? Long.valueOf(taskData.get("executorId").toString()) : null;
            String type = (String) taskData.get("type");
            Integer taskType = taskData.get("taskType") != null ? Integer.valueOf(taskData.get("taskType").toString()) : 1;

            log.info("解析调度任务: taskId={}, taskNo={}, executorId={}, taskType={}", 
                taskId, taskNo, executorId, taskType);

            if (executorId == null) {
                log.warn("执行人ID为空，跳过推送");
                channel.basicAck(deliveryTag, false);
                return;
            }

            String userId = String.valueOf(executorId);

            String taskTypeName = getTaskTypeName(taskType);
            String title = "新" + taskTypeName;
            String content = "您有新的" + taskTypeName + "：" + taskNo;

            Map<String, Object> messageContent = new HashMap<>();
            messageContent.put("taskId", taskId);
            messageContent.put("taskNo", taskNo);
            messageContent.put("type", type);
            messageContent.put("taskType", taskType);
            messageContent.put("taskTypeName", taskTypeName);
            messageContent.put("title", title);
            messageContent.put("content", content);
            messageContent.put("createTime", taskData.get("createTime"));

            Message wsMessage = Message.builder()
                    .messageId(UUID.randomUUID().toString())
                    .messageType(MessageTypeEnum.DISPATCH_COMMAND)
                    .sender("SYSTEM")
                    .receiver(userId)
                    .content(messageContent)
                    .build();

            log.info("准备推送消息: userId={}, isOnline={}", userId, onlineUserManager.isOnline(userId));
            
            if (onlineUserManager.isOnline(userId)) {
                messagePushService.pushToUser(userId, wsMessage);
                log.info("调度任务推送成功: userId={}, taskId={}", userId, taskId);
            } else {
                log.info("用户不在线，保存离线消息: userId={}", userId);
                messageHistoryService.saveOfflineMessage(userId, wsMessage);
            }

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("处理调度任务消息失败: error={}", e.getMessage(), e);
            channel.basicNack(deliveryTag, false, true);
        }
    }
    
    private String getTaskTypeName(Integer taskType) {
        if (taskType == null) return "调度任务";
        return switch (taskType) {
            case 1 -> "运输任务";
            case 2 -> "维修任务";
            case 3 -> "巡检任务";
            default -> "调度任务";
        };
    }
}
