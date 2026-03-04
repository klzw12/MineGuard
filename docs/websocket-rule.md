# MineGuard WebSocket 路径规则文档

## 1. 概述

本文档定义了 MineGuard 系统中 WebSocket 的路径规则、消息类型和使用规范，确保实时通讯的一致性和可维护性。

## 2. WebSocket 连接路径

### 2.1 基础连接路径

| 路径 | 说明 | 用途 | 认证要求 |
| ---- | ---- | ---- | -------- |
| `/ws` | WebSocket 基础连接路径 | 建立WebSocket连接，进行实时通讯 | 需要 JWT Token 认证 |
| `/ws/message` | 消息通讯路径 | 用于点对点消息通讯 | 需要 JWT Token 认证 |
| `/ws/vehicle` | 车辆状态路径 | 用于车辆状态实时更新 | 需要 JWT Token 认证 |
| `/ws/warning` | 预警通知路径 | 用于预警消息实时推送 | 需要 JWT Token 认证 |
| `/ws/trip` | 行程状态路径 | 用于行程状态实时更新 | 需要 JWT Token 认证 |
| `/ws/system` | 系统公告路径 | 用于系统公告实时推送 | 需要 JWT Token 认证 |

### 2.2 路径详细说明

#### `/ws` - 基础连接路径

- **用途**：建立 WebSocket 连接的入口路径
- **连接方式**：`ws://host:port/ws?token={jwt_token}`
- **认证**：通过 URL 参数传递 JWT Token
- **心跳**：30秒心跳间隔，60秒超时断开
- **重连**：断线后自动重连，最多重试5次

#### `/ws/message` - 消息通讯路径

- **用途**：点对点消息通讯
- **适用场景**：
  - 司机与调度中心通讯
  - 用户之间的即时消息
  - 工作协同消息
- **消息类型**：`chat_message`
- **推送对象**：指定用户

#### `/ws/vehicle` - 车辆状态路径

- **用途**：车辆状态实时更新
- **适用场景**：
  - 车辆位置更新
  - 车辆状态变更（运行、停止、维修等）
  - 车辆油量、速度等实时数据
- **消息类型**：`vehicle_status`
- **推送对象**：调度中心

#### `/ws/warning` - 预警通知路径

- **用途**：预警消息实时推送
- **适用场景**：
  - 车辆超速预警
  - 车辆故障预警
  - 地理围栏越界预警
  - 紧急情况预警
- **消息类型**：`warning_notification`
- **推送对象**：调度中心、司机端

#### `/ws/trip` - 行程状态路径

- **用途**：行程状态实时更新
- **适用场景**：
  - 行程开始/结束通知
  - 行程路线变更
  - 行程状态变更（进行中、已完成、已取消）
- **消息类型**：`trip_update`
- **推送对象**：调度中心、司机端

#### `/ws/system` - 系统公告路径

- **用途**：系统公告实时推送
- **适用场景**：
  - 系统维护通知
  - 重要公告发布
  - 紧急通知
- **消息类型**：`system_notice`
- **推送对象**：所有在线用户

## 3. 消息类型定义

### 3.1 消息类型枚举

| 消息类型 | 枚举值 | 说明 | 优先级 |
| -------- | ------ | ---- | ------ |
| 车辆状态变更 | `vehicle_status` | 车辆位置、状态等实时数据 | 中 |
| 预警通知 | `warning_notification` | 各类预警消息 | 高 |
| 调度指令 | `dispatch_command` | 调度员下发的指令 | 高 |
| 行程状态更新 | `trip_update` | 行程相关状态变更 | 中 |
| 系统公告 | `system_notice` | 系统级别的公告通知 | 低 |
| 聊天消息 | `chat_message` | 点对点聊天消息 | 低 |
| 心跳消息 | `heartbeat` | 连接心跳检测 | 高 |
| 认证消息 | `auth` | 连接认证相关 | 高 |

### 3.2 消息结构

#### 通用消息结构

```json
{
  "messageId": "MSG_20260302_001",
  "messageType": "vehicle_status",
  "sender": "system",
  "receiver": "user_001",
  "timestamp": 1709337600000,
  "priority": "MEDIUM",
  "content": {
    // 具体消息内容
  }
}
```

