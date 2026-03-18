# MineGuard API 文档

> 本文档用于前后端联调沟通，记录所有 API 接口定义。

## 1. 基础信息

### 1.1 服务地址

| 环境 | Gateway 地址 | 说明 |
| - | - | - |
| 开发环境 | <http://localhost:8081> | 本地开发 |
| 测试环境 | <http://192.168.110.128:8081> | 测试服务器 |
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

#### 3.1.5 获取图形验证码

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

#### 3.1.6 发送短信验证码

``` http
POST /api/auth/sms/send
Content-Type: application/json
```

**请求体：**

```json
{
  "phone": "13800138000"
}
```

| 字段 | 类型 | 必填 | 说明 |
| - | - | - | - |
| phone | string | 是 | 手机号 |

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "success": true,
    "message": "验证码发送成功",
    "remainingSeconds": null
  }
}
```

#### 3.1.7 验证短信验证码

``` http
POST /api/auth/sms/verify
Content-Type: application/json
```

**请求体：**

```json
{
  "phone": "13800138000",
  "code": "1234"
}
```

| 字段 | 类型 | 必填 | 说明 |
| - | - | - | - |
| phone | string | 是 | 手机号 |
| code | string | 是 | 验证码（4位数字） |

**响应示例：**

```json
{
  "code": 200,
  "message": "验证成功",
  "data": true
}
```

#### 3.1.8 通过手机号重置密码

``` http
POST /api/auth/reset-password
Content-Type: application/json
```

**请求体：**

```json
{
  "phone": "13800138000",
  "code": "1234",
  "newPassword": "NewPass123"
}
```

| 字段 | 类型 | 必填 | 说明 |
| - | - | - | - |
| phone | string | 是 | 手机号 |
| code | string | 是 | 验证码（4位数字） |
| newPassword | string | 是 | 新密码（至少6位，包含大小写字母和数字） |

**响应示例：**

```json
{
  "code": 200,
  "message": "密码重置成功",
  "data": null
}
```

#### 3.1.9 管理员认证

> 管理员首次登录时，如果状态为禁用，需要通过此接口进行身份认证后才能启用账户

``` http
POST /api/auth/admin/verify
Content-Type: application/json
```

**请求体：**

```json
{
  "userId": 1,
  "realName": "张三",
  "phone": "13800138000",
  "idCardFrontBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAA..."
}
```

| 字段 | 类型 | 必填 | 说明 |
| - | - | - | - |
| userId | long | 是 | 用户ID |
| realName | string | 否 | 真实姓名（用于与身份证比对） |
| phone | string | 否 | 手机号 |
| idCardFrontBase64 | string | 是 | 身份证正面图片（Base64编码，支持data:image/xxx;base64,前缀） |

**响应示例：**

```json
{
  "code": 200,
  "message": "管理员认证成功",
  "data": {
    "id": "1",
    "username": "admin",
    "realName": "张三",
    "phone": "13800138000",
    "email": "admin@example.com",
    "avatarUrl": null,
    "status": 1,
    "userType": 1,
    "roleCode": "ADMIN",
    "createTime": "2024-01-01 00:00:00",
    "updateTime": "2024-01-01 00:00:00"
  }
}
```

**前端判断逻辑**：
- 登录返回的UserVO中包含`status`和`userType`/`roleCode`
- 如果`roleCode === "ADMIN"`且`status === 0`（禁用），前端跳转到管理员认证页面
- 用户上传身份证照片进行认证，认证成功后`status`变为`1`（启用）

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

#### 3.2.4 上传用户头像

``` http
POST /api/user/avatar
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
| - | - | - | - |
| file | file | 是 | 头像文件（支持jpg、jpeg、png、gif格式） |

**响应示例：**

```json
{
  "code": 200,
  "message": "头像上传成功",
  "data": {
    "id": "uuid-xxx",
    "username": "admin",
    "realName": "管理员",
    "email": "admin@example.com",
    "phone": "13800138000",
    "avatarUrl": "avatar/12345678-1234-1234-1234-1234567890ab.jpg",
    "status": 1,
    "userType": 2,
    "createTime": "2024-01-01 00:00:00",
    "updateTime": "2024-01-01 00:00:00"
  }
}
```

#### 3.2.5 分页查询用户（管理员）

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
        "avatarUrl": null,
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

#### 3.2.6 获取用户详情（管理员）

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
    "avatarUrl": null,
    "status": 1,
    "userType": 2,
    "roles": ["ROLE_ADMIN"],
    "createTime": "2024-01-01 00:00:00",
    "updateTime": "2024-01-01 00:00:00"
  }
}
```

#### 3.2.7 禁用用户（管理员）

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

#### 3.2.8 启用用户（管理员）

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

### 3.4 资格认证接口（/api/qualification）

> **认证流程**：先完成身份证验证（实名认证），再上传资格证书
> 
> **前置条件**：用户必须先完成身份证验证才能上传资格证书

#### 3.4.1 身份证验证（实名认证）

``` http
POST /api/qualification/idcard/verify
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体：**

