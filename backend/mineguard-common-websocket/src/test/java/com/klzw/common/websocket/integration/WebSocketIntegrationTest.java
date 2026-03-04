package com.klzw.common.websocket.integration;

import com.klzw.common.websocket.AbstractWebSocketIntegrationTest;
import com.klzw.common.websocket.domain.Message;
import com.klzw.common.websocket.domain.MessageHistory;
import com.klzw.common.websocket.enums.MessageTypeEnum;
import com.klzw.common.websocket.manager.ConnectionManager;
import com.klzw.common.websocket.manager.OnlineUserManager;
import com.klzw.common.websocket.service.MessageHistoryService;
import com.klzw.common.websocket.service.SmartMessagePushService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WebSocket集成测试-消息推送")
class WebSocketIntegrationTest extends AbstractWebSocketIntegrationTest {

    @Autowired
    private SmartMessagePushService smartMessagePushService;

    @Autowired
    private MessageHistoryService messageHistoryService;

    @Autowired
    private OnlineUserManager onlineUserManager;

    @Autowired
    private ConnectionManager connectionManager;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        connectionManager.clear();
        onlineUserManager.clear();
    }

    @Test
    @DisplayName("Redis连接测试")
    void testRedisConnection() {
        String testKey = "test:websocket:connection";
        String testValue = "test-value-" + System.currentTimeMillis();
        
        redisTemplate.opsForValue().set(testKey, testValue);
        String retrievedValue = redisTemplate.opsForValue().get(testKey);
        
        assertEquals(testValue, retrievedValue);
        
        redisTemplate.delete(testKey);
    }

    @Test
    @DisplayName("在线用户管理-Redis集成")
    void testOnlineUserManager_RedisIntegration() {
        String userId = "test-user-" + System.currentTimeMillis();
        
        assertFalse(onlineUserManager.isOnline(userId));
        
        onlineUserManager.addOnlineUser(userId, "testUser", "DRIVER", "session-" + userId, "192.168.1.1");
        
        assertTrue(onlineUserManager.isOnline(userId));
        
        onlineUserManager.removeOnlineUser(userId);
        
        assertFalse(onlineUserManager.isOnline(userId));
    }

    @Test
    @DisplayName("离线消息存储-MongoDB集成")
    void testOfflineMessageStorage_MongoDBIntegration() {
        String userId = "test-user-" + System.currentTimeMillis();
        
        Map<String, Object> content = new HashMap<>();
        content.put("message", "测试离线消息");
        content.put("timestamp", System.currentTimeMillis());
        
        Message message = Message.builder()
                .messageId("MSG_INTEGRATION_" + System.currentTimeMillis())
                .messageType(MessageTypeEnum.CHAT_MESSAGE)
                .sender("system")
                .receiver(userId)
                .content(content)
                .build();
        
        MessageHistory savedHistory = messageHistoryService.saveOfflineMessage(userId, message);
        
        assertNotNull(savedHistory);
        assertNotNull(savedHistory.getId());
        assertEquals(userId, savedHistory.getReceiver());
        assertEquals("system", savedHistory.getSender());
        
        List<MessageHistory> offlineMessages = messageHistoryService.getOfflineMessages(userId);
        assertFalse(offlineMessages.isEmpty());
    }

    @Test
    @DisplayName("智能消息推送-离线用户")
    void testSmartPush_OfflineUser() {
        String userId = "test-offline-user-" + System.currentTimeMillis();
        
        assertFalse(onlineUserManager.isOnline(userId));
        
        Map<String, Object> content = new HashMap<>();
        content.put("message", "测试离线推送");
        
        Message message = Message.builder()
                .messageId("MSG_OFFLINE_" + System.currentTimeMillis())
                .messageType(MessageTypeEnum.SYSTEM_NOTICE)
                .sender("system")
                .receiver(userId)
                .content(content)
                .build();
        
        smartMessagePushService.pushToUserWithOffline(userId, message);
        
        List<MessageHistory> offlineMessages = messageHistoryService.getOfflineMessages(userId);
        assertFalse(offlineMessages.isEmpty());
    }

    @Test
    @DisplayName("消息历史服务-完整流程")
    void testMessageHistoryService_FullFlow() {
        String userId = "test-history-user-" + System.currentTimeMillis();
        
        for (int i = 0; i < 3; i++) {
            Map<String, Object> content = new HashMap<>();
            content.put("index", i);
            
            Message message = Message.builder()
                    .messageId("MSG_HISTORY_" + i + "_" + System.currentTimeMillis())
                    .messageType(MessageTypeEnum.CHAT_MESSAGE)
                    .sender("user-sender")
                    .receiver(userId)
                    .content(content)
                    .build();
            
            messageHistoryService.saveOfflineMessage(userId, message);
        }
        
        List<MessageHistory> messages = messageHistoryService.getOfflineMessages(userId);
        assertEquals(3, messages.size());
        
        String messageId = messages.get(0).getMessageId();
        messageHistoryService.markOfflineMessageAsSent(messageId);
    }

    @Test
    @DisplayName("系统公告广播")
    void testSystemNoticeBroadcast() {
        assertDoesNotThrow(() -> {
            smartMessagePushService.pushSystemNotice(
                    "集成测试公告", 
                    "这是一条集成测试系统公告", 
                    "TEST"
            );
        });
    }
}