#### 车辆状态消息示例

```json
{
  "messageId": "MSG_20260302_001",
  "messageType": "vehicle_status",
  "sender": "vehicle_1001",
  "receiver": "dispatcher_001",
  "timestamp": 1709337600000,
  "priority": "MEDIUM",
  "content": {
    "carId": 1001,
    "carNumber": "鄂A12345",
    "status": "running",
    "location": {
      "longitude": 114.123456,
      "latitude": 30.654321
    },
    "speed": 60.5,
    "fuelLevel": 75.3,
    "direction": 180
  }
}
```

#### 预警通知消息示例

```json
{
  "messageId": "MSG_20260302_002",
  "messageType": "warning_notification",
  "sender": "system",
  "receiver": "dispatcher_001",
  "timestamp": 1709337600000,
  "priority": "HIGH",
  "content": {
    "warningId": 2001,
    "warningType": "OVERSPEED",
    "warningLevel": "HIGH",
    "carId": 1001,
    "carNumber": "鄂A12345",
    "driverId": 3001,
    "driverName": "张三",
    "location": {
      "longitude": 114.123456,
      "latitude": 30.654321
    },
    "description": "车辆超速，当前速度：80km/h，限速：60km/h",
    "suggestion": "请立即减速"
  }
}
```

#### 调度指令消息示例

```json
{
  "messageId": "MSG_20260302_003",
  "messageType": "dispatch_command",
  "sender": "dispatcher_001",
  "receiver": "driver_3001",
  "timestamp": 1709337600000,
  "priority": "HIGH",
  "content": {
    "commandId": 4001,
    "commandType": "ROUTE_CHANGE",
    "command": "变更路线",
    "tripId": 5001,
    "newRoute": {
      "startPoint": "武汉市洪山区",
      "endPoint": "武汉市武昌区",
      "waypoints": [
        {"longitude": 114.1, "latitude": 30.6},
        {"longitude": 114.2, "latitude": 30.7}
      ]
    },
    "reason": "原路线拥堵，建议变更路线",
    "urgency": "HIGH"
  }
}
```

#### 行程状态更新消息示例

```json
{
  "messageId": "MSG_20260302_004",
  "messageType": "trip_update",
  "sender": "system",
  "receiver": "dispatcher_001",
  "timestamp": 1709337600000,
  "priority": "MEDIUM",
  "content": {
    "tripId": 5001,
    "carId": 1001,
    "driverId": 3001,
    "status": "IN_PROGRESS",
    "startTime": "2026-03-02 08:00:00",
    "currentLocation": {
      "longitude": 114.123456,
      "latitude": 30.654321
    },
    "distance": 15.5,
    "estimatedArrival": "2026-03-02 09:30:00"
  }
}
```

#### 系统公告消息示例

```json
{
  "messageId": "MSG_20260302_005",
  "messageType": "system_notice",
  "sender": "admin",
  "receiver": "all",
  "timestamp": 1709337600000,
  "priority": "LOW",
  "content": {
    "noticeId": 6001,
    "title": "系统维护通知",
    "content": "系统将于今晚22:00-23:00进行维护，届时服务将暂停",
    "type": "MAINTENANCE",
    "publishTime": "2026-03-02 10:00:00",
    "expireTime": "2026-03-02 23:00:00"
  }
}
```

## 4. 连接管理规则

### 4.1 连接建立

#### 连接流程

1. 客户端发起 WebSocket 连接请求：`ws://host:port/ws?token={jwt_token}`
2. 服务端验证 JWT Token
3. 验证成功后建立连接，返回连接成功消息
4. 客户端开始心跳检测

#### 连接成功消息

```json
{
  "messageId": "MSG_CONN_001",
  "messageType": "auth",
  "sender": "system",
  "receiver": "user_001",
  "timestamp": 1709337600000,
  "content": {
    "status": "SUCCESS",
    "userId": "user_001",
    "username": "张三",
    "role": "DRIVER",
    "sessionId": "SESSION_001"
  }
}
```

