# MineGuard 后端 YAML 配置管理规则

## 1. 配置管理概述

### 1.1 配置文件类型

| 配置类型 | 文件名 | 用途 | 优先级 |
| ------- | ------ | ---- | ------ |
| 引导配置 | bootstrap.yml | 服务启动配置、Nacos 配置 | 最高 |
| 应用配置 | application.yml | 应用核心配置 | 中 |
| 环境配置 | application-{profile}.yml | 环境特定配置 | 低 |
| 本地配置 | application-local.yml | 本地开发配置 | 最低 |

### 1.2 配置管理原则

- **集中管理**：核心配置通过 Nacos 集中管理
- **环境隔离**：不同环境使用不同配置文件
- **分层管理**：按模块和功能分层配置
- **可追溯性**：配置变更有版本记录
- **安全性**：敏感配置加密存储
- **公共模块配置原则**：
  - **核心功能不提供开关**：公共模块的核心功能（如异常处理、拦截器、校验器）默认启用，不提供 enabled 开关
  - **只配置可调整参数**：只保留真正需要动态调整的参数（如文件大小、CORS 配置）
  - **避免误配置**：防止因关闭核心功能开关导致系统异常
  - **被动功能无需配置**：工具类、异常处理等被动功能不需要配置文件

### 1.3 先解决“错乱源”：配置归属与禁用项（必须遵守）

#### 1) 配置归属（单一事实来源）

- **服务模块（可执行应用）**：唯一允许持有 `application*.yml(yaml)` / `bootstrap*.yml(yaml)` 的地方。
- **公共模块（`mineguard-common-*`，打成 jar 供别人依赖）**：只提供代码能力（AutoConfiguration、`@ConfigurationProperties`、默认值），**不允许**携带 `application*.yml(yaml)` 作为“默认配置”。

原因：Spring Boot 会从 classpath 上加载 `application*.yml(yaml)`，公共模块 jar 里的同名文件会与服务自身配置**合并或覆盖**，非常容易出现“看似配置在 A 服务，实际被 B jar 覆盖”的错乱。

#### 2) 明确禁止（高风险，容易导致全局错乱）

- **禁止**：在任何 `mineguard-common-*` 模块里配置 `spring.application.name`（会污染所有依赖它的服务，导致 Nacos 组/配置名/日志/指标等整体串台）。
- **禁止**：公共模块 jar 内放置 `application.yml` / `application.yaml` / `bootstrap.yml` / `bootstrap.yaml`。
- **禁止**：同一个 key 在“本地文件 + Nacos + 共享配置”多处重复定义且没有明确覆盖顺序。

### 1.4 当前仓库的落地现状（用于排查“为什么配置会乱”）

目前 `backend/` 下已出现公共模块携带配置文件的情况（示例）：

- `mineguard-common-auth/src/main/resources/application.yaml`：包含 `spring.application.name: mineguard-common-auth`
- `mineguard-common-redis/src/main/resources/application.yaml`：包含 `spring.application.name: mineguard-common-redis`
- `mineguard-common-web/src/main/resources/application.yml`：包含 `mineguard.web.cors.*`、`spring.servlet.multipart.*`

这会导致：

- 任何依赖这些 common 模块的“服务模块”在启动时，都可能被依赖 jar 内的 `application*.yml(yaml)` **合并/覆盖**配置。
- 最危险的是 `spring.application.name`：一旦被覆盖，会导致 Nacos 配置分组/日志/链路/指标等出现“串台”，表象就是“配置明明没写，却被改了”。

建议迁移策略（不在本规则文档里强制落地代码变更，但建议尽快执行）：

1. 公共模块移除 `application*.yml(yaml)`（或至少移除所有 `spring.*` 关键项）。
2. 公共模块的可配置项通过 `@ConfigurationProperties` 暴露，默认值放在代码里；实际值由服务模块或 Nacos 提供。

## 2. 配置分组策略

### 2.1 模块级配置分组

