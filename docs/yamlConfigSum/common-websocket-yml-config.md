# common-websocket模块 YML 配置示例

## 1. 完整配置示例

```yaml
mineguard:
  websocket:
    enabled: true
    heartbeat-interval: 30000
    heartbeat-timeout: 60000
    max-connections: 10000
    max-topics-per-user: 50
    max-messages-per-minute: 100
    reconnect-max-retries: 5
    offline-message-expire-days: 7
    use-encryption: false
    encryption-key: ""
    enable-message-persistence: false
    allowed-origins:
      - "http://localhost:3000"
      - "https://example.com"
```

## 2. 最小配置示例

```yaml
mineguard:
  websocket:
    enabled: true
```

## 3. 配置项说明

| 配置项 | 类型 | 默认值 | 说明 |
| ------ | ---- | ------ | ---- |
| `enabled` | boolean | true | 是否启用WebSocket功能 |
| `heartbeat-interval` | int | 30000 | 心跳间隔（毫秒） |
| `heartbeat-timeout` | int | 60000 | 心跳超时时间（毫秒） |
| `max-connections` | int | 10000 | 最大连接数 |
| `max-topics-per-user` | int | 50 | 每个用户最大订阅主题数 |
| `max-messages-per-minute` | int | 100 | 每分钟最大消息数（限流） |
| `reconnect-max-retries` | int | 5 | 最大重连次数 |
| `offline-message-expire-days` | int | 7 | 离线消息过期天数 |
| `use-encryption` | boolean | false | 是否启用消息加密 |
| `encryption-key` | String | "" | 消息加密密钥 |
| `enable-message-persistence` | boolean | false | 是否启用消息持久化 |
| `allowed-origins` | String[] | [] | 允许的跨域来源 |

## 4. 生产环境配置示例

```yaml
mineguard:
  websocket:
    enabled: true
    heartbeat-interval: 30000
    heartbeat-timeout: 60000
    max-connections: 50000
    max-topics-per-user: 100
    max-messages-per-minute: 200
    reconnect-max-retries: 3
    offline-message-expire-days: 30
    use-encryption: true
    encryption-key: "${WEBSOCKET_ENCRYPTION_KEY}"
    enable-message-persistence: true
    allowed-origins:
      - "https://app.example.com"
      - "https://admin.example.com"
```

## 5. 开发环境配置示例

```yaml
mineguard:
  websocket:
    enabled: true
    heartbeat-interval: 60000
    heartbeat-timeout: 120000
    max-connections: 1000
    max-topics-per-user: 20
    max-messages-per-minute: 500
    reconnect-max-retries: 10
    offline-message-expire-days: 1
    use-encryption: false
    enable-message-persistence: false
    allowed-origins:
      - "http://localhost:*"
      - "http://127.0.0.1:*"
```
