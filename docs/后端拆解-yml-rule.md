# MineGuard 后端 YAML 配置管理规则

## 1. 配置管理概述

### 1.1 配置中心架构

本项目采用 **Nacos 作为配置中心**，使用 `public` 命名空间，通过 **Group 区分环境和配置类型**。

```txt
┌─────────────────────────────────────────────────────────────┐
│                   Nacos 配置中心 (public)                     │
├─────────────────────────────────────────────────────────────┤
│  Group: Shared_Dev / Shared_Test / Shared_Prod               │
│                                                              │
│  公共配置（common-*）：10个模块对应20个配置文件                │
│  ├── common-core.yml (Dev/Test)    # 核心配置               │
│  ├── common-database.yml (Dev/Test) # MySQL 数据库配置       │
│  ├── common-auth.yml (Dev/Test)    # 认证授权配置            │
│  ├── common-redis.yml (Dev/Test)   # Redis 缓存配置          │
│  ├── common-mq.yml (Dev/Test)     # RabbitMQ 消息队列配置    │
│  ├── common-mongodb.yml (Dev/Test) # MongoDB 配置            │
│  ├── common-web.yml (Dev/Test)     # Web 配置                │
│  ├── common-websocket.yml (Dev/Test)# WebSocket 配置         │
│  ├── common-file.yml (Dev/Test)    # 文件存储配置            │
│  └── common-map.yml (Dev/Test)     # 地图服务配置            │
├─────────────────────────────────────────────────────────────┤
│  Group: Service_Dev / Service_Test / Service_Prod             │
│                                                              │
│  服务配置：                                                   │
│  ├── user-service.yml      # 用户服务配置                   │
│  ├── vehicle-service.yml   # 车辆服务配置                   │
│  ├── gateway-service.yml   # 网关服务配置                   │
│  └── ...                                                     │
└──────────────────────────────────────────────────────────────┘
```

### 1.2 配置命名规则

| 配置类型 | Data ID 格式 | Group 格式 | 示例 |
| - | - | - | - |
| 公共配置 | `common-{模块名}.yml` | `Shared_{环境}` | Data ID: `common-core.yml`, Group: `Shared_Dev` |
| 服务配置 | `{服务名}.yml` | `Service_{环境}` | Data ID: `user-service.yml`, Group: `Service_Dev` |

| 环境类型 | 公共配置 Group | 服务配置 Group |
| - | - | - |
| 开发环境 | `Shared_Dev` | `Service_Dev` |
| 测试环境 | `Shared_Test` | `Service_Test` |
| 生产环境 | `Shared_Prod` | `Service_Prod` |

### 1.3 配置数量统计

| 环境 | 公共配置数量 | 服务配置数量 | 合计 |
| - | - | - | - |
| 开发 | 10 | - | 10 |
| 测试 | 10 | - | 10 |
| 生产 | 10 | - | 10 |
| 总计 | 30 | - | 30 |

**10个公共模块对应的 Data ID：**

| Data ID | 说明 | 所属模块 |
| - | - | - |
| `common-core.yml` | 核心配置（环境变量加载等） | mineguard-common-core |
| `common-database.yml` | MySQL 数据库配置 | mineguard-common-database |
| `common-auth.yml` | 认证授权配置 | mineguard-common-auth |
| `common-redis.yml` | Redis 缓存配置 | mineguard-common-redis |
| `common-mq.yml` | RabbitMQ 消息队列配置 | mineguard-common-mq |
| `common-mongodb.yml` | MongoDB 配置 | mineguard-common-mongodb |
| `common-web.yml` | Web 配置（CORS等） | mineguard-common-web |
| `common-websocket.yml` | WebSocket 配置 | mineguard-common-websocket |
| `common-file.yml` | 文件存储配置 | mineguard-common-file |
| `common-map.yml` | 地图服务配置 | mineguard-common-map |

### 1.4 配置管理原则

| 原则 | 说明 |
| - | - |
| 集中管理 | 所有配置通过 Nacos 集中管理，本地只保留引导配置 |
| 环境隔离 | 通过 Group 区分不同环境（dev/test/prod） |
| 分层管理 | 公共配置（Shared_*）+ 服务配置（Service_*） |
| 安全存储 | 敏感配置通过 Nacos 加密存储，不提交到代码仓库 |
| 版本追溯 | Nacos 提供配置版本管理和回滚能力 |