| 模块名称 | 配置分组 | 说明 |
| ------- | -------- | ---- |
| user-service | user-service | 用户服务相关配置 |
| vehicle-service | vehicle-service | 车辆服务相关配置 |
| trip-service | trip-service | 行程服务相关配置 |
| warning-service | warning-service | 预警服务相关配置 |
| statistics-service | statistics-service | 统计服务相关配置 |
| cost-service | cost-service | 成本服务相关配置 |
| ai-service | ai-service | AI 分析服务相关配置 |
| iot-service | iot-service | IoT 服务相关配置 |
| gateway-service | gateway-service | 网关服务相关配置 |

### 2.2 环境级配置分组

| 环境 | 配置分组 | 激活方式 |
| ---- | -------- | -------- |
| 开发环境 | dev | spring.profiles.active=dev |
| 测试环境 | test | spring.profiles.active=test |
| 预发环境 | pre | spring.profiles.active=pre |
| 生产环境 | prod | spring.profiles.active=prod |

## 3. 配置包含机制

### 3.1 配置片段的包含方式（推荐使用 `spring.config.import`）

在 Spring Boot 2.4+（当前项目父 POM 为 Spring Boot 4.x）中，更推荐使用 `spring.config.import` 来显式引入配置片段，这样顺序清晰、覆盖关系可控。

```yaml
# application.yml（服务模块内）
spring:
  config:
    import:
      - optional:classpath:config/application-database.yml
      - optional:classpath:config/application-redis.yml
      - optional:classpath:config/application-mq.yml
      - optional:classpath:config/application-security.yml
      - optional:classpath:config/application-monitor.yml
```

说明：

- `optional:` 表示片段缺失时不报错（方便按需启用）。
- **不要**把这些片段放在 `mineguard-common-*` 里；它们只能属于“服务模块”或“Nacos 配置中心”。

### 3.2 推荐的服务模块配置文件结构（避免与“配置模块”概念混淆）

这里的 `config/` 指的是**服务模块 resources 下的配置片段目录**（不是 Maven 模块、也不是所谓“配置模块”）。

```tree
{service-module}/
└── src/main/resources/
    ├── application.yml                  # 主配置（只放该服务的通用配置）
    ├── application-dev.yml              # 环境配置（可选）
    ├── application-test.yml             # 环境配置（可选）
    ├── application-pre.yml              # 环境配置（可选）
    ├── application-prod.yml             # 环境配置（可选）
    ├── application-local.yml            # 本地开发覆盖（可选）
    ├── bootstrap.yml                    # 仅放“配置中心连接信息”（如果你仍使用 bootstrap 机制）
    └── config/                          # 仅放“配置片段”，由 spring.config.import 引入
        ├── application-database.yml
        ├── application-redis.yml
        ├── application-mq.yml
        ├── application-security.yml
        └── application-monitor.yml
```

### 3.3 配置加载与覆盖优先级（从低到高）

1. **默认值**：代码默认值（`@ConfigurationProperties` 字段默认值等）
2. **本地配置片段**：`spring.config.import` 引入的 `classpath:config/application-*.yml`
3. **本地主配置**：`application.yml`
4. **本地环境配置**：`application-{profile}.yml` / `application-local.yml`
5. **Nacos 远程配置**：作为“最终可运维调整”的来源（建议覆盖本地）
6. **系统环境变量 / JVM 参数 / 命令行参数**：最高优先级

关键点：无论采用哪种拆分方式，都要保证**同一个 key 只有一个“主负责位置”**，其余位置只能作为覆盖（并在文档里明确覆盖顺序）。

## 4. 各模块配置结构

### 4.1 通用模块配置

#### mineguard-common-core

**说明**：common-core 模块不包含配置文件，因为：
- 异常处理、响应封装、工具类都是被动功能
- 不需要配置开关
- 避免误配置导致系统异常

**配置原则**：无配置文件，所有功能默认启用

#### mineguard-common-web

