# common-redis模块 YML 配置示例

## 1. 完整配置示例

```yaml
# Redis模块配置
mineguard:
  redis:
    # 是否启用Redis模块，默认值：true
    enabled: true
    # 缓存键前缀，默认值：mineguard:
    key-prefix: "mineguard:"
    # 默认缓存过期时间（秒），默认值：3600
    default-expire: 3600
    # 分布式锁配置
    lock:
      # 锁默认过期时间（秒），默认值：30
      default-expire: 30
      # 获取锁重试次数，默认值：3
      retry-count: 3
      # 重试间隔（毫秒），默认值：100
      retry-interval: 100
    # 限流配置
    rate-limit:
      # 默认时间窗口（秒），默认值：60
      default-window: 60
      # 默认限制次数，默认值：100
      default-limit: 100
    # Redisson配置
    redisson:
      # 是否启用Redisson，默认值：true
      enabled: true
      # 重试次数，默认值：3
      retry-attempts: 3
      # 重试间隔（毫秒），默认值：1000
      retry-interval: 1000
      # 连接超时（毫秒），默认值：30000
      connect-timeout: 30000

# Spring Redis连接配置
spring:
  data:
    redis:
      # Redis服务器地址
      host: "${REDIS_HOST:localhost}"
      # Redis服务器端口
      port: 6379
      # Redis数据库索引
      database: 0
      # Redis密码（如有）
      password: "${REDIS_PASSWORD:}"
      # 连接超时时间（毫秒）
      timeout: 60000
      # Lettuce连接池配置
      lettuce:
        pool:
          # 最大连接数
          max-active: 8
          # 最大等待时间（-1表示无限制）
          max-wait: -1
          # 最大空闲连接数
          max-idle: 8
          # 最小空闲连接数
          min-idle: 0
```

## 2. 最小配置示例

```yaml
# Redis模块配置（最小配置）
mineguard:
  redis:
    # 是否启用Redis模块
    enabled: true

# Spring Redis连接配置
spring:
  data:
    redis:
      host: "${REDIS_HOST:localhost}"
      port: 6379
      password: "${REDIS_PASSWORD:}"
```

## 3. 生产环境配置示例

```yaml
# Redis模块配置（生产环境）
mineguard:
  redis:
    enabled: true
    key-prefix: "mineguard:prod:"
    default-expire: 7200
    lock:
      default-expire: 60
      retry-count: 5
      retry-interval: 200
    rate-limit:
      default-window: 60
      default-limit: 1000
    redisson:
      enabled: true
      retry-attempts: 5
      retry-interval: 2000
      connect-timeout: 60000

# Spring Redis连接配置（生产环境）
spring:
  data:
    redis:
      host: "${REDIS_HOST}"
      port: 6379
      database: 1
      password: "${REDIS_PASSWORD}"
      timeout: 60000
      lettuce:
        pool:
          max-active: 20
          max-wait: 5000
          max-idle: 10
          min-idle: 5
```

## 4. 配置项说明

| 配置项 | 类型 | 默认值 | 说明 |
| ------ | ---- | ------ | ---- |
| `mineguard.redis.enabled` | boolean | true | 是否启用Redis模块 |
| `mineguard.redis.key-prefix` | String | mineguard: | 缓存键前缀 |
| `mineguard.redis.default-expire` | int | 3600 | 默认缓存过期时间（秒） |
| `mineguard.redis.lock.default-expire` | int | 30 | 分布式锁默认过期时间（秒） |
| `mineguard.redis.lock.retry-count` | int | 3 | 获取锁重试次数 |
| `mineguard.redis.lock.retry-interval` | int | 100 | 重试间隔（毫秒） |
| `mineguard.redis.rate-limit.default-window` | int | 60 | 限流默认时间窗口（秒） |
| `mineguard.redis.rate-limit.default-limit` | int | 100 | 限流默认次数 |
| `mineguard.redis.redisson.enabled` | boolean | true | 是否启用Redisson |
| `mineguard.redis.redisson.retry-attempts` | int | 3 | Redisson重试次数 |
| `mineguard.redis.redisson.retry-interval` | int | 1000 | Redisson重试间隔（毫秒） |
| `mineguard.redis.redisson.connect-timeout` | int | 30000 | Redisson连接超时（毫秒） |
