# common-map模块 YML 配置示例

## 1. 完整配置示例

```yaml
# 地图服务模块配置
mineguard:
  # 高德地图服务配置
  map:
    gaode:
      # 是否启用高德地图服务，默认值：true
      enabled: true
      # 高德地图API密钥（必填）
      api-key: "${GAODE_API_KEY:}"
      # 高德地图API地址，默认值：https://restapi.amap.com/v3
      api-url: "https://restapi.amap.com/v3"
      # HTTP连接超时时间（毫秒），默认值：5000
      connect-timeout: 5000
      # HTTP读取超时时间（毫秒），默认值：10000
      read-timeout: 10000
      # 地理围栏缓存过期时间（秒），默认值：3600
      cache-expire: 3600

# Spring 全局配置
spring:
  # 线程池配置（用于HTTP请求）
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
    # 地图服务包日志级别
    "com.klzw.common.map": "info"
    # Spring 框架日志级别
    "org.springframework": "warn"
  # 日志格式
  pattern:
    # 控制台日志格式
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  # 文件配置
  file:
    # 日志文件名称
    name: "logs/map-service.log"
    # 单个日志文件最大大小
    max-size: "10MB"
    # 日志文件保留天数
    max-history: "7"
```

## 2. 最小配置示例

```yaml
# 地图服务模块配置（最小配置）
mineguard:
  map:
    gaode:
      # 高德地图API密钥（必填）
      api-key: "${GAODE_API_KEY:}"
      # 其他配置使用默认值
      # enabled: true
      # api-url: "https://restapi.amap.com/v3"
      # connect-timeout: 5000
      # read-timeout: 10000
      # cache-expire: 3600

# 日志配置
logging:
  level:
    "com.klzw.common.map": "info"
```

## 3. 配置项说明

### 3.1 高德地图服务配置

| 配置项 | 类型 | 默认值 | 必填 | 说明 |
| ------ | ---- | ------ | ---- | ---- |
| `mineguard.map.gaode.enabled` | boolean | `true` | 否 | 是否启用高德地图服务 |
| `mineguard.map.gaode.api-key` | string | - | 是 | 高德地图API密钥，需在高德开放平台申请 |
| `mineguard.map.gaode.api-url` | string | `"https://restapi.amap.com/v3"` | 否 | 高德地图API地址 |
| `mineguard.map.gaode.connect-timeout` | int | `5000` | 否 | HTTP连接超时时间（毫秒） |
| `mineguard.map.gaode.read-timeout` | int | `10000` | 否 | HTTP读取超时时间（毫秒） |
| `mineguard.map.gaode.cache-expire` | long | `3600` | 否 | 地理围栏缓存过期时间（秒） |

### 3.2 环境变量配置

推荐使用环境变量配置敏感信息：

```yaml
mineguard:
  map:
    gaode:
      api-key: "${GAODE_API_KEY:}"
```

### 3.3 配置验证

配置验证规则：

- `api-key` 必须配置，否则服务无法正常工作
- `connect-timeout` 和 `read-timeout` 必须大于0
- `cache-expire` 必须大于0

## 4. 功能说明

### 4.1 地理编码服务

- **功能**：地址与坐标相互转换
- **配置影响**：`api-key`、`api-url`、超时配置
- **使用场景**：地址解析、坐标定位

### 4.2 路线规划服务

- **功能**：驾车、步行、骑行路线规划
- **配置影响**：`api-key`、`api-url`、超时配置
- **使用场景**：导航、路径规划

### 4.3 地理围栏服务

- **功能**：创建和管理地理围栏，检测点是否在围栏内
- **配置影响**：`cache-expire` 影响围栏缓存时间
- **使用场景**：区域监控、电子围栏

### 4.4 兴趣点搜索

- **功能**：搜索周边兴趣点（POI）
- **配置影响**：`api-key`、`api-url`、超时配置
- **使用场景**：周边搜索、位置服务

## 5. 错误处理

### 5.1 配置错误

- **错误类型**：配置缺失或无效
- **处理方式**：启动时检查配置，抛出配置异常
- **建议**：使用环境变量配置敏感信息

### 5.2 API调用错误

- **错误类型**：网络超时、API限流、认证失败
- **处理方式**：重试机制、异常捕获、降级处理
- **建议**：合理设置超时时间，监控API调用状态

## 6. 性能优化建议

### 6.1 超时配置

- **连接超时**：建议设置为3-5秒
- **读取超时**：建议设置为10-15秒
- **考虑因素**：网络状况、API响应时间

### 6.2 缓存配置

- **缓存时间**：根据业务需求调整
- **缓存策略**：本地缓存 + Redis缓存
- **优化建议**：高频数据适当延长缓存时间

### 6.3 线程池配置

- **核心线程数**：根据并发量调整
- **队列容量**：避免内存溢出
- **监控建议**：监控线程池使用情况

## 7. 安全注意事项

### 7.1 API密钥安全

- **存储安全**：使用环境变量或配置中心
- **访问控制**：限制API密钥使用范围
- **定期更新**：定期更换API密钥

### 7.2 网络传输安全

- **HTTPS**：确保API调用使用HTTPS
- **数据加密**：敏感数据加密传输
- **访问日志**：记录API调用日志

## 8. 部署建议

### 8.1 开发环境

```yaml
mineguard:
  map:
    gaode:
      enabled: true
      api-key: "${GAODE_API_KEY:}"
      connect-timeout: 5000
      read-timeout: 10000
```

### 8.2 测试环境

```yaml
mineguard:
  map:
    gaode:
      enabled: true
      api-key: "${GAODE_API_KEY:}"
      connect-timeout: 3000
      read-timeout: 8000
```

### 8.3 生产环境

```yaml
mineguard:
  map:
    gaode:
      enabled: true
      api-key: "${GAODE_API_KEY:}"
      connect-timeout: 3000
      read-timeout: 5000
      cache-expire: 1800
```

## 9. 变更记录

| 日期 | 变更内容 |
| ---- | ------- |
| 2026-03-12 | 创建common-map模块YAML配置文档 |
| 2026-03-12 | 添加完整配置示例和最小配置示例 |
| 2026-03-12 | 添加配置项说明和功能说明 |
| 2026-03-12 | 添加错误处理、性能优化、安全注意事项 |
| 2026-03-12 | 添加部署建议和变更记录 |
