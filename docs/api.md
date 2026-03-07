# MineGuard API 文档

> 本文档用于前后端联调沟通，记录所有 API 接口定义。

## 1. 基础信息

### 1.1 服务地址

| 环境 | Gateway 地址 | 说明 |
| - | - | - |
| 开发环境 | <http://localhost:8080> | 本地开发 |
| 测试环境 | <http://192.168.110.128:8080> | 测试服务器 |
| 生产环境 | <https://api.mineguard.com> | 生产环境 |

### 1.2 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "traceId": "abc123def456"
}
```

| 字段 | 类型 | 说明 |
| - | - | - |
| code | int | 状态码，200表示成功 |
| message | string | 响应消息 |
| data | object | 响应数据 |
| traceId | string | 链路追踪ID |

### 1.3 通用状态码

| 状态码 | 说明 |
| - | - |
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未授权/Token无效 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
| 503 | 服务不可用（熔断） |

### 1.4 认证方式

- 使用 JWT Token 认证
- Token 放在请求头 `Authorization: Bearer {token}`
- 登录接口返回 `accessToken` 和 `refreshToken`

### 1.5 分页请求格式

**请求参数：**

| 参数 | 类型 | 默认值 | 说明 |
| - | - | - | - |
| pageNum | int | 1 | 页码 |
| pageSize | int | 10 | 每页条数 |

**响应格式：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 100,
    "pages": 10,
    "pageNum": 1,
    "pageSize": 10,
    "records": []
  }
}
```

---

## 2. Gateway 网关服务

### 2.1 网关白名单接口（无需认证）

#### 2.1.1 健康检查

``` http
GET /actuator/health
```

**响应示例：**

```json
{
  "status": "UP"
}
```

#### 2.1.2 API 文档

``` http
GET /swagger-ui.html
GET /webjars/swagger-ui/index.html
```

**说明：** 访问聚合的 Swagger UI，可查看所有服务的 API 文档。

#### 2.1.3 各服务 API 文档

| 服务 | 文档地址 |
| - | - |
| 用户服务 | /api/user/v3/api-docs |
| 车辆服务 | /api/vehicle/v3/api-docs |
| 行程服务 | /api/trip/v3/api-docs |
| 预警服务 | /api/warning/v3/api-docs |
| 统计服务 | /api/statistics/v3/api-docs |
| 费用服务 | /api/cost/v3/api-docs |
| AI服务 | /api/ai/v3/api-docs |

### 2.2 网关错误响应

#### 2.2.1 Token 缺失

``` http
HTTP/1.1 401 Unauthorized
```

```json
{
  "code": 401,
  "message": "Token缺失",
  "data": null,
  "traceId": "xxx"
}
```

#### 2.2.2 Token 无效/过期

``` http
HTTP/1.1 401 Unauthorized
```

```json
{
  "code": 401,
  "message": "Token无效或已过期",
  "data": null,
  "traceId": "xxx"
}
```

#### 2.2.3 服务不可用（熔断）

``` http
HTTP/1.1 503 Service Unavailable
```

```json
{
  "code": 503,
  "message": "服务暂时不可用，请稍后重试",
  "data": null,
  "traceId": "xxx"
}
```

### 2.3 网关路由规则

