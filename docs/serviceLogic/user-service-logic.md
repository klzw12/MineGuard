# 用户服务逻辑

## 1. 认证服务逻辑 (AuthService)

### 1.1 用户注册

**接口**: `POST /api/auth/register`  
**DTO**: `UserRegisterDTO(username, password, phone, smsCode, email)`  
**VO**: `UserVO`

**处理流程**:

1. 校验用户名是否已存在（`UserService.getByUsername`）
2. 校验手机号是否已存在（`UserService.getByPhone`）
3. 验证短信验证码（调用阿里云SmsService.verifySmsCode）
4. 创建用户，设置默认状态为启用，无默认角色
5. 密码加密存储
6. 生成JWT Token
7. 返回用户信息（含token、refreshToken、expiresIn）

---

### 1.2 用户登录

**接口**: `POST /api/auth/login`  
**DTO**: `UserLoginDTO(username, password)`  
**VO**: `UserVO`

**处理流程**:

1. 根据用户名查询用户
2. 校验密码是否正确
3. 校验用户状态（是否被禁用且并非管理员）
4. 生成JWT Token
5. 查询用户角色信息
6. 返回用户信息（含token）

---

### 1.3 用户登出

**接口**: `POST /api/auth/logout`  
**返回**: `void`

**处理流程**:

1. 获取当前用户Token
2. 将Token加入黑名单（Redis）
3. 清除用户缓存

---

### 1.4 发送短信验证码

**接口**: `POST /api/auth/sms/send`  
**DTO**: `SendSmsCodeDTO(phone)`  
**VO**: `SmsCodeVO`

**处理流程**:

1. 校验手机号格式
2. 调用阿里云短信服务发送验证码
3. 阿里云服务内部处理验证码生成和存储
4. 返回发送结果

---

### 1.5 验证短信验证码

**接口**: `POST /api/auth/sms/verify`  
**DTO**: `VerifySmsCodeDTO(phone, code)`  
**返回**: `Boolean`

**处理流程**:

1. 调用阿里云短信服务验证验证码
2. 返回验证结果

---

### 1.6 重置密码

**接口**: `POST /api/auth/reset-password`  
**DTO**: `ResetPasswordDTO(phone, smsCode, newPassword)`  
**返回**: `void`

**处理流程**:

1. 验证短信验证码（调用阿里云SmsService.verifySmsCode）
2. 根据手机号查询用户
3. 新密码加密后更新
4. 清除用户缓存

---

### 1.7 刷新Token

**接口**: `POST /api/auth/refresh`  
**参数**: `refreshToken`  
**VO**: `TokenVO(accessToken, refreshToken, tokenType, expiresIn)`

**处理流程**:

1. 验证refreshToken是否有效
2. 从refreshToken中解析用户信息
3. 生成新的accessToken和refreshToken
4. 返回新的Token信息

---

### 1.8 管理员认证

**接口**: `POST /api/auth/admin/verify`  
**DTO**: `AdminVerifyDTO(userId, realName, phone, idCardFrontBase64, idCardBackBase64)`  
**VO**: `UserVO`

**处理流程**:

1. 校验用户是否存在
2. 校验用户是否为管理员角色
3. 上传身份证图片到存储服务
4. 调用OCR服务识别身份证信息
5. 校验身份证姓名与提交姓名是否一致（如果提交了realName）
6. 更新用户身份证信息和实名信息
7. 更新用户状态为启用
8. 生成新的JWT Token
9. 返回用户信息（含token、refreshToken、expiresIn）

---

## 2. 用户服务逻辑 (UserService)

### 2.1 获取当前用户

**接口**: `GET /api/user/current`  
**VO**: `UserVO`

**处理流程**:

1. 从Token中获取用户ID
2. 从缓存中获取用户信息
3. 缓存未命中则查询数据库
4. 查询用户角色信息
5. 返回用户信息

---

### 2.2 获取用户详情