### 1.5 配置归属规则（必须遵守）

#### 1) 服务模块（唯一配置持有者）

- **唯一允许持有** `bootstrap.yml` 的地方
- 只包含服务启动引导信息和 Nacos 连接配置
- 业务配置全部从 Nacos 拉取
- **不需要** `application.yml`（配置都在 Nacos）

#### 2) 公共模块（禁止携带配置）

- **禁止**在 `mineguard-common-*` 模块中放置任何配置文件
- **禁止**在公共模块中配置 `spring.application.name`
- 公共模块只提供代码能力（AutoConfiguration、`@ConfigurationProperties`）
- 默认值写在代码中，实际值由 Nacos 提供

#### 3) 测试配置（本地独立）

- 集成测试使用本地 `application-test.yml`
- **不依赖** Nacos 配置中心
- 保证测试的独立性和可重复性
- CI/CD 环境无需外部依赖

#### 4) 违规后果

| 违规行为 | 后果 |
| - | - |
| 公共模块配置 `spring.application.name` | 导致所有依赖服务 Nacos 分组串台 |
| 公共模块放置 `application.yml` | 配置合并/覆盖，导致配置错乱 |
| 本地文件与 Nacos 重复定义 | 配置来源不明确，难以排查问题 |
| 测试依赖 Nacos | CI/CD 环境测试失败 |

## 2. Nacos 配置结构

### 2.1 命名空间（Namespace）

本项目使用 **`public`** 命名空间（默认命名空间），不额外创建 namespace。

### 2.2 配置分组（Group）规划

通过 Group 区分环境和配置类型，共 6 个分组：

| Group | 说明 | 包含配置 |
| - | - | - |
| `Shared_Dev` | 开发环境公共配置 | common-*.yml (Dev) |
| `Shared_Test` | 测试环境公共配置 | common-*.yml (Test) |
| `Shared_Prod` | 生产环境公共配置 | common-*.yml (Prod) |
| `Service_Dev` | 开发环境服务配置 | {服务名}.yml |
| `Service_Test` | 测试环境服务配置 | {服务名}.yml |
| `Service_Prod` | 生产环境服务配置 | {服务名}.yml |

### 2.3 公共配置文件（common-*.yml）

以下配置放在 `Shared_{环境}` 分组中，每个环境10个配置文件：

#### 2.3.1 common-core.yml（Shared_Dev / Shared_Test）

```yaml
mineguard:
  core:
    env-file: ".env"
    enabled: true
```

#### 2.3.2 common-database.yml（Shared_Dev / Shared_Test）

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://${mysql.host}:${mysql.port}/${mysql.database}?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: ${mysql.username}
    password: ${mysql.password}
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 300000
      connection-timeout: 30000
      max-lifetime: 1800000

mybatis-plus:
  mapper-locations: classpath*:mapper/**/*.xml
  type-aliases-package: com.klzw.**.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

#### 2.3.3 common-auth.yml（Shared_Dev / Shared_Test）

```yaml
mineguard:
  auth:
    enabled: true
    jwt:
      secret: ${JWT_SECRET:default-jwt-secret-key}
      expiration: 3600000
      enable-blacklist: true
      blacklist-prefix: "jwt:blacklist:"
      blacklist-expire: 86400
```

#### 2.3.4 common-redis.yml（Shared_Dev / Shared_Test）

```yaml
spring:
  data:
    redis:
      host: ${redis.host}
      port: ${redis.port}
      password: ${redis.password}
      database: ${redis.database:0}
      timeout: 3000
      lettuce:
        pool:
          max-active: 8
          max-wait: -1
          max-idle: 8
          min-idle: 0

mineguard:
  redis:
    enabled: true
    default-expire: 3600
```

#### 2.3.5 common-mq.yml（Shared_Dev / Shared_Test）

```yaml
spring:
  rabbitmq:
    host: ${rabbitmq.host}
    port: ${rabbitmq.port}
    username: ${rabbitmq.username}
    password: ${rabbitmq.password}
    virtual-host: ${rabbitmq.virtual-host:/}
    publisher-confirm-type: correlated
    publisher-returns: true
```

#### 2.3.6 common-mongodb.yml（Shared_Dev / Shared_Test）

