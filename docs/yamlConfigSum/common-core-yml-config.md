# common-core模块 YML 配置示例

## 1. 完整配置示例

```yaml
# 核心模块配置
mineguard:
  # 分页配置
  pagination:
    # 默认页码，默认值：1
    default-page: 1
    # 默认每页大小，默认值：10
    default-page-size: 10
    # 最大页大小，默认值：100
    max-page-size: 100
    # 默认排序方向 (asc/desc)，默认值："asc"
    default-sort-order: "asc"

# Spring 全局配置
spring:
  # 国际化配置
  messages:
    # 消息资源文件基础名
    basename: "i18n/messages"
    # 消息编码
    encoding: "UTF-8"
  # 线程池配置
  task:
    execution:
      pool:
        # 核心线程数
        core-size: 10
        # 最大线程数
        max-size: 50
        # 队列容量
        queue-capacity: 1000
        # 线程保持时间
        keep-alive: "60s"

# 日志配置
logging:
  # 日志级别
  level:
    # 应用包日志级别
    "com.klzw": "info"
    # Spring 框架日志级别
    "org.springframework": "warn"
  # 日志格式
  pattern:
    # 控制台日志格式
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  # 文件配置
  file:
    # 日志文件名称
    name: "logs/application.log"
    # 单个日志文件最大大小
    max-size: "10MB"
    # 日志文件保留天数
    max-history: "7"
```

## 2. 最小配置示例

```yaml
# 核心模块配置（最小配置）
mineguard:
  # 分页配置（可选，使用默认值）
  # pagination:
  #   default-page: 1
  #   default-page-size: 10
  #   max-page-size: 100
  #   default-sort-order: "asc"

# 日志配置
logging:
  level:
    "com.klzw": "info"
```