| 路径前缀 | 目标服务 | 说明 |
| - | - | - |
| /api/auth/** | user-service | 认证相关 |
| /api/user/** | user-service | 用户管理 |
| /api/role/** | user-service | 角色管理 |
| /api/vehicle/** | vehicle-service | 车辆管理 |
| /api/driver/** | vehicle-service | 司机管理 |
| /api/trip/** | trip-service | 行程管理 |
| /api/warning/** | warning-service | 预警管理 |
| /api/statistics/** | statistics-service | 统计分析 |
| /api/cost/** | cost-service | 费用管理 |
| /api/ai/** | ai-service | AI服务 |

### 2.4 请求头传递

网关认证通过后，会向下游服务传递以下请求头：

| 请求头 | 说明 | 示例 |
| - | - | - |
| X-User-Id | 用户ID | 123 |
| X-Username | 用户名 | admin |
| X-User-Roles | 角色列表（逗号分隔） | ROLE_ADMIN,ROLE_USER |

---

## 3. User Service 用户服务

> ⚠️ **注意：以下为预期 API，实际实现时可能调整**

### 3.1 认证接口（/api/auth）

#### 3.1.1 用户登录

``` http
POST /api/auth/login
Content-Type: application/json
```

**请求体：**

```json
{
  "username": "admin",
  "password": "123456",
  "captchaKey": "xxx",
  "captchaCode": "1234"
}
```

| 字段 | 类型 | 必填 | 说明 |
| - | - | - | - |
| username | string | 是 | 用户名 |
| password | string | 是 | 密码 |
| captchaKey | string | 否 | 验证码Key |
| captchaCode | string | 否 | 验证码 |

**响应示例：**

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "user": {
      "id": "uuid-xxx",
      "username": "admin",
      "realName": "管理员",
      "email": "admin@example.com",
      "phone": "13800138000",
      "status": 1,
      "userType": 2,
      "roles": ["ROLE_ADMIN"],
      "createTime": "2024-01-01 00:00:00"
    }
  }
}
```

#### 3.1.2 用户注册

``` http
POST /api/user/register
Content-Type: application/json
```

**请求体：**

```json
{
  "username": "newuser",
  "password": "123456",
  "realName": "张三",
  "email": "user@example.com",
  "phone": "13800138001"
}
```

| 字段 | 类型 | 必填 | 说明 |
| - | - | - | - |
| username | string | 是 | 用户名（4-20位字母或数字） |
| password | string | 是 | 密码（至少6位） |
| realName | string | 是 | 真实姓名 |
| email | string | 否 | 邮箱 |
| phone | string | 否 | 手机号 |

**响应示例：**

```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "id": "uuid-xxx",
    "username": "newuser",
    "realName": "张三",
    "email": "user@example.com",
    "phone": "13800138001",
    "status": 1,
    "userType": 1,
    "createTime": "2024-01-01 00:00:00"
  }
}
```

#### 3.1.3 用户登出

``` http
POST /api/auth/logout
Authorization: Bearer {token}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "登出成功",
  "data": null
}
```

#### 3.1.4 刷新Token

``` http
POST /api/auth/refresh
Content-Type: application/json
```

**请求体：**

```json
{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "刷新成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200
  }
}
```

#### 3.1.5 获取验证码

``` http
GET /api/auth/captcha
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "captchaKey": "uuid-xxx",
    "captchaImage": "data:image/png;base64,iVBORw0KGgo..."
  }
}
```

### 3.2 用户管理接口（/api/user）

#### 3.2.1 获取当前用户信息

``` http
GET /api/user/current
Authorization: Bearer {token}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "uuid-xxx",
    "username": "admin",
    "realName": "管理员",
    "email": "admin@example.com",
    "phone": "13800138000",
    "avatar": null,
    "status": 1,
    "userType": 2,
    "roles": ["ROLE_ADMIN"],
    "createTime": "2024-01-01 00:00:00",
    "updateTime": "2024-01-01 00:00:00"
  }
}
```

#### 3.2.2 更新用户信息

``` http
PUT /api/user/{id}
Authorization: Bearer {token}
Content-Type: application/json
```

**路径参数：**

| 参数 | 类型 | 说明 |
| - | - | - |
| id | string | 用户ID |

**请求体：**

```json
{
  "realName": "新名字",
  "email": "newemail@example.com",
  "phone": "13900139000",
  "avatar": "https://example.com/avatar.jpg"
}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

#### 3.2.3 修改密码

``` http
PUT /api/user/password
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体：**

```json
{
  "oldPassword": "123456",
  "newPassword": "654321",
  "confirmPassword": "654321"
}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": null
}
```

#### 3.2.4 分页查询用户（管理员）

``` http
GET /api/user/page?pageNum=1&pageSize=10&username=&status=
Authorization: Bearer {token}
```

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
| - | - | - | - |
| pageNum | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页条数，默认10 |
| username | string | 否 | 用户名（模糊查询） |
| status | int | 否 | 状态：0-禁用，1-启用 |

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": "uuid-xxx",
        "username": "admin",
        "realName": "管理员",
        "email": "admin@example.com",
        "phone": "13800138000",
        "avatar": null,
        "status": 1,
        "userType": 2,
        "roles": ["ROLE_ADMIN"],
        "createTime": "2024-01-01 00:00:00"
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

#### 3.2.5 获取用户详情（管理员）

``` http
GET /api/user/{id}
Authorization: Bearer {token}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "uuid-xxx",
    "username": "admin",
    "realName": "管理员",
    "email": "admin@example.com",
    "phone": "13800138000",
    "avatar": null,
    "status": 1,
    "userType": 2,
    "roles": ["ROLE_ADMIN"],
    "createTime": "2024-01-01 00:00:00",
    "updateTime": "2024-01-01 00:00:00"
  }
}
```

#### 3.2.6 禁用用户（管理员）

``` http  
PUT /api/user/{id}/disable
Authorization: Bearer {token}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "禁用成功",
  "data": null
}
```

#### 3.2.7 启用用户（管理员）

``` http
PUT /api/user/{id}/enable
Authorization: Bearer {token}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "启用成功",
  "data": null
}
```

### 3.3 角色管理接口（/api/role）

#### 3.3.1 获取角色列表

``` http
GET /api/role/list
Authorization: Bearer {token}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": "uuid-xxx",
      "roleName": "管理员",
      "roleCode": "ROLE_ADMIN",
      "description": "系统管理员",
      "createTime": "2024-01-01 00:00:00"
    },
    {
      "id": "uuid-yyy",
      "roleName": "司机",
      "roleCode": "ROLE_DRIVER",
      "description": "司机角色",
      "createTime": "2024-01-01 00:00:00"
    }
  ]
}
```

#### 3.3.2 为用户分配角色（管理员）

``` http
POST /api/user/{id}/roles
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体：**

```json
{
  "roleIds": [1, 2]
}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "分配成功",
  "data": null
}
```

#### 3.3.3 获取用户角色（管理员）

``` http
GET /api/user/{id}/roles
Authorization: Bearer {token}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": ["ROLE_ADMIN", "ROLE_USER"]
}
```

> **说明：** 返回用户的角色编码列表，而非角色详情对象

---

## 4. Vehicle Service 车辆服务

> 待实现后补充

---

## 5. Trip Service 行程服务

> 待实现后补充

---

## 6. Warning Service 预警服务

> 待实现后补充

---

## 7. Statistics Service 统计服务

> 待实现后补充

---

## 8. Cost Service 费用服务

> 待实现后补充

---

## 9. AI Service AI服务

> 待实现后补充

---

## 附录

### A. 错误码对照表

| 错误码范围 | 模块 | 说明 |
| - | - | - |
| 100-199 | 系统 | 系统级错误 |
| 200-299 | 系统 | 系统级错误 |
| 300-399 | 业务 | 通用业务错误 |
| 800-899 | auth | 认证授权错误 |
| 900-999 | redis | Redis错误 |
| 1000-1099 | database | 数据库错误 |
| 1100-1199 | web | Web错误 |
| 1200-1299 | file | 文件错误 |

### B. 角色枚举

| 角色编码 | 角色名称 | 说明 |
| - | - | - |
| ROLE_ADMIN | 管理员 | 系统管理员，拥有所有权限 |
| ROLE_MANAGER | 普通管理员 | 普通管理员，部分管理权限 |
| ROLE_DRIVER | 司机 | 司机角色 |
| ROLE_SAFETY | 安全员 | 安全员角色 |
| ROLE_REPAIR | 维修员 | 维修员角色 |

### C. 权限枚举

| 权限编码 | 权限名称 | 说明 |
| - | - | - |
| user:read | 用户查询 | 查看用户信息 |
| user:write | 用户管理 | 用户增删改 |
| vehicle:read | 车辆查询 | 查看车辆信息 |
| vehicle:write | 车辆管理 | 车辆增删改 |
| trip:read | 行程查询 | 查看行程信息 |
| trip:write | 行程管理 | 行程增删改 |
| warning:read | 预警查询 | 查看预警信息 |
| warning:write | 预警管理 | 预警处理 |
| system:read | 系统查询 | 查看系统配置 |
| system:write | 系统管理 | 系统配置管理 |

### D. 更新日志

| 日期 | 版本 | 更新内容 |
| - | - | - |
| 2026-03-08 | v1.1 | 更新 User Service API：注册接口移至 /api/user/register，用户ID改为UUID字符串，响应格式调整 |
| 2024-01-01 | v1.0 | 初始版本，定义Gateway和User Service API |
