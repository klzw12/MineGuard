# API测试计划

## 项目概述

本测试计划基于MineGuard项目的后端API，按照后端控制器结构对应前端测试内容，确保API功能与前端需求一致。

## 测试环境

- 后端服务：Spring Boot 3.5.10
- 认证方式：JWT
- 基础路径：`http://localhost:8080/api`

## 测试范围

### 管理端测试
- 针对管理员角色的API测试
- 包含所有管理功能的API

### 用户端测试
- 针对普通用户角色的API测试
- 包含用户个人操作的API

## 控制器测试计划

### 1. 认证管理（AuthController）

| API路径 | 方法 | 功能描述 | 测试状态 | 前端对应功能 | 测试要点 | 测试是否完成 |
| ------- | ---- | -------- | -------- | ------------ | -------- | ------------ |
| `/auth/register` | POST | 用户注册 | 需要测试 | 注册页面 | 验证注册成功、失败场景 | ✅ |
| `/auth/login` | POST | 用户登录 | 需要测试 | 登录页面 | 验证登录成功、失败场景，JWT生成 | ✅ |
| `/auth/logout` | POST | 用户登出 | 需要测试 | 登出功能 | 验证登出功能 | ✅ |
| `/auth/refresh` | POST | 刷新Token | 需要测试 | 自动登录 | 验证Token刷新功能 | ✅ |
| `/auth/sms/send` | POST | 发送短信验证码 | 需要测试 | 注册、找回密码 | 验证短信发送功能 | ✅ |
| `/auth/sms/verify` | POST | 验证短信验证码 | 需要测试 | 注册、找回密码 | 验证短信验证功能 | ✅ |
| `/auth/reset-password` | POST | 通过手机号重置密码 | 需要测试 | 找回密码页面 | 验证密码重置功能 | ✅ |
| `/auth/admin/verify` | POST | 管理员认证 | 需要测试 | 管理员认证页面 | 验证管理员身份认证 | ✅ |

### 2. 用户管理（UserController）

| API路径 | 方法 | 功能描述 | 测试状态 | 前端对应功能 | 测试要点 | 测试是否完成 |
| ------- | ---- | -------- | -------- | ------------ | -------- | ------------ |
| `/user/current` | GET | 获取当前用户信息 | 需要测试 | 个人中心 | 验证用户信息查询 | ✅ |
| `/user/{id}` | GET | 获取用户详情 | 需要测试 | 用户详情页面 | 验证用户详情查询 | ✅ |
| `/user/{id}` | PUT | 更新用户信息 | 需要测试 | 编辑个人资料 | 验证用户信息更新 | ✅ |
| `/user/password` | PUT | 修改密码 | 需要测试 | 修改密码页面 | 验证密码修改功能 | ✅ |
| `/user/phone` | PUT | 更新手机号 | 需要测试 | 绑定手机号 | 验证手机号更新功能 | ✅ |
| `/user/role` | GET | 获取当前用户角色 | 需要测试 | 权限管理 | 验证角色查询功能 | ✅ |
| `/user/role-code` | GET | 获取当前用户角色编码 | 需要测试 | 权限管理 | 验证角色编码查询 | ✅ |
| `/user/avatar` | POST | 上传用户头像 | 需要测试 | 个人中心 | 验证头像上传功能 | ✅ |
| `/user/avatar/signed-url` | GET | 获取头像签名URL | 需要测试 | 个人中心 | 验证签名URL获取 | ✅ |
| `/user/id-card/signed-urls` | GET | 获取身份证图片签名URL | 需要测试 | 实名认证 | 验证身份证URL获取 | ✅ |
| `/user/list` | GET | 根据角色获取用户列表 | 需要测试 | 用户管理页面 | 验证按角色查询功能 | ✅ |
| `/user/admin/create` | POST | 管理员创建用户 | 需要测试 | 创建用户页面 | 验证用户创建功能 | ✅ |
| `/user/{id}/role` | PUT | 分配角色 | 需要测试 | 用户管理页面 | 验证角色分配功能 | ✅ |
| `/user/{id}/disable` | PUT | 禁用用户 | 需要测试 | 用户管理页面 | 验证用户禁用功能 | ✅ |
| `/user/{id}/enable` | PUT | 启用用户 | 需要测试 | 用户管理页面 | 验证用户启用功能 | ✅ |

### 3. 管理员管理（AdminController）