```yaml
spring:
  data:
    mongodb:
      host: ${mongodb.host}
      port: ${mongodb.port}
      database: ${mongodb.database}
      username: ${mongodb.username}
      password: ${mongodb.password}
      authentication-database: ${mongodb.authentication-database:admin}

mineguard:
  mongodb:
    enabled: true
    host: ${mongodb.host}
    port: ${mongodb.port}
    database: ${mongodb.database}
    username: ${mongodb.username}
    password: ${mongodb.password}
    authentication-database: ${mongodb.authentication-database:admin}
```

#### 2.3.7 common-web.yml（Shared_Dev / Shared_Test）

```yaml
mineguard:
  web:
    enabled: true
    cors:
      allowed-origins: "*"
      allowed-methods: "GET,POST,PUT,DELETE,OPTIONS"
      allowed-headers: "*"
      allow-credentials: true
      max-age: 3600
    resources:
      static-locations: "classpath:/static/"
      cache-period: 3600
    file-upload:
      max-file-size: "10MB"
      max-request-size: "100MB"
```

#### 2.3.8 common-websocket.yml（Shared_Dev / Shared_Test）

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
    enable-message-persistence: false
    allowed-origins:
      - "*"
```

#### 2.3.9 common-file.yml（Shared_Dev / Shared_Test）

```yaml
mineguard:
  file:
    storage:
      enabled: true
      max-file-size: 104857600
      allowed-extensions: "jpg,jpeg,png,gif,bmp,pdf,doc,docx,xls,xlsx"
      default-module: "user"
      dual-write:
        enabled: false
        primary: "minio"
    minio:
      enabled: true
      endpoint: ${minio.endpoint:http://localhost:9000}
      access-key: ${minio.access-key:minioadmin}
      secret-key: ${minio.secret-key:minioadmin}
      bucket-name: ${minio.bucket-name:mineguard}
    aliyun:
      enabled: false
      endpoint: ${aliyun.oss.endpoint:}
      access-key-id: ${aliyun.oss.access-key-id:}
      access-key-secret: ${aliyun.oss.access-key-secret:}
      bucket-name: ${aliyun.oss.bucket-name:}
```

#### 2.3.10 common-map.yml（Shared_Dev / Shared_Test）

```yaml
mineguard:
  map:
    gaode:
      enabled: true
      key: ${gaode.map.key:}
      security-key: ${gaode.map.security-key:}
```

## 3. 服务模块配置模板

### 3.1 本地引导配置（bootstrap.yml）

每个服务模块只需一个 `bootstrap.yml`，通过环境变量指定环境：

```yaml
spring:
  application:
    name: ${服务名称}
  cloud:
    nacos:
      server-addr: ${nacos.server-addr:localhost:8848}
      username: ${nacos.username:nacos}
      password: ${nacos.password:nacos}
      discovery:
        namespace: public
        group: Service_${nacos.env:dev}
      config:
        namespace: public
        group: Service_${nacos.env:dev}
        file-extension: yml
        shared-configs:
          - data-id: common-core.yml
            group: Shared_${nacos.env:dev}
            refresh: true
          - data-id: common-database.yml
            group: Shared_${nacos.env:dev}
            refresh: true
          - data-id: common-redis.yml
            group: Shared_${nacos.env:dev}
            refresh: true
```

**关键点：**
- `nacos.env` 环境变量控制当前环境（dev/test/prod）
- 服务配置 Group：`Service_{环境}`
- 公共配置 Group：`Shared_{环境}`
- Data ID 不含环境名，通过 Group 区分环境

### 3.2 服务模块公共配置依赖关系

不同服务模块需要导入不同的公共配置文件：

| 服务模块 | 必需导入的公共配置 |
| - | - |
| user-service | common-core, common-database, common-auth, common-redis |
| vehicle-service | common-core, common-database, common-redis, common-mq, common-map |
| trip-service | common-core, common-database, common-redis, common-mq |
| warning-service | common-core, common-database, common-redis, common-mq, common-websocket |
| statistics-service | common-core, common-database, common-redis |
| cost-service | common-core, common-database, common-redis |
| ai-service | common-core, common-database, common-redis |
| iot-service | common-core, common-database, common-redis, common-mq, common-websocket |
| gateway-service | common-core, common-redis, common-auth |
| file-service | common-core, common-redis, common-file |

### 3.3 web-service 配置示例

#### bootstrap.yml

```yaml
spring:
  application:
    name: web-service
  cloud:
    nacos:
      server-addr: ${nacos.server-addr:localhost:8848}
      username: ${nacos.username:nacos}
      password: ${nacos.password:nacos}
      discovery:
        namespace: public
        group: Service_${nacos.env:dev}
      config:
        namespace: public
        group: Service_${nacos.env:dev}
        file-extension: yml
        shared-configs:
          - data-id: common-core.yml
            group: Shared_${nacos.env:dev}
            refresh: true
          - data-id: common-auth.yml
            group: Shared_${nacos.env:dev}
            refresh: true
          - data-id: common-redis.yml
            group: Shared_${nacos.env:dev}
            refresh: true
          - data-id: common-web.yml
            group: Shared_${nacos.env:dev}
            refresh: true
```

#### Nacos 配置：web-service.yml（Group: Service_Dev）

```yaml
server:
  port: 8080
```

### 3.4 user-service 配置

#### bootstrap.yml

```yaml
spring:
  application:
    name: user-service
  cloud:
    nacos:
      server-addr: ${nacos.server-addr:localhost:8848}
      username: ${nacos.username:nacos}
      password: ${nacos.password:nacos}
      discovery:
        namespace: public
        group: Service_${nacos.env:dev}
      config:
        namespace: public
        group: Service_${nacos.env:dev}
        file-extension: yml
        shared-configs:
          - data-id: common-core.yml
            group: Shared_${nacos.env:dev}
            refresh: true
          - data-id: common-database.yml
            group: Shared_${nacos.env:dev}
            refresh: true
          - data-id: common-auth.yml
            group: Shared_${nacos.env:dev}
            refresh: true
          - data-id: common-redis.yml
            group: Shared_${nacos.env:dev}
            refresh: true
```

#### Nacos 配置：user-service.yml（Group: Service_Dev）

```yaml
server:
  port: 8001

mineguard:
  user:
    enable-cache: true
    cache-expire: 3600
```

### 3.5 vehicle-service 配置

#### bootstrap.yml

```yaml
spring:
  application:
    name: vehicle-service
  cloud:
    nacos:
      server-addr: ${nacos.server-addr:localhost:8848}
      username: ${nacos.username:nacos}
      password: ${nacos.password:nacos}
      discovery:
        namespace: public
        group: Service_${nacos.env:dev}
      config:
        namespace: public
        group: Service_${nacos.env:dev}
        file-extension: yml
        shared-configs:
          - data-id: common-core.yml
            group: Shared_${nacos.env:dev}
            refresh: true
          - data-id: common-database.yml
            group: Shared_${nacos.env:dev}
            refresh: true
          - data-id: common-redis.yml
            group: Shared_${nacos.env:dev}
            refresh: true
          - data-id: common-mq.yml
            group: Shared_${nacos.env:dev}
            refresh: true
          - data-id: common-map.yml
            group: Shared_${nacos.env:dev}
            refresh: true
```

#### Nacos 配置：vehicle-service.yml（Group: Service_Dev）

```yaml
server:
  port: 8002

mineguard:
  vehicle:
    location:
      update-interval: 10000
      cache-expire: 300
    status:
      monitor-enabled: true
      offline-threshold: 300000
```

### 3.6 gateway-service 配置

#### bootstrap.yml

```yaml
spring:
  application:
    name: gateway-service
  cloud:
    nacos:
      server-addr: ${nacos.server-addr:localhost:8848}
      username: ${nacos.username:nacos}
      password: ${nacos.password:nacos}
      discovery:
        namespace: public
        group: Service_${nacos.env:dev}
      config:
        namespace: public
        group: Service_${nacos.env:dev}
        file-extension: yml
        shared-configs:
          - data-id: common-core.yml
            group: Shared_${nacos.env:dev}
            refresh: true
          - data-id: common-redis.yml
            group: Shared_${nacos.env:dev}
            refresh: true
          - data-id: common-auth.yml
            group: Shared_${nacos.env:dev}
            refresh: true
```

#### Nacos 配置：gateway-service.yml（Group: Service_Dev）

```yaml
server:
  port: 8080

spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/user/**,/api/auth/**
        - id: vehicle-service
          uri: lb://vehicle-service
          predicates:
            - Path=/api/vehicle/**

mineguard:
  gateway:
    rate-limit:
      enabled: true
      default-limit: 1000
      default-period: 60
    circuit-breaker:
      enabled: true
      timeout: 3000
    auth:
      white-list:
        - /api/auth/login
        - /api/auth/register
        - /doc.html
        - /webjars/**
        - /swagger-resources/**
        - /v3/api-docs/**
```

## 4. 环境变量说明

### 4.1 必需的环境变量

所有服务模块都需要以下环境变量：

| 环境变量 | 说明 | 默认值 |
| - | - | - |
| `nacos.server-addr` | Nacos 服务地址 | localhost:8848 |
| `nacos.username` | Nacos 用户名 | nacos |
| `nacos.password` | Nacos 密码 | nacos |
| `nacos.env` | 运行环境 | dev |

### 4.2 数据库相关环境变量

| 环境变量 | 说明 | 默认值 |
| - | - | - |
| `mysql.host` | MySQL 主机 | localhost |
| `mysql.port` | MySQL 端口 | 3306 |
| `mysql.database` | 数据库名称 | mineguard |
| `mysql.username` | 数据库用户名 | root |
| `mysql.password` | 数据库密码 | - |

### 4.3 Redis 相关环境变量

| 环境变量 | 说明 | 默认值 |
| - | - | - |
| `redis.host` | Redis 主机 | localhost |
| `redis.port` | Redis 端口 | 6379 |
| `redis.password` | Redis 密码 | - |
| `redis.database` | Redis 数据库编号 | 0 |

### 4.4 RabbitMQ 相关环境变量

| 环境变量 | 说明 | 默认值 |
| - | - | - |
| `rabbitmq.host` | RabbitMQ 主机 | localhost |
| `rabbitmq.port` | RabbitMQ 端口 | 5672 |
| `rabbitmq.username` | RabbitMQ 用户名 | guest |
| `rabbitmq.password` | RabbitMQ 密码 | guest |
| `rabbitmq.virtual-host` | 虚拟主机 | / |

### 4.5 MongoDB 相关环境变量

| 环境变量 | 说明 | 默认值 |
| - | - | - |
| `mongodb.host` | MongoDB 主机 | localhost |
| `mongodb.port` | MongoDB 端口 | 27017 |
| `mongodb.database` | 数据库名称 | mineguard |
| `mongodb.username` | 数据库用户名 | - |
| `mongodb.password` | 数据库密码 | - |
| `mongodb.authentication-database` | 认证数据库 | admin |

### 4.6 文件存储相关环境变量

| 环境变量 | 说明 | 默认值 |
| - | - | - |
| `minio.endpoint` | MinIO 端点 | http://localhost:9000 |
| `minio.access-key` | MinIO AccessKey | minioadmin |
| `minio.secret-key` | MinIO SecretKey | minioadmin |
| `minio.bucket-name` | MinIO 桶名称 | mineguard |
| `aliyun.oss.endpoint` | 阿里云 OSS 端点 | - |
| `aliyun.oss.access-key-id` | 阿里云 AccessKeyId | - |
| `aliyun.oss.access-key-secret` | 阿里云 AccessKeySecret | - |
| `aliyun.oss.bucket-name` | 阿里云 OSS 桶名称 | - |

### 4.7 地图服务相关环境变量

| 环境变量 | 说明 | 默认值 |
| - | - | - |
| `gaode.map.key` | 高德地图 Web服务密钥 | - |
| `gaode.map.security-key` | 高德地图安全密钥 | - |

### 4.8 JWT 相关环境变量

| 环境变量 | 说明 | 默认值 |
| - | - | - |
| `JWT_SECRET` | JWT 密钥 | - |

## 5. 配置文件优先级

当服务启动时，配置加载优先级如下（从高到低）：

1. **服务自身配置**（{服务名}.yml，Group: Service_{环境}）
2. **公共配置**（common-*.yml，Group: Shared_{环境}）
3. **bootstrap.yml**（本地引导配置）
4. **环境变量**
5. **代码默认值**（@ConfigurationProperties）

**注意**：公共配置的加载顺序由 shared-configs 列表中的顺序决定，后加载的配置会覆盖先加载的同名配置。
