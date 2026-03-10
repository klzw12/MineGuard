# MineGuard 后端 YAML 配置管理规则

## 1. 配置管理概述

### 1.1 配置中心架构

本项目采用 **Nacos 作为配置中心**，使用 `public` 命名空间，通过 **Group 区分环境和配置类型**。

```txt
┌─────────────────────────────────────────────────────────────┐
│                   Nacos 配置中心 (public)                     │
├─────────────────────────────────────────────────────────────┤
│  Group: shared-dev / shared-test / shared-prod               │
│                                                              │
│  公共配置（common-*）：                                        │
│  ├── common-database.yml   # MySQL 数据库配置                 │
│  ├── common-redis.yml      # Redis 缓存配置                   │
│  ├── common-rocketmq.yml   # RocketMQ 消息队列配置             │
│  └── common-monitor.yml    # 监控配置                         │
├─────────────────────────────────────────────────────────────┤
│  Group: service-dev / service-test / service-prod            │
│                                                              │
│  服务配置：                                                   │
│  ├── user-service.yml      # 用户服务配置                     │
│  ├── vehicle-service.yml   # 车辆服务配置                     │
│  ├── gateway-service.yml   # 网关服务配置                     │
│  └── ...                                                     │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 配置命名规则

| 配置类型 | Data ID 格式 | Group 格式 | 示例 |
| - | - | - | - |
| 公共配置 | `common-{模块名}.yml` | `shared-{环境}` | Data ID: `common-database.yml`, Group: `shared-dev` |
| 服务配置 | `{服务名}.yml` | `service-{环境}` | Data ID: `user-service.yml`, Group: `service-dev` |

| 环境类型 | 公共配置 Group | 服务配置 Group |
| - | - | - |
| 开发环境 | `shared-dev` | `service-dev` |
| 测试环境 | `shared-test` | `service-test` |
| 生产环境 | `shared-prod` | `service-prod` |

### 1.3 配置管理原则

| 原则 | 说明 |
| - | - |
| 集中管理 | 所有配置通过 Nacos 集中管理，本地只保留引导配置 |
| 环境隔离 | 通过 Group 区分不同环境（dev/test/prod） |
| 分层管理 | 公共配置（shared-*）+ 服务配置（service-*） |
| 安全存储 | 敏感配置通过 Nacos 加密存储，不提交到代码仓库 |
| 版本追溯 | Nacos 提供配置版本管理和回滚能力 |

### 1.4 配置归属规则（必须遵守）

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
| `shared-dev` | 开发环境公共配置 | common-*.yml |
| `shared-test` | 测试环境公共配置 | common-*.yml |
| `shared-prod` | 生产环境公共配置 | common-*.yml |
| `service-dev` | 开发环境服务配置 | {服务名}.yml |
| `service-test` | 测试环境服务配置 | {服务名}.yml |
| `service-prod` | 生产环境服务配置 | {服务名}.yml |

### 2.3 公共配置文件（common-*.yml）

以下配置放在 `shared-{环境}` 分组中：

#### common-database.yml

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

#### common-redis.yml

```yaml
spring:
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
    cache:
      default-expire: 3600
      key-prefix: "mineguard:"
    lock:
      wait-time: 3000
      lease-time: 30000
```

#### common-rocketmq.yml

```yaml
rocketmq:
  name-server: ${rocketmq.namesrvAddr}
  producer:
    group: ${rocketmq.producer.group}
    send-message-timeout: 3000
    retry-times-when-send-failed: 2
  consumer:
    group: ${rocketmq.consumer.group}
    consume-thread-min: 20
    consume-thread-max: 64
```

#### common-monitor.yml

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    tags:
      application: ${spring.application.name}

logging:
  level:
    com.klzw: ${logging.level:INFO}
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
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
        group: service-${nacos.env:dev}
      config:
        namespace: public
        group: service-${nacos.env:dev}
        file-extension: yml
        shared-configs:
          - data-id: common-database.yml
            group: shared-${nacos.env:dev}
            refresh: true
          - data-id: common-redis.yml
            group: shared-${nacos.env:dev}
            refresh: true
          - data-id: common-monitor.yml
            group: shared-${nacos.env:dev}
            refresh: true
```

**关键点：**
- `nacos.env` 环境变量控制当前环境（dev/test/prod）
- 服务配置 Group：`service-{环境}`
- 公共配置 Group：`shared-{环境}`
- Data ID 不含环境名，通过 Group 区分环境