#### 连接失败消息

```json
{
  "messageId": "MSG_CONN_002",
  "messageType": "auth",
  "sender": "system",
  "receiver": "unknown",
  "timestamp": 1709337600000,
  "content": {
    "status": "FAILED",
    "errorCode": "TOKEN_INVALID",
    "errorMessage": "Token无效或已过期"
  }
}
```

### 4.2 心跳检测

#### 心跳请求

```json
{
  "messageId": "MSG_HEARTBEAT_001",
  "messageType": "heartbeat",
  "sender": "user_001",
  "receiver": "system",
  "timestamp": 1709337600000,
  "content": {
    "type": "PING"
  }
}
```

#### 心跳响应

```json
{
  "messageId": "MSG_HEARTBEAT_002",
  "messageType": "heartbeat",
  "sender": "system",
  "receiver": "user_001",
  "timestamp": 1709337600000,
  "content": {
    "type": "PONG"
  }
}
```

#### 心跳规则

- **心跳间隔**：30秒
- **超时时间**：60秒
- **重试次数**：最多5次
- **超时处理**：超时后断开连接，客户端需要重新连接

### 4.3 断线重连

#### 重连策略

1. 检测到连接断开
2. 等待1秒后尝试重连
3. 重连失败则等待2秒、4秒、8秒、16秒（指数退避）
4. 最多重试5次
5. 5次重试失败后，提示用户手动重连

#### 重连成功消息

```json
{
  "messageId": "MSG_RECONN_001",
  "messageType": "auth",
  "sender": "system",
  "receiver": "user_001",
  "timestamp": 1709337600000,
  "content": {
    "status": "RECONNECTED",
    "userId": "user_001",
    "sessionId": "SESSION_002"
  }
}
```

### 4.4 在线用户管理

#### 用户上线

- 连接建立成功后，将用户加入在线用户列表
- 记录用户ID、用户名、角色、连接时间、SessionID

#### 用户下线

- 连接断开后，将用户从在线用户列表移除
- 记录下线时间、下线原因

#### 在线状态查询

```json
{
  "messageId": "MSG_ONLINE_001",
  "messageType": "online_status",
  "sender": "dispatcher_001",
  "receiver": "system",
  "timestamp": 1709337600000,
  "content": {
    "action": "QUERY",
    "userIds": ["user_001", "user_002", "user_003"]
  }
}
```

#### 在线状态响应

```json
{
  "messageId": "MSG_ONLINE_002",
  "messageType": "online_status",
  "sender": "system",
  "receiver": "dispatcher_001",
  "timestamp": 1709337600000,
  "content": {
    "onlineUsers": [
      {"userId": "user_001", "status": "ONLINE", "lastActiveTime": 1709337500000},
      {"userId": "user_002", "status": "OFFLINE", "lastActiveTime": 1709336500000},
      {"userId": "user_003", "status": "ONLINE", "lastActiveTime": 1709337400000}
    ]
  }
}
```

## 5. 消息推送策略

### 5.1 单播（Unicast）

- **定义**：向指定用户推送消息
- **场景**：点对点消息、个人通知
- **示例**：向司机推送调度指令

```json
{
  "messageType": "dispatch_command",
  "receiver": "driver_3001",
  "content": {
    "command": "变更路线"
  }
}
```

### 5.2 广播（Broadcast）

- **定义**：向所有在线用户推送消息
- **场景**：系统公告、紧急通知
- **示例**：系统维护通知

```json
{
  "messageType": "system_notice",
  "receiver": "all",
  "content": {
    "title": "系统维护通知"
  }
}
```

### 5.3 群播（Multicast）

- **定义**：向指定角色或部门推送消息
- **场景**：角色通知、部门消息
- **示例**：向所有司机推送预警通知

```json
{
  "messageType": "warning_notification",
  "receiver": "role:DRIVER",
  "content": {
    "warningType": "OVERSPEED"
  }
}
```

### 5.4 订阅推送（Subscribe）

- **定义**：用户订阅主题后接收相关消息
- **场景**：车辆状态订阅、预警订阅
- **示例**：订阅车辆状态更新