```json
{
  "userId": 1,
  "realName": "张三",
  "idCard": "110101199001011234",
  "idCardFrontBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAA...",
  "idCardBackBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAA..."
}
```

| 字段 | 类型 | 必填 | 说明 |
| - | - | - | - |
| userId | long | 是 | 用户ID |
| realName | string | 是 | 真实姓名 |
| idCard | string | 是 | 身份证号（18位） |
| idCardFrontBase64 | string | 是 | 身份证正面图片（Base64编码） |
| idCardBackBase64 | string | 否 | 身份证背面图片（Base64编码） |

**响应示例：**

```json
{
  "code": 200,
  "message": "身份证验证成功",
  "data": true
}
```

#### 3.4.2 检查身份证验证状态

``` http
GET /api/qualification/idcard/check/{userId}
Authorization: Bearer {token}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

#### 3.4.3 上传驾驶证（司机资格认证）

> **前置条件**：用户必须已完成身份证验证（实名认证）

``` http
POST /api/qualification/cert/driver
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体：**

```json
{
  "userId": 1,
  "certNumber": "JZ123456789",
  "drivingLicenseBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAA..."
}
```

| 字段 | 类型 | 必填 | 说明 |
| - | - | - | - |
| userId | long | 是 | 用户ID |
| certNumber | string | 否 | 证书编号（可选，OCR识别失败时使用） |
| drivingLicenseBase64 | string | 是 | 驾驶证图片（Base64编码） |

**响应示例：**

```json
{
  "code": 200,
  "message": "驾驶证上传成功",
  "data": true
}
```

#### 3.4.4 上传应急救援证（安全员资格认证）

> **前置条件**：用户必须已完成身份证验证（实名认证）

``` http
POST /api/qualification/cert/safety-officer
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体：**

```json
{
  "userId": 1,
  "certNumber": "AQ123456789",
  "emergencyCertBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAA..."
}
```

| 字段 | 类型 | 必填 | 说明 |
| - | - | - | - |
| userId | long | 是 | 用户ID |
| certNumber | string | 否 | 证书编号（可选，OCR识别失败时使用） |
| emergencyCertBase64 | string | 是 | 应急救援证图片（Base64编码） |

**响应示例：**

```json
{
  "code": 200,
  "message": "应急救援证上传成功",
  "data": true
}
```

#### 3.4.5 上传维修资格证（维修员资格认证）

> **前置条件**：用户必须已完成身份证验证（实名认证）

``` http
POST /api/qualification/cert/repairman
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体：**

```json
{
  "userId": 1,
  "certNumber": "WX123456789",
  "repairCertBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAA..."
}
```

| 字段 | 类型 | 必填 | 说明 |
| - | - | - | - |
| userId | long | 是 | 用户ID |
| certNumber | string | 否 | 证书编号（可选，OCR识别失败时使用） |
| repairCertBase64 | string | 是 | 维修资格证图片（Base64编码） |

**响应示例：**

```json
{
  "code": 200,
  "message": "维修资格证上传成功",
  "data": true
}
```

### 3.5 管理员接口（/api/user/admin）

#### 3.5.1 分页查询用户（管理员）

``` http
GET /api/user/admin/page?pageNum=1&pageSize=10&username=&status=
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

#### 3.5.2 获取用户详情（管理员）

``` http
GET /api/user/admin/user/{id}
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

#### 3.5.3 禁用用户（管理员）

``` http  
PUT /api/user/admin/user/{id}/disable
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

#### 3.5.4 启用用户（管理员）

``` http
PUT /api/user/admin/user/{id}/enable
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

#### 3.5.5 变更用户角色（管理员专用）

``` http
PUT /api/user/admin/user/{id}/role-change
Authorization: Bearer {token}
```

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
| - | - | - | - |
| roleId | string | 是 | 新角色ID |
| reason | string | 否 | 变更原因 |

**响应示例：**

```json
{
  "code": 200,
  "message": "角色变更成功",
  "data": null
}
```

#### 3.5.6 获取待处理的角色变更申请

``` http
GET /api/user/admin/role-change/apply/pending
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
      "userId": "uuid-yyy",
      "username": "user1",
      "currentRoleId": "uuid-role1",
      "currentRoleCode": "ROLE_DRIVER",
      "currentRoleName": "司机",
      "applyRoleId": "uuid-role2",
      "applyRoleCode": "ROLE_SAFETY_OFFICER",
      "applyRoleName": "安全员",
      "applyReason": "用户尝试认证新角色，需要管理员审核",
      "status": 1,
      "statusLabel": "待处理",
      "createTime": "2024-01-01 00:00:00"
    }
  ]
}
```

#### 3.5.7 获取所有角色变更申请

