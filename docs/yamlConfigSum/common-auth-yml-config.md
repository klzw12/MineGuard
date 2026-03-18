# common-auth模块 YML 配置示例

## 1. 完整配置示例

```yaml
# 认证模块配置
mineguard:
  auth:
    # 是否启用认证模块，默认值：true
    enabled: true
    jwt:
      # 是否启用JWT功能，默认值：true
      enabled: true
      # JWT签名密钥（生产环境必须配置，建议使用环境变量）
      # 注意：密钥长度建议至少32字符，以支持HS256算法
      secret: ${JWT_SECRET:yourSecretKeyMustBeAtLeast32CharactersLongForHS256Algorithm1234567890}
      # Token过期时间（毫秒），默认值：86400000（24小时）
      expiration: 86400000
      # 请求头名称，默认值：Authorization
      header: Authorization
      # Token前缀，默认值：Bearer 
      prefix: "Bearer "
      # 是否启用Token黑名单，默认值：true
      enable-blacklist: true
      # 黑名单Key前缀，默认值：jwt:blacklist:
      blacklist-prefix: "jwt:blacklist:"
      # 黑名单过期时间（秒），默认值：86400（24小时）
      blacklist-expire: 86400
      # 是否启用Token刷新，默认值：true
      enable-refresh: true
      # 刷新Token过期时间（毫秒），默认值：604800000（7天）
      refresh-expiration: 604800000

# Redis配置（JWT黑名单功能依赖Redis）
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: 0
      timeout: 10000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0

# Spring Security配置
spring:
  security:
    user:
      name: user
      password: user

# 日志配置
logging:
  level:
    com.klzw.common.auth: DEBUG
    org.springframework.security: INFO
```

## 2. 最小配置示例

```yaml
# 认证模块最小配置（生产环境必须配置secret）
mineguard:
  auth:
    enabled: true
    jwt:
      # 生产环境必须使用环境变量配置密钥
      secret: ${JWT_SECRET}

# Redis配置（JWT黑名单功能依赖）
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
```

## 3. 配置项说明

### 3.1 核心配置项

| 配置项 | 类型 | 默认值 | 必填 | 说明 |
| ---- | ------- | --------- | -- | ------ |
| `mineguard.auth.enabled` | boolean | true | 否 | 是否启用认证模块 |
| `mineguard.auth.jwt.enabled` | boolean | true | 否 | 是否启用JWT功能 |
| `mineguard.auth.jwt.secret` | String | 无（硬编码默认值） | **是** | JWT签名密钥，生产环境必须配置 |
| `mineguard.auth.jwt.expiration` | Long | 86400000 | 否 | Token过期时间（毫秒） |
| `mineguard.auth.jwt.header` | String | Authorization | 否 | 请求头名称 |
| `mineguard.auth.jwt.prefix` | String | "Bearer " | 否 | Token前缀 |

### 3.2 黑名单配置项

| 配置项 | 类型 | 默认值 | 必填 | 说明 |
| ----------------- | ------- | --------- | -- | ------------ |
| `mineguard.auth.jwt.enable-blacklist` | Boolean | true | 否 | 是否启用Token黑名单 |
| `mineguard.auth.jwt.blacklist-prefix` | String | "jwt:blacklist:" | 否 | 黑名单Key前缀 |
| `mineguard.auth.jwt.blacklist-expire` | Long | 86400 | 否 | 黑名单过期时间（秒） |

### 3.3 刷新Token配置项

| 配置项 | 类型 | 默认值 | 必填 | 说明 |
| ---- | ------- | --------- | -- | ------ |
| `mineguard.auth.jwt.enable-refresh` | Boolean | true | 否 | 是否启用Token刷新 |
| `mineguard.auth.jwt.refresh-expiration` | Long | 604800000 | 否 | 刷新Token过期时间（毫秒） |

## 4. 安全建议

### 4.1 密钥配置

- ⚠️ 重要：生产环境必须使用环境变量配置JWT密钥

```yaml
# 推荐方式：使用环境变量
mineguard:
  auth:
    jwt:
      secret: ${JWT_SECRET}
```

### 4.2 密钥要求

- 密钥长度：至少32个字符（支持HS256算法）
- 密钥复杂度：建议使用随机生成的强密码
- 密钥存储：使用环境变量或密钥管理服务，不要硬编码

### 4.3 生成密钥示例

```bash
# 使用OpenSSL生成随机密钥
openssl rand -base64 64

# 或使用Java代码生成
java -e 'import java.util.UUID; System.out.println(UUID.randomUUID().toString().replace("-","") + UUID.randomUUID().toString().replace("-",""));'
```

## 5. 环境变量配置

### 5.1 Docker环境

```yaml
# docker-compose.yml
services:
  app:
    environment:
      - JWT_SECRET=yourSecretKeyMustBeAtLeast32CharactersLongForHS256Algorithm1234567890
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - REDIS_PASSWORD=yourpassword
```

### 5.2 Kubernetes环境

```yaml
# configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: auth-config
data:
  JWT_SECRET: "yourSecretKeyMustBeAtLeast32CharactersLongForHS256Algorithm1234567890"
---
# secret.yaml（推荐使用Secret存储敏感信息）
apiVersion: v1
kind: Secret
metadata:
  name: auth-secret
type: Opaque
stringData:
  JWT_SECRET: "yourSecretKeyMustBeAtLeast32CharactersLongForHS256Algorithm1234567890"
  REDIS_PASSWORD: "yourpassword"
```

### 5.3 .env文件

```properties
# .env文件（不要提交到版本控制）
JWT_SECRET=yourSecretKeyMustBeAtLeast32CharactersLongForHS256Algorithm1234567890
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=yourpassword
```