**接口**: `GET /api/user/{id}`  
**VO**: `UserVO`

**处理流程**:

1. 根据ID查询用户
2. 查询用户角色信息
3. 返回用户信息

---

### 2.3 更新用户信息

**接口**: `PUT /api/user/{id}`  
**DTO**: `UserUpdateDTO(username, email, avatarUrl)`  
**VO**: `UserVO`

**说明**: realName（真实姓名）实名认证后不可修改，phone（手机号）需通过专门接口修改

**处理流程**:

1. 校验用户是否存在
2. 如果修改用户名，校验用户名是否已被使用
3. 更新用户信息
4. 清除用户缓存
5. 返回更新后的用户信息

---

### 2.4 修改密码

**接口**: `PUT /api/user/password`  
**DTO**: `PasswordUpdateDTO(oldPassword, newPassword)`  
**返回**: `void`

**处理流程**:

1. 校验用户是否存在
2. 校验原密码是否正确
3. 新密码加密后更新
4. 清除用户缓存

---

### 2.5 更新手机号

**接口**: `PUT /api/user/phone`  
**DTO**: `UpdatePhoneDTO(newPhone, smsCode)`  
**VO**: `UserVO`

**处理流程**:

1. 验证短信验证码（调用阿里云SmsService.verifySmsCode）
2. 校验用户是否存在
3. 校验新手机号是否已被其他用户使用
4. 更新用户手机号
5. 清除用户缓存
6. 返回更新后的用户信息

---

### 2.6 上传头像

**接口**: `POST /api/user/avatar`  
**请求**: `MultipartFile file`  
**VO**: `UserVO`

**处理流程**:

1. 校验用户是否存在
2. 上传图片到存储服务
3. 更新用户头像URL
4. 清除用户缓存
5. 返回更新后的用户信息

---

### 2.7 分页查询用户

**接口**: `GET /api/user/page`  
**参数**: `pageNum, pageSize, username, status`  
**VO**: `Page<UserVO>`

**处理流程**:

1. 构建查询条件
2. 分页查询用户列表
3. 查询每个用户的角色信息
4. 返回分页结果

---

### 2.8 禁用/启用用户

**接口**: `PUT /api/user/{id}/disable` / `PUT /api/user/{id}/enable`  
**返回**: `void`

**处理流程**:

1. 校验用户是否存在
2. 更新用户状态
3. 清除用户缓存

---

### 2.9 分配角色

**接口**: `PUT /api/user/{id}/role`  
**参数**: `roleId`  
**返回**: `void`

**处理流程**:

1. 校验用户是否存在
2. 校验角色是否存在
3. 更新用户角色
4. 清除用户缓存

---

## 3. 角色服务逻辑 (RoleService)

### 3.1 获取角色列表

**接口**: `GET /api/role/list`  
**权限**: 普通用户可访问  
**VO**: `List<RoleVO>`

**处理流程**:

1. 查询所有角色
2. 返回角色列表

---

### 3.2 获取角色详情

**接口**: `GET /api/role/{id}`  
**权限**: 管理员权限  
**VO**: `RoleVO`

**处理流程**:

1. 根据ID查询角色
2. 返回角色信息

---

### 3.3 创建角色

**接口**: `POST /api/role`  
**DTO**: `RoleVO`  
**VO**: `RoleVO`

**处理流程**:

1. 校验角色编码是否已存在
2. 创建角色
3. 返回角色信息

---

### 3.4 更新角色

**接口**: `PUT /api/role/{id}`  
**权限**: 管理员权限  
**DTO**: `RoleVO`  
**VO**: `RoleVO`

**处理流程**:

1. 校验角色是否存在
2. 更新角色信息
3. 返回角色信息

---

### 3.5 删除角色

**接口**: `DELETE /api/role/{id}`  
**权限**: 管理员权限  
**返回**: `Boolean`

**处理流程**:

