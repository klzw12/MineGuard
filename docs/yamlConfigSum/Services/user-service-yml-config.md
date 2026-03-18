# user-service 模块 YML 配置示例

## 说明

user-service 模块的配置主要包含：

- 管理员初始化配置
- 考勤配置
- 阿里云短信配置

其他配置（数据库、Redis、JWT等）通过 Nacos 配置中心获取。

---

## 本地配置（application.yml）

### 最小配置

```yaml
server:
  port: 8081

spring:
  application:
    name: user-service
```

### 完整配置

```yaml
server:
  port: 8081

spring:
  application:
    name: user-service

# 管理员初始化配置
mineguard:
  user:
    initadmin: true                    # 是否开启管理员初始化
    admin-username: admin               # 管理员用户名
    admin-password: adminpass          # 管理员密码
    admin-real-name: 王敏               # 管理员真实姓名
    admin-phone: 17612243308           # 管理员手机号
    admin-email: admin@mineguard.com   # 管理员邮箱

# 考勤配置
  attendance:
    work-start-time: "09:00"            # 上班时间（格式：HH:mm）
    work-end-time: "18:00"              # 下班时间（格式：HH:mm）
    late-threshold: 15                  # 迟到阈值（分钟）
    early-leave-threshold: 15           # 早退阈值（分钟）

# 日志配置
logging:
  level:
    com.klzw.service.user: info
```

---

## Nacos 配置中心配置

### 公共配置（common-*.yml）

通过 Nacos 配置中心引入的公共配置：

| 配置文件 | 说明 |
| ------ | ---- |
| common-core.yml | 核心配置（服务发现、Nacos等） |
| common-auth.yml | 认证配置（JWT、Redis等） |
| common-redis.yml | Redis 配置 |
| common-database.yml | 数据库配置 |
| common-web.yml | Web 配置 |
| common-file.yml | 文件存储配置 |

### 服务专属配置（user-service.yml）

```yaml
mineguard:
  user:
    initadmin: true                    # 是否开启管理员初始化
    admin-username: admin               # 管理员用户名
    admin-password: adminpass          # 管理员密码（生产环境使用环境变量）
    admin-real-name: 王敏               # 管理员真实姓名
    admin-phone: 17612243308           # 管理员手机号
    admin-email: admin@mineguard.com   # 管理员邮箱

# 考勤配置
  attendance:
    work-start-time: "09:00"            # 上班时间（格式：HH:mm）
    work-end-time: "18:00"              # 下班时间（格式：HH:mm）
    late-threshold: 15                  # 迟到阈值（分钟）
    early-leave-threshold: 15           # 早退阈值（分钟）

# 阿里云短信配置
  sms:
    aliyun:
      enabled: false                    # 是否启用阿里云短信（无密钥时设为false）
      access-key-id: "${ALIYUN_ACCESS_KEY_ID}"     # 阿里云AccessKeyId
      access-key-secret: "${ALIYUN_ACCESS_KEY_SECRET}"  # 阿里云AccessKeySecret
      endpoint: "dypnsapi.aliyuncs.com"  # 短信服务端点
      sign-name: "速通互联验证码"         # 短信签名
```

---

## 配置项说明

### 管理员初始化配置（mineguard.user）

| 配置项 | 类型 | 默认值 | 说明 |
| ----- | ---- | ------ | ---- |
| initadmin | boolean | false | 是否开启管理员初始化脚本 |
| admin-username | string | admin | 管理员用户名 |
| admin-password | string | admin123 | 管理员密码（生产环境应使用加密或环境变量） |
| admin-real-name | string | 系统管理员 | 管理员真实姓名 |
| admin-phone | string | 13800138000 | 管理员手机号 |
| admin-email | string | <admin@mineguard.com> | 管理员邮箱 |

### 考勤配置（mineguard.attendance）

| 配置项 | 类型 | 默认值 | 说明 |
| ----- | ---- | ------ | ---- |
| work-start-time | string | "09:00" | 上班时间，格式HH:mm |
| work-end-time | string | "18:00" | 下班时间，格式HH:mm |
| late-threshold | int | 15 | 迟到阈值（分钟），超过此时间算迟到 |
| early-leave-threshold | int | 15 | 早退阈值（分钟），提前此时间算早退 |

### 阿里云短信配置（mineguard.sms.aliyun）

| 配置项 | 类型 | 默认值 | 说明 |
| ----- | ---- | ------ | ---- |
| enabled | boolean | false | 是否启用阿里云短信服务 |
| access-key-id | string | - | 阿里云AccessKeyId（无密钥时保持enabled:false） |
| access-key-secret | string | - | 阿里云AccessKeySecret |
| endpoint | string | dypnsapi.aliyuncs.com | 短信服务端点 |
| sign-name | string | - | 短信签名 |

**无阿里云密钥解决方案：**

1. **方案一：禁用短信服务**（推荐开发测试使用）

   ```yaml
   mineguard:
     sms:
       aliyun:
         enabled: false
   ```

   设置为 false 后，短信验证码功能会被 Mock 实现替代，验证码会直接返回给前端。

2. **方案二：配置环境变量**（生产环境）

   ```yaml
   mineguard:
     sms:
       aliyun:
         enabled: true
         access-key-id: "${ALIYUN_ACCESS_KEY_ID}"
         access-key-secret: "${ALIYUN_ACCESS_KEY_SECRET}"
   ```

   在环境变量或 K8s Secret 中配置真实的 AccessKey。

---

## 环境变量配置示例

生产环境建议使用环境变量配置敏感信息：

```yaml
mineguard:
  user:
    initadmin: ${INIT_ADMIN:false}
    admin-username: ${ADMIN_USERNAME:admin}
    admin-password: ${ADMIN_PASSWORD:admin123}
    admin-real-name: ${ADMIN_REAL_NAME:系统管理员}
    admin-phone: ${ADMIN_PHONE:13800138000}
    admin-email: ${ADMIN_EMAIL:admin@mineguard.com}
  attendance:
    work-start-time: ${WORK_START_TIME:09:00}
    work-end-time: ${WORK_END_TIME:18:00}
    late-threshold: ${LATE_THRESHOLD:15}
    early-leave-threshold: ${EARLY_LEAVE_THRESHOLD:15}
  sms:
    aliyun:
      enabled: ${SMS_ENABLED:false}
      access-key-id: ${ALIYUN_ACCESS_KEY_ID:}
      access-key-secret: ${ALIYUN_ACCESS_KEY_SECRET:}
```

---

## Nacos 服务注册说明

user-service 启动后会自动注册到 Nacos，使用负载均衡调用时：

- **服务名**: user-service
- **端口**: 8081
- **协议**: HTTP

Gateway 通过 `lb://user-service` 路由到该服务。