### 3.2 user-service 配置

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
        group: service-${nacos.env:dev}
      config:
        namespace: public
        group: service-${nacos.env:dev}
        file-extension: yml
        shared-configs:
          - data-id: common-database.yml
            group: shared-${nacos.env:dev}
            refresh: true
          - data-id: common-redis.yml
            group: shared-${nacos.env:dev}
            refresh: true
          - data-id: common-monitor.yml
            group: shared-${nacos.env:dev}
            refresh: true
```

#### Nacos 配置：user-service.yml（Group: service-dev）

```yaml
server:
  port: 8001

mineguard:
  auth:
    jwt:
      secret: ${jwt.secret:mineguard-jwt-secret-key-2024}
      expire-time: 7200
      refresh-time: 604800
    permission:
      super-admin-role: admin
  user:
    enable-cache: true
    cache-expire: 3600
```

### 3.3 vehicle-service 配置

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
        group: service-${nacos.env:dev}
      config:
        namespace: public
        group: service-${nacos.env:dev}
        file-extension: yml
        shared-configs:
          - data-id: common-database.yml
            group: shared-${nacos.env:dev}
            refresh: true
          - data-id: common-redis.yml
            group: shared-${nacos.env:dev}
            refresh: true
          - data-id: common-rocketmq.yml
            group: shared-${nacos.env:dev}
            refresh: true
          - data-id: common-monitor.yml
            group: shared-${nacos.env:dev}
            refresh: true
```

#### Nacos 配置：vehicle-service.yml（Group: service-dev）

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

### 3.4 trip-service 配置

#### bootstrap.yml

```yaml
spring:
  application:
    name: trip-service
  cloud:
    nacos:
      server-addr: ${nacos.server-addr:localhost:8848}
      username: ${nacos.username:nacos}
      password: ${nacos.password:nacos}
      discovery:
        namespace: public
        group: service-${nacos.env:dev}
      config:
        namespace: public
        group: service-${nacos.env:dev}
        file-extension: yml
        shared-configs:
          - data-id: common-database.yml
            group: shared-${nacos.env:dev}
            refresh: true
          - data-id: common-redis.yml
            group: shared-${nacos.env:dev}
            refresh: true
          - data-id: common-rocketmq.yml
            group: shared-${nacos.env:dev}
            refresh: true
          - data-id: common-monitor.yml
            group: shared-${nacos.env:dev}
            refresh: true
```

#### Nacos 配置：trip-service.yml（Group: service-dev）

```yaml
server:
  port: 8003

mineguard:
  trip:
    route:
      cache-time: 300000
      max-points: 10000
    history:
      retention-days: 90
```

### 3.5 warning-service 配置

#### bootstrap.yml

```yaml
spring:
  application:
    name: warning-service
  cloud:
    nacos:
      server-addr: ${nacos.server-addr:localhost:8848}
      username: ${nacos.username:nacos}
      password: ${nacos.password:nacos}
      discovery:
        namespace: public
        group: service-${nacos.env:dev}
      config:
        namespace: public
        group: service-${nacos.env:dev}
        file-extension: yml
        shared-configs:
          - data-id: common-database.yml
            group: shared-${nacos.env:dev}
            refresh: true
          - data-id: common-redis.yml
            group: shared-${nacos.env:dev}
            refresh: true
          - data-id: common-rocketmq.yml
            group: shared-${nacos.env:dev}
            refresh: true
          - data-id: common-monitor.yml
            group: shared-${nacos.env:dev}
            refresh: true
```

#### Nacos 配置：warning-service.yml（Group: service-dev）

```yaml
server:
  port: 8004

mineguard:
  warning:
    detection:
      interval: 5000
      enabled: true
    notification:
      enabled: true
      channels: ["sms", "push", "websocket"]
    rule:
      cache-expire: 600
```

### 3.6 statistics-service 配置

#### bootstrap.yml

```yaml
spring:
  application:
    name: statistics-service
  cloud:
    nacos:
      server-addr: ${nacos.server-addr:localhost:8848}
      username: ${nacos.username:nacos}
      password: ${nacos.password:nacos}
      discovery:
        namespace: public
        group: service-${nacos.env:dev}
      config:
        namespace: public
        group: service-${nacos.env:dev}
        file-extension: yml
        shared-configs:
          - data-id: common-database.yml
            group: shared-${nacos.env:dev}
            refresh: true
          - data-id: common-redis.yml
            group: shared-${nacos.env:dev}
            refresh: true
          - data-id: common-monitor.yml
            group: shared-${nacos.env:dev}
            refresh: true
```

#### Nacos 配置：statistics-service.yml（Group: service-dev）

