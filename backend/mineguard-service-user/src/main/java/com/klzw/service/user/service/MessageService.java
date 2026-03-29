package com.klzw.service.user.service;

import com.klzw.service.user.dto.ContactVO;
import com.klzw.service.user.dto.MessageVO;

import java.util.List;

public interface MessageService {

    void sendMessage(Long userId, String title, String content, Integer type);

    void sendBatchMessage(List<Long> userIds, String title, String content, Integer type);

    void sendMessageByRole(String roleCode, String title, String content, Integer type);
    
    List<MessageVO> getUserMessages(Long userId, Integer type);
    
    List<MessageVO> getPrivateMessages(Long userId, Long contactId);
    
    void markAsRead(String messageId);
    
    void markAllAsRead(Long userId);
    
    Integer getUnreadCount(Long userId);
    
    List<ContactVO> getContactList(Long userId);
    
    void sendPrivateMessage(Long senderId, Long receiverId, String content);
}