``` http
GET /api/user/admin/role-change/apply/list
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
      "userId": "uuid-yyy",
      "username": "user1",
      "currentRoleId": "uuid-role1",
      "currentRoleCode": "ROLE_DRIVER",
      "currentRoleName": "司机",
      "applyRoleId": "uuid-role2",
      "applyRoleCode": "ROLE_SAFETY_OFFICER",
      "applyRoleName": "安全员",
      "applyReason": "用户尝试认证新角色，需要管理员审核",
      "status": 2,
      "statusLabel": "已通过",
      "adminOpinion": "同意变更",
      "handleTime": "2024-01-01 01:00:00",
      "handlerId": "uuid-admin",
      "handlerName": "管理员",
      "createTime": "2024-01-01 00:00:00"
    }
  ]
}
```

#### 3.5.8 获取用户的角色变更申请历史

``` http
GET /api/user/admin/role-change/apply/user/{userId}
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
      "userId": "uuid-yyy",
      "username": "user1",
      "currentRoleId": "uuid-role1",
      "currentRoleCode": "ROLE_DRIVER",
      "currentRoleName": "司机",
      "applyRoleId": "uuid-role2",
      "applyRoleCode": "ROLE_SAFETY_OFFICER",
      "applyRoleName": "安全员",
      "applyReason": "用户尝试认证新角色，需要管理员审核",
      "status": 2,
      "statusLabel": "已通过",
      "adminOpinion": "同意变更",
      "handleTime": "2024-01-01 01:00:00",
      "handlerId": "uuid-admin",
      "handlerName": "管理员",
      "createTime": "2024-01-01 00:00:00"
    }
  ]
}
```

#### 3.5.9 处理角色变更申请

``` http
PUT /api/user/admin/role-change/apply/{id}/handle
Authorization: Bearer {token}
```

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
| - | - | - | - |
| status | int | 是 | 处理状态：2-已通过，3-已拒绝 |
| adminOpinion | string | 否 | 管理员处理意见 |
| handlerId | string | 是 | 处理人ID |
| handlerName | string | 是 | 处理人姓名 |

**响应示例：**

```json
{
  "code": 200,
  "message": "处理成功",
  "data": null
}
```

---

## 4. Vehicle Service 车辆服务

### 4.1 车辆管理接口（/api/vehicle）

#### 4.1.1 创建车辆

``` http
POST /api/vehicle
Content-Type: application/json
Authorization: Bearer {token}
```

**请求体：**

```json
{
  "vehicleNo": "京A12345",
  "vehicleType": 1,
  "brand": "大众",
  "model": "帕萨特",
  "userId": "123"
}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "vehicleNo": "京A12345",
    "vehicleType": 1,
    "brand": "大众",
    "model": "帕萨特",
    "userId": "123",
    "status": 0,
    "createTime": "2024-01-01 00:00:00",
    "updateTime": "2024-01-01 00:00:00"
  }
}
```

#### 4.1.2 更新车辆

``` http
PUT /api/vehicle/{id}
Content-Type: application/json
Authorization: Bearer {token}
```

**路径参数：**

| 参数 | 类型 | 说明 |
| - | - | - |
| id | long | 车辆ID |

**请求体：**

```json
{
  "vehicleNo": "京A12345",
  "vehicleType": 1,
  "brand": "大众",
  "model": "帕萨特",
  "userId": "123"
}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "vehicleNo": "京A12345",
    "vehicleType": 1,
    "brand": "大众",
    "model": "帕萨特",
    "userId": "123",
    "status": 0,
    "createTime": "2024-01-01 00:00:00",
    "updateTime": "2024-01-01 00:00:00"
  }
}
```

#### 4.1.3 删除车辆

``` http
DELETE /api/vehicle/{id}
Authorization: Bearer {token}
```

**路径参数：**

| 参数 | 类型 | 说明 |
| - | - | - |
| id | long | 车辆ID |

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

#### 4.1.4 获取车辆详情

``` http
GET /api/vehicle/{id}
Authorization: Bearer {token}
```

**路径参数：**

