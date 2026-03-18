# gateway-service模块 YML 配置示例

## 1. 完整配置示例

```yaml
# 网关服务配置
mineguard:
  gateway:
    # CORS跨域配置
    cors:
      # 允许的源（多个用逗号分隔）
      allowed-origins: "http://127.0.0.1:5173,http://127.0.0.1:8080"
      # 允许的HTTP方法
      allowed-methods: "GET,POST,PUT,DELETE,OPTIONS"
      # 允许的HTTP头
      allowed-headers: "*"
      # 是否允许携带凭证
      allow-credentials: true
      # 预检请求缓存时间（秒）
      max-age: 3600
    
    # 忽略认证的路径配置
    ignore-auth:
      paths:
        - "/api/auth/login"
        - "/api/auth/register"
        - "/api/auth/reset-password"
        - "/api/public/"
        - "/actuator/"
        - "/swagger-ui/"
        - "/v3/api-docs/"
    
    # 限流配置
    enable-rate-limit: true
    # 限流次数
    rate-limit-count: 2000
    # 限流时间窗口（秒）
    rate-limit-time: 60
    
    # 熔断降级配置
    enable-circuit-breaker: true

# Spring Cloud Gateway 配置
spring:
  cloud:
    gateway:
      server:
        webflux:
          # 服务发现路由配置
          discovery:
            locator:
              # 开启服务发现路由
              enabled: true
              # 服务名转小写
              lower-case-service-id: true
          # 路由配置（使用负载均衡 lb://）
          routes: 
            # 用户服务路由
            - id: user-service 
              uri: lb://user-service
              predicates: 
                - Path=/api/user/** 
              filters: 
                - StripPrefix=1 

            # 用户认证路由
            - id: user-service-auth 
              uri: lb://user-service
              predicates: 
                - Path=/api/auth/** 
              filters: 
                - StripPrefix=1 

            # 资格认证路由
            - id: qualification-service 
              uri: lb://user-service
              predicates: 
                - Path=/api/qualification/** 
              filters: 
                - StripPrefix=1 

            # 角色服务路由
            - id: role-service 
              uri: lb://user-service
              predicates: 
                - Path=/api/role/** 
              filters: 
                - StripPrefix=1 

            # 消息服务路由
            - id: message-service 
              uri: lb://user-service
              predicates: 
                - Path=/api/messages/** 
              filters: 
                - StripPrefix=1 

            # 考勤服务路由
            - id: attendance-service 
              uri: lb://user-service
              predicates: 
                - Path=/api/attendance/** 
              filters: 
                - StripPrefix=1 

            # 车辆服务路由
            - id: vehicle-service 
              uri: lb://vehicle-service
              predicates: 
                - Path=/api/vehicle/** 
              filters: 
                - StripPrefix=1 

            # 行程服务路由
            - id: trip-service 
              uri: lb://trip-service
              predicates: 
                - Path=/api/trip/** 
              filters: 
                - StripPrefix=1 

            # 警告服务路由
            - id: warning-service 
              uri: lb://warning-service
              predicates: 
                - Path=/api/warning/** 
              filters: 
                - StripPrefix=1 

            # 统计服务路由
            - id: statistics-service 
              uri: lb://statistics-service
              predicates: 
                - Path=/api/statistics/** 
              filters: 
                - StripPrefix=1 

            # 调度服务路由
            - id: dispatch-service 
              uri: lb://dispatch-service
              predicates: 
                - Path=/api/dispatch/** 
              filters: 
                - StripPrefix=1 

            # 成本服务路由
            - id: cost-service 
              uri: lb://cost-service
              predicates: 
                - Path=/api/cost/** 
              filters: 
                - StripPrefix=1 

            # AI服务路由
            - id: ai-service 
              uri: lb://ai-service
              predicates: 
                - Path=/api/ai/** 
              filters: 
                - StripPrefix=1 

            # Python服务路由
            - id: python-service 
              uri: lb://python-service
              predicates: 
                - Path=/api/python/** 
              filters: 
                - StripPrefix=1

# 日志配置
logging: 
  level: 
    com.klzw: debug
    org.springframework.cloud.gateway: debug
```