```yaml
# 仅包含可配置的参数，核心功能不提供开关
mineguard:
  web:
    cors:
      allowed-origins: "*"
      allowed-methods: "*"
      allowed-headers: "*"
      allow-credentials: true
      max-age: 3600
    file-upload:
      max-file-size: 50MB
      max-request-size: 100MB
      location: ${java.io.tmpdir}
```

**配置原则**：
- **核心功能不提供开关**：拦截器、校验器等核心功能默认启用，不提供 enabled 开关
- **只配置可调整参数**：只保留真正需要动态调整的参数（如文件大小、CORS 配置）
- **避免误配置**：防止因关闭核心功能开关导致系统异常

#### mineguard-common-database

```yaml
# 仅包含功能相关配置项
spring:
  datasource:
    url: jdbc:mysql://${mysql.host}:${mysql.port}/${mysql.database}?useUnicode=true&characterEncoding=utf-8&useSSL=false
    username: ${mysql.username}
    password: ${mysql.password}
    driver-class-name: com.mysql.cj.jdbc.Driver
  
mybatis-plus:
  mapper-locations: classpath*:mapper/**/*.xml
  type-aliases-package: com.klzw.**.domain
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

#### mineguard-common-redis

```yaml
# 仅包含功能相关配置项
spring:
  redis:
    host: ${redis.host}
    port: ${redis.port}
    password: ${redis.password}
    database: ${redis.database}
    timeout: 3000
    lettuce:
      pool:
        max-active: 8
        max-wait: -1
        max-idle: 8
        min-idle: 0
```

#### mineguard-common-mq

```yaml
# 仅包含功能相关配置项
rocketmq:
  name-server: ${rocketmq.namesrvAddr}
  producer:
    group: ${rocketmq.producer.group}
    send-message-timeout: 3000
  consumer:
    group: ${rocketmq.consumer.group}
    consume-thread-min: 20
    consume-thread-max: 64
```

#### mineguard-common-auth

```yaml
# 仅包含功能相关配置项
mineguard:
  auth:
    jwt:
      secret: ${jwt.secret}
      expire-time: 3600
      refresh-time: 7200
    permission:
      enabled: true
      super-admin-role: admin
```

#### mineguard-common-log

```yaml
# 仅包含功能相关配置项
logging:
  level:
    com.klzw: ${logging.level}
  file:
    name: ${logging.file.path}/${spring.application.name}.log
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"

mineguard:
  log:
    async:
      enabled: true
    audit:
      enabled: true
```

#### mineguard-common-file

```yaml
# 仅包含功能相关配置项
mineguard:
  file:
    storage:
      # MinIO 配置
      minio:
        enabled: true
        endpoint: ${minio.endpoint}
        access-key: ${minio.access-key}
        secret-key: ${minio.secret-key}
        bucket-name: ${minio.bucket-name}
        secure: false
      # 阿里云 OSS 配置
      aliyun-oss:
        enabled: true
        endpoint: ${aliyun.oss.endpoint}
        access-key-id: ${aliyun.oss.access-key-id}
        access-key-secret: ${aliyun.oss.access-key-secret}
        bucket-name: ${aliyun.oss.bucket-name}
        region: ${aliyun.oss.region}
    # 文件配置
    config:
      max-file-size: 104857600  # 100MB
      max-request-size: 104857600
      allowed-file-types: ["image/jpeg", "image/png", "image/gif", "application/pdf"]
      storage-path: "files"
    # OCR 配置
    ocr:
      enabled: true
      type: "baidu"  # tesseract 或 baidu
      baidu:
        app-id: ${baidu.ocr.app-id}
        api-key: ${baidu.ocr.api-key}
        secret-key: ${baidu.ocr.secret-key}
      tesseract:
        path: ${tesseract.path}
    # 容灾配置
    disaster-recovery:
      enabled: true
      sync-strategy: "realtime"  # realtime 或 scheduled
      sync-interval: 3600  # 同步间隔（秒），仅用于 scheduled 策略