```yaml
server:
  port: 8005

mineguard:
  statistics:
    cache:
      expire-time: 3600000
    export:
      enabled: true
      max-rows: 100000
```

### 3.7 cost-service 配置

#### bootstrap.yml

```yaml
spring:
  application:
    name: cost-service
  cloud:
    nacos:
      server-addr: ${nacos.server-addr:localhost:8848}
      username: ${nacos.username:nacos}
      password: ${nacos.password:nacos}
      discovery:
        namespace: public
        group: service-${nacos.env:dev}
      config:
        namespace: public
        group: service-${nacos.env:dev}
        file-extension: yml
        shared-configs:
          - data-id: common-database.yml
            group: shared-${nacos.env:dev}
            refresh: true
          - data-id: common-redis.yml
            group: shared-${nacos.env:dev}
            refresh: true
          - data-id: common-monitor.yml
            group: shared-${nacos.env:dev}
            refresh: true
```

#### Nacos 配置：cost-service.yml（Group: service-dev）

```yaml
server:
  port: 8006

mineguard:
  cost:
    budget:
      alert-threshold: 0.8
    analysis:
      enabled: true
      cache-expire: 1800
```

### 3.8 ai-service 配置

#### bootstrap.yml

```yaml
spring:
  application:
    name: ai-service
  cloud:
    nacos:
      server-addr: ${nacos.server-addr:localhost:8848}
      username: ${nacos.username:nacos}
      password: ${nacos.password:nacos}
      discovery:
        namespace: public
        group: service-${nacos.env:dev}
      config:
        namespace: public
        group: service-${nacos.env:dev}
        file-extension: yml
        shared-configs:
          - data-id: common-database.yml
            group: shared-${nacos.env:dev}
            refresh: true
          - data-id: common-redis.yml
            group: shared-${nacos.env:dev}
            refresh: true
          - data-id: common-monitor.yml
            group: shared-${nacos.env:dev}
            refresh: true
```

#### Nacos 配置：ai-service.yml（Group: service-dev）

```yaml
server:
  port: 8007

mineguard:
  ai:
    prediction:
      enabled: true
      model-cache-size: 10
    recommendation:
      enabled: true
      cache-expire: 600
```

### 3.9 iot-service 配置

#### bootstrap.yml

```yaml
spring:
  application:
    name: iot-service
  cloud:
    nacos:
      server-addr: ${nacos.server-addr:localhost:8848}
      username: ${nacos.username:nacos}
      password: ${nacos.password:nacos}
      discovery:
        namespace: public
        group: service-${nacos.env:dev}
      config:
        namespace: public
        group: service-${nacos.env:dev}
        file-extension: yml
        shared-configs:
          - data-id: common-database.yml
            group: shared-${nacos.env:dev}
            refresh: true
          - data-id: common-redis.yml
            group: shared-${nacos.env:dev}
            refresh: true
          - data-id: common-rocketmq.yml
            group: shared-${nacos.env:dev}
            refresh: true
          - data-id: common-monitor.yml
            group: shared-${nacos.env:dev}
            refresh: true
```

#### Nacos 配置：iot-service.yml（Group: service-dev）

```yaml
server:
  port: 8008

mineguard:
  iot:
    mqtt:
      broker: ${mqtt.broker:tcp://localhost:1883}
      client-id-prefix: mineguard-iot
      qos: 1
    websocket:
      path: /ws/device
      heartbeat-interval: 30000
```

### 3.10 gateway-service 配置

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
        group: service-${nacos.env:dev}
      config:
        namespace: public
        group: service-${nacos.env:dev}
        file-extension: yml
        shared-configs:
          - data-id: common-redis.yml
            group: shared-${nacos.env:dev}
            refresh: true
          - data-id: common-monitor.yml
            group: shared-${nacos.env:dev}
            refresh: true
```

#### Nacos 配置：gateway-service.yml（Group: service-dev）

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
        - id: trip-service
          uri: lb://trip-service
          predicates:
            - Path=/api/trip/**
        - id: warning-service
          uri: lb://warning-service
          predicates:
            - Path=/api/warning/**
        - id: statistics-service
          uri: lb://statistics-service
          predicates:
            - Path=/api/statistics/**
        - id: cost-service
          uri: lb://cost-service
          predicates:
            - Path=/api/cost/**
        - id: ai-service
          uri: lb://ai-service
          predicates:
            - Path=/api/ai/**
        - id: iot-service
          uri: lb://iot-service
          predicates:
            - Path=/api/iot/**

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
        - /api/auth/captcha
        - /doc.html
        - /webjars/**
        - /swagger-resources/**
        - /v3/api-docs/**
```

