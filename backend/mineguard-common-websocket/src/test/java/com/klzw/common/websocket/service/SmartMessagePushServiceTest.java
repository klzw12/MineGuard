package com.klzw.common.websocket.service;

import com.klzw.common.mq.constant.MqConstants;
import com.klzw.common.mq.producer.IMessageProducer;
import com.klzw.common.websocket.config.WebSocketProperties;
import com.klzw.common.websocket.domain.Message;
import com.klzw.common.websocket.domain.MessageHistory;
import com.klzw.common.websocket.enums.MessageTypeEnum;
import com.klzw.common.websocket.manager.MessageManager;
import com.klzw.common.websocket.manager.OnlineUserManager;
import com.klzw.common.websocket.service.impl.SmartMessagePushServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SmartMessagePushService单元测试")
class SmartMessagePushServiceTest {

    private SmartMessagePushServiceImpl smartMessagePushService;

    @Mock
    private MessagePushService messagePushService;

    @Mock
    private MessageManager messageManager;

    @Mock
    private OnlineUserManager onlineUserManager;

    @Mock
    private MessageHistoryService messageHistoryService;

    @Mock
    private IMessageProducer messageProducer;

    @Mock
    private WebSocketProperties webSocketProperties;

    @BeforeEach
    void setUp() {
        smartMessagePushService = new SmartMessagePushServiceImpl(
                messagePushService, messageManager, onlineUserManager, 
                messageHistoryService, messageProducer);
    }

    @Test
    @DisplayName("推送消息到在线用户")
    void testPushToUser_UserOnline() {
        when(onlineUserManager.isOnline("user-1")).thenReturn(true);
        
        Message message = createTestMessage();
        smartMessagePushService.pushToUser("user-1", message);
        
        verify(messagePushService).pushToUser("user-1", message);
    }

    @Test
    @DisplayName("推送消息到离线用户-跳过")
    void testPushToUser_UserOffline() {
        when(onlineUserManager.isOnline("user-1")).thenReturn(false);
        
        Message message = createTestMessage();
        smartMessagePushService.pushToUser("user-1", message);
        
        verify(messagePushService, never()).pushToUser(anyString(), any());
    }

    @Test
    @DisplayName("智能推送-在线用户")
    void testPushToUserWithOffline_UserOnline() {
        when(onlineUserManager.isOnline("user-1")).thenReturn(true);
        
        Message message = createTestMessage();
        smartMessagePushService.pushToUserWithOffline("user-1", message);
        
        verify(messagePushService).pushToUser("user-1", message);
        verify(messageHistoryService, never()).saveOfflineMessage(anyString(), any());
    }

    @Test
    @DisplayName("智能推送-离线用户")
    void testPushToUserWithOffline_UserOffline() {
        when(onlineUserManager.isOnline("user-1")).thenReturn(false);
        lenient().when(messageHistoryService.saveOfflineMessage(anyString(), any())).thenReturn(new MessageHistory());
        
        Message message = createTestMessage();
        smartMessagePushService.pushToUserWithOffline("user-1", message);
        
        verify(messageHistoryService).saveOfflineMessage("user-1", message);
        verify(messageProducer).sendMessage(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("广播消息")
    void testBroadcast() {
        Message message = createTestMessage();
        smartMessagePushService.broadcast(message);
        
        verify(messagePushService).broadcast(message);
    }

    @Test
    @DisplayName("群播消息")
    void testMulticast() {
        lenient().when(onlineUserManager.isOnline(anyString())).thenReturn(true);
        
        Message message = createTestMessage();
        smartMessagePushService.multicast(Arrays.asList("user-1", "user-2"), message);
        
        verify(messagePushService, times(2)).pushToUser(anyString(), any());
    }

    @Test
    @DisplayName("推送车辆状态")
    void testPushVehicleStatus() {
        when(onlineUserManager.isOnline("user-1")).thenReturn(true);
        
        Map<String, Object> vehicleData = new HashMap<>();
        vehicleData.put("speed", 60.5);
        vehicleData.put("fuel", 75.0);
        
        smartMessagePushService.pushVehicleStatus("user-1", 1001L, vehicleData);
        
        verify(messagePushService).pushToUser(anyString(), any(Message.class));
    }

    @Test
    @DisplayName("推送预警通知")
    void testPushWarningNotification() {
        when(onlineUserManager.isOnline("user-1")).thenReturn(true);
        
        Map<String, Object> warningData = new HashMap<>();
        warningData.put("type", "OVERSPEED");
        warningData.put("level", "HIGH");
        
        smartMessagePushService.pushWarningNotification("user-1", 2001L, warningData);
        
        verify(messagePushService).pushToUser(anyString(), any(Message.class));
    }

    @Test
    @DisplayName("推送调度指令")
    void testPushDispatchCommand() {
        when(onlineUserManager.isOnline("user-1")).thenReturn(true);
        
        Map<String, Object> commandData = new HashMap<>();
        commandData.put("type", "ROUTE_CHANGE");
        
        smartMessagePushService.pushDispatchCommand("user-1", 3001L, commandData);
        
        verify(messagePushService).pushToUser(anyString(), any(Message.class));
    }

    @Test
    @DisplayName("推送行程更新")
    void testPushTripUpdate() {
        when(onlineUserManager.isOnline("user-1")).thenReturn(true);
        
        Map<String, Object> tripData = new HashMap<>();
        tripData.put("status", "IN_PROGRESS");
        
        smartMessagePushService.pushTripUpdate("user-1", 4001L, tripData);
        
        verify(messagePushService).pushToUser(anyString(), any(Message.class));
    }

    @Test
    @DisplayName("推送系统公告")
    void testPushSystemNotice() {
        smartMessagePushService.pushSystemNotice("系统维护", "系统将于今晚维护", "MAINTENANCE");
        
        verify(messagePushService).broadcast(any(Message.class));
    }

    @Test
    @DisplayName("推送离线消息-用户不在线")
    void testPushOfflineMessages_UserOffline() {
        when(onlineUserManager.isOnline("user-1")).thenReturn(false);
        
        smartMessagePushService.pushOfflineMessages("user-1");
        
        verify(messageHistoryService, never()).getOfflineMessages(anyString());
    }

    @Test
    @DisplayName("推送离线消息-无离线消息")
    void testPushOfflineMessages_NoMessages() {
        when(onlineUserManager.isOnline("user-1")).thenReturn(true);
        when(messageHistoryService.getOfflineMessages("user-1")).thenReturn(Collections.emptyList());
        
        smartMessagePushService.pushOfflineMessages("user-1");
        
        verify(messagePushService, never()).pushToUser(anyString(), any());
    }

    @Test
    @DisplayName("推送离线消息-有离线消息")
    void testPushOfflineMessages_HasMessages() {
        when(onlineUserManager.isOnline("user-1")).thenReturn(true);
        
        MessageHistory history = new MessageHistory();
        history.setMessageId("MSG_001");
        history.setMessageType("chat_message");
        history.setSender("user-2");
        history.setReceiver("user-1");
        history.setTimestamp(System.currentTimeMillis());
        history.setContent(new HashMap<>());
        
        when(messageHistoryService.getOfflineMessages("user-1")).thenReturn(Arrays.asList(history));
        
        smartMessagePushService.pushOfflineMessages("user-1");
        
        verify(messagePushService).pushToUser(anyString(), any(Message.class));
        verify(messageHistoryService).markOfflineMessageAsSent("MSG_001");
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