1. 校验角色是否存在
2. 校验是否有用户使用该角色
3. 删除角色
4. 返回结果

---

## 4. 人员资格认证服务逻辑 (QualificationService)

### 4.1 身份证验证（实名认证）

**接口**: `POST /api/qualification/idcard/verify`  
**DTO**: `IdCardVerifyDTO(userId, realName, idCard, idCardFrontBase64, idCardBackBase64)`  
**返回**: `Boolean`

**处理流程**:

1. 校验用户是否已完成身份证验证
2. 校验身份证号格式
3. 调用OCR服务识别身份证信息
4. 校验OCR识别的姓名和身份证号与提交信息是否一致
5. 更新用户实名信息（realName, idCard, gender, nation, birthDate, address）
6. 返回验证结果

---

### 4.2 检查身份证验证状态

**接口**: `GET /api/qualification/idcard/check/{userId}`  
**返回**: `Boolean`

**处理流程**:

1. 根据用户ID查询用户信息
2. 检查用户是否已完成身份证验证（realName和idCard是否已填写）
3. 返回验证状态

---

### 4.3 上传驾驶证（司机）

**接口**: `POST /api/qualification/cert/driver`  
**DTO**: `CertVerifyDTO(userId, personType, certNumber, drivingLicenseBase64)`  
**返回**: `Boolean`

**处理流程**:

1. 校验用户是否已完成身份证验证
2. 调用OCR服务识别驾驶证信息
3. 校验驾驶证姓名与实名认证姓名是否一致
4. 校验驾驶证是否过期
5. 创建或更新司机信息（Driver表）
6. 返回验证结果

---

### 4.4 上传应急救援证（安全员）

**接口**: `POST /api/qualification/cert/safety-officer`  
**DTO**: `CertVerifyDTO(userId, personType, certNumber, emergencyCertBase64)`  
**返回**: `Boolean`

**处理流程**:

1. 校验用户是否已完成身份证验证
2. 调用OCR服务识别应急救援证信息
3. 校验证书姓名与实名认证姓名是否一致
4. 校验证书是否过期
5. 创建或更新安全员信息（SafetyOfficer表）
6. 返回验证结果

---

### 4.5 上传维修资格证（维修员）

**接口**: `POST /api/qualification/cert/repairman`  
**DTO**: `CertVerifyDTO(userId, personType, certNumber, repairCertBase64)`  
**返回**: `Boolean`

**处理流程**:

1. 校验用户是否已完成身份证验证
2. 调用OCR服务识别维修资格证信息
3. 校验证书姓名与实名认证姓名是否一致
4. 校验证书是否过期
5. 创建或更新维修员信息（Repairman表）
6. 返回验证结果

---

## 5. 考勤服务逻辑 (AttendanceService)

> **说明**：考勤功能适用于所有用户角色（司机、安全员、维修员等），用户ID从JWT Token中自动获取

### 5.1 上班打卡

**接口**: `POST /api/attendance/check-in`  
**DTO**: `CheckInDTO(latitude, longitude, address, remark)`  
**VO**: `AttendanceVO`

**处理流程**:

1. 从JWT Token获取当前用户ID
2. 校验用户是否存在
3. 查询当天是否已有出勤记录
4. 如果没有记录，创建新出勤记录；如果已有记录，更新上班打卡信息
5. 根据配置的工作开始时间和迟到阈值判断是否迟到
6. 设置出勤状态（正常/迟到）和迟到分钟数
7. 保存打卡位置信息（经纬度、地址）
8. 返回出勤记录

---

### 5.2 下班打卡

**接口**: `POST /api/attendance/check-out`  
**DTO**: `CheckOutDTO(latitude, longitude, address, remark)`  
**VO**: `AttendanceVO`

**处理流程**:

1. 从JWT Token获取当前用户ID
2. 校验用户是否存在
3. 查询当天出勤记录
4. 如果没有记录，创建新记录并标记为缺勤；如果有记录，更新下班打卡信息
5. 根据配置的工作结束时间和早退阈值判断是否早退
6. 更新出勤状态和早退分钟数
7. 保存打卡位置信息（经纬度、地址）
8. 返回出勤记录