### 3.11 file-service 配置

#### bootstrap.yml

```yaml
spring:
  application:
    name: file-service
  cloud:
    nacos:
      server-addr: ${nacos.server-addr:localhost:8848}
      username: ${nacos.username:nacos}
      password: ${nacos.password:nacos}
      discovery:
        namespace: public
        group: service-${nacos.env:dev}
      config:
        namespace: public
        group: service-${nacos.env:dev}
        file-extension: yml
        shared-configs:
          - data-id: common-redis.yml
            group: shared-${nacos.env:dev}
            refresh: true
          - data-id: common-monitor.yml
            group: shared-${nacos.env:dev}
            refresh: true
```

#### Nacos 配置：file-service.yml（Group: service-dev）

```yaml
server:
  port: 8009

mineguard:
  file:
    storage:
      minio:
        enabled: true
        endpoint: ${minio.endpoint:http://localhost:9000}
        access-key: ${minio.access-key:minioadmin}
        secret-key: ${minio.secret-key:minioadmin}
        bucket-name: ${minio.bucket-name:mineguard}
        secure: false
      aliyun-oss:
        enabled: false
        endpoint: ${aliyun.oss.endpoint}
        access-key-id: ${aliyun.oss.access-key-id}
        access-key-secret: ${aliyun.oss.access-key-secret}
        bucket-name: ${aliyun.oss.bucket-name}
        region: ${aliyun.oss.region}
    config:
      max-file-size: 104857600
      max-request-size: 104857600
      allowed-file-types:
        - image/jpeg
        - image/png
        - image/gif
        - application/pdf
      storage-path: files
    ocr:
      enabled: true
      type: baidu
      baidu:
        app-id: ${baidu.ocr.app-id}
        api-key: ${baidu.ocr.api-key}
        secret-key: ${baidu.ocr.secret-key}
    disaster-recovery:
      enabled: true
      sync-strategy: realtime
```

## 4. 公共模块配置说明

### 4.1 配置原则

公共模块（`mineguard-common-*`）**不携带任何配置文件**，所有配置通过以下方式提供：

1. **默认值**：在 `@ConfigurationProperties` 类中定义
2. **Nacos 配置**：由公共配置（common-*.yml）或服务配置提供实际值

### 4.2 公共模块配置属性清单

| 模块 | 配置前缀 | 主要属性 | 配置位置 |
| - | - | - | - |
| mineguard-common-web | `mineguard.web` | cors.*, file-upload.* | 服务配置 |
| mineguard-common-auth | `mineguard.auth` | jwt.*, permission.* | user-service.yml |
| mineguard-common-redis | `mineguard.redis` | cache.*, lock.* | common-redis.yml |
| mineguard-common-file | `mineguard.file` | storage.*, ocr.* | file-service.yml |
| mineguard-common-map | `mineguard.map` | gaode.*, cache.* | 服务配置 |
| mineguard-common-log | `mineguard.log` | async.*, audit.* | common-monitor.yml |

### 4.3 配置属性类示例

```java
@ConfigurationProperties(prefix = "mineguard.auth.jwt")
public class JwtProperties {
    private String secret = "default-secret-key";
    private Long expireTime = 7200L;
    private Long refreshTime = 604800L;
}
```

## 5. 测试配置管理

### 5.1 测试配置原则

| 原则 | 说明 |
| - | - |
| 独立性 | 测试不依赖外部服务（Nacos） |
| 可重复性 | 测试结果可重复验证 |
| 隔离性 | 测试配置不污染 Nacos 配置 |
| 快速性 | 无需网络请求，快速启动 |

### 5.2 测试配置文件

公共模块的集成测试使用本地 `application-test.yml`：

```
mineguard-common-database/
└── src/
    └── test/
        └── resources/
            └── application-test.yml    # 测试配置（本地）
```

### 5.3 测试配置示例

#### application-test.yml（公共模块测试配置）

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/mineguard_test?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: test_password
  redis:
    host: localhost
    port: 6379
    database: 1

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

logging:
  level:
    com.klzw: DEBUG