#### 订阅请求

```json
{
  "messageId": "MSG_SUB_001",
  "messageType": "subscribe",
  "sender": "dispatcher_001",
  "receiver": "system",
  "timestamp": 1709337600000,
  "content": {
    "action": "SUBSCRIBE",
    "topics": ["vehicle_1001", "vehicle_1002"]
  }
}
```

#### 订阅成功响应

```json
{
  "messageId": "MSG_SUB_002",
  "messageType": "subscribe",
  "sender": "system",
  "receiver": "dispatcher_001",
  "timestamp": 1709337600000,
  "content": {
    "status": "SUCCESS",
    "topics": ["vehicle_1001", "vehicle_1002"]
  }
}
```

#### 取消订阅请求

```json
{
  "messageId": "MSG_UNSUB_001",
  "messageType": "subscribe",
  "sender": "dispatcher_001",
  "receiver": "system",
  "timestamp": 1709337600000,
  "content": {
    "action": "UNSUBSCRIBE",
    "topics": ["vehicle_1001"]
  }
}
```

## 6. 消息确认机制

### 6.1 需要确认的消息类型

以下消息类型需要接收方确认：

- `dispatch_command`：调度指令
- `warning_notification`：预警通知（高级别）
- `system_notice`：系统公告（重要）

### 6.2 消息确认流程

1. 服务端发送消息，设置 `requireAck: true`
2. 客户端收到消息后，发送确认消息
3. 服务端收到确认后，标记消息已送达
4. 如果未收到确认，服务端重发消息（最多3次）

#### 需要确认的消息

```json
{
  "messageId": "MSG_ACK_001",
  "messageType": "dispatch_command",
  "sender": "dispatcher_001",
  "receiver": "driver_3001",
  "timestamp": 1709337600000,
  "requireAck": true,
  "content": {
    "command": "变更路线"
  }
}
```

#### 确认消息

```json
{
  "messageId": "MSG_ACK_002",
  "messageType": "ack",
  "sender": "driver_3001",
  "receiver": "dispatcher_001",
  "timestamp": 1709337600000,
  "content": {
    "originalMessageId": "MSG_ACK_001",
    "status": "RECEIVED"
  }
}
```

## 7. 错误处理

### 7.1 错误消息格式

```json
{
  "messageId": "MSG_ERROR_001",
  "messageType": "error",
  "sender": "system",
  "receiver": "user_001",
  "timestamp": 1709337600000,
  "content": {
    "errorCode": "INVALID_MESSAGE_FORMAT",
    "errorMessage": "消息格式无效",
    "originalMessageId": "MSG_001"
  }
}
```

### 7.2 错误码定义

| 错误码 | 说明 |
| ------ | ---- |
| `INVALID_MESSAGE_FORMAT` | 消息格式无效 |
| `UNKNOWN_MESSAGE_TYPE` | 未知的消息类型 |
| `AUTHENTICATION_FAILED` | 认证失败 |
| `AUTHORIZATION_FAILED` | 授权失败 |
| `USER_NOT_ONLINE` | 用户不在线 |
| `SUBSCRIPTION_FAILED` | 订阅失败 |
| `RATE_LIMIT_EXCEEDED` | 消息频率超限 |

## 8. 安全规范

### 8.1 认证授权

- 所有 WebSocket 连接必须携带有效的 JWT Token
- Token 过期后需要重新连接
- 不同角色的用户有不同的消息订阅权限

### 8.2 消息过滤

- 服务端对发送的消息进行权限验证
- 用户只能订阅自己有权限的主题
- 敏感消息需要加密传输

### 8.3 频率限制

- 单个用户消息发送频率：100条/分钟
- 单个用户订阅主题数量：50个
- 超过限制后返回 `RATE_LIMIT_EXCEEDED` 错误

## 9. 性能优化

### 9.1 连接池管理

- 使用连接池管理 WebSocket 连接
- 最大连接数：10000
- 连接超时时间：60秒

### 9.2 消息队列

- 使用消息队列缓冲消息
- 消息持久化到 MongoDB
- 离线消息保存7天