---

### 5.3 获取某日考勤记录

**接口**: `GET /api/attendance/{userId}?date=2024-01-01`  
**参数**: `date` (LocalDate格式 yyyy-MM-dd)  
**VO**: `AttendanceVO`

**处理流程**:

1. 根据用户ID和日期查询出勤记录
2. 查询用户姓名
3. 返回出勤记录（包含状态标签）

---

### 5.4 获取某月考勤记录列表

**接口**: `GET /api/attendance/{userId}/list?yearMonth=2024-01`  
**参数**: `yearMonth` (格式 yyyy-MM)  
**VO**: `List<AttendanceVO>`

**处理流程**:

1. 解析年月参数
2. 计算该月的起始和结束日期
3. 根据用户ID和日期范围查询出勤记录列表
4. 查询用户姓名
5. 返回出勤记录列表

---

### 5.5 获取某月考勤统计

**接口**: `GET /api/attendance/{userId}/statistics?yearMonth=2024-01`  
**参数**: `yearMonth` (格式 yyyy-MM)  
**VO**: `AttendanceStatisticsVO`

**处理流程**:

1. 解析年月参数
2. 计算该月的起始和结束日期
3. 计算应出勤天数（工作日，排除周末）
4. 查询实际出勤天数
5. 统计迟到次数、早退次数
6. 计算缺勤天数 = 应出勤天数 - 实际出勤天数
7. 计算正常出勤天数 = 实际出勤天数 - 迟到次数 - 早退次数
8. 计算出勤率 = 实际出勤天数 / 应出勤天数 * 100
9. 返回统计数据

**统计字段说明**:

- `shouldAttendanceDays`: 应出勤天数（工作日）
- `actualAttendanceDays`: 实际出勤天数
- `normalDays`: 正常出勤天数
- `lateTimes`: 迟到次数
- `earlyLeaveTimes`: 早退次数
- `absentDays`: 缺勤天数
- `attendanceRate`: 出勤率（百分比）

---

### 5.6 补卡（管理员功能）

**接口**: `PUT /api/attendance/{attendanceId}/supplement`  
**参数**: `checkInTime, checkOutTime, status, remark`  
**VO**: `AttendanceVO`

**处理流程**:

1. 校验出勤记录是否存在
2. 更新上班时间（如果提供）
3. 更新下班时间（如果提供）
4. 更新出勤状态（如果提供）
5. 更新备注信息（如果提供）
6. 返回更新后的出勤记录

---

### 5.7 申请请假

**接口**: `POST /api/attendance/leave`  
**DTO**: `LeaveApplyDTO(userId, leaveType, startTime, endTime, reason)`  
**VO**: `AttendanceVO`

**处理流程**:

1. 从JWT Token获取当前用户ID
2. 校验用户是否存在
3. 校验请假类型是否有效（1-事假，2-病假，3-年假，4-调休）
4. 校验请假时间是否有效（结束时间需大于开始时间）
5. 校验是否与已有请假记录冲突
6. 创建出勤记录，设置状态为请假(5)
7. 设置请假类型、开始时间、结束时间、原因
8. **检查用户角色，调用调度服务重新分配任务**
9. 返回出勤记录

**请假后任务重新分配逻辑**:

1. 根据userId查询用户角色编码（ROLE_DRIVER/ROLE_REPAIRMAN/ROLE_SAFETY_OFFICER）
2. 如果用户无角色或角色无需任务分配，跳过
3. 调用DispatchClient.reassignTasksByUserLeave(userId, roleCode)
4. 调度服务根据角色类型重新分配任务：
   - 司机(ROLE_DRIVER)：重新分配运输任务
   - 维修员(ROLE_REPAIRMAN)：重新分配维修任务
   - 安全员(ROLE_SAFETY_OFFICER)：重新分配安全检查任务