| 参数 | 类型 | 说明 |
| - | - | - |
| id | long | 车辆ID |

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "1",
    "vehicleNo": "京A12345",
    "vehicleType": 1,
    "brand": "大众",
    "model": "帕萨特",
    "userId": "123",
    "userName": "张三",
    "status": 0,
    "photoUrl": "http://example.com/vehicle.jpg",
    "licenseFrontUrl": "http://example.com/license_front.jpg",
    "licenseBackUrl": "http://example.com/license_back.jpg",
    "owner": "张三",
    "address": "北京市朝阳区",
    "brandModel": "大众牌FV7187FBDBG",
    "vehicleModel": "FV7187FBDBG",
    "engineNumber": "123456",
    "vin": "LSVAF0338C2123456",
    "useNature": "非营运",
    "registerDate": "2020-01-01",
    "issueDate": "2020-01-01",
    "seatingCapacity": 5,
    "totalMass": "1500kg",
    "curbWeight": "1200kg",
    "ratedLoad": "300kg",
    "dimensions": "4870×1834×1480",
    "remarks": "无",
    "inspectionRecord": "2024-01-01",
    "insuranceNo": "1234567890",
    "insuranceCompany": "人保财险",
    "insuranceExpiry": "2025-01-01",
    "createTime": "2024-01-01 00:00:00",
    "updateTime": "2024-01-01 00:00:00"
  }
}
```

#### 4.1.5 分页查询车辆

``` http
GET /api/vehicle/page?page=1&size=10&vehicleNo=&status=
Authorization: Bearer {token}
```

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
| - | - | - | - |
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页条数，默认10 |
| vehicleNo | string | 否 | 车牌号（模糊查询） |
| status | int | 否 | 状态：0-离线，1-在线，2-行驶中，3-故障，4-维修中 |

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": "1",
      "vehicleNo": "京A12345",
      "vehicleType": 1,
      "brand": "大众",
      "model": "帕萨特",
      "userId": "123",
      "userName": "张三",
      "status": 0,
      "photoUrl": "http://example.com/vehicle.jpg",
      "licenseFrontUrl": "http://example.com/license_front.jpg",
      "licenseBackUrl": "http://example.com/license_back.jpg",
      "owner": "张三",
      "address": "北京市朝阳区",
      "brandModel": "大众牌FV7187FBDBG",
      "vehicleModel": "FV7187FBDBG",
      "engineNumber": "123456",
      "vin": "LSVAF0338C2123456",
      "useNature": "非营运",
      "registerDate": "2020-01-01",
      "issueDate": "2020-01-01",
      "seatingCapacity": 5,
      "totalMass": "1500kg",
      "curbWeight": "1200kg",
      "ratedLoad": "300kg",
      "dimensions": "4870×1834×1480",
      "remarks": "无",
      "inspectionRecord": "2024-01-01",
      "insuranceNo": "1234567890",
      "insuranceCompany": "人保财险",
      "insuranceExpiry": "2025-01-01",
      "createTime": "2024-01-01 00:00:00",
      "updateTime": "2024-01-01 00:00:00"
    }
  ]
}
```

#### 4.1.6 绑定用户

``` http
POST /api/vehicle/{id}/bind?userId=123
Authorization: Bearer {token}
```

**路径参数：**

| 参数 | 类型 | 说明 |
| - | - | - |
| id | long | 车辆ID |

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
| - | - | - | - |
| userId | long | 是 | 用户ID |

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

#### 4.1.7 解绑用户

``` http
POST /api/vehicle/{id}/unbind
Authorization: Bearer {token}
```

**路径参数：**

| 参数 | 类型 | 说明 |
| - | - | - |
| id | long | 车辆ID |

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

#### 4.1.8 上传车辆照片

``` http
POST /api/vehicle/{id}/photo
Content-Type: multipart/form-data
Authorization: Bearer {token}
```

**路径参数：**

| 参数 | 类型 | 说明 |
| - | - | - |
| id | long | 车辆ID |

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
| - | - | - | - |
| file | file | 是 | 车辆照片文件 |

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": "http://example.com/vehicle.jpg"
}
```

#### 4.1.9 上传行驶证并进行OCR识别

``` http
POST /api/vehicle/{id}/license
Content-Type: multipart/form-data
Authorization: Bearer {token}
```

**路径参数：**

| 参数 | 类型 | 说明 |
| - | - | - |
| id | long | 车辆ID |

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
| - | - | - | - |
| file | file | 是 | 行驶证照片文件 |

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "vehicleNo": "京A12345",
    "vehicleType": 1,
    "brand": "大众",
    "model": "帕萨特",
    "userId": "123",
    "status": 0,
    "licenseFrontUrl": "http://example.com/license_front.jpg",
    "owner": "张三",
    "address": "北京市朝阳区",
    "brandModel": "大众牌FV7187FBDBG",
    "vehicleModel": "FV7187FBDBG",
    "engineNumber": "123456",
    "vin": "LSVAF0338C2123456",
    "useNature": "非营运",
    "registerDate": "2020-01-01",
    "issueDate": "2020-01-01",
    "createTime": "2024-01-01 00:00:00",
    "updateTime": "2024-01-01 00:00:00"
  }
}
```

#### 4.1.10 上传行驶证正面并进行OCR识别

``` http
POST /api/vehicle/{id}/license/front
Content-Type: multipart/form-data
Authorization: Bearer {token}
```

**路径参数：**