| API路径 | 方法 | 功能描述 | 测试状态 | 前端对应功能 | 测试要点 | 测试是否完成 |
| -------- | ---- | -------- | -------- | ------------ | -------- | ------------ |
| `/user/admin/page` | GET | 分页查询用户 | 需要测试 | 用户管理页面 | 验证管理员分页查询功能 | ✅ |
| `/user/admin/user/{id}/role-change` | PUT | 变更用户角色 | 需要测试 | 用户管理页面 | 验证管理员角色变更功能 | 无法测试,角色变更需要资格证件照，但资格证件照目前没有模板保证同样身份，测试必要性低 |
| `/user/admin/role-change/apply` | POST | 提交角色变更申请 | 需要测试 | 角色申请页面 | 验证申请提交功能 | |
| `/user/admin/role-change/apply/my` | GET | 获取我的角色变更申请 | 需要测试 | 角色申请页面 | 验证个人申请查询 | |
| `/user/admin/role-change/apply/pending` | GET | 获取待处理的角色变更申请 | 需要测试 | 申诉管理页面 | 验证待处理申请查询 | |
| `/user/admin/role-change/apply/list` | GET | 获取所有角色变更申请 | 需要测试 | 申诉管理页面 | 验证所有申请查询 | |
| `/user/admin/role-change/apply/user/{userId}` | GET | 获取用户的角色变更申请历史 | 需要测试 | 用户详情页面 | 验证用户申请历史查询 | |
| `/user/admin/role-change/apply/{id}/handle` | PUT | 处理角色变更申请 | 需要测试 | 申诉管理页面 | 验证申请处理功能 | |

### 4. 车辆管理（VehicleController）

| API路径 | 方法 | 功能描述 | 测试状态 | 前端对应功能 | 测试要点 | 测试是否完成 |
| -------- | ---- | -------- | -------- | ------------ | -------- | ------------ |
| `/vehicle` | POST | 创建车辆 | 需要测试 | 车辆管理页面 | 验证车辆创建功能 | |
| `/vehicle/create-with-photos` | POST | 创建车辆并上传照片 | 需要测试 | 车辆管理页面 | 验证车辆创建和照片上传功能 | |
| `/vehicle/{id}` | PUT | 更新车辆 | 需要测试 | 车辆管理页面 | 验证车辆更新功能 | |
| `/vehicle/{id}` | DELETE | 删除车辆 | 需要测试 | 车辆管理页面 | 验证车辆删除功能 | |
| `/vehicle/{id}` | GET | 获取车辆详情 | 需要测试 | 车辆详情页面 | 验证车辆详情查询 | |
| `/vehicle/page` | GET | 分页查询车辆 | 需要测试 | 车辆管理页面 | 验证车辆分页查询 | |
| `/vehicle/{id}/photo` | POST | 上传车辆照片 | 需要测试 | 车辆管理页面 | 验证照片上传功能 | |
| `/vehicle/{id}/license` | POST | 上传行驶证并OCR | 需要测试 | 车辆管理页面 | 验证OCR功能 | |
| `/vehicle/{id}/license/front` | POST | 上传行驶证正面并OCR | 需要测试 | 车辆管理页面 | 验证行驶证正面OCR功能 | |
| `/vehicle/{id}/license/back` | POST | 上传行驶证反面 | 需要测试 | 车辆管理页面 | 验证行驶证反面上传功能 | |
| `/vehicle/{id}/insurance` | POST | 上传车辆保险信息 | 需要测试 | 车辆管理页面 | 验证保险信息上传 | |
| `/vehicle/{id}/maintenance` | PUT | 更新车辆维修状态 | 需要测试 | 车辆管理页面 | 验证维修状态更新功能 | |
| `/vehicle/best` | POST | 选择最佳车辆 | 需要测试 | 调度管理页面 | 验证最佳车辆推荐 | |
| `/vehicle/available` | GET | 获取所有可用车辆 | 需要测试 | 调度管理页面 | 验证可用车辆查询 | |

### 5. 行程管理（TripController）