```

#### mineguard-common-map

```yaml
# 仅包含功能相关配置项
mineguard:
  map:
    gaode:
      enabled: true
      api-key: ${gaode.api-key}
      service-host: https://restapi.amap.com
      timeout: 5000
      retry-count: 3
    # 缓存配置
    cache:
      enabled: true
      expire-time: 3600  # 缓存过期时间（秒）
      max-size: 1000  # 最大缓存数量
    # 地理编码配置
    geocode:
      batch-size: 10  # 批量地理编码请求大小
      rate-limit: 100  # 每秒请求限制
```

### 4.2 服务模块配置

#### user-service

```yaml
# bootstrap.yml
spring:
  application:
    name: user-service
  profiles:
    active: @spring.profiles.active@
  cloud:
    nacos:
      discovery:
        server-addr: ${nacos.server-addr}
      config:
        server-addr: ${nacos.server-addr}
        namespace: ${nacos.namespace}
        group: user-service

# application.yml
server:
  port: 8001

spring:
  config:
    import:
      - optional:classpath:config/application-database.yml
      - optional:classpath:config/application-redis.yml
      - optional:classpath:config/application-security.yml
      - optional:classpath:config/application-monitor.yml

mineguard:
  user:
    enable-feign: true
    enable-cache: true
```

#### vehicle-service

```yaml
# bootstrap.yml
spring:
  application:
    name: vehicle-service
  profiles:
    active: @spring.profiles.active@
  cloud:
    nacos:
      discovery:
        server-addr: ${nacos.server-addr}
      config:
        server-addr: ${nacos.server-addr}
        namespace: ${nacos.namespace}
        group: vehicle-service

# application.yml
server:
  port: 8002

spring:
  config:
    import:
      - optional:classpath:config/application-database.yml
      - optional:classpath:config/application-redis.yml
      - optional:classpath:config/application-mq.yml
      - optional:classpath:config/application-monitor.yml

mineguard:
  vehicle:
    enable-location: true
    enable-status-monitor: true
    location-update-interval: 10000
```

#### trip-service

```yaml
# bootstrap.yml
spring:
  application:
    name: trip-service
  profiles:
    active: @spring.profiles.active@
  cloud:
    nacos:
      discovery:
        server-addr: ${nacos.server-addr}
      config:
        server-addr: ${nacos.server-addr}
        namespace: ${nacos.namespace}
        group: trip-service

# application.yml
server:
  port: 8003

spring:
  config:
    import:
      - optional:classpath:config/application-database.yml
      - optional:classpath:config/application-redis.yml
      - optional:classpath:config/application-mq.yml
      - optional:classpath:config/application-monitor.yml

mineguard:
  trip:
    enable-route: true
    enable-history: true
    route-cache-time: 300000
```

#### warning-service

```yaml
# bootstrap.yml
spring:
  application:
    name: warning-service
  profiles:
    active: @spring.profiles.active@
  cloud:
    nacos:
      discovery:
        server-addr: ${nacos.server-addr}
      config:
        server-addr: ${nacos.server-addr}
        namespace: ${nacos.namespace}
        group: warning-service

# application.yml
server:
  port: 8004

spring:
  config:
    import:
      - optional:classpath:config/application-database.yml
      - optional:classpath:config/application-redis.yml
      - optional:classpath:config/application-mq.yml
      - optional:classpath:config/application-monitor.yml

mineguard:
  warning:
    enable-detection: true
    enable-notification: true
    detection-interval: 5000
```

#### statistics-service

```yaml
# bootstrap.yml
spring:
  application:
    name: statistics-service
  profiles:
    active: @spring.profiles.active@
  cloud:
    nacos:
      discovery:
        server-addr: ${nacos.server-addr}
      config:
        server-addr: ${nacos.server-addr}
        namespace: ${nacos.namespace}
        group: statistics-service

# application.yml
server:
  port: 8005