| 参数 | 类型 | 说明 |
| - | - | - |
| id | long | 车辆ID |

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
| - | - | - | - |
| file | file | 是 | 行驶证正面照片文件 |

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "vehicleNo": "京A12345",
    "vehicleType": 1,
    "brand": "大众",
    "model": "帕萨特",
    "userId": "123",
    "status": 0,
    "licenseFrontUrl": "http://example.com/license_front.jpg",
    "owner": "张三",
    "address": "北京市朝阳区",
    "brandModel": "大众牌FV7187FBDBG",
    "vehicleModel": "FV7187FBDBG",
    "engineNumber": "123456",
    "vin": "LSVAF0338C2123456",
    "useNature": "非营运",
    "registerDate": "2020-01-01",
    "issueDate": "2020-01-01",
    "createTime": "2024-01-01 00:00:00",
    "updateTime": "2024-01-01 00:00:00"
  }
}
```

#### 4.1.11 上传行驶证反面

``` http
POST /api/vehicle/{id}/license/back
Content-Type: multipart/form-data
Authorization: Bearer {token}
```

**路径参数：**

| 参数 | 类型 | 说明 |
| - | - | - |
| id | long | 车辆ID |

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
| - | - | - | - |
| file | file | 是 | 行驶证反面照片文件 |

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "vehicleNo": "京A12345",
    "vehicleType": 1,
    "brand": "大众",
    "model": "帕萨特",
    "userId": "123",
    "status": 0,
    "licenseBackUrl": "http://example.com/license_back.jpg",
    "seatingCapacity": 5,
    "totalMass": "1500kg",
    "curbWeight": "1200kg",
    "ratedLoad": "300kg",
    "dimensions": "4870×1834×1480",
    "remarks": "无",
    "inspectionRecord": "2024-01-01",
    "createTime": "2024-01-01 00:00:00",
    "updateTime": "2024-01-01 00:00:00"
  }
}
```

#### 4.1.12 上传车辆保险信息

``` http
POST /api/vehicle/{id}/insurance?insuranceCompany=人保财险&policyNo=1234567890&startDate=2024-01-01&endDate=2025-01-01
Authorization: Bearer {token}
```

**路径参数：**

| 参数 | 类型 | 说明 |
| - | - | - |
| id | long | 车辆ID |

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
| - | - | - | - |
| insuranceCompany | string | 是 | 保险公司 |
| policyNo | string | 是 | 保单号 |
| startDate | string | 是 | 开始日期（yyyy-MM-dd） |
| endDate | string | 是 | 结束日期（yyyy-MM-dd） |

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "vehicleNo": "京A12345",
    "vehicleType": 1,
    "brand": "大众",
    "model": "帕萨特",
    "userId": "123",
    "status": 0,
    "insuranceNo": "1234567890",
    "insuranceCompany": "人保财险",
    "createTime": "2024-01-01 00:00:00",
    "updateTime": "2024-01-01 00:00:00"
  }
}
```

#### 4.1.13 更新车辆维修状态

``` http
PUT /api/vehicle/{id}/maintenance?maintenanceStatus=4
Authorization: Bearer {token}
```

**路径参数：**

| 参数 | 类型 | 说明 |
| - | - | - |
| id | long | 车辆ID |

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
| - | - | - | - |
| maintenanceStatus | int | 是 | 维修状态：0-离线，1-在线，2-行驶中，3-故障，4-维修中 |

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "vehicleNo": "京A12345",
    "vehicleType": 1,
    "brand": "大众",
    "model": "帕萨特",
    "userId": "123",
    "status": 4,
    "createTime": "2024-01-01 00:00:00",
    "updateTime": "2024-01-01 00:00:00"
  }
}
```

### 4.2 车辆加油管理接口（/api/vehicle/refueling）

#### 4.2.1 添加加油记录

``` http
POST /api/vehicle/refueling
Content-Type: application/json
Authorization: Bearer {token}
```

**请求体：**

```json
{
  "vehicleId": 1,
  "driverId": 123,
  "refuelingDate": "2024-01-01 10:00:00",
  "fuelType": "92#汽油",
  "fuelAmount": 50.5,
  "fuelPrice": 7.5,
  "totalCost": 378.75,
  "mileage": 10000,
  "gasStation": "中石化加油站",
  "remark": "正常加油"
}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "vehicleId": 1,
    "driverId": 123,
    "refuelingDate": "2024-01-01 10:00:00",
    "fuelType": "92#汽油",
    "fuelAmount": 50.5,
    "fuelPrice": 7.5,
    "totalCost": 378.75,
    "mileage": 10000,
    "gasStation": "中石化加油站",
    "remark": "正常加油",
    "createTime": "2024-01-01 10:00:00",
    "updateTime": "2024-01-01 10:00:00"
  }
}
```

#### 4.2.2 获取车辆加油记录

``` http
GET /api/vehicle/refueling/vehicle/{vehicleId}?page=1&size=10
Authorization: Bearer {token}
```

**路径参数：**