| API路径 | 方法 | 功能描述 | 测试状态 | 前端对应功能 | 测试要点 | 测试是否完成 |
| -------- | ---- | -------- | -------- | ------------ | -------- | ------------ |
| `/trip/page` | GET | 分页查询行程 | 需要测试 | 行程管理页面 | 验证行程分页查询 | |
| `/trip` | POST | 创建行程 | 需要测试 | 行程管理页面 | 验证行程创建功能 | |
| `/trip/{id}` | PUT | 更新行程 | 需要测试 | 行程管理页面 | 验证行程更新功能 | |
| `/trip/{id}` | DELETE | 删除行程 | 需要测试 | 行程管理页面 | 验证行程删除功能 | |
| `/trip/{id}/start` | POST | 开始行程 | 需要测试 | 行程详情页面 | 验证行程开始功能 | |
| `/trip/{id}/end` | POST | 结束行程 | 需要测试 | 行程详情页面 | 验证行程结束功能 | |
| `/trip/{id}/accept` | POST | 接受行程 | 需要测试 | 行程详情页面 | 验证行程接受功能 | |
| `/trip/{id}/pause` | POST | 暂停行程 | 需要测试 | 行程详情页面 | 验证行程暂停功能 | |
| `/trip/{id}/resume` | POST | 恢复行程 | 需要测试 | 行程详情页面 | 验证行程恢复功能 | |
| `/trip/vehicle/{vehicleId}` | GET | 根据车辆ID查询行程 | 需要测试 | 车辆详情页面 | 验证按车辆查询 | |
| `/trip/driver/{driverId}` | GET | 根据司机ID查询行程 | 需要测试 | 司机详情页面 | 验证按司机查询 | |
| `/trip/latest/{vehicleId}` | GET | 获取车辆最近行程 | 需要测试 | 车辆详情页面 | 验证最近行程查询 | |
| `/trip/{id}/statistics` | GET | 获取行程统计 | 需要测试 | 行程详情页面 | 验证行程统计查询 | |

### 6. 调度管理（DispatchController）

| API路径 | 方法 | 功能描述 | 测试状态 | 前端对应功能 | 测试要点 | 测试是否完成 |
| -------- | ---- | -------- | -------- | ------------ | -------- | ------------ |
| `/dispatch/main/task` | POST | 创建调度任务 | 需要测试 | 任务调度页面 | 验证任务创建功能 | |
| `/dispatch/main/task/{id}` | PUT | 更新调度任务 | 需要测试 | 任务调度页面 | 验证任务更新功能 | |
| `/dispatch/main/task/{taskId}/execute` | POST | 执行调度任务 | 需要测试 | 任务调度页面 | 验证任务执行功能 | |
| `/dispatch/main/task/{taskId}/cancel` | POST | 取消调度任务 | 需要测试 | 任务调度页面 | 验证任务取消功能 | |
| `/dispatch/main/task/{taskId}` | GET | 获取调度任务 | 需要测试 | 任务详情页面 | 验证任务查询功能 | |
| `/dispatch/main/task/list` | GET | 获取调度任务列表 | 需要测试 | 任务调度页面 | 验证任务列表查询 | |
| `/dispatch/main/task/dynamic-adjust/user-leave` | POST | 用户请假任务重新分配 | 需要测试 | 任务调度页面 | 验证任务重新分配 | |

### 7. 预警管理（WarningController）

| API路径 | 方法 | 功能描述 | 测试状态 | 前端对应功能 | 测试要点 | 测试是否完成 |
| -------- | ---- | -------- | -------- | ------------ | -------- | ------------ |
| `/warning/record/page` | GET | 分页查询预警记录 | 需要测试 | 预警管理页面 | 验证预警记录分页查询 | |
| `/warning/record/{id}` | GET | 获取预警记录详情 | 需要测试 | 预警详情页面 | 验证预警记录详情查询 | |
| `/warning/record/trip` | GET | 根据行程ID查询预警记录 | 需要测试 | 行程详情页面 | 验证按行程查询预警记录 | |
| `/warning/record` | POST | 创建预警记录 | 需要测试 | 预警管理页面 | 验证预警记录创建功能 | |
| `/warning/record/{id}/handle` | PUT | 处理预警记录 | 需要测试 | 预警管理页面 | 验证预警记录处理功能 | |
| `/warning/record/{id}/ignore` | PUT | 忽略预警记录 | 需要测试 | 预警管理页面 | 验证预警记录忽略功能 | |
| `/warning/process-event` | POST | 处理事件触发 | 不需要测试 | 系统内部 | 验证事件触发处理功能 | |
| `/warning/process-track` | POST | 处理轨迹点 | 不需要测试 | 系统内部 | 验证轨迹点处理功能 | |
| `/warning/statistics` | GET | 获取预警统计 | 需要测试 | 统计分析页面 | 验证预警统计功能 | |
| `/warning/trend` | GET | 获取预警趋势 | 需要测试 | 统计分析页面 | 验证预警趋势查询 | |
| `/warning/statistics/type` | GET | 获取预警类型统计 | 需要测试 | 统计分析页面 | 验证预警类型统计 | |
| `/warning/statistics/level` | GET | 获取预警级别统计 | 需要测试 | 统计分析页面 | 验证预警级别统计 | |
| `/warning/rule/page` | GET | 分页查询预警规则 | 需要测试 | 预警规则页面 | 验证预警规则分页查询 | |
| `/warning/rule/{id}` | GET | 获取预警规则详情 | 需要测试 | 预警规则详情页面 | 验证预警规则详情查询 | |
| `/warning/rule/code/{ruleCode}` | GET | 根据编码获取预警规则 | 需要测试 | 预警规则页面 | 验证按编码查询预警规则 | |
| `/warning/rule` | POST | 创建预警规则 | 需要测试 | 预警规则页面 | 验证预警规则创建功能 | |
| `/warning/rule/{id}` | PUT | 更新预警规则 | 需要测试 | 预警规则页面 | 验证预警规则更新功能 | |
| `/warning/rule/{id}` | DELETE | 删除预警规则 | 需要测试 | 预警规则页面 | 验证预警规则删除功能 | |
| `/warning/rule/list` | GET | 获取所有启用的预警规则 | 需要测试 | 预警规则页面 | 验证启用预警规则查询 | |
| `/warning/rule/{id}/enable` | PUT | 启用预警规则 | 需要测试 | 预警规则页面 | 验证预警规则启用功能 | |
| `/warning/rule/{id}/disable` | PUT | 禁用预警规则 | 需要测试 | 预警规则页面 | 验证预警规则禁用功能 | |