spring:
  config:
    import:
      - optional:classpath:config/application-database.yml
      - optional:classpath:config/application-redis.yml
      - optional:classpath:config/application-monitor.yml

mineguard:
  statistics:
    enable-cache: true
    cache-expire-time: 3600000
    enable-export: true
```

#### cost-service

```yaml
# bootstrap.yml
spring:
  application:
    name: cost-service
  profiles:
    active: @spring.profiles.active@
  cloud:
    nacos:
      discovery:
        server-addr: ${nacos.server-addr}
      config:
        server-addr: ${nacos.server-addr}
        namespace: ${nacos.namespace}
        group: cost-service

# application.yml
server:
  port: 8006

spring:
  config:
    import:
      - optional:classpath:config/application-database.yml
      - optional:classpath:config/application-redis.yml
      - optional:classpath:config/application-monitor.yml

mineguard:
  cost:
    enable-budget: true
    enable-analysis: true
```

#### ai-service

```yaml
# bootstrap.yml
spring:
  application:
    name: ai-service
  profiles:
    active: @spring.profiles.active@
  cloud:
    nacos:
      discovery:
        server-addr: ${nacos.server-addr}
      config:
        server-addr: ${nacos.server-addr}
        namespace: ${nacos.namespace}
        group: ai-service

# application.yml
server:
  port: 8007

spring:
  config:
    import:
      - optional:classpath:config/application-database.yml
      - optional:classpath:config/application-redis.yml
      - optional:classpath:config/application-monitor.yml

mineguard:
  ai:
    enable-prediction: true
    enable-recommendation: true
    model-cache-size: 10
```

#### iot-service

```yaml
# bootstrap.yml
spring:
  application:
    name: iot-service
  profiles:
    active: @spring.profiles.active@
  cloud:
    nacos:
      discovery:
        server-addr: ${nacos.server-addr}
      config:
        server-addr: ${nacos.server-addr}
        namespace: ${nacos.namespace}
        group: iot-service

# application.yml
server:
  port: 8008

spring:
  config:
    import:
      - optional:classpath:config/application-database.yml
      - optional:classpath:config/application-redis.yml
      - optional:classpath:config/application-mq.yml
      - optional:classpath:config/application-monitor.yml

mineguard:
  iot:
    enable-mqtt: true
    enable-websocket: true
    mqtt-broker: ${mqtt.broker}
    websocket-path: /ws/device
```

#### gateway-service

```yaml
# bootstrap.yml
spring:
  application:
    name: gateway-service
  profiles:
    active: @spring.profiles.active@
  cloud:
    nacos:
      discovery:
        server-addr: ${nacos.server-addr}
      config:
        server-addr: ${nacos.server-addr}
        namespace: ${nacos.namespace}
        group: gateway-service

# application.yml
server:
  port: 8080

spring:
  config:
    import:
      - optional:classpath:config/application-security.yml
      - optional:classpath:config/application-monitor.yml

mineguard:
  gateway:
    enable-rate-limit: true
    enable-circuit-breaker: true
    rate-limit-count: 1000
    rate-limit-time: 60
```

#### file-service

```yaml
# bootstrap.yml
spring:
  application:
    name: file-service
  profiles:
    active: @spring.profiles.active@
  cloud:
    nacos:
      discovery:
        server-addr: ${nacos.server-addr}
      config:
        server-addr: ${nacos.server-addr}
        namespace: ${nacos.namespace}
        group: file-service

# application.yml
server:
  port: 8009

spring:
  config:
    import:
      - optional:classpath:config/application-redis.yml
      - optional:classpath:config/application-monitor.yml