**请假类型枚举**:

| 值 | 名称 | 说明 |
| - | - | - |
| 1 | 事假 | 个人事务请假 |
| 2 | 病假 | 因病请假 |
| 3 | 年假 | 年度休假 |
| 4 | 调休 | 加班调休 |

---

### 5.8 取消请假

**接口**: `DELETE /api/attendance/leave/{attendanceId}`  
**返回**: `Boolean`

**处理流程**:

1. 校验出勤记录是否存在
2. 校验出勤记录状态是否为请假状态
3. 校验请假开始时间是否已过（已开始的请假不可取消）
4. 删除出勤记录
5. 返回操作结果

---

### 5.9 获取请假记录列表

**接口**: `GET /api/attendance/leave/{userId}/list?yearMonth=2024-01`  
**参数**: `yearMonth` (格式 yyyy-MM)  
**VO**: `List<AttendanceVO>`

**处理流程**:

1. 解析年月参数
2. 计算该月的起始和结束日期
3. 根据用户ID、日期范围和请假状态查询出勤记录列表
4. 查询用户姓名
5. 返回请假记录列表

---

## 6. 用户申诉服务逻辑 (UserAppealService)

### 6.1 提交申诉

**接口**: `POST /api/user/appeal`  
**DTO**: `UserAppealDTO(appealReason)`  
**返回**: `String` (申诉ID)

**处理流程**:

1. 校验用户是否存在
2. 校验用户是否已被禁用
3. 校验是否已有待处理的申诉
4. 创建申诉记录
5. 返回申诉ID

---

### 6.2 获取当前用户的申诉列表

**接口**: `GET /api/user/appeal/my`  
**VO**: `List<UserAppealVO>`

**处理流程**:

1. 从Token获取用户ID
2. 查询用户的申诉列表
3. 返回申诉列表

---

### 6.3 检查是否有待处理的申诉

**接口**: `GET /api/user/appeal/pending/check`  
**返回**: `Boolean`

**处理流程**:

1. 从Token获取用户ID
2. 检查是否有待处理的申诉
3. 返回结果

---

### 6.4 获取待处理的申诉列表（管理员）

**接口**: `GET /api/user/appeal/admin/pending`  
**VO**: `List<UserAppealVO>`

**处理流程**:

1. 查询状态为待处理的申诉列表
2. 返回申诉列表

---

### 6.5 获取所有申诉列表（管理员）

**接口**: `GET /api/user/appeal/admin/list`  
**VO**: `List<UserAppealVO>`

**处理流程**:

1. 查询所有申诉列表
2. 返回申诉列表

---

### 6.6 根据用户ID获取申诉列表（管理员）

**接口**: `GET /api/user/appeal/admin/user/{userId}`  
**VO**: `List<UserAppealVO>`

**处理流程**:

1. 根据用户ID查询申诉列表
2. 返回申诉列表

---

### 6.7 处理申诉（管理员）

**接口**: `PUT /api/user/appeal/admin/{id}/handle`  
**DTO**: `HandleAppealDTO(status, adminOpinion, handlerId, handlerName)`  
**返回**: `Boolean`

**处理流程**:

1. 校验申诉是否存在
2. 校验申诉是否已处理
3. 更新申诉状态和处理意见
4. 如果申诉通过，启用用户
5. 返回处理结果

---

## 7. 角色变更申请服务逻辑 (RoleChangeApplyService)

> **说明**：角色变更申请接口位于管理员接口下，路径为 `/api/user/admin/role-change/apply`

### 7.1 创建角色变更申请（用户端）

**接口**: `POST /api/user/admin/role-change/apply`  
**DTO**: `RoleChangeApply(applyRoleId, applyReason)`  
**返回**: `String` (申请ID)

**处理流程**:

