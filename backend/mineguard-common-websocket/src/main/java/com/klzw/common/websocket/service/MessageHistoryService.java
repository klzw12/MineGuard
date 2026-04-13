package com.klzw.common.websocket.service;

import com.klzw.common.websocket.domain.Message;
import com.klzw.common.websocket.domain.MessageHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MessageHistoryService {
    MessageHistory save(MessageHistory messageHistory);
    
    MessageHistory saveMessage(Message message);
    
    MessageHistory updateMessageStatus(String messageId, String status);
    
    MessageHistory markAsDelivered(String messageId);
    
    MessageHistory markAsRead(String messageId);
    
    List<MessageHistory> getUndeliveredMessages(String receiver);
    
    List<MessageHistory> getUnreadMessages(String receiver);
    
    Page<MessageHistory> getMessageHistory(String receiver, Pageable pageable);
    
    Page<MessageHistory> getSentMessages(String sender, Pageable pageable);
    
    void deleteExpiredMessages();
    
    List<MessageHistory> getPendingRetryMessages(int maxRetryCount);
    
    void incrementRetryCount(String messageId);
    
    long getUnreadCount(String receiver);
    
    long getUnreadNotificationCount(String receiver);
    
    MessageHistory saveOfflineMessage(String userId, Message message);
    
    List<MessageHistory> getOfflineMessages(String userId);
    
    void markOfflineMessageAsSent(String messageId);
    
    Page<MessageHistory> getPrivateMessages(String userId, String contactId, Pageable pageable);
    
    void markAllMessagesAsRead(String userId);
    
    Page<MessageHistory> getDeadLetterMessages(Pageable pageable);
    
    MessageHistory getById(String id);
    
    void deleteById(String id);
}