| 参数 | 类型 | 说明 |
| - | - | - |
| vehicleId | long | 车辆ID |

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
| - | - | - | - |
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页条数，默认10 |

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "vehicleId": 1,
      "driverId": 123,
      "refuelingDate": "2024-01-01 10:00:00",
      "fuelType": "92#汽油",
      "fuelAmount": 50.5,
      "fuelPrice": 7.5,
      "totalCost": 378.75,
      "mileage": 10000,
      "gasStation": "中石化加油站",
      "remark": "正常加油",
      "createTime": "2024-01-01 10:00:00",
      "updateTime": "2024-01-01 10:00:00"
    }
  ]
}
```

### 4.3 车辆故障管理接口（/api/vehicle/fault）

#### 4.3.1 报告故障

``` http
POST /api/vehicle/fault
Content-Type: application/json
Authorization: Bearer {token}
```

**请求体：**

```json
{
  "vehicleId": 1,
  "faultType": "发动机故障",
  "faultDescription": "发动机异响",
  "faultDate": "2024-01-01 10:00:00",
  "severity": 2
}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "vehicleId": 1,
    "faultType": "发动机故障",
    "faultDescription": "发动机异响",
    "faultDate": "2024-01-01 10:00:00",
    "severity": 2,
    "status": 1,
    "createTime": "2024-01-01 10:00:00",
    "updateTime": "2024-01-01 10:00:00"
  }
}
```

#### 4.3.2 处理故障

``` http
PUT /api/vehicle/fault/{id}/handle?repairmanId=456&repairContent=更换火花塞&repairCost=200
Authorization: Bearer {token}
```

**路径参数：**

| 参数 | 类型 | 说明 |
| - | - | - |
| id | long | 故障记录ID |

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
| - | - | - | - |
| repairmanId | long | 是 | 维修员ID |
| repairContent | string | 是 | 维修内容 |
| repairCost | number | 是 | 维修费用 |

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "vehicleId": 1,
    "faultType": "发动机故障",
    "faultDescription": "发动机异响",
    "faultDate": "2024-01-01 10:00:00",
    "severity": 2,
    "status": 3,
    "repairmanId": 456,
    "repairDate": "2024-01-01 14:00:00",
    "repairCost": 200,
    "repairContent": "更换火花塞",
    "createTime": "2024-01-01 10:00:00",
    "updateTime": "2024-01-01 14:00:00"
  }
}
```

#### 4.3.3 获取车辆故障记录

``` http
GET /api/vehicle/fault/vehicle/{vehicleId}?status=1&page=1&size=10
Authorization: Bearer {token}
```

**路径参数：**

| 参数 | 类型 | 说明 |
| - | - | - |
| vehicleId | long | 车辆ID |

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
| - | - | - | - |
| status | int | 否 | 状态：1-未处理，2-处理中，3-已处理 |
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页条数，默认10 |

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "vehicleId": 1,
      "faultType": "发动机故障",
      "faultDescription": "发动机异响",
      "faultDate": "2024-01-01 10:00:00",
      "severity": 2,
      "status": 1,
      "createTime": "2024-01-01 10:00:00",
      "updateTime": "2024-01-01 10:00:00"
    }
  ]
}
```

### 4.4 车辆保养管理接口（/api/vehicle/maintenance）

#### 4.4.1 添加保养记录

``` http
POST /api/vehicle/maintenance
Content-Type: application/json
Authorization: Bearer {token}
```

**请求体：**

```json
{
  "vehicleId": 1,
  "maintenanceType": 1,
  "maintenanceDate": "2024-01-01",
  "maintenanceContent": "更换机油和机滤",
  "maintenanceCost": 300,
  "repairmanId": 456,
  "nextMaintenanceDate": "2024-07-01",
  "mileage": 10000,
  "remark": "常规保养"
}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "vehicleId": 1,
    "maintenanceType": 1,
    "maintenanceDate": "2024-01-01",
    "maintenanceContent": "更换机油和机滤",
    "maintenanceCost": 300,
    "repairmanId": 456,
    "nextMaintenanceDate": "2024-07-01",
    "mileage": 10000,
    "remark": "常规保养",
    "createTime": "2024-01-01 10:00:00",
    "updateTime": "2024-01-01 10:00:00"
  }
}
```

#### 4.4.2 获取车辆保养记录

``` http
GET /api/vehicle/maintenance/vehicle/{vehicleId}?page=1&size=10
Authorization: Bearer {token}
```

**路径参数：**

| 参数 | 类型 | 说明 |
| - | - | - |
| vehicleId | long | 车辆ID |

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
| - | - | - | - |
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页条数，默认10 |

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "vehicleId": 1,
      "maintenanceType": 1,
      "maintenanceDate": "2024-01-01",
      "maintenanceContent": "更换机油和机滤",
      "maintenanceCost": 300,
      "repairmanId": 456,
      "nextMaintenanceDate": "2024-07-01",
      "mileage": 10000,
      "remark": "常规保养",
      "createTime": "2024-01-01 10:00:00",
      "updateTime": "2024-01-01 10:00:00"
    }
  ]
}
```