1. 获取当前用户信息
2. 校验申请角色是否存在
3. 校验是否已有待处理的申请
4. 创建角色变更申请
5. 返回申请ID

---

### 7.2 获取用户的角色变更申请（用户端）

**接口**: `GET /api/user/admin/role-change/apply/my`  
**VO**: `List<RoleChangeApplyVO>`

**处理流程**:

1. 从Token获取用户ID
2. 查询用户的角色变更申请列表
3. 返回申请列表

---

### 7.3 获取待处理申请（管理员）

**接口**: `GET /api/user/admin/role-change/apply/pending`  
**VO**: `List<RoleChangeApplyVO>`

**处理流程**:

1. 查询状态为待处理的申请列表
2. 返回申请列表

---

### 7.4 获取所有角色变更申请（管理员）

**接口**: `GET /api/user/admin/role-change/apply/list`  
**VO**: `List<RoleChangeApplyVO>`

**处理流程**:

1. 查询所有角色变更申请列表
2. 返回申请列表

---

### 7.5 获取用户的角色变更申请历史（管理员）

**接口**: `GET /api/user/admin/role-change/apply/user/{userId}`  
**VO**: `List<RoleChangeApplyVO>`

**处理流程**:

1. 根据用户ID查询角色变更申请列表
2. 返回申请列表

---

### 7.6 处理角色变更申请（管理员）

**接口**: `PUT /api/user/admin/role-change/apply/{id}/handle`  
**参数**: `status, adminOpinion, handlerId, handlerName`  
**返回**: `Boolean`

**处理流程**:

1. 校验申请是否存在
2. 校验申请是否已处理
3. 更新申请状态和处理意见
4. 如果申请通过，更新用户角色
5. 返回处理结果

---

## 8. 消息服务逻辑 (MessageService)

### 8.1 发送单播消息

**接口**: `POST /api/messages/unicast`  
**DTO**: `MessageDTO(receiver, type, content, priority, businessId, businessType)`  
**返回**: `void`

**处理流程**:

1. 创建消息记录
2. 存储到MongoDB
3. 如果用户在线，通过WebSocket推送消息

---

### 8.2 发送广播消息

**接口**: `POST /api/messages/broadcast`  
**DTO**: `MessageDTO(type, content, priority)`  
**参数**: `userIds`  
**返回**: `void`

**处理流程**:

1. 批量创建消息记录
2. 存储到MongoDB
3. 通过WebSocket推送给在线用户

---

### 8.3 获取用户消息列表

**接口**: `GET /api/messages`  
**参数**: `page, size`  
**VO**: `List<Message>`

**处理流程**:

1. 从Token获取用户ID
2. 分页查询用户消息
3. 返回消息列表

---

### 8.4 标记消息已读

**接口**: `PUT /api/messages/{messageId}/read`  
**返回**: `void`

**处理流程**:

1. 更新消息状态为已读
2. 更新阅读时间

---

### 8.5 获取未读消息数量

**接口**: `GET /api/messages/unread/count`  
**返回**: `long`

**处理流程**:

1. 从Token获取用户ID
2. 统计未读消息数量
3. 返回数量

---

## 9. 管理员初始化服务逻辑 (AdminInitService)

### 9.1 初始化管理员

**触发时机**: 应用启动时  
**配置**: `AdminInitProperties(initadmin, adminUsername, adminPassword, adminRealName, adminPhone, adminEmail)`

**处理流程**:

1. 检查是否开启管理员初始化功能
2. 查找或创建管理员角色（ADMIN）
3. 检查是否已有管理员用户
4. 创建管理员用户，密码加密
5. 设置管理员状态为禁用（需完成实名认证后启用）
6. 分配管理员角色

---

## 10. 司机服务逻辑 (DriverService)

> **说明**：司机服务管理司机信息。司机信息通过实名认证+资格认证被动创建，不可主动创建。
>
> **创建流程**：
>
> 1. 用户完成实名认证（身份证验证）
> 2. 用户上传驾驶证进行资格认证
> 3. 系统自动创建司机记录
>
> **车辆关系**：车辆属于矿山，通过调度动态分配给司机。司机可以有常用车辆（多对多关系）。

