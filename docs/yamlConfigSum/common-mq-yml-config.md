# common-mq模块 YML 配置示例

## 1. 完整配置示例

```yaml
# 消息队列模块配置
mineguard:
  # RabbitMQ配置
  rabbitmq:
    # 主机地址，默认值：localhost
    host: "localhost"
    # 端口，默认值：5672
    port: 5672
    # 用户名，默认值：guest
    username: "guest"
    # 密码，默认值：guest
    password: "guest"
    # 虚拟主机，默认值：/
    virtual-host: "/"
    # 连接超时时间（毫秒），默认值：60000
    connection-timeout: 60000
    # 通道缓存大小，默认值：25
    channel-cache-size: 25
    # 发布确认模式，默认值：NONE
    # 可选值：NONE, CORRELATED, SIMPLE
    publisher-confirm-type: "NONE"
    # 发布返回模式，默认值：false
    publisher-returns: false

# Spring RabbitMQ配置
spring:
  rabbitmq:
    # 地址（优先级高于单独配置）
    addresses: "localhost:5672"
    # 主机
    host: "localhost"
    # 端口
    port: 5672
    # 用户名
    username: "guest"
    # 密码
    password: "guest"
    # 虚拟主机
    virtual-host: "/"
    # 连接工厂配置
    connection-factory:
      # 连接超时
      connection-timeout: 60000
      # 通道缓存大小
      channel-cache-size: 25
    # 模板配置
    template:
      # 交换器名称
      exchange: "default.exchange"
      # 路由键
      routing-key: "default.routing.key"
      # 强制模式
      mandatory: true
      # 接收超时
      receive-timeout: 10000
    # 监听容器配置
    listener:
      simple:
        # 并发性
        concurrency: 3
        # 最大并发性
        max-concurrency: 10
        # 预取计数
        prefetch: 1
        # 确认模式
        # 可选值：NONE, AUTO, MANUAL
        acknowledge-mode: "MANUAL"
        # 重试配置
        retry:
          # 启用重试
          enabled: true
          # 最大重试次数
          max-attempts: 3
          # 初始间隔
          initial-interval: 1000
          # 乘数
          multiplier: 2.0
          # 最大间隔
          max-interval: 10000

# 日志配置
logging:
  level:
    # MQ相关日志级别
    "org.springframework.amqp": "warn"
    "com.klzw.common.mq": "info"
```

## 2. 最小配置示例

```yaml
# 消息队列模块配置（最小配置）
mineguard:
  # RabbitMQ配置（可选，使用默认值）
  # rabbitmq:
  #   host: "localhost"
  #   port: 5672
  #   username: "guest"
  #   password: "guest"
  #   virtual-host: "/"

# Spring RabbitMQ配置（最小配置）
spring:
  rabbitmq:
    # 基本连接信息
    host: "localhost"
    port: 5672
    username: "guest"
    password: "guest"

# 日志配置
logging:
  level:
    "com.klzw.common.mq": "info"
```