#### 4.4.3 获取下次保养信息

``` http
GET /api/vehicle/maintenance/vehicle/{vehicleId}/next
Authorization: Bearer {token}
```

**路径参数：**

| 参数 | 类型 | 说明 |
| - | - | - |
| vehicleId | long | 车辆ID |

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "vehicleId": 1,
    "maintenanceType": 1,
    "maintenanceDate": "2024-01-01",
    "maintenanceContent": "更换机油和机滤",
    "maintenanceCost": 300,
    "repairmanId": 456,
    "nextMaintenanceDate": "2024-07-01",
    "mileage": 10000,
    "remark": "常规保养",
    "createTime": "2024-01-01 10:00:00",
    "updateTime": "2024-01-01 10:00:00"
  }
}
```

### 4.5 车辆保险管理接口（/api/vehicle/insurance）

#### 4.5.1 添加保险信息

``` http
POST /api/vehicle/insurance
Content-Type: application/json
Authorization: Bearer {token}
```

**请求体：**

```json
{
  "vehicleId": 1,
  "insuranceCompany": "人保财险",
  "insuranceNumber": "1234567890",
  "insuranceType": 1,
  "insuranceAmount": 5000,
  "startDate": "2024-01-01",
  "expiryDate": "2025-01-01",
  "remark": "交强险"
}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "vehicleId": 1,
    "insuranceCompany": "人保财险",
    "insuranceNumber": "1234567890",
    "insuranceType": 1,
    "insuranceAmount": 5000,
    "startDate": "2024-01-01",
    "expiryDate": "2025-01-01",
    "status": 1,
    "remark": "交强险",
    "createTime": "2024-01-01 10:00:00",
    "updateTime": "2024-01-01 10:00:00"
  }
}
```

#### 4.5.2 获取车辆保险信息

``` http
GET /api/vehicle/insurance/vehicle/{vehicleId}
Authorization: Bearer {token}
```

**路径参数：**

| 参数 | 类型 | 说明 |
| - | - | - |
| vehicleId | long | 车辆ID |

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "vehicleId": 1,
      "insuranceCompany": "人保财险",
      "insuranceNumber": "1234567890",
      "insuranceType": 1,
      "insuranceAmount": 5000,
      "startDate": "2024-01-01",
      "expiryDate": "2025-01-01",
      "status": 1,
      "remark": "交强险",
      "createTime": "2024-01-01 10:00:00",
      "updateTime": "2024-01-01 10:00:00"
    }
  ]
}
```

#### 4.5.3 获取当前有效保险信息

``` http
GET /api/vehicle/insurance/vehicle/{vehicleId}/current
Authorization: Bearer {token}
```

**路径参数：**

| 参数 | 类型 | 说明 |
| - | - | - |
| vehicleId | long | 车辆ID |

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "vehicleId": 1,
    "insuranceCompany": "人保财险",
    "insuranceNumber": "1234567890",
    "insuranceType": 1,
    "insuranceAmount": 5000,
    "startDate": "2024-01-01",
    "expiryDate": "2025-01-01",
    "status": 1,
    "remark": "交强险",
    "createTime": "2024-01-01 10:00:00",
    "updateTime": "2024-01-01 10:00:00"
  }
}
```

---

## 5. Trip Service 行程服务

### 5.1 行程管理接口（/api/trip）

#### 5.1.1 创建行程

``` http
POST /api/trip
Content-Type: application/json
Authorization: Bearer {token}
```

**请求体：**

```json
{
  "vehicleId": 1,
  "driverId": 123,
  "startLocation": "矿区A",
  "endLocation": "矿区B",
  "startLongitude": 116.397428,
  "startLatitude": 39.90923,
  "endLongitude": 116.417428,
  "endLatitude": 39.91923,
  "estimatedStartTime": "2024-01-01 10:00:00",
  "estimatedEndTime": "2024-01-01 10:30:00",
  "tripType": 1
}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 1
}
```

#### 5.1.2 开始行程

``` http
POST /api/trip/{id}/start
Authorization: Bearer {token}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

#### 5.1.3 结束行程

``` http
POST /api/trip/{id}/end
Content-Type: application/json
Authorization: Bearer {token}
```

**请求体：**

