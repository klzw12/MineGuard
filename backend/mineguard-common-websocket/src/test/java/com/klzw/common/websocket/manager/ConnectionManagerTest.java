package com.klzw.common.websocket.manager;

import com.klzw.common.websocket.constant.WebSocketResultCode;
import com.klzw.common.websocket.domain.ConnectionInfo;
import com.klzw.common.websocket.exception.WebSocketException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.WebSocketSession;

import java.net.InetSocketAddress;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConnectionManager单元测试")
class ConnectionManagerTest {

    private ConnectionManager connectionManager;

    @Mock
    private WebSocketSession session1;

    @Mock
    private WebSocketSession session2;

    @BeforeEach
    void setUp() {
        connectionManager = new ConnectionManager();
        
        lenient().when(session1.getId()).thenReturn("session-1");
        lenient().when(session2.getId()).thenReturn("session-2");
        lenient().when(session1.getRemoteAddress()).thenReturn(new InetSocketAddress("192.168.1.1", 8080));
        lenient().when(session2.getRemoteAddress()).thenReturn(new InetSocketAddress("192.168.1.2", 8080));
    }

    @Test
    @DisplayName("添加连接-正常")
    void testAddConnection() {
        connectionManager.addConnection(session1, "user-1", "张三", "DRIVER");
        
        assertTrue(connectionManager.isOnline("user-1"));
        assertEquals(1, connectionManager.getOnlineCount());
        
        ConnectionInfo info = connectionManager.getConnection("session-1");
        assertNotNull(info);
        assertEquals("user-1", info.getUserId());
        assertEquals("张三", info.getUsername());
        assertEquals("DRIVER", info.getRole());
    }

    @Test
    @DisplayName("移除连接-正常")
    void testRemoveConnection() {
        connectionManager.addConnection(session1, "user-1", "张三", "DRIVER");
        connectionManager.removeConnection("session-1");
        
        assertFalse(connectionManager.isOnline("user-1"));
        assertEquals(0, connectionManager.getOnlineCount());
        assertNull(connectionManager.getConnection("session-1"));
    }

    @Test
    @DisplayName("根据用户ID获取连接")
    void testGetConnectionByUserId() {
        connectionManager.addConnection(session1, "user-1", "张三", "DRIVER");
        
        ConnectionInfo info = connectionManager.getConnectionByUserId("user-1");
        assertNotNull(info);
        assertEquals("session-1", info.getSessionId());
    }

    @Test
    @DisplayName("获取会话")
    void testGetSession() {
        connectionManager.addConnection(session1, "user-1", "张三", "DRIVER");
        
        WebSocketSession session = connectionManager.getSession("session-1");
        assertNotNull(session);
        assertEquals("session-1", session.getId());
    }

    @Test
    @DisplayName("根据用户ID获取会话")
    void testGetSessionByUserId() {
        connectionManager.addConnection(session1, "user-1", "张三", "DRIVER");
        
        WebSocketSession session = connectionManager.getSessionByUserId("user-1");
        assertNotNull(session);
    }

    @Test
    @DisplayName("更新最后活跃时间")
    void testUpdateLastActiveTime() {
        connectionManager.addConnection(session1, "user-1", "张三", "DRIVER");
        
        ConnectionInfo info = connectionManager.getConnection("session-1");
        var beforeTime = info.getLastActiveTime();
        
        connectionManager.updateLastActiveTime("session-1");
        
        var afterTime = info.getLastActiveTime();
        assertTrue(afterTime.isAfter(beforeTime) || afterTime.isEqual(beforeTime));
    }

    @Test
    @DisplayName("订阅主题-正常")
    void testSubscribeTopic() {
        connectionManager.addConnection(session1, "user-1", "张三", "DRIVER");
        
        connectionManager.subscribeTopic("session-1", "vehicle-1001");
        
        ConnectionInfo info = connectionManager.getConnection("session-1");
        assertTrue(info.isSubscribed("vehicle-1001"));
    }

    @Test
    @DisplayName("订阅主题-会话不存在")
    void testSubscribeTopic_SessionNotFound() {
        WebSocketException exception = assertThrows(WebSocketException.class, 
                () -> connectionManager.subscribeTopic("non-existent", "vehicle-1001"));
        
        assertEquals(WebSocketResultCode.SESSION_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("取消订阅主题")
    void testUnsubscribeTopic() {
        connectionManager.addConnection(session1, "user-1", "张三", "DRIVER");
        connectionManager.subscribeTopic("session-1", "vehicle-1001");
        connectionManager.unsubscribeTopic("session-1", "vehicle-1001");
        
        ConnectionInfo info = connectionManager.getConnection("session-1");
        assertFalse(info.isSubscribed("vehicle-1001"));
    }

    @Test
    @DisplayName("获取所有连接")
    void testGetAllConnections() {
        connectionManager.addConnection(session1, "user-1", "张三", "DRIVER");
        connectionManager.addConnection(session2, "user-2", "李四", "DISPATCHER");
        
        Collection<ConnectionInfo> connections = connectionManager.getAllConnections();
        assertEquals(2, connections.size());
    }

    @Test
    @DisplayName("清空所有连接")
    void testClear() {
        connectionManager.addConnection(session1, "user-1", "张三", "DRIVER");
        connectionManager.addConnection(session2, "user-2", "李四", "DISPATCHER");
        
        connectionManager.clear();
        
        assertEquals(0, connectionManager.getOnlineCount());
        assertFalse(connectionManager.isOnline("user-1"));
        assertFalse(connectionManager.isOnline("user-2"));
    }
}
