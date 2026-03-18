# vehicle-service 模块 YML 配置示例

## 说明

vehicle-service 模块的配置遵循微服务架构原则，大部分配置（数据库、Redis等）通过服务发现和配置中心获取，本地只保留必要的业务配置。

## 最小配置示例

```yaml
# 服务器配置
server:
  port: 8082

# 应用配置
spring:
  application:
    name: vehicle-service

# MyBatis-Plus配置
mybatis-plus:
  mapper-locations: classpath:/mapper/**/*.xml
  type-aliases-package: com.klzw.service.vehicle.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: assign_id
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

# Knife4j文档配置
knife4j:
  enable: true
  setting:
    language: zh_cn

# 日志配置
logging:
  level:
    com.klzw: debug
    org.springframework: info

# 文件存储配置
mineguard:
  file:
    storage:
      type: local
      local:
        base-path: ./uploads
```

## 完整配置示例

```yaml
# 服务器配置
server:
  port: 8082

# 应用配置
spring:
  application:
    name: vehicle-service
  # 文件上传配置
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 20MB

# MyBatis-Plus配置
mybatis-plus:
  mapper-locations: classpath:/mapper/**/*.xml
  type-aliases-package: com.klzw.service.vehicle.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    cache-enabled: false
  global-config:
    db-config:
      id-type: assign_id
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
      table-prefix: ""

# Knife4j文档配置
knife4j:
  enable: true
  setting:
    language: zh_cn
    swagger-model-name: 实体类列表

# 日志配置
logging:
  level:
    com.klzw: debug
    org.springframework: info
    com.baomidou: debug
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

# MineGuard业务配置
mineguard:
  # 文件存储配置
  file:
    storage:
      type: local
      local:
        base-path: ./uploads
        url-prefix: /uploads/
  # 车辆服务配置
  vehicle:
    # 默认状态
    default-status: 0
    # 状态更新间隔（秒）
    status-update-interval: 30
    # 离线判定时间（秒）
    offline-threshold: 300
    # GPS精度阈值（米）
    gps-accuracy-threshold: 50
  # 调度配置
  dispatch:
    # 最大并行调度数
    max-concurrent-dispatches: 10
    # 调度提前提醒时间（分钟）
    advance-reminder-minutes: 30
    # 自动完成调度（到达结束时间后）
    auto-complete-enabled: true
```

## 配置项说明

### MyBatis-Plus配置（mybatis-plus）

| 配置项 | 类型 | 默认值 | 说明 |
|-------|------|--------|------|
| mapper-locations | string | classpath:/mapper/**/*.xml | Mapper XML文件位置 |
| type-aliases-package | string | com.klzw.service.vehicle.entity | 实体类包路径 |
| map-underscore-to-camel-case | boolean | true | 开启驼峰命名转换 |
| id-type | string | assign_id | 主键类型（雪花算法） |
| logic-delete-field | string | deleted | 逻辑删除字段名 |

### 文件存储配置（mineguard.file.storage）

| 配置项 | 类型 | 默认值 | 说明 |
|-------|------|--------|------|
| type | string | local | 存储类型（local/oss） |
| local.base-path | string | ./uploads | 本地存储路径 |
| local.url-prefix | string | /uploads/ | URL前缀 |

### 车辆服务配置（mineguard.vehicle）

| 配置项 | 类型 | 默认值 | 说明 |
|-------|------|--------|------|
| default-status | int | 0 | 新车辆默认状态（0-离线） |
| status-update-interval | int | 30 | 状态更新间隔（秒） |
| offline-threshold | int | 300 | 离线判定时间（秒） |
| gps-accuracy-threshold | int | 50 | GPS精度阈值（米） |

### 调度配置（mineguard.dispatch）

| 配置项 | 类型 | 默认值 | 说明 |
|-------|------|--------|------|
| max-concurrent-dispatches | int | 10 | 最大并行调度数 |
| advance-reminder-minutes | int | 30 | 调度提前提醒时间（分钟） |
| auto-complete-enabled | boolean | true | 是否自动完成调度 |

## 通过配置中心获取的配置

以下配置通过 Nacos 配置中心获取，不在本地 application.yml 中配置：

- 数据库配置（spring.datasource）
- Redis配置（spring.data.redis）
- 服务发现配置（spring.cloud.nacos）
- 链路追踪配置（spring.sleuth）
- 监控配置（management）

## 环境变量配置示例

生产环境建议使用环境变量配置敏感信息：

```yaml
server:
  port: ${SERVER_PORT:8082}

spring:
  application:
    name: vehicle-service

mineguard:
  file:
    storage:
      type: ${STORAGE_TYPE:local}
      local:
        base-path: ${STORAGE_BASE_PATH:./uploads}
  vehicle:
    default-status: ${VEHICLE_DEFAULT_STATUS:0}
    status-update-interval: ${STATUS_UPDATE_INTERVAL:30}
    offline-threshold: ${OFFLINE_THRESHOLD:300}
  dispatch:
    max-concurrent-dispatches: ${MAX_DISPATCHES:10}
    advance-reminder-minutes: ${ADVANCE_REMINDER:30}
```

## 多环境配置

### 开发环境（application-dev.yml）

```yaml
logging:
  level:
    com.klzw: debug

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

mineguard:
  file:
    storage:
      type: local
      local:
        base-path: ./uploads
```

### 生产环境（application-prod.yml）

```yaml
logging:
  level:
    com.klzw: info
    org.springframework: warn

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.nologging.NoLoggingImpl

mineguard:
  file:
    storage:
      type: oss
      oss:
        endpoint: ${OSS_ENDPOINT}
        bucket-name: ${OSS_BUCKET}
        access-key: ${OSS_ACCESS_KEY}
        secret-key: ${OSS_SECRET_KEY}
```