### 10.1 获取司机详情

**接口**: `GET /api/driver/{id}`  
**VO**: `DriverVO`

**处理流程**:

1. 根据ID查询司机信息
2. 查询关联的用户信息
3. 查询常用车辆列表
4. 返回司机详情

---

### 10.2 根据用户ID获取司机信息

**接口**: `GET /api/driver/user/{userId}`  
**VO**: `DriverVO`

**处理流程**:

1. 根据用户ID查询司机信息
2. 返回司机信息

---

### 10.3 分页查询司机列表

**接口**: `GET /api/driver/list`  
**参数**: `driverName, status`  
**VO**: `List<DriverVO>`

**处理流程**:

1. 构建查询条件
2. 查询司机列表
3. 查询每个司机关联的用户信息
4. 返回司机列表

---

### 10.4 更新司机状态

**接口**: `PUT /api/driver/{driverId}/status?status=xxx`  
**参数**: `status` (0-离职, 1-在职, 2-休假, 3-停职)  
**返回**: `void`

**处理流程**:

1. 校验司机是否存在
2. 更新司机状态
3. 记录操作日志

---

### 10.5 获取可用司机列表

**接口**: `GET /api/driver/available`  
**VO**: `List<DriverVO>`

**处理流程**:

1. 查询状态为在职(1)的司机列表
2. 排除当天有请假记录的司机（调用考勤服务）
3. 返回司机列表

---

### 10.6 选择最佳司机

**接口**: `POST /api/driver/best`  
**参数**: `vehicleId, scheduledTime`  
**VO**: `DriverVO`

**处理流程**:

1. 获取可用司机列表
2. 如果指定车辆ID，优先选择该车辆的常用司机
3. 根据评分规则计算每个司机的得分：
   - 出勤率得分：近30天出勤率
   - 违规次数扣分：近30天违规次数
   - 驾龄得分：驾龄越长得分越高
4. 返回得分最高的司机

**评分规则**:

- 出勤率得分：出勤率 * 40
- 违规扣分：违规次数 * 5
- 驾龄得分：min(驾龄月数, 60)

---

### 10.7 添加常用车辆

**接口**: `POST /api/driver/{driverId}/common-vehicle?vehicleId=xxx`  
**返回**: `void`

**处理流程**:

1. 校验司机是否存在
2. 校验车辆是否存在
3. 检查是否已是常用车辆
4. 添加司机常用车辆关系记录
5. 如果是第一个常用车辆，设为默认

---

### 10.8 移除常用车辆

**接口**: `DELETE /api/driver/{driverId}/common-vehicle/{vehicleId}`  
**返回**: `void`

**处理流程**:

1. 校验司机是否存在
2. 校验常用车辆关系是否存在
3. 删除常用车辆关系记录
4. 如果移除的是默认车辆，自动设置其他车辆为默认

---

### 10.9 设置默认常用车辆

**接口**: `PUT /api/driver/{driverId}/common-vehicle/{vehicleId}/default`  
**返回**: `void`

**处理流程**:

1. 校验司机是否存在
2. 校验常用车辆关系是否存在
3. 清除当前默认车辆标记
4. 设置指定车辆为默认

---

### 10.10 获取司机常用车辆列表

**接口**: `GET /api/driver/{driverId}/common-vehicles`  
**VO**: `List<DriverVehicleVO>`

**处理流程**:

1. 校验司机是否存在
2. 查询司机的常用车辆列表
3. 返回常用车辆列表（包含使用次数、最后使用时间、是否默认）

---

### 10.11 司机状态枚举

| 编码 | 名称 | 说明 |
| - | - | - |
| 0 | 离职 | 已离职司机 |
| 1 | 在职 | 正常在职司机 |
| 2 | 休假 | 休假中的司机 |
| 3 | 停职 | 停职中的司机 |

