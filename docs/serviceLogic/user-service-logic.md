# 用户服务逻辑文档

## 1. 模块概述

用户服务是MineGuard系统的核心服务之一，负责用户的全生命周期管理。主要功能包括用户注册、登录认证、用户信息管理、角色管理、资格认证、考勤管理、站内消息通信（WebSocket）、用户申诉处理等。

**服务端口**：`8081`

**数据库**：

- MySQL：存储用户、角色、司机、安全员、维修员、考勤、申诉等数据
- MongoDB：存储站内消息（Message集合）

**服务依赖**：

- Redis：缓存和Token存储
- RabbitMQ：消息队列
- 阿里云短信服务：验证码发送

---

## 2. 核心功能

### 2.1 认证模块（Auth）

| 功能 | 说明 | 实现状态 |
| ---- | ---- | ---- |
| 用户登录 | 用户名密码登录，返回JWT令牌 | ✅ 已实现 |
| 用户注册 | 用户注册，默认角色为司机 | ✅ 已实现 |
| 密码重置 | 通过手机号验证码重置密码 | ✅ 已实现 |
| 管理员认证 | 管理员首次登录需身份认证后启用账户 | ✅ 已实现 |
| 图形验证码 | 获取登录图形验证码 | ⬜ 不暴露（用短信验证码替代） |
| 短信验证码 | 发送/验证手机验证码 | ✅ 已实现 |
| Token刷新 | 刷新accessToken | ✅ 已实现 |
| 用户登出 | 退出登录清除Token | ✅ 已实现 |

### 2.2 用户管理模块（User）

| 功能 | 说明 | 实现状态 |
| ---- | ---- | ---- |
| 获取当前用户信息 | 获取登录用户详细信息 | ✅ 已实现 |
| 更新用户信息 | 更新用户基本信息 | ✅ 已实现 |
| 修改密码 | 验证旧密码后修改 | ✅ 已实现 |
| 上传头像 | 上传用户头像到OSS | ✅ 已实现 |
| 获取用户角色 | 获取当前用户角色信息 | ✅ 已实现 |
| 管理员创建用户 | 管理员手动创建用户 | ✅ 已实现 |

### 2.3 管理员模块（Admin）

| 功能 | 说明 | 实现状态 |
| ---- | ---- | ---- |
| 分页查询用户 | 管理员分页查询用户列表 | ✅ 已实现 |
| 获取用户详情 | 管理员获取指定用户详情 | ✅ 已实现 |
| 禁用用户 | 管理员禁用用户账号 | ✅ 已实现 |
| 启用用户 | 管理员启用用户账号 | ✅ 已实现 |
| 变更用户角色 | 管理员变更用户角色 | ✅ 已实现 |
| 角色变更申请处理 | 审批用户角色变更申请 | ✅ 已实现 |
| 获取待处理申请 | 获取所有待处理的角色变更申请 | ✅ 已实现 |
| 获取所有申请 | 获取所有角色变更申请 | ✅ 已实现 |

### 2.4 角色管理模块（Role）

| 功能 | 说明 | 实现状态 |
| ---- | ---- | -------- |
| 获取角色列表 | 获取所有角色 | ✅ 已实现 |
| 获取角色详情 | 根据ID获取角色 | ✅ 已实现 |
| 创建角色 | 创建新角色 | ✅ 已实现 |
| 更新角色 | 更新角色信息 | ✅ 已实现 |
| 删除角色 | 删除角色 | ✅ 已实现 |
| 分配角色权限 | 分配权限给角色 | ❌ 未实现 |
| 权限验证 | 验证用户权限 | ❌ 未实现 |

### 2.5 资格认证模块（Qualification）

| 功能 | 说明 | 实现状态 |
| ---- | ---- | -------- |
| 身份证验证 | 实名认证，上传身份证照片 | ✅ 已实现 |
| 检查身份证状态 | 检查用户是否已实名认证 | ✅ 已实现 |
| 驾驶证认证 | 司机资格认证 | ✅ 已实现 |
| 应急救援证认证 | 安全员资格认证 | ✅ 已实现 |
| 维修资格证认证 | 维修员资格认证 | ✅ 已实现 |

