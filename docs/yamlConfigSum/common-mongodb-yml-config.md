# common-mongodb模块 YML 配置示例

## 1. 完整配置示例

```yaml
# MongoDB模块配置
mineguard:
  # MongoDB配置
  mongodb:
    # 是否启用MongoDB模块，默认值：true
    enabled: true
    # MongoDB主机地址，默认值：localhost
    host: "${MONGODB_HOST:localhost}"
    # MongoDB端口，默认值：27017
    port: ${MONGODB_PORT:27017}
    # 数据库名称（必填）
    database: "${MONGODB_DATABASE:}"
    # 用户名（可选）
    username: "${MONGODB_USERNAME:}"
    # 密码（可选）
    password: "${MONGODB_PASSWORD:}"
    # 认证数据库，默认值：admin
    authentication-database: "${MONGODB_AUTH_DB:admin}"
    # 连接超时时间（毫秒），默认值：10000
    connect-timeout: 10000
    # 读取超时时间（毫秒），默认值：10000
    read-timeout: 10000
    # 最大连接数，默认值：100
    max-connections: 100
    # 最小连接数，默认值：10
    min-connections-per-host: 10
    # 最大等待时间（毫秒），默认值：120000
    max-wait-time: 120000
    # 心跳频率（毫秒），默认值：10000
    heartbeat-frequency: 10000

# Spring Data MongoDB配置
spring:
  data:
    mongodb:
      # 自动索引创建，默认值：true
      auto-index-creation: true
      # 字段命名策略，默认值：camelCase
      field-naming-strategy: camelCase
  # 线程池配置（用于异步操作）
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
    # MongoDB模块日志级别
    "com.klzw.common.mongodb": "info"
    # Spring Data MongoDB日志级别
    "org.springframework.data.mongodb": "warn"
    # MongoDB驱动日志级别
    "org.mongodb": "warn"
  # 日志格式
  pattern:
    # 控制台日志格式
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  # 文件配置
  file:
    # 日志文件名称
    name: "logs/mongodb-service.log"
    # 单个日志文件最大大小
    max-size: "10MB"
    # 日志文件保留天数
    max-history: "7"
```

## 2. 最小配置示例

```yaml
# MongoDB模块配置（最小配置）
mineguard:
  mongodb:
    # 数据库名称（必填）
    database: "${MONGODB_DATABASE:}"
    # 其他配置使用默认值
    # enabled: true
    # host: "localhost"
    # port: 27017
    # connect-timeout: 10000
    # read-timeout: 10000
    # max-connections: 100

# 日志配置
logging:
  level:
    "com.klzw.common.mongodb": "info"
```

## 3. 配置项说明

### 3.1 MongoDB连接配置

| 配置项 | 类型 | 默认值 | 必填 | 说明 |
| ------ | ---- | ------ | ---- | ---- |
| `mineguard.mongodb.enabled` | boolean | `true` | 否 | 是否启用MongoDB模块 |
| `mineguard.mongodb.host` | string | `"localhost"` | 否 | MongoDB主机地址 |
| `mineguard.mongodb.port` | int | `27017` | 否 | MongoDB端口 |
| `mineguard.mongodb.database` | string | - | 是 | 数据库名称 |
| `mineguard.mongodb.username` | string | - | 否 | 用户名（认证时必填） |
| `mineguard.mongodb.password` | string | - | 否 | 密码（认证时必填） |
| `mineguard.mongodb.authentication-database` | string | `"admin"` | 否 | 认证数据库 |

### 3.2 连接池配置

| 配置项 | 类型 | 默认值 | 必填 | 说明 |
| ------ | ---- | ------ | ---- | ---- |
| `mineguard.mongodb.connect-timeout` | int | `10000` | 否 | 连接超时时间（毫秒） |
| `mineguard.mongodb.read-timeout` | int | `10000` | 否 | 读取超时时间（毫秒） |
| `mineguard.mongodb.max-connections` | int | `100` | 否 | 最大连接数 |
| `mineguard.mongodb.min-connections-per-host` | int | `10` | 否 | 最小连接数 |
| `mineguard.mongodb.max-wait-time` | int | `120000` | 否 | 最大等待时间（毫秒） |
| `mineguard.mongodb.heartbeat-frequency` | int | `10000` | 否 | 心跳频率（毫秒） |

### 3.3 环境变量配置

推荐使用环境变量配置敏感信息：

```yaml
mineguard:
  mongodb:
    host: "${MONGODB_HOST:localhost}"
    port: ${MONGODB_PORT:27017}
    database: "${MONGODB_DATABASE:}"
    username: "${MONGODB_USERNAME:}"
    password: "${MONGODB_PASSWORD:}"
```

### 3.4 配置验证

配置验证规则：

- `database` 必须配置，否则服务无法正常工作
- `username` 和 `password` 必须同时配置或同时不配置
- 所有超时和连接池配置必须大于0

## 4. 功能说明

### 4.1 基础CRUD操作

- **功能**：通过BaseMongoRepository提供标准化的CRUD操作
- **配置影响**：连接池配置影响并发性能
- **使用场景**：所有MongoDB数据访问操作

### 4.2 聚合查询

