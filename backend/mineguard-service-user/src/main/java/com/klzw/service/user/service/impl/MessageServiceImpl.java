package com.klzw.service.user.service.impl;

import com.klzw.common.websocket.domain.Message;
import com.klzw.common.websocket.domain.MessageHistory;
import com.klzw.common.websocket.enums.MessageTypeEnum;
import com.klzw.common.websocket.service.MessageHistoryService;
import com.klzw.common.websocket.service.SmartMessagePushService;
import com.klzw.service.user.dto.ContactVO;
import com.klzw.service.user.dto.MessageVO;
import com.klzw.service.user.entity.User;
import com.klzw.service.user.mapper.UserMapper;
import com.klzw.service.user.mapper.UserRoleMapper;
import com.klzw.service.user.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final MessageHistoryService messageHistoryService;
    private final SmartMessagePushService smartMessagePushService;

    @Override
    public void sendMessage(Long userId, String title, String content, Integer type) {
        log.info("发送消息给用户：userId={}, title={}, type={}", userId, title, type);
        
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("title", title);
        contentMap.put("content", content);
        contentMap.put("type", type);
        contentMap.put("timestamp", System.currentTimeMillis());
        
        Message message = Message.builder()
                .messageId("MSG_NOTIFY_" + System.currentTimeMillis())
                .messageType(MessageTypeEnum.NOTIFICATION)
                .sender("system")
                .receiver(String.valueOf(userId))
                .content(contentMap)
                .build();
        
        smartMessagePushService.pushToUserWithOffline(String.valueOf(userId), message);
    }

    @Override
    public void sendBatchMessage(List<Long> userIds, String title, String content, Integer type) {
        log.info("批量发送消息：userIds={}, title={}, type={}", userIds, title, type);
        for (Long userId : userIds) {
            sendMessage(userId, title, content, type);
        }
    }

    @Override
    public void sendMessageByRole(String roleCode, String title, String content, Integer type) {
        List<Long> userIds = userRoleMapper.selectUserIdsByRoleCode(roleCode);
        if (userIds == null || userIds.isEmpty()) {
            log.warn("未找到角色对应的用户：roleCode={}", roleCode);
            return;
        }
        sendBatchMessage(userIds, title, content, type);
    }

    @Override
    public List<MessageVO> getUserMessages(Long userId, Integer type) {
        Page<MessageHistory> page = messageHistoryService.getMessageHistory(
            String.valueOf(userId),
            PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "timestamp"))
        );
        
        return page.getContent().stream()
            .filter(m -> !MessageTypeEnum.CHAT_MESSAGE.getCode().equals(m.getMessageType()))
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }

    @Override
    public List<MessageVO> getPrivateMessages(Long userId, Long contactId) {
        Page<MessageHistory> page = messageHistoryService.getPrivateMessages(
            String.valueOf(userId),
            String.valueOf(contactId),
            PageRequest.of(0, 100, Sort.by(Sort.Direction.ASC, "timestamp"))
        );
        
        return page.getContent().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }

    @Override
    public void markAsRead(String messageId) {
        messageHistoryService.markAsRead(messageId);
    }

    @Override
    public void markAllAsRead(Long userId) {
        messageHistoryService.markAllMessagesAsRead(String.valueOf(userId));
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        return (int) messageHistoryService.getUnreadNotificationCount(String.valueOf(userId));
    }

    @Override
    public List<ContactVO> getContactList(Long userId) {
        Page<MessageHistory> sentPage = messageHistoryService.getSentMessages(
            String.valueOf(userId),
            PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "timestamp"))
        );
        
        Page<MessageHistory> receivedPage = messageHistoryService.getMessageHistory(
            String.valueOf(userId),
            PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "timestamp"))
        );
        
        Set<Long> contactIds = new HashSet<>();
        Map<Long, MessageHistory> lastMessageMap = new HashMap<>();
        String userIdStr = String.valueOf(userId);
        
        for (MessageHistory message : sentPage.getContent()) {
            String receiver = message.getReceiver();
            if (receiver != null && !receiver.equals(userIdStr)) {
                try {
                    Long contactId = Long.parseLong(receiver);
                    if (!contactIds.contains(contactId)) {
                        contactIds.add(contactId);
                        lastMessageMap.put(contactId, message);
                    }
                } catch (NumberFormatException e) {
                    log.warn("解析联系人ID失败: {}", receiver);
                }
            }
        }
        
        for (MessageHistory message : receivedPage.getContent()) {
            String sender = message.getSender();
            if (sender != null && !sender.equals(userIdStr) && !sender.equals("system")) {
                try {
                    Long contactId = Long.parseLong(sender);
                    if (!contactIds.contains(contactId)) {
                        contactIds.add(contactId);
                        lastMessageMap.put(contactId, message);
                    }
                } catch (NumberFormatException e) {
                    log.warn("解析联系人ID失败: {}", sender);
                }
            }
        }
        
        if (contactIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<User> users = userMapper.selectBatchIds(contactIds);
        Map<Long, User> userMap = users.stream()
            .collect(Collectors.toMap(User::getId, u -> u));
        
        List<ContactVO> contacts = new ArrayList<>();
        for (Long contactId : contactIds) {
            User user = userMap.get(contactId);
            if (user != null) {
                ContactVO contact = new ContactVO();
                contact.setId(user.getId());
                contact.setUsername(user.getUsername());
                contact.setRealName(user.getRealName());
                contact.setAvatar(user.getAvatarUrl());
                
                if (user.getRoleId() != null) {
                    String roleName = userRoleMapper.selectRoleNameByRoleId(user.getRoleId());
                    contact.setRoleName(roleName);
                }
                
                MessageHistory lastMsg = lastMessageMap.get(contactId);
                if (lastMsg != null && lastMsg.getContent() instanceof Map) {
                    Map<?, ?> contentMap = (Map<?, ?>) lastMsg.getContent();
                    Object contentObj = contentMap.get("content");
                    contact.setLastMessage(contentObj != null ? contentObj.toString() : "");
                    contact.setLastMessageTime(lastMsg.getTimestamp() != null 
                        ? lastMsg.getTimestamp().toString() : null);
                }
                
                long unreadCount = getUnreadCountFromContact(String.valueOf(userId), String.valueOf(contactId));
                contact.setUnreadCount((int) unreadCount);
                
                contacts.add(contact);
            }
        }
        
        return contacts;
    }

    @Override
    public void sendPrivateMessage(Long senderId, Long receiverId, String content) {
        log.info("发送私聊消息：senderId={}, receiverId={}, content={}", senderId, receiverId, content);
        
        User sender = userMapper.selectById(senderId);
        
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("content", content);
        contentMap.put("senderId", senderId);
        contentMap.put("senderName", sender != null ? sender.getUsername() : "未知用户");
        contentMap.put("timestamp", System.currentTimeMillis());
        
        Message message = Message.builder()
                .messageId("MSG_PRIVATE_" + System.currentTimeMillis())
                .messageType(MessageTypeEnum.CHAT_MESSAGE)
                .sender(String.valueOf(senderId))
                .receiver(String.valueOf(receiverId))
                .content(contentMap)
                .build();
        
        smartMessagePushService.pushToUserWithOffline(String.valueOf(receiverId), message);
    }

    private MessageVO convertToVO(MessageHistory history) {
        MessageVO vo = new MessageVO();
        vo.setId(history.getId());
        vo.setMessageId(history.getMessageId());
        vo.setSender(history.getSender());
        vo.setReceiver(history.getReceiver());
        vo.setType(history.getMessageType());
        vo.setStatus(history.getStatus());
        vo.setCreateTime(history.getTimestamp() != null ? history.getTimestamp().toString() : null);
        vo.setReadTime(history.getReadTime() != null ? history.getReadTime().toString() : null);
        
        Object content = history.getContent();
        if (content instanceof Map) {
            Map<?, ?> contentMap = (Map<?, ?>) content;
            Object contentObj = contentMap.get("content");
            vo.setContent(contentObj != null ? contentObj.toString() : "");
            
            Object titleObj = contentMap.get("title");
            vo.setTitle(titleObj != null ? titleObj.toString() : "");
            
            if (contentMap.containsKey("senderId")) {
                try {
                    vo.setSenderId(Long.parseLong(contentMap.get("senderId").toString()));
                } catch (NumberFormatException e) {
                    log.warn("解析发送者ID失败: {}", contentMap.get("senderId"));
                }
            }
            if (contentMap.containsKey("senderName")) {
                vo.setSenderName(contentMap.get("senderName").toString());
            }
        } else if (content instanceof String) {
            vo.setContent((String) content);
        }
        
        vo.setIsRead("READ".equals(history.getStatus()) ? 1 : 0);
        
        return vo;
    }
    
    private long getUnreadCountFromContact(String userId, String contactId) {
        List<MessageHistory> unreadMessages = messageHistoryService.getUnreadMessages(userId);
        return unreadMessages.stream()
            .filter(m -> {
                if (m.getContent() instanceof Map) {
                    Map<?, ?> content = (Map<?, ?>) m.getContent();
                    Object senderId = content.get("senderId");
                    return senderId != null && contactId.equals(senderId.toString());
                }
                return false;
            })
            .count();
    }
}