### 2.6 考勤管理模块（Attendance）

| 功能 | 说明 | 实现状态 |
| ---- | ---- | -------- |
| 上班打卡 | 司机签到 | ✅ 已实现 |
| 下班打卡 | 司机签退 | ✅ 已实现 |
| 获取考勤记录 | 获取指定日期考勤 | ✅ 已实现 |
| 获取考勤列表 | 获取月度考勤列表 | ✅ 已实现 |
| 考勤统计 | 获取月度考勤统计 | ✅ 已实现 |
| 补卡 | 管理员补卡 | ✅ 已实现 |

### 2.7 站内消息模块（Message - WS）

| 功能 | 说明 | 实现状态 |
| ---- | ---- | -------- |
| WebSocket连接 | 建立WS连接 `/ws/messages` | ✅ 已实现 |
| 单播消息 | 发送给指定用户 | ✅ 已实现 |
| 广播消息 | 发送给指定用户群体 | ✅ 已实现 |
| 全局广播 | 发送给所有在线用户 | ✅ 已实现 |
| 获取消息列表 | 获取用户消息分页列表 | ✅ 已实现 |
| 标记已读 | 标记消息为已读 | ✅ 已实现 |
| 删除消息 | 删除消息 | ✅ 已实现 |
| 未读计数 | 获取未读消息数量 | ✅ 已实现 |

**消息存储**：MongoDB集合 `messages`

### 2.8 用户申诉模块（UserAppeal）

| 功能 | 说明 | 实现状态 |
| ---- | ---- | -------- |
| 提交申诉 | 用户提交申诉 | ✅ 已实现 |
| 我的申诉 | 获取当前用户申诉列表 | ✅ 已实现 |
| 检查待处理 | 检查是否有待处理申诉 | ✅ 已实现 |
| 待处理列表 | 管理员获取待处理申诉 | ✅ 已实现 |
| 所有申诉 | 管理员获取所有申诉 | ✅ 已实现 |
| 处理申诉 | 管理员处理申诉 | ✅ 已实现 |
| 按用户查询 | 管理员按用户ID查询申诉 | ✅ 已实现 |

### 2.9 管理员初始化模块

| 功能 | 说明 | 实现状态 |
| ---- | ---- | -------- |
| 自动创建管理员 | 系统启动时自动创建管理员账户 | ✅ 已实现 |
| 管理员角色自动创建 | 自动创建管理员角色 | ✅ 已实现 |

**配置项**：`AdminInitProperties.initadmin` 控制是否开启

---

## 3. 未实现功能

### 3.1 认证模块

- **图形验证码**：Service已实现，不暴露接口（用短信验证码替代）
- **短信验证码**：✅ 已实现（Controller + Service）
- **Token刷新**：✅ 已实现（Controller + Service）
- **用户登出**：✅ 已实现（Controller + Service）

### 3.2 权限模块

- **权限管理**：permission表已创建但无对应Controller和Service
- **角色权限分配**：RoleController未实现权限分配接口
- **权限验证**：未实现基于权限的验证逻辑

### 3.3 其他

- **消息已读未读状态推送**：WebSocket未实现实时推送已读状态
- **消息撤回**：未实现消息撤回功能

---

## 4. API与前端审查

### 4.1 API文档对照