## 2. 最小配置示例

```yaml
# 网关服务配置（最小配置）
mineguard:
  gateway:
    # 忽略认证的路径配置（必填）
    ignore-auth:
      paths:
        - "/api/auth/login"
        - "/api/auth/register"
        - "/swagger-ui/"
        - "/v3/api-docs/"

# Spring Cloud Gateway 配置
spring:
  cloud:
    gateway:
      server:
        webflux:
          discovery:
            locator:
              enabled: true
              lower-case-service-id: true
          routes: 
            - id: user-service 
              uri: lb://user-service
              predicates: 
                - Path=/api/user/** 
              filters: 
                - StripPrefix=1 

# 日志配置
logging: 
  level: 
    com.klzw: debug
```

## 3. 配置项说明

### 3.1 mineguard.gateway 配置

| 配置项 | 类型 | 默认值 | 说明 |
| ------ | ---- | ------ | ---- |
| `cors.allowed-origins` | String | null | 允许的跨域源，多个用逗号分隔 |
| `cors.allowed-methods` | String | "GET,POST,PUT,DELETE,OPTIONS" | 允许的HTTP方法 |
| `cors.allowed-headers` | String | "*" | 允许的HTTP头 |
| `cors.allow-credentials` | Boolean | true | 是否允许携带凭证 |
| `cors.max-age` | Long | 3600 | 预检请求缓存时间（秒） |
| `ignore-auth.paths` | List\<String\> | [] | 忽略认证的路径列表 |
| `enable-rate-limit` | Boolean | true | 是否启用限流 |
| `rate-limit-count` | Integer | 1000 | 限流次数 |
| `rate-limit-time` | Integer | 60 | 限流时间窗口（秒） |
| `enable-circuit-breaker` | Boolean | true | 是否启用熔断降级 |

### 3.2 spring.cloud.gateway.server.webflux.routes 路由配置说明

| 配置项 | 说明 |
| ------ | ---- |
| `id` | 路由唯一标识 |
| `uri` | 目标服务地址，`lb://` 表示使用 Nacos 负载均衡 |
| `predicates` | 路由断言，用于匹配请求 |
| `filters` | 路由过滤器，用于处理请求/响应 |

**URI 格式说明：**

- `lb://service-name`：从 Nacos 获取服务实例，使用负载均衡转发请求
- `http://host:port`：直接连接指定地址

### 3.3 ignore-auth.paths 路径匹配规则

- 支持前缀匹配：`/api/public/` 会匹配所有以 `/api/public/` 开头的路径
- 支持精确匹配：`/api/auth/login` 只匹配该精确路径

### 3.4 服务路由汇总

| 路由ID | 服务名 | 路径 | StripPrefix |
| ------ | ------ | ---- | ------------ |
| user-service | user-service | /api/user/** | 1 |
| user-service-auth | user-service | /api/auth/** | 1 |
| qualification-service | user-service | /api/qualification/** | 1 |
| role-service | user-service | /api/role/** | 1 |
| message-service | user-service | /api/messages/** | 1 |
| attendance-service | user-service | /api/attendance/** | 1 |
| vehicle-service | vehicle-service | /api/vehicle/** | 1 |
| trip-service | trip-service | /api/trip/** | 1 |
| warning-service | warning-service | /api/warning/** | 1 |
| statistics-service | statistics-service | /api/statistics/** | 1 |
| dispatch-service | dispatch-service | /api/dispatch/** | 1 |
| cost-service | cost-service | /api/cost/** | 1 |
| ai-service | ai-service | /api/ai/** | 1 |
| python-service | python-service | /api/python/** | 1 |

**注意**：使用 `lb://` 负载均衡需要确保：

1. Gateway 和各服务已添加 `spring-cloud-starter-alibaba-nacos-discovery` 依赖
2. Gateway 和各服务在同一个 Nacos 命名空间
3. 各服务已启动并注册到 Nacos