```json
{
  "endLongitude": 116.417428,
  "endLatitude": 39.91923
}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

#### 5.1.4 获取行程详情

``` http
GET /api/trip/{id}
Authorization: Bearer {token}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "tripNo": "TRIP20240101100000",
    "vehicleId": 1,
    "driverId": 123,
    "startLocation": "矿区A",
    "endLocation": "矿区B",
    "estimatedStartTime": "2024-01-01 10:00:00",
    "estimatedEndTime": "2024-01-01 10:30:00",
    "actualStartTime": "2024-01-01 10:00:00",
    "actualEndTime": "2024-01-01 10:35:00",
    "status": 3,
    "tripType": 1,
    "estimatedMileage": 5.5,
    "actualMileage": 5.8,
    "estimatedDuration": 30,
    "actualDuration": 35,
    "fuelConsumption": 0.5,
    "averageSpeed": 9.9,
    "startLongitude": 116.397428,
    "startLatitude": 39.90923,
    "endLongitude": 116.417428,
    "endLatitude": 39.91923,
    "createTime": "2024-01-01 09:00:00",
    "updateTime": "2024-01-01 10:35:00"
  }
}
```

#### 5.1.5 分页查询行程

``` http
GET /api/trip/page?pageNum=1&pageSize=10&vehicleId=&driverId=&status=
Authorization: Bearer {token}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 100,
    "pages": 10,
    "pageNum": 1,
    "pageSize": 10,
    "records": [
      {
        "id": 1,
        "tripNo": "TRIP20240101100000",
        "vehicleId": 1,
        "driverId": 123,
        "startLocation": "矿区A",
        "endLocation": "矿区B",
        "estimatedStartTime": "2024-01-01 10:00:00",
        "estimatedEndTime": "2024-01-01 10:30:00",
        "actualStartTime": "2024-01-01 10:00:00",
        "actualEndTime": "2024-01-01 10:35:00",
        "status": 3,
        "tripType": 1,
        "estimatedMileage": 5.5,
        "actualMileage": 5.8,
        "estimatedDuration": 30,
        "actualDuration": 35,
        "fuelConsumption": 0.5,
        "averageSpeed": 9.9,
        "startLongitude": 116.397428,
        "startLatitude": 39.90923,
        "endLongitude": 116.417428,
        "endLatitude": 39.91923,
        "createTime": "2024-01-01 09:00:00",
        "updateTime": "2024-01-01 10:35:00"
      }
    ]
  }
}
```

#### 5.1.6 获取行程轨迹

``` http
GET /api/trip/{id}/track
Authorization: Bearer {token}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "longitude": 116.397428,
      "latitude": 39.90923,
      "speed": 0,
      "direction": 0,
      "altitude": 0,
      "recordTime": "2024-01-01 10:00:00"
    },
    {
      "longitude": 116.407428,
      "latitude": 39.91423,
      "speed": 45,
      "direction": 90,
      "altitude": 0,
      "recordTime": "2024-01-01 10:15:00"
    },
    {
      "longitude": 116.417428,
      "latitude": 39.91923,
      "speed": 0,
      "direction": 0,
      "altitude": 0,
      "recordTime": "2024-01-01 10:35:00"
    }
  ]
}
```

### 5.2 轨迹回放

**说明：** 轨迹回放由前端负责处理，前端从后端获取轨迹数据后，在前端进行轨迹回放的动画展示。

**前端处理流程：**
1. 调用 `/api/trip/{id}/track` 获取行程轨迹数据
2. 解析轨迹数据，按时间顺序排列
3. 使用地图API（如高德地图、百度地图）在前端实现轨迹回放动画
4. 可控制播放速度、暂停、继续等操作

### 5.3 事件触发

**说明：** 事件触发由前端负责处理，前端通过WebSocket或定时轮询获取事件数据，并在前端触发相应的事件处理逻辑。

**前端处理流程：**
1. 建立WebSocket连接或定时轮询获取事件数据
2. 监听事件数据，根据事件类型触发相应的处理逻辑
3. 展示事件通知、预警信息等
4. 处理用户对事件的响应操作

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
| 2026-03-15 | v1.5 | 新增管理员认证接口：POST /api/auth/admin/verify，管理员首次登录状态为禁用时需进行身份认证；更新用户服务逻辑文档 |
| 2026-03-14 | v1.4 | 新增 User Service 接口：通过手机号重置密码 /api/auth/reset-password，上传用户头像 /api/user/avatar；修正响应字段，将avatar改为avatarUrl |
| 2026-03-14 | v1.3 | 新增 User Service 资格认证接口：司机资格验证 /api/qualification/verify/driver，安全员资格验证 /api/qualification/verify/safety-officer，维修员资格验证 /api/qualification/verify/repairman；新增角色变更申请相关接口：获取待处理申请 /api/user/admin/role-change/apply/pending，处理申请 /api/user/admin/role-change/apply/{id}/handle 等；新增 role_change_apply 表 |
| 2026-03-14 | v1.2 | 新增 User Service 短信验证码API：发送短信验证码 /api/auth/sms/send，验证短信验证码 /api/auth/sms/verify；集成阿里云短信服务（Dypnsapi） |
| 2026-03-08 | v1.1 | 更新 User Service API：注册接口移至 /api/user/register，用户ID改为UUID字符串，响应格式调整 |
| 2024-01-01 | v1.0 | 初始版本，定义Gateway和User Service API |