### 9.3 负载均衡

- 使用 Nginx 进行 WebSocket 负载均衡
- 支持粘性会话（Sticky Session）
- 支持集群部署

## 10. 监控与日志

### 10.1 监控指标

- 在线用户数
- 消息发送速率
- 消息送达率
- 连接成功率
- 平均响应时间

### 10.2 日志记录

- 连接建立/断开日志
- 消息发送/接收日志
- 错误日志
- 性能日志

## 11. 使用示例

### 11.1 JavaScript 客户端示例

```javascript
// 建立 WebSocket 连接
const token = 'your_jwt_token';
const ws = new WebSocket(`ws://localhost:8080/ws?token=${token}`);

// 连接成功
ws.onopen = function(event) {
  console.log('WebSocket 连接成功');
  
  // 订阅车辆状态
  ws.send(JSON.stringify({
    messageId: 'MSG_SUB_001',
    messageType: 'subscribe',
    sender: 'dispatcher_001',
    receiver: 'system',
    timestamp: Date.now(),
    content: {
      action: 'SUBSCRIBE',
      topics: ['vehicle_1001', 'vehicle_1002']
    }
  }));
};

// 接收消息
ws.onmessage = function(event) {
  const message = JSON.parse(event.data);
  console.log('收到消息:', message);
  
  // 处理不同类型的消息
  switch (message.messageType) {
    case 'vehicle_status':
      handleVehicleStatus(message);
      break;
    case 'warning_notification':
      handleWarningNotification(message);
      break;
    case 'heartbeat':
      handleHeartbeat(message);
      break;
  }
};

// 连接关闭
ws.onclose = function(event) {
  console.log('WebSocket 连接关闭');
};

// 连接错误
ws.onerror = function(error) {
  console.error('WebSocket 错误:', error);
};

// 发送心跳
function sendHeartbeat() {
  ws.send(JSON.stringify({
    messageId: 'MSG_HEARTBEAT_001',
    messageType: 'heartbeat',
    sender: 'dispatcher_001',
    receiver: 'system',
    timestamp: Date.now(),
    content: {
      type: 'PING'
    }
  }));
}

// 定时发送心跳
setInterval(sendHeartbeat, 30000);
```

### 11.2 Java 服务端示例

```java
@Component
public class WebSocketHandler extends TextWebSocketHandler {
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 连接建立后的处理
        String token = getTokenFromSession(session);
        UserInfo userInfo = validateToken(token);
        if (userInfo != null) {
            onlineUserManager.addUser(userInfo.getUserId(), session);
            sendConnectionSuccessMessage(session, userInfo);
        } else {
            sendConnectionFailedMessage(session);
            session.close();
        }
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 处理接收到的消息
        Message msg = parseMessage(message.getPayload());
        