### 8. 统计管理（StatisticsController）

| API路径 | 方法 | 功能描述 | 测试状态 | 前端对应功能 | 测试要点 | 测试是否完成 |
| -------- | ---- | -------- | -------- | ------------ | -------- | ------------ |
| `/statistics/trip` | GET | 获取行程统计 | 需要测试 | 统计分析页面 | 验证行程统计查询 | |
| `/statistics/cost` | GET | 获取成本统计 | 需要测试 | 统计分析页面 | 验证成本统计查询 | |
| `/statistics/vehicle` | GET | 获取车辆统计 | 需要测试 | 统计分析页面 | 验证车辆统计查询 | |
| `/statistics/driver` | GET | 获取司机统计 | 需要测试 | 统计分析页面 | 验证司机统计查询 | |
| `/statistics/overall` | GET | 获取总体统计 | 需要测试 | 统计分析页面 | 验证总体统计查询 | |
| `/statistics/fault` | GET | 获取故障统计 | 需要测试 | 统计分析页面 | 验证故障统计查询 | |
| `/statistics/fault/overall` | GET | 获取故障总体统计 | 需要测试 | 统计分析页面 | 验证故障总体统计 | |
| `/statistics/transport` | GET | 获取运输统计 | 需要测试 | 统计分析页面 | 验证运输统计查询 | |
| `/statistics/trip/calculate` | POST | 计算行程统计 | 需要测试 | 统计分析页面 | 验证行程统计计算 | |
| `/statistics/cost/calculate` | POST | 计算成本统计 | 需要测试 | 统计分析页面 | 验证成本统计计算 | |
| `/statistics/vehicle/calculate` | POST | 计算车辆统计 | 需要测试 | 统计分析页面 | 验证车辆统计计算 | |
| `/statistics/driver/calculate` | POST | 计算司机统计 | 需要测试 | 统计分析页面 | 验证司机统计计算 | |
| `/statistics/fault/calculate` | POST | 计算故障统计 | 需要测试 | 统计分析页面 | 验证故障统计计算 | |
| `/statistics/transport/calculate` | POST | 计算运输统计 | 需要测试 | 统计分析页面 | 验证运输统计计算 | |

## 测试准备

### 1. 测试数据

- 管理员账号：`admin@mineguard.com` / `password123`
- 司机账号：`driver@mineguard.com` / `password123`
- 普通用户账号：`user@mineguard.com` / `password123`

### 2. 测试工具

- Postman：用于API测试
- JWT Debugger：用于Token验证
- Swagger UI：用于API文档查看

## 测试流程

1. **认证测试**：测试登录、注册、登出等认证相关API
2. **管理端测试**：使用管理员账号测试管理端API
3. **用户端测试**：使用普通用户账号测试用户端API
4. **权限测试**：验证不同角色的权限控制
5. **边界测试**：测试API的边界条件和错误处理

## 预期结果

- 所有API返回正确的状态码
- 认证API能够正确生成和验证JWT
- 管理端API只有管理员能够访问
- 用户端API所有用户都能访问
- 数据操作API能够正确执行CRUD操作

## 测试报告

测试完成后，将生成详细的测试报告，包括：

- 测试用例执行情况
- 成功/失败的API列表
- 错误信息和原因分析
- 性能测试结果

---

**注**：本测试计划基于后端API实现，对应前端的API调用。测试时请确保后端服务正常运行，并且数据库中存在必要的测试数据。