mineguard:
  file:
    storage:
      # MinIO 配置
      minio:
        enabled: true
        endpoint: ${minio.endpoint}
        access-key: ${minio.access-key}
        secret-key: ${minio.secret-key}
        bucket-name: ${minio.bucket-name}
        secure: false
      # 阿里云 OSS 配置
      aliyun-oss:
        enabled: true
        endpoint: ${aliyun.oss.endpoint}
        access-key-id: ${aliyun.oss.access-key-id}
        access-key-secret: ${aliyun.oss.access-key-secret}
        bucket-name: ${aliyun.oss.bucket-name}
        region: ${aliyun.oss.region}
    # 文件配置
    config:
      max-file-size: 104857600  # 100MB
      max-request-size: 104857600
      allowed-file-types: ["image/jpeg", "image/png", "image/gif", "application/pdf"]
      storage-path: "files"
    # OCR 配置
    ocr:
      enabled: true
      type: "baidu"  # tesseract 或 baidu
      baidu:
        app-id: ${baidu.ocr.app-id}
        api-key: ${baidu.ocr.api-key}
        secret-key: ${baidu.ocr.secret-key}
      tesseract:
        path: ${tesseract.path}
    # 容灾配置
    disaster-recovery:
      enabled: true
      sync-strategy: "realtime"  # realtime 或 scheduled
      sync-interval: 3600  # 同步间隔（秒），仅用于 scheduled 策略
```

## 5. 配置激活策略

### 5.1 环境激活方式

#### 1. 配置文件激活

```yaml
# application.yml
spring:
  profiles:
    active: dev
```

#### 2. 命令行激活

```bash
# 开发环境
java -jar user-service.jar --spring.profiles.active=dev

# 测试环境
java -jar user-service.jar --spring.profiles.active=test

# 生产环境
java -jar user-service.jar --spring.profiles.active=prod
```

#### 3. 启动脚本激活

```bash
# start.sh
#!/bin/bash
PROFILE=${1:-dev}
java -jar user-service.jar --spring.profiles.active=$PROFILE
```

### 5.2 多环境激活

```yaml
# 激活多个环境
spring:
  profiles:
    active:
      - dev
      - local
      - debug
```

## 6. 配置命名规范

### 6.1 文件命名规范

| 配置类型 | 命名格式 | 示例 |
| ------- | -------- | ---- |
| 主配置文件 | application.yml | application.yml |
| 环境配置文件 | application-{profile}.yml | application-dev.yml |
| 功能配置文件 | application-{function}.yml | application-database.yml |
| 模块配置文件 | {module}-{function}.yml | user-datasource.yml |

### 6.2 配置项命名规范

| 配置类别 | 命名格式 | 示例 |
| ------- | -------- | ---- |
| 系统配置 | spring.{category}.{name} | spring.datasource.url |
| 模块配置 | mineguard.{module}.{name} | mineguard.user.enable-cache |
| 环境变量 | {UPPER_CASE_NAME} | MYSQL_HOST |
| 占位符 | ${variable.name} | ${mysql.host} |

### 6.3 目录结构规范

#### 配置模块目录结构

```tree
{config-module}/
├── src/main/resources/
│   └── application.yml    # 仅包含功能相关配置
└── pom.xml
```

#### 服务模块目录结构

```tree
{service-module}/
├── src/main/resources/
│   ├── bootstrap.yml
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-test.yml
│   ├── application-prod.yml
│   ├── application-local.yml
│   └── config/
│       ├── application-database.yml
│       ├── application-redis.yml
│       ├── application-mq.yml
│       ├── application-security.yml
│       └── application-monitor.yml
└── pom.xml
```

## 7. 配置优先级规则

### 7.1 内部优先级（从高到低）

1. **命令行参数**：`java -jar --key=value`
2. **系统环境变量**：`MYSQL_HOST=localhost`
3. **Nacos 远程配置**：远程配置中心
4. **本地配置文件**：按以下顺序
   - application-{profile}.yml
   - application.yml
   - bootstrap-{profile}.yml
   - bootstrap.yml
5. **默认配置**：框架默认值

### 7.2 外部优先级（从高到低）

1. **Docker 环境变量**：容器环境变量
2. **Kubernetes ConfigMap**：集群配置
3. **云服务配置**：云平台配置服务
4. **本地环境变量**：操作系统环境变量

## 8. 配置管理最佳实践

### 8.1 敏感配置管理

#### 1. 加密存储

```yaml
# 加密配置
spring:
  datasource:
    password: ${encrypted.password}

