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

### 3.1 使用 include 包含配置

```yaml
# application.yml
spring:
  profiles:
    include:
      - database    # 数据库配置
      - redis       # Redis 配置
      - mq          # 消息队列配置
      - security    # 安全配置
      - monitor     # 监控配置
```

### 3.2 配置文件结构

```tree
config/
├── application.yml            # 主配置文件
├── application-dev.yml        # 开发环境配置
├── application-test.yml       # 测试环境配置
├── application-pre.yml        # 预发环境配置
├── application-prod.yml       # 生产环境配置
├── application-database.yml   # 数据库配置
├── application-redis.yml      # Redis 配置
├── application-mq.yml         # 消息队列配置
├── application-security.yml   # 安全配置
└── application-monitor.yml    # 监控配置
```

### 3.3 配置包含优先级

1. **主配置文件**：application.yml
2. **环境配置文件**：application-{profile}.yml
3. **包含的配置文件**：按 include 顺序
4. **Nacos 配置**：远程配置覆盖本地配置
5. **命令行参数**：最高优先级

## 4. 各模块配置结构

### 4.1 通用模块配置

#### mineguard-common-core

```yaml
# 仅包含功能相关配置项
mineguard:
  core:
    exception:
      enable-global-exception: true
    response:
      enable-unified-response: true
    utils:
      enable-common-utils: true
```

#### mineguard-common-web

```yaml
# 仅包含功能相关配置项
mineguard:
  web:
    cors:
      enabled: true
      allowed-origins: "*"
    interceptor:
      enabled: true
    validator:
      enabled: true
```

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
  type-aliases-package: com.mineguard.**.domain
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
    com.mineguard: ${logging.level}
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
  profiles:
    include:
      - database
      - redis
      - security
      - monitor

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
  profiles:
    include:
      - database
      - redis
      - mq
      - monitor

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
  profiles:
    include:
      - database
      - redis
      - mq
      - monitor

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
  profiles:
    include:
      - database
      - redis
      - mq
      - monitor

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
  profiles:
    include:
      - database
      - redis
      - monitor

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
  profiles:
    include:
      - database
      - redis
      - monitor

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
  profiles:
    include:
      - database
      - redis
      - monitor

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
  profiles:
    include:
      - database
      - redis
      - mq
      - monitor

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
  profiles:
    include:
      - security
      - monitor

mineguard:
  gateway:
    enable-rate-limit: true
    enable-circuit-breaker: true
    rate-limit-count: 1000
    rate-limit-time: 60
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
    com.mineguard: debug

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
