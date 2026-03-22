package com.klzw.service.user.service.impl;

import com.klzw.common.mq.producer.IMessageProducer;
import com.klzw.common.websocket.manager.MessageManager;
import com.klzw.common.websocket.manager.OnlineUserManager;
import com.klzw.service.user.dto.MessageDTO;
import com.klzw.service.user.entity.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock
    private IMessageProducer messageProducer;

    @Mock
    private MessageManager messageManager;

    @Mock
    private OnlineUserManager onlineUserManager;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private MessageServiceImpl messageService;

    private MessageDTO testMessageDTO;
    private com.klzw.service.user.entity.Message testMessage;

    @BeforeEach
    void setUp() {
        testMessageDTO = new MessageDTO();
        testMessageDTO.setMessageId(UUID.randomUUID().toString());
        testMessageDTO.setSender("admin");
        testMessageDTO.setReceiver("user123");
        testMessageDTO.setType("SYSTEM");
        testMessageDTO.setContent("测试消息内容");
        testMessageDTO.setPriority("HIGH");
        testMessageDTO.setBusinessId("ORDER001");
        testMessageDTO.setBusinessType("ORDER");

        testMessage = new com.klzw.service.user.entity.Message();
        testMessage.setId("msg001");
        testMessage.setMessageId(testMessageDTO.getMessageId());
        testMessage.setSender("admin");
        testMessage.setReceiver("user123");
        testMessage.setType("SYSTEM");
        testMessage.setContent("测试消息内容");
        testMessage.setStatus("UNREAD");
        testMessage.setPriority("HIGH");
        testMessage.setCreatedAt(LocalDateTime.now());
        testMessage.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("发送单播消息-成功")
    void testSendUnicastMessage_Success() {
        messageService.sendUnicastMessage(testMessageDTO);

        verify(mongoTemplate).save(any(com.klzw.service.user.entity.Message.class));
        verify(messageManager).sendMessageToUser(eq("user123"), any(com.klzw.common.websocket.domain.Message.class));
        verify(messageProducer).sendMessage(eq("message-exchange"), eq("message.unicast"), any(MessageDTO.class));
    }

    @Test
    @DisplayName("发送单播消息-无MessageId时自动生成")
    void testSendUnicastMessage_AutoGenerateMessageId() {
        testMessageDTO.setMessageId(null);

        messageService.sendUnicastMessage(testMessageDTO);

        verify(mongoTemplate).save(argThat((com.klzw.service.user.entity.Message msg) -> msg.getMessageId() != null));
    }

    @Test
    @DisplayName("发送单播消息-WebSocket失败时仅记录警告")
    void testSendUnicastMessage_WebSocketFailure_LogWarning() {
        doThrow(new RuntimeException("WebSocket连接失败"))
                .when(messageManager).sendMessageToUser(anyString(), any(com.klzw.common.websocket.domain.Message.class));

        messageService.sendUnicastMessage(testMessageDTO);

        verify(mongoTemplate).save(any(com.klzw.service.user.entity.Message.class));
        verify(messageProducer).sendMessage(anyString(), anyString(), any(MessageDTO.class));
    }

    @Test
    @DisplayName("发送广播消息给特定群体-成功")
    void testSendBroadcastMessage_Success() {
        List<String> userIds = Arrays.asList("user1", "user2", "user3");

        messageService.sendBroadcastMessage(testMessageDTO, userIds);

        verify(mongoTemplate, times(3)).save(any(com.klzw.service.user.entity.Message.class));
        verify(messageManager, times(3)).sendMessageToUser(anyString(), any(com.klzw.common.websocket.domain.Message.class));
        verify(messageProducer).sendMessage(eq("message-exchange"), eq("message.broadcast"), any(MessageDTO.class));
    }

    @Test
    @DisplayName("发送广播消息给特定群体-部分用户离线")
    void testSendBroadcastMessage_PartialUsersOffline() {
        List<String> userIds = Arrays.asList("user1", "user2");

        doThrow(new RuntimeException("用户离线")).when(messageManager).sendMessageToUser(eq("user1"), any(com.klzw.common.websocket.domain.Message.class));

        messageService.sendBroadcastMessage(testMessageDTO, userIds);

        verify(mongoTemplate, times(2)).save(any(com.klzw.service.user.entity.Message.class));
        verify(messageManager).sendMessageToUser(eq("user1"), any(com.klzw.common.websocket.domain.Message.class));
        verify(messageManager).sendMessageToUser(eq("user2"), any(com.klzw.common.websocket.domain.Message.class));
    }

    @Test
    @DisplayName("发送全员广播消息-成功")
    void testSendBroadcastMessageToAll_Success() {
        List<com.klzw.common.websocket.domain.OnlineUser> onlineUsers = Arrays.asList(
                createOnlineUser("user1"),
                createOnlineUser("user2"),
                createOnlineUser("user3")
        );
        when(onlineUserManager.getAllOnlineUsers()).thenReturn(onlineUsers);

        messageService.sendBroadcastMessageToAll(testMessageDTO);

        verify(onlineUserManager).getAllOnlineUsers();
        verify(mongoTemplate, times(3)).save(any(com.klzw.service.user.entity.Message.class));
    }

    @Test
    @DisplayName("发送全员广播消息-无在线用户")
    void testSendBroadcastMessageToAll_NoOnlineUsers() {
        when(onlineUserManager.getAllOnlineUsers()).thenReturn(Arrays.asList());

        messageService.sendBroadcastMessageToAll(testMessageDTO);

        verify(onlineUserManager).getAllOnlineUsers();
        verify(mongoTemplate, never()).save(any(com.klzw.service.user.entity.Message.class));
    }

    @Test
    @DisplayName("获取用户消息列表-成功")
    void testGetUserMessages_Success() {
        List<com.klzw.service.user.entity.Message> messages = Arrays.asList(testMessage);
        when(mongoTemplate.find(any(Query.class), eq(com.klzw.service.user.entity.Message.class))).thenReturn(messages);

        List<com.klzw.service.user.entity.Message> result = messageService.getUserMessages("user123", 1, 10);

        verify(mongoTemplate).find(any(Query.class), eq(com.klzw.service.user.entity.Message.class));
    }

    @Test
    @DisplayName("标记消息为已读-成功")
    void testMarkMessageAsRead_Success() {
        String messageId = "msg001";

        messageService.markMessageAsRead(messageId);

        verify(mongoTemplate).updateFirst(any(Query.class), any(Update.class), eq(com.klzw.service.user.entity.Message.class));
    }

    @Test
    @DisplayName("删除消息-成功")
    void testDeleteMessage_Success() {
        String messageId = "msg001";

        messageService.deleteMessage(messageId);

        verify(mongoTemplate).remove(any(Query.class), eq(com.klzw.service.user.entity.Message.class));
    }

    @Test
    @DisplayName("获取未读消息数量-成功")
    void testGetUnreadMessageCount_Success() {
        when(mongoTemplate.count(any(Query.class), eq(com.klzw.service.user.entity.Message.class))).thenReturn(5L);

        verify(mongoTemplate).count(any(Query.class), eq(com.klzw.service.user.entity.Message.class));
    }

    @Test
    @DisplayName("获取未读消息数量-无未读消息")
    void testGetUnreadMessageCount_Zero() {
        when(mongoTemplate.count(any(Query.class), eq(com.klzw.service.user.entity.Message.class))).thenReturn(0L);

    }

    private com.klzw.common.websocket.domain.OnlineUser createOnlineUser(String userId) {
        com.klzw.common.websocket.domain.OnlineUser user = new com.klzw.common.websocket.domain.OnlineUser();
        user.setUserId(userId);
        user.setUsername("user" + userId);
        return user;
    }
}