# 使用 Jasypt 加密
jasypt:
  encryptor:
    password: ${JASYPT_ENCRYPTOR_PASSWORD}
```

#### 2. 环境变量注入

```yaml
# 环境变量注入
spring:
  datasource:
    username: ${MYSQL_USERNAME:root}
    password: ${MYSQL_PASSWORD:password}
```

### 8.2 配置变更管理

#### 1. 变更流程

1. **变更申请**：提交配置变更申请
2. **变更审批**：相关人员审批
3. **变更实施**：通过 Nacos 控制台修改
4. **变更验证**：验证配置生效
5. **变更记录**：记录变更历史

#### 2. 回滚策略

- **版本管理**：Nacos 配置版本控制
- **快速回滚**：支持配置版本回滚
- **灰度发布**：部分实例先应用变更

### 8.3 配置监控

#### 1. 监控指标

- **配置加载时间**：启动时配置加载耗时
- **配置变更次数**：运行时配置变更次数
- **配置错误率**：配置加载错误率
- **配置一致性**：集群配置一致性

#### 2. 告警机制

- **配置变更告警**：重要配置变更通知
- **配置错误告警**：配置加载失败告警
- **配置过期告警**：配置有效期监控

## 9. 配置示例

### 9.1 Nacos 配置示例

#### 1. 开发环境配置

**Data ID**: user-service-dev.yml
**Group**: user-service

```yaml
mysql:
  host: localhost
  port: 3306
  database: mineguard_dev
  username: root
  password: password

redis:
  host: localhost
  port: 6379
  password: 
  database: 0

nacos:
  server-addr: localhost:8848
  namespace: dev

logging:
  level: info
```

#### 2. 生产环境配置

**Data ID**: user-service-prod.yml
**Group**: user-service

```yaml
mysql:
  host: mysql-prod
  port: 3306
  database: mineguard_prod
  username: ${MYSQL_USERNAME}
  password: ${MYSQL_PASSWORD}

redis:
  host: redis-prod
  port: 6379
  password: ${REDIS_PASSWORD}
  database: 0

nacos:
  server-addr: nacos-prod:8848
  namespace: prod

logging:
  level: warn
```

### 9.2 本地开发配置

```yaml
# application-local.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mineguard_local
    username: root
    password: root

  redis:
    host: localhost
    port: 6379
    password: 

  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
      config:
        server-addr: localhost:8848

logging:
  level:
    com.klzw: debug

mineguard:
  user:
    enable-debug: true
  vehicle:
    enable-simulator: true
```

## 10. 配置验证

### 10.1 验证工具

| 工具 | 用途 | 命令 |
| ---- | ---- | ---- |
| Spring Boot Config Processor | 配置元数据生成 | mvn compile |
| Spring Boot Actuator | 配置查看 | /actuator/configprops |
| Nacos Config SDK | 配置管理 | 编程方式管理配置 |

### 10.2 验证流程

1. **启动验证**：服务启动时验证配置加载
2. **运行验证**：通过 Actuator 查看配置
3. **变更验证**：配置变更后验证生效
4. **性能验证**：配置对性能的影响

## 11. 总结

本配置管理规则文档定义了 MineGuard 后端系统的 YAML 配置管理规范，包括：

1. **配置文件结构**：清晰的文件分类和命名
2. **配置激活策略**：多环境配置管理
3. **配置包含机制**：模块化配置管理
4. **配置优先级**：明确的优先级规则
5. **最佳实践**：敏感配置、变更管理、监控

通过遵循这些规则，可以实现配置的集中管理、环境隔离、分层管理和安全存储，为系统的稳定运行和快速部署提供保障。
