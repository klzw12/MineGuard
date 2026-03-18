package com.klzw.service.user.service;

import com.klzw.service.user.dto.MessageDTO;
import com.klzw.service.user.entity.Message;

import java.util.List;

public interface MessageService {
    /**
     * 发送单播消息
     * @param messageDTO 消息DTO
     */
    void sendUnicastMessage(MessageDTO messageDTO);

    /**
     * 发送广播消息给特定群体
     * @param messageDTO 消息DTO
     * @param userIds 用户ID列表
     */
    void sendBroadcastMessage(MessageDTO messageDTO, List<String> userIds);

    /**
     * 发送广播消息给所有在线用户
     * @param messageDTO 消息DTO
     */
    void sendBroadcastMessageToAll(MessageDTO messageDTO);

    /**
     * 获取用户的消息列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 消息列表
     */
    List<Message> getUserMessages(String userId, int page, int size);

    /**
     * 标记消息为已读
     * @param messageId 消息ID
     */
    void markMessageAsRead(String messageId);

    /**
     * 删除消息
     * @param messageId 消息ID
     */
    void deleteMessage(String messageId);

    /**
     * 获取用户未读消息数量
     * @param userId 用户ID
     * @return 未读消息数量
     */
    long getUnreadMessageCount(String userId);
}