```

### 5.4 测试配置最佳实践

| 场景 | 配置方式 |
| - | - |
| 单元测试 | Mock 依赖，无需配置文件 |
| 集成测试 | 本地 `application-test.yml` |
| 测试容器（Testcontainers） | 使用 Docker 容器启动依赖服务 |

### 5.5 测试容器示例

```java
@SpringBootTest
@Testcontainers
class DatabaseIntegrationTest {
    
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("mineguard_test");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }
}
```

## 6. 环境变量配置

### 6.1 必需的环境变量

| 变量名 | 说明 | 示例 |
| - | - | - |
| `nacos.server-addr` | Nacos 服务地址 | `localhost:8848` |
| `nacos.username` | Nacos 用户名 | `nacos` |
| `nacos.password` | Nacos 密码 | `nacos` |
| `nacos.env` | 环境标识 | `dev` / `test` / `prod` |
| `mysql.host` | MySQL 主机 | `localhost` |
| `mysql.port` | MySQL 端口 | `3306` |
| `mysql.database` | 数据库名 | `mineguard` |
| `mysql.username` | 数据库用户名 | `root` |
| `mysql.password` | 数据库密码 | `password` |
| `redis.host` | Redis 主机 | `localhost` |
| `redis.port` | Redis 端口 | `6379` |
| `redis.password` | Redis 密码 | `password` |

### 6.2 可选的环境变量

| 变量名 | 说明 | 默认值 |
| - | - | - |
| `jwt.secret` | JWT 密钥 | `mineguard-jwt-secret-key-2024` |
| `minio.endpoint` | MinIO 端点 | `http://localhost:9000` |
| `minio.access-key` | MinIO 访问密钥 | `minioadmin` |
| `minio.secret-key` | MinIO 密钥 | `minioadmin` |
| `rocketmq.namesrvAddr` | RocketMQ 地址 | `localhost:9876` |

### 6.3 启动命令示例

```bash
java -jar user-service.jar \
  -Dnacos.server-addr=localhost:8848 \
  -Dnacos.env=dev \
  -Dmysql.host=localhost \
  -Dmysql.password=your_password
```

## 7. Nacos 配置初始化

### 7.1 创建配置列表

在 Nacos 控制台创建以下配置：

**Group: shared-dev（开发环境公共配置）**

| Data ID | 说明 |
| - | - |
| common-database.yml | MySQL 数据库配置 |
| common-redis.yml | Redis 缓存配置 |
| common-rocketmq.yml | RocketMQ 消息队列配置 |
| common-monitor.yml | 监控配置 |

**Group: service-dev（开发环境服务配置）**

| Data ID | 说明 |
| - | - |
| user-service.yml | 用户服务配置 |
| vehicle-service.yml | 车辆服务配置 |
| gateway-service.yml | 网关服务配置 |
| ... | 其他服务配置 |

**Group: shared-test / service-test（测试环境）**

同上结构，配置值不同。

**Group: shared-prod / service-prod（生产环境）**

同上结构，配置值不同。

### 7.2 环境切换

切换环境只需修改 `nacos.env` 环境变量：

```bash
export nacos.env=test
```

服务会自动加载：
- `shared-test` 分组的公共配置
- `service-test` 分组的服务配置

## 8. 配置最佳实践

### 8.1 敏感配置处理

```yaml
spring:
  datasource:
    password: ${mysql.password}
```

- 敏感配置通过环境变量注入
- 不在 Nacos 中存储明文密码
- 生产环境使用 Nacos 加密配置功能

### 8.2 配置变更流程

1. 在 Nacos 控制台修改配置
2. 发布配置（支持灰度发布）
3. 服务自动刷新配置（`refresh: true`）
4. 查看配置历史和回滚

### 8.3 配置验证

启动服务时检查配置是否正确加载：

```bash
curl http://localhost:8001/actuator/health
```

## 9. 迁移指南

### 9.1 清理公共模块配置

需要从以下公共模块中移除配置文件：

- `mineguard-common-auth/src/main/resources/`
- `mineguard-common-redis/src/main/resources/`
- `mineguard-common-web/src/main/resources/`

**注意**：保留 `src/test/resources/application-test.yml` 用于测试。

### 9.2 迁移步骤

1. 在 Nacos 创建公共配置（Group: shared-dev）
   - common-database.yml
   - common-redis.yml
   - common-rocketmq.yml
   - common-monitor.yml
2. 在 Nacos 创建服务配置（Group: service-dev）
   - user-service.yml
   - vehicle-service.yml
   - ...
3. 删除公共模块 `src/main/resources/` 中的配置文件
4. 删除服务模块中的 `application.yml`（只保留 `bootstrap.yml`）
5. 保留测试配置 `src/test/resources/application-test.yml`
6. 验证服务启动正常

---

通过遵循这些规则，可以实现配置的集中管理、环境隔离和安全存储，为系统的稳定运行和快速部署提供保障。
