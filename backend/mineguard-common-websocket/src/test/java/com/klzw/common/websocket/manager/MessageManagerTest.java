package com.klzw.common.websocket.manager;

import com.klzw.common.websocket.properties.WebSocketProperties;
import com.klzw.common.websocket.constant.WebSocketResultCode;
import com.klzw.common.websocket.domain.ConnectionInfo;
import com.klzw.common.websocket.domain.Message;
import com.klzw.common.websocket.enums.MessageTypeEnum;
import com.klzw.common.websocket.exception.WebSocketException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageManager单元测试")
class MessageManagerTest {

    private MessageManager messageManager;

    @Mock
    private ConnectionManager connectionManager;

    @Mock
    private WebSocketProperties webSocketProperties;

    @Mock
    private WebSocketSession session;

    @Mock
    private ConnectionInfo connectionInfo;

    @BeforeEach
    void setUp() {
        messageManager = new MessageManager(connectionManager, webSocketProperties);
        
        lenient().when(webSocketProperties.isUseEncryption()).thenReturn(false);
        lenient().when(webSocketProperties.getMaxMessagesPerMinute()).thenReturn(100);
    }

    @Test
    @DisplayName("发送消息-正常")
    void testSendMessage() throws Exception {
        when(connectionManager.getSession("session-1")).thenReturn(session);
        when(session.isOpen()).thenReturn(true);
        
        Message message = createTestMessage();
        
        assertDoesNotThrow(() -> messageManager.sendMessage("session-1", message));
        
        verify(session).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("发送消息-会话不存在")
    void testSendMessage_SessionNotFound() {
        when(connectionManager.getSession("session-1")).thenReturn(null);
        
        Message message = createTestMessage();
        
        WebSocketException exception = assertThrows(WebSocketException.class, 
                () -> messageManager.sendMessage("session-1", message));
        
        assertEquals(WebSocketResultCode.SESSION_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("发送消息-会话已关闭")
    void testSendMessage_SessionClosed() {
        when(connectionManager.getSession("session-1")).thenReturn(session);
        when(session.isOpen()).thenReturn(false);
        
        Message message = createTestMessage();
        
        WebSocketException exception = assertThrows(WebSocketException.class, 
                () -> messageManager.sendMessage("session-1", message));
        
        assertEquals(WebSocketResultCode.SESSION_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("发送消息到用户-正常")
    void testSendMessageToUser() throws Exception {
        when(connectionManager.getSessionByUserId("user-1")).thenReturn(session);
        when(session.getId()).thenReturn("session-1");
        when(session.isOpen()).thenReturn(true);
        when(connectionManager.getSession("session-1")).thenReturn(session);
        
        Message message = createTestMessage();
        
        assertDoesNotThrow(() -> messageManager.sendMessageToUser("user-1", message));
    }

    @Test
    @DisplayName("发送消息到用户-用户不在线")
    void testSendMessageToUser_UserNotOnline() {
        when(connectionManager.getSessionByUserId("user-1")).thenReturn(null);
        
        Message message = createTestMessage();
        
        WebSocketException exception = assertThrows(WebSocketException.class, 
                () -> messageManager.sendMessageToUser("user-1", message));
        
        assertEquals(WebSocketResultCode.USER_NOT_ONLINE.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("广播消息")
    void testBroadcast() throws Exception {
        when(connectionManager.getAllConnections()).thenReturn(Arrays.asList(connectionInfo, connectionInfo));
        when(connectionInfo.getSessionId()).thenReturn("session-1");
        when(connectionManager.getSession("session-1")).thenReturn(session);
        when(session.isOpen()).thenReturn(true);
        
        Message message = createTestMessage();
        
        assertDoesNotThrow(() -> messageManager.broadcast(message));
        
        verify(session, times(2)).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("群播消息")
    void testMulticast() throws Exception {
        when(connectionManager.getSessionByUserId("user-1")).thenReturn(session);
        when(connectionManager.getSessionByUserId("user-2")).thenReturn(session);
        when(session.getId()).thenReturn("session-1");
        when(session.isOpen()).thenReturn(true);
        when(connectionManager.getSession("session-1")).thenReturn(session);
        
        Message message = createTestMessage();
        
        assertDoesNotThrow(() -> messageManager.multicast(Arrays.asList("user-1", "user-2"), message));
    }

    @Test
    @DisplayName("发送消息到角色")
    void testSendToRole() throws Exception {
        when(connectionManager.getAllConnections()).thenReturn(Arrays.asList(connectionInfo));
        when(connectionInfo.getRole()).thenReturn("DRIVER");
        when(connectionInfo.getSessionId()).thenReturn("session-1");
        when(connectionManager.getSession("session-1")).thenReturn(session);
        when(session.isOpen()).thenReturn(true);
        
        Message message = createTestMessage();
        
        assertDoesNotThrow(() -> messageManager.sendToRole("DRIVER", message));
    }

    @Test
    @DisplayName("发送消息到主题")
    void testSendToTopic() throws Exception {
        when(connectionManager.getAllConnections()).thenReturn(Arrays.asList(connectionInfo));
        when(connectionInfo.isSubscribed("vehicle-1001")).thenReturn(true);
        when(connectionInfo.getSessionId()).thenReturn("session-1");
        when(connectionManager.getSession("session-1")).thenReturn(session);
        when(session.isOpen()).thenReturn(true);
        
        Message message = createTestMessage();
        
        assertDoesNotThrow(() -> messageManager.sendToTopic("vehicle-1001", message));
    }

    @Test
    @DisplayName("重置频率限制")
    void testResetRateLimit() {
        assertDoesNotThrow(() -> messageManager.resetRateLimit());
    }

    @Test
    @DisplayName("重置指定用户频率限制")
    void testResetRateLimitForUser() {
        assertDoesNotThrow(() -> messageManager.resetRateLimit("user-1"));
    }

    private Message createTestMessage() {
        Map<String, Object> content = new HashMap<>();
        content.put("test", "data");
        
        return Message.builder()
                .messageId("MSG_TEST_001")
                .messageType(MessageTypeEnum.CHAT_MESSAGE)
                .sender("user-1")
                .receiver("user-2")
                .content(content)
                .build();
    }
}