        switch (msg.getMessageType()) {
            case "heartbeat":
                handleHeartbeat(session, msg);
                break;
            case "subscribe":
                handleSubscribe(session, msg);
                break;
            case "vehicle_status":
                handleVehicleStatus(session, msg);
                break;
            // 其他消息类型处理
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // 连接关闭后的处理
        onlineUserManager.removeUser(session);
    }
}
```

## 12. 附录

### 12.1 消息类型完整列表

| 消息类型 | 说明 | 路径 | 推送对象 | 优先级 | 需要确认 |
| -------- | ---- | ---- | -------- | ------ | -------- |
| `vehicle_status` | 车辆状态变更 | `/ws/vehicle` | 调度中心 | 中 | 否 |
| `warning_notification` | 预警通知 | `/ws/warning` | 调度中心、司机端 | 高 | 是（高级别） |
| `dispatch_command` | 调度指令 | `/ws/message` | 司机端 | 高 | 是 |
| `trip_update` | 行程状态更新 | `/ws/trip` | 调度中心、司机端 | 中 | 否 |
| `system_notice` | 系统公告 | `/ws/system` | 所有用户 | 低 | 是（重要） |
| `chat_message` | 聊天消息 | `/ws/message` | 指定用户 | 低 | 否 |
| `heartbeat` | 心跳消息 | `/ws` | 系统 | 高 | 否 |
| `auth` | 认证消息 | `/ws` | 系统 | 高 | 否 |
| `subscribe` | 订阅消息 | `/ws` | 系统 | 高 | 否 |
| `ack` | 确认消息 | `/ws` | 系统 | 高 | 否 |
| `error` | 错误消息 | `/ws` | 系统 | 高 | 否 |
| `online_status` | 在线状态 | `/ws` | 系统 | 中 | 否 |

### 12.2 优先级定义

| 优先级 | 说明 | 处理方式 |
| ------ | ---- | -------- |
| `HIGH` | 高优先级 | 立即处理，优先推送 |
| `MEDIUM` | 中优先级 | 正常处理，按顺序推送 |
| `LOW` | 低优先级 | 延迟处理，批量推送 |

### 12.3 角色权限

| 角色 | 可订阅主题 | 可发送消息类型 |
| ---- | ---------- | -------------- |
| `ADMIN` | 所有主题 | 所有类型 |
| `DISPATCHER` | 车辆、预警、行程、系统 | 调度指令、聊天消息 |
| `DRIVER` | 个人车辆、预警、行程、系统 | 车辆状态、聊天消息 |
| `USER` | 系统、个人消息 | 聊天消息 |

---

**文档版本**：v1.1  
**最后更新**：2026-03-05  
**维护者**：MineGuard 架构团队

---

## 13. 实现说明

### 13.1 模块结构

WebSocket模块位于 `mineguard-common-websocket`，主要组件：

| 组件 | 类 | 说明 |
| ---- | -- | ---- |
| 配置 | `WebSocketConfig` | 路径注册、跨域配置 |
| 配置属性 | `WebSocketProperties` | 心跳、连接数、限流等配置 |
| 处理器 | `WebSocketHandler` | 消息分发、连接管理 |
| 连接管理 | `ConnectionManager` | 会话、订阅管理 |
| 消息管理 | `MessageManager` | 消息发送、广播、限流 |
| 在线用户 | `OnlineUserManager` | 在线用户Redis存储 |
| 推送服务 | `MessagePushService` | 业务友好的推送接口 |
| 心跳检测 | `HeartbeatCheckTask` | 定时检测超时连接 |

### 13.2 错误码范围

WebSocket模块错误码范围：**1500-1518**

| 错误码 | 说明 |
| ------ | ---- |
| 1500 | CONNECTION_FAILED - 连接失败 |
| 1501 | CONNECTION_TIMEOUT - 连接超时 |
| 1502 | CONNECTION_CLOSED - 连接已关闭 |
| 1503 | AUTHENTICATION_FAILED - 认证失败 |
| 1504 | TOKEN_INVALID - Token无效或已过期 |
| 1505 | TOKEN_MISSING - Token缺失 |
| 1506 | MESSAGE_SEND_FAILED - 消息发送失败 |
| 1507 | MESSAGE_FORMAT_INVALID - 消息格式无效 |
| 1508 | MESSAGE_TYPE_UNKNOWN - 未知的消息类型 |
| 1509 | SUBSCRIBE_FAILED - 订阅失败 |
| 1510 | UNSUBSCRIBE_FAILED - 取消订阅失败 |
| 1511 | USER_NOT_ONLINE - 用户不在线 |
| 1512 | USER_ALREADY_ONLINE - 用户已在线 |
| 1513 | SESSION_NOT_FOUND - 会话不存在 |
| 1514 | HEARTBEAT_TIMEOUT - 心跳超时 |
| 1515 | RATE_LIMIT_EXCEEDED - 消息频率超限 |
| 1516 | TOPIC_NOT_FOUND - 主题不存在 |
| 1517 | PERMISSION_DENIED - 权限不足 |
| 1518 | INTERNAL_ERROR - 内部错误 |

### 13.3 配置项

| 配置项 | 默认值 | 说明 |
| ------ | ------ | ---- |
| `websocket.enabled` | true | 是否启用WebSocket |
| `websocket.heartbeat-interval` | 30000 | 心跳间隔（毫秒） |
| `websocket.heartbeat-timeout` | 60000 | 心跳超时（毫秒） |
| `websocket.max-connections` | 10000 | 最大连接数 |
| `websocket.max-topics-per-user` | 50 | 每用户最大订阅主题数 |
| `websocket.max-messages-per-minute` | 100 | 每分钟最大消息数 |
| `websocket.reconnect-max-retries` | 5 | 最大重连次数 |
| `websocket.offline-message-expire-days` | 7 | 离线消息过期天数 |
| `websocket.use-encryption` | false | 是否启用消息加密 |
| `websocket.enable-message-persistence` | false | 是否启用消息持久化 |
| `websocket.allowed-origins` | [] | 允许的跨域源 |

### 13.4 业务推送服务使用

业务模块可通过注入 `SmartMessagePushService` 推送消息（推荐）：

```java
@Autowired
private SmartMessagePushService smartMessagePushService;