| API文档接口 | 后端实现 | 前端实现 | 备注 |
| ----------- | -------- | -------- | ---- |
| POST /api/auth/login | ✅ | ✅ auth.uts | |
| POST /api/auth/logout | ✅ | ✅ auth.uts | |
| POST /api/auth/refresh | ✅ | ✅ auth.uts | |
| GET /api/auth/captcha | ⬜ | ✅ auth.uts | 不暴露（用短信验证码替代） |
| POST /api/auth/sms/send | ✅ | ✅ auth.uts | |
| POST /api/auth/sms/verify | ✅ | ✅ auth.uts | |
| POST /api/auth/reset-password | ✅ | ✅ auth.uts | |
| POST /api/auth/admin/verify | ✅ | ✅ auth.uts | |
| POST /api/user/register | ✅ | ✅ user.uts | 实际路径为/api/user/register |
| GET /api/user/current | ✅ | ❌ | 需补充 |
| PUT /api/user/{id} | ✅ | ❌ | 需补充 |
| PUT /api/user/password | ✅ | ❌ | 需补充 |
| POST /api/user/avatar | ✅ | ❌ | 需补充 |
| GET /api/user/admin/page | ✅ | ✅ user.uts | |
| GET /api/user/admin/user/{id} | ✅ | ✅ user.uts | |
| PUT /api/user/admin/user/{id}/disable | ✅ | ✅ user.uts | |
| PUT /api/user/admin/user/{id}/enable | ✅ | ✅ user.uts | |
| PUT /api/user/admin/user/{id}/role-change | ✅ | ✅ user.uts | |
| GET /api/role/list | ✅ | ✅ user.uts | |
| GET /api/role/{id} | ✅ | ✅ user.uts | |
| POST /api/role | ✅ | ❌ | 需补充 |
| PUT /api/role/{id} | ✅ | ❌ | 需补充 |
| DELETE /api/role/{id} | ✅ | ❌ | 需补充 |
| POST /api/qualification/idcard/verify | ✅ | ✅ qualification.uts | |
| GET /api/qualification/idcard/check/{userId} | ✅ | ✅ qualification.uts | |
| POST /api/qualification/cert/driver | ✅ QualificationController | ✅ qualification.uts | |
| POST /api/qualification/cert/safety-officer | ✅ QualificationController | ✅ qualification.uts | |
| POST /api/qualification/cert/repairman | ✅ QualificationController | ✅ qualification.uts | |
| POST /api/messages/unicast | ✅ MessageController | ❌ 未实现 | 需补充 |
| POST /api/messages/broadcast | ✅ MessageController | ❌ 未实现 | 需补充 |
| POST /api/messages/broadcast/all | ✅ MessageController | ❌ 未实现 | 需补充 |
| GET /api/messages/user/{userId} | ✅ MessageController | ❌ 未实现 | 需补充 |
| PUT /api/messages/{messageId}/read | ✅ MessageController | ❌ 未实现 | 需补充 |
| DELETE /api/messages/{messageId} | ✅ MessageController | ❌ 未实现 | 需补充 |
| GET /api/messages/unread/count/{userId} | ✅ MessageController | ❌ 未实现 | 需补充 |
| GET /api/user/appeal/my | ✅ UserAppealController | ❌ 未实现 | 需补充 |
| POST /api/user/appeal | ✅ UserAppealController | ❌ 未实现 | 需补充 |
| GET /api/user/appeal/pending/check | ✅ UserAppealController | ❌ 未实现 | 需补充 |
| GET /api/user/appeal/admin/pending | ✅ UserAppealController | ❌ 未实现 | 需补充 |
| GET /api/user/appeal/admin/list | ✅ UserAppealController | ❌ 未实现 | 需补充 |
| PUT /api/user/appeal/admin/{id}/handle | ✅ UserAppealController | ❌ 未实现 | 需补充 |
| 考勤相关接口 | ✅ AttendanceController | ❌ 未实现 | 需补充 |

### 4.2 前端缺失接口

前端 `user.uts` 缺少以下功能：

- `getCurrentUser()` - 获取当前用户信息
- `updateUser()` - 更新用户信息
- `updatePassword()` - 修改密码
- `uploadAvatar()` - 上传头像（函数存在但未导出或使用方式可能有问题）
- 角色管理CRUD接口
- 站内消息接口
- 用户申诉接口
- 考勤接口

### 4.3 需要补充的建议

1. **后端补充**：
   - 实现图形验证码接口
   - 实现短信验证码发送和验证
   - 实现Token刷新接口
   - 实现登出接口
   - 实现权限管理相关接口

2. **前端补充**：
   - 在 `user.uts` 中添加获取当前用户、更新用户、修改密码等接口
   - 创建 `message.uts` 实现站内消息功能
   - 创建 `appeal.uts` 实现用户申诉功能
   - 创建 `attendance.uts` 实现考勤功能

3. **API文档更新**：
   - 补充缺失的接口文档
   - 添加站内消息接口文档
   - 添加用户申诉接口文档
   - 添加考勤接口文档
