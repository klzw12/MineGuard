package com.klzw.service.user.service.impl;

import com.klzw.common.websocket.domain.Message;
import com.klzw.common.websocket.enums.MessageTypeEnum;
import com.klzw.common.websocket.service.SmartMessagePushService;
import com.klzw.service.user.entity.UserNotification;
import com.klzw.service.user.mapper.UserNotificationMapper;
import com.klzw.service.user.mapper.UserRoleMapper;
import com.klzw.service.user.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final UserNotificationMapper notificationMapper;
    private final UserRoleMapper userRoleMapper;
    private final SmartMessagePushService smartMessagePushService;

    @Override
    public void sendMessage(Long userId, String title, String content, Integer type) {
        log.info("发送消息给用户：userId={}, title={}, type={}", userId, title, type);
        
        UserNotification notification = new UserNotification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(notification);
        
        Message wsMessage = buildNotificationMessage(title, content, type);
        smartMessagePushService.pushToUserWithOffline(String.valueOf(userId), wsMessage);
    }

    @Override
    public void sendBatchMessage(List<Long> userIds, String title, String content, Integer type) {
        log.info("批量发送消息：userIds={}, title={}, type={}", userIds, title, type);
        
        for (Long userId : userIds) {
            UserNotification notification = new UserNotification();
            notification.setUserId(userId);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setType(type);
            notification.setIsRead(0);
            notification.setCreateTime(LocalDateTime.now());
            notificationMapper.insert(notification);
        }
        
        Message wsMessage = buildNotificationMessage(title, content, type);
        List<String> userIdStrings = userIds.stream().map(String::valueOf).toList();
        smartMessagePushService.multicast(userIdStrings, wsMessage);
    }

    @Override
    public void sendMessageByRole(String roleCode, String title, String content, Integer type) {
        List<Long> userIds = userRoleMapper.selectUserIdsByRoleCode(roleCode);
        if (userIds == null || userIds.isEmpty()) {
            log.warn("未找到角色对应的用户：roleCode={}", roleCode);
            return;
        }
        log.info("按角色发送消息：roleCode={}, userCount={}, title={}", roleCode, userIds.size(), title);
        
        for (Long userId : userIds) {
            UserNotification notification = new UserNotification();
            notification.setUserId(userId);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setType(type);
            notification.setIsRead(0);
            notification.setCreateTime(LocalDateTime.now());
            notificationMapper.insert(notification);
        }
        
        Message wsMessage = buildNotificationMessage(title, content, type);
        smartMessagePushService.pushToRole(roleCode, wsMessage);
    }
    
    private Message buildNotificationMessage(String title, String content, Integer type) {
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("title", title);
        contentMap.put("content", content);
        contentMap.put("type", type);
        contentMap.put("timestamp", System.currentTimeMillis());
        
        return Message.builder()
                .messageId("MSG_NOTIFY_" + System.currentTimeMillis())
                .messageType(MessageTypeEnum.NOTIFICATION)
                .sender("system")
                .content(contentMap)
                .build();
    }
}