- **功能**：通过AggregationUtil提供专用聚合查询
- **配置影响**：超时配置影响查询性能
- **使用场景**：行驶里程统计、预警趋势分析

### 4.3 地理空间查询

- **功能**：通过GeoQueryUtil提供地理空间查询
- **配置影响**：连接池配置影响并发查询
- **使用场景**：附近车辆查询、地理围栏检测

### 4.4 时序数据管理

- **功能**：通过TimeSeriesUtil提供时序数据操作
- **配置影响**：连接池配置影响批量操作性能
- **使用场景**：车辆轨迹、设备数据管理

### 4.5 TTL索引管理

- **功能**：通过TtlIndexUtil提供自动过期数据管理
- **配置影响**：TTL索引在应用启动时自动创建
- **使用场景**：自动清理过期数据

## 5. 错误处理

### 5.1 配置错误

- **错误类型**：配置缺失或无效
- **处理方式**：启动时检查配置，抛出配置异常
- **建议**：使用环境变量配置敏感信息

### 5.2 连接错误

- **错误类型**：网络超时、认证失败、连接池耗尽
- **处理方式**：重试机制、异常捕获、连接池监控
- **建议**：合理设置超时时间和连接池大小

### 5.3 操作错误

- **错误类型**：文档不存在、键值冲突、索引错误
- **处理方式**：使用MongoDbException统一处理
- **建议**：使用MongoDbResultCode错误码进行错误追踪

## 6. 性能优化建议

### 6.1 连接池配置

- **最大连接数**：根据并发量调整，建议50-200
- **最小连接数**：根据平均负载调整，建议5-20
- **超时时间**：根据网络状况调整，建议5-15秒

### 6.2 索引优化

- **业务索引**：为常用查询字段创建索引
- **复合索引**：为多字段查询创建复合索引
- **TTL索引**：为过期数据配置TTL索引

### 6.3 批量操作

- **批量插入**：使用TimeSeriesUtil.batchInsertTimeSeriesData
- **批量删除**：优化BaseMongoRepositoryImpl.deleteAll方法
- **批量更新**：使用MongoTemplate的批量更新功能

## 7. 安全注意事项

### 7.1 认证安全

- **认证信息**：使用环境变量配置用户名和密码
- **网络传输**：确保MongoDB连接使用SSL/TLS
- **访问控制**：配置MongoDB用户权限最小化原则

### 7.2 数据安全

- **数据加密**：敏感数据加密存储
- **备份策略**：定期备份重要数据
- **访问日志**：记录数据访问日志

### 7.3 网络安全

- **网络隔离**：MongoDB部署在内网环境
- **防火墙**：配置防火墙限制访问来源
- **监控告警**：监控MongoDB连接和操作状态

## 8. 部署建议

### 8.1 开发环境

```yaml
mineguard:
  mongodb:
    enabled: true
    host: "localhost"
    port: 27017
    database: "mineguard_dev"
    connect-timeout: 10000
    read-timeout: 10000
    max-connections: 50
```

### 8.2 测试环境

```yaml
mineguard:
  mongodb:
    enabled: true
    host: "${MONGODB_HOST:192.168.110.128}"
    port: ${MONGODB_PORT:27018}
    database: "${MONGODB_DATABASE:mineguard_test}"
    username: "${MONGODB_USERNAME:admin}"
    password: "${MONGODB_PASSWORD:mongodbadmin}"
    connect-timeout: 5000
    read-timeout: 5000
    max-connections: 100
```

### 8.3 生产环境

```yaml
mineguard:
  mongodb:
    enabled: true
    host: "${MONGODB_HOST:}"
    port: ${MONGODB_PORT:27017}
    database: "${MONGODB_DATABASE:}"
    username: "${MONGODB_USERNAME:}"
    password: "${MONGODB_PASSWORD:}"
    connect-timeout: 3000
    read-timeout: 3000
    max-connections: 200
    min-connections-per-host: 20
    max-wait-time: 60000
```

## 9. TTL索引配置

模块自动配置以下集合的TTL索引：

| 集合 | 过期时间 | TTL索引字段 | 说明 |
| ---- | ------- | ----------- | ---- |
| vehicle_trajectory | 6个月 | timestamp | 车辆轨迹数据 |
| trip_history | 1年 | createTime | 行程历史数据 |
| warning_event | 1年 | warningTime | 预警事件数据 |
| operation_log | 30天 | requestTime | 操作日志数据 |
| exception_log | 90天 | occurTime | 异常日志数据 |
| device_data | 3个月 | timestamp | 设备数据 |
| message_history | 30天 | expireTime | 消息历史数据 |
| statistics_data | 2年 | statDate | 统计数据 |
| cost_record | 2年 | costTime | 成本记录数据 |
| vehicle_maintenance | 2年 | createTime | 车辆维护记录 |

## 10. 变更记录

| 日期 | 变更内容 |
| ---- | ------- |
| 2026-03-12 | 创建common-mongodb模块YAML配置文档 |
| 2026-03-12 | 添加完整配置示例和最小配置示例 |
| 2026-03-12 | 添加配置项说明和功能说明 |
| 2026-03-12 | 添加错误处理、性能优化、安全注意事项 |
| 2026-03-12 | 添加部署建议和TTL索引配置 |
| 2026-03-12 | 添加变更记录 |