---

## 11. 司机出车统计服务逻辑 (DriverTripStatisticsService)

> **说明**：统计司机出车数据，依赖trip模块和warning模块数据

### 11.1 获取司机出车统计

**接口**: `GET /api/driver/trip-statistics/{driverId}`  
**参数**: `startDate, endDate`  
**VO**: `DriverTripStatistics`

**处理流程**:

1. 根据司机ID和日期范围查询trip表
2. 统计总出车次数、成功次数、失败次数
3. 计算成功率
4. 统计总里程、总时长、总载重
5. 查询warning_record表统计预警次数和违规次数
6. 返回统计数据

---

### 11.2 获取日期范围内所有司机统计

**接口**: `GET /api/driver/trip-statistics/range`  
**参数**: `startDate, endDate`  
**VO**: `List<DriverTripStatistics>`

**处理流程**:

1. 查询所有在职司机
2. 逐个获取每个司机的统计数据
3. 返回统计列表

---

### 11.3 获取月度统计

**接口**: `GET /api/driver/trip-statistics/{driverId}/monthly/{yearMonth}`  
**参数**: `yearMonth` (格式 yyyy-MM)  
**VO**: `DriverTripStatistics`

**处理流程**:

1. 解析年月参数
2. 计算该月的起始和结束日期
3. 调用获取司机出车统计接口
4. 返回统计数据

---

## 12. 司机评分服务逻辑 (DriverScoreService)

> **说明**：管理司机评分，支持评分初始化、加分、扣分、警告等

### 12.1 评分规则

| 场景 | 分数变化 | 说明 |
| - | - | - |
| 创建账户 | 基准分 = 10 + min(驾龄, 10) × 3 | 最高40分 |
| 出勤 | +1~3分 | 根据出勤状态加分 |
| 完成调度任务 | +2~5分 | 根据任务完成质量加分 |
| 任务违规 | -5~20分 | 根据违规严重程度扣分 |
| 分数 < 10 | MQ普通警告 | 提醒司机注意 |
| 分数 < 5 | MQ严重警告 | 通知管理员 |
| 盗泻行为 | 分数清零 + 封禁账户 | 严重违规处理 |

### 12.2 初始化司机评分

**方法**: `initDriverScore(driverId, drivingYears)`  
**触发时机**: 司机资格认证通过后自动调用

**处理流程**:

1. 根据驾龄计算基准分数
2. 设置司机score字段
3. 记录日志

---

### 12.3 出勤加分

**方法**: `addScoreForAttendance(driverId, points)`  
**触发时机**: 每日出勤打卡后自动调用

---

### 12.4 完成任务加分

**方法**: `addScoreForTripComplete(driverId, points)`  
**触发时机**: 调度任务完成后自动调用

---

### 12.5 违规扣分

**方法**: `deductScoreForViolation(driverId, points)`  
**触发时机**: 预警模块检测到违规行为后调用

**处理流程**:

1. 扣除分数（不低于0）
2. 检查是否需要发送警告
3. 分数 < 10 发送普通警告
4. 分数 < 5 发送严重警告

---

### 12.6 盗泻处理

**方法**: `handleTheftCase(driverId)`  
**触发时机**: 预警模块检测到盗泻行为后调用

**处理流程**:

1. 分数清零
2. 账户状态设为禁用
3. 发送严重警告通知管理员

---

## 13. 出勤状态枚举

| 编码 | 名称 | 说明 |
| - | - | - |
| 1 | 正常 | 正常出勤 |
| 2 | 迟到 | 迟到打卡 |
| 3 | 早退 | 早退打卡 |
| 4 | 缺勤 | 未打卡 |
| 5 | 请假 | 请假中 |

> **注意**：偏离路线、中途停留过长、偷卸行为、超速行驶等属于预警类型，由预警模块管理。
