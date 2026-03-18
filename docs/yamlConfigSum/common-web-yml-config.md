# common-web模块 YML 配置示例

## 1. 完整配置示例

```yaml
# Web模块配置
mineguard:
  # Web配置
  web:
    # 启用Web模块，默认值：true
    enabled: true
    # CORS配置
    cors:
      # 允许的源，默认值：*（生产环境建议配置具体域名）
      allowed-origins: "*"
      # 允许的HTTP方法，默认值：GET,POST,PUT,DELETE,OPTIONS
      allowed-methods: "GET,POST,PUT,DELETE,OPTIONS"
      # 允许的请求头，默认值：Content-Type,Authorization
      allowed-headers: "Content-Type,Authorization,X-User-Id,X-Username,X-User-Roles"
      # 是否允许携带凭证，默认值：true
      allow-credentials: true
      # 预检请求结果缓存时间（秒），默认值：3600
      max-age: 3600
    # 文件上传配置
    file-upload:
      # 最大文件大小，默认值：50MB
      max-file-size: "50MB"
      # 最大请求大小，默认值：100MB
      max-request-size: "100MB"
      # 临时文件存储位置，默认值：${java.io.tmpdir}
      location: "${java.io.tmpdir}"

# Spring Web配置
spring:
  # Web配置
  web:
    # 资源路径
    resources:
      # 静态资源路径
      static-locations: "classpath:/static/"
  # 验证配置
  validation:
    # 启用方法参数验证，默认值：true
    enabled: true
  # 消息国际化
  messages:
    # 消息资源文件基础名
    basename: "i18n/messages"
    # 消息编码
    encoding: "UTF-8"

# 日志配置
logging:
  level:
    # Web模块日志级别
    "com.klzw.common.web": "info"
    # Spring Web日志级别
    "org.springframework.web": "warn"
    # Spring MVC日志级别
    "org.springframework.web.servlet": "warn"
  # 日志格式
  pattern:
    # 控制台日志格式
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"

# 服务器配置
server:
  # 端口
  port: 8080
  # 上下文路径
  servlet:
    context-path: "/"
  # 编码
  encoding:
    charset: "UTF-8"
    enabled: true
    force: true
```

## 2. 最小配置示例

```yaml
# Web模块配置（最小配置）
mineguard:
  # Web配置（可选，使用默认值）
  # web:
  #   enabled: true
  #   cors:
  #     allowed-origins: "*"
  #   file-upload:
  #     max-file-size: "50MB"

# 服务器配置
server:
  port: 8080

# 日志配置
logging:
  level:
    "com.klzw.common.web": "info"
```