// 智能推送（在线WebSocket实时推送，离线存储到MongoDB + MQ异步推送）
smartMessagePushService.pushToUserWithOffline(userId, message);

// 仅在线推送（用户离线时跳过）
smartMessagePushService.pushToUser(userId, message);

// 推送车辆状态（自动处理离线）
public void notifyVehicleStatus(String userId, Long carId, Map<String, Object> vehicleData) {
    smartMessagePushService.pushVehicleStatus(userId, carId, vehicleData);
}

// 推送预警通知（自动处理离线）
public void notifyWarning(String userId, Long warningId, Map<String, Object> warningData) {
    smartMessagePushService.pushWarningNotification(userId, warningId, warningData);
}

// 推送调度指令（自动处理离线）
public void sendDispatchCommand(String userId, Long commandId, Map<String, Object> commandData) {
    smartMessagePushService.pushDispatchCommand(userId, commandId, commandData);
}

// 推送行程更新（自动处理离线）
public void notifyTripUpdate(String userId, Long tripId, Map<String, Object> tripData) {
    smartMessagePushService.pushTripUpdate(userId, tripId, tripData);
}

// 推送系统公告（广播）
public void broadcastSystemNotice(String title, String content, String type) {
    smartMessagePushService.pushSystemNotice(title, content, type);
}
```

也可使用基础 `MessagePushService`（仅在线推送）：

```java
@Autowired
private MessagePushService messagePushService;

// 推送车辆状态
public void notifyVehicleStatus(String userId, Long carId, Map<String, Object> vehicleData) {
    messagePushService.pushVehicleStatus(userId, carId, vehicleData);
}

// 推送预警通知
public void notifyWarning(String userId, Long warningId, Map<String, Object> warningData) {
    messagePushService.pushWarningNotification(userId, warningId, warningData);
}

// 推送调度指令
public void sendDispatchCommand(String userId, Long commandId, Map<String, Object> commandData) {
    messagePushService.pushDispatchCommand(userId, commandId, commandData);
}

// 推送行程更新
public void notifyTripUpdate(String userId, Long tripId, Map<String, Object> tripData) {
    messagePushService.pushTripUpdate(userId, tripId, tripData);
}

// 推送系统公告
public void broadcastSystemNotice(String title, String content, String type) {
    messagePushService.pushSystemNotice(title, content, type);
}
```

### 13.5 依赖模块

| 模块 | 用途 |
| ---- | ---- |
| mineguard-common-core | 核心工具类、异常处理 |
| mineguard-common-auth | JWT Token验证 |
| mineguard-common-redis | 在线用户存储 |
| mineguard-common-mongodb | 消息历史存储（可选） |
| mineguard-common-mq | 离线消息异步推送 |

### 13.6 智能推送服务架构

```
业务服务 → SmartMessagePushService → 用户在线？→ WebSocket实时推送
                                      → 用户离线？→ MongoDB存储 + MQ异步推送
```

**离线消息流程：**

1. **消息发送**：用户离线时，消息存储到MongoDB + 发送到MQ
2. **用户上线**：WebSocket连接建立后，自动推送离线消息
3. **MQ消费**：用户上线后MQ消息到达时，检查在线状态并推送
4. **消息确认**：推送成功后标记消息状态为已发送
