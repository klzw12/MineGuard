# 行程服务逻辑

## 1. 模块概述

行程服务负责管理车辆行程和轨迹数据，提供行程创建、状态管理、实时轨迹上传、历史轨迹查询等功能。

**核心职责**：
- 行程生命周期管理（创建、开始、结束、取消）
- 实时轨迹数据存储
- 历史轨迹数据持久化
- 行程统计（里程、时长、油耗等）
- 行程通知管理

**注意**：车辆运行中的异常检测（超速、偏航、长时间停留等）由**预警模块**负责，trip模块仅负责轨迹数据的存储和查询。

---

## 2. 行程服务逻辑 (TripService)

### 1.1 创建行程

**接口**: `POST /api/trip`  
**DTO**: `TripDTO(vehicleId, driverId, routeId, startLocation, endLocation, startLongitude, startLatitude, endLongitude, endLatitude, estimatedStartTime, estimatedEndTime, tripType)`  
**返回**: `Long` (行程ID)

**处理流程**:

1. 验证车辆是否存在（调用VehicleServiceClient）
2. 验证司机是否存在（调用UserServiceClient）
3. 生成行程编号（格式：TRIP + 时间戳 + 随机码）
4. 计算预计里程和时长（基于起终点坐标）
5. 设置初始状态为待开始(0)
6. 保存行程记录
7. 触发状态变更处理
8. 返回行程ID

---

### 1.2 更新行程

**接口**: `PUT /api/trip/{id}`  
**DTO**: `TripDTO`  
**返回**: `void`

**处理流程**:

1. 校验行程是否存在
2. 校验行程状态是否为待开始（只有待开始的行程可修改）
3. 验证车辆和司机
4. 更新行程信息
5. 返回结果

---

### 1.3 删除行程

**接口**: `DELETE /api/trip/{id}`  
**返回**: `void`

**处理流程**:

1. 校验行程是否存在
2. 校验行程状态（进行中的行程不能删除）
3. 删除行程记录
4. 返回结果

---

### 1.4 获取行程详情

**接口**: `GET /api/trip/{id}`  
**VO**: `TripVO`

**处理流程**:

1. 根据ID查询行程
2. 查询车辆信息（车牌号）
3. 查询司机信息（姓名）
4. 返回行程详情

---

### 1.5 分页查询行程

**接口**: `GET /api/trip/page`  
**参数**: `page, size`  
**VO**: `PageResult<TripVO>`

**处理流程**:

1. 构建分页查询条件
2. 按创建时间倒序排列
3. 分页查询行程列表
4. 查询每个行程的车辆和司机信息
5. 返回分页结果

---

### 1.6 开始行程

**接口**: `POST /api/trip/{id}/start`  
**返回**: `void`

**处理流程**:

1. 校验行程是否存在
2. 校验行程状态（只有待开始或已接单的行程可以开始）
3. 更新状态为进行中(2)
4. 设置实际开始时间
5. 触发状态变更处理
6. 返回结果

---

### 1.7 结束行程

**接口**: `POST /api/trip/{id}/end`  
**参数**: `endLongitude, endLatitude`  
**返回**: `void`

**处理流程**:

1. 校验行程是否存在
2. 校验行程状态（只有进行中的行程可以结束）
3. 更新状态为已完成(3)
4. 设置实际结束时间和终点坐标
5. 计算实际里程和时长
6. 批量写入轨迹点到MongoDB
7. 删除Redis中的轨迹数据
8. 触发状态变更处理
9. 返回结果

---

### 1.8 接单

**接口**: `POST /api/trip/{id}/accept`  
**返回**: `void`

**处理流程**:

1. 校验行程是否存在
2. 校验行程状态（只有待开始的行程可以接单）
3. 更新状态为已接单(1)
4. 触发状态变更处理
5. 返回结果

---

### 1.9 暂停行程

**接口**: `POST /api/trip/{id}/pause`  
**返回**: `void`

**处理流程**:

1. 校验行程是否存在
2. 校验行程状态（只有进行中的行程可以暂停）
3. 更新状态为暂停中(5)
4. 触发状态变更处理
5. 返回结果

---

### 1.10 恢复行程

**接口**: `POST /api/trip/{id}/resume`  
**返回**: `void`

**处理流程**:

1. 校验行程是否存在
2. 校验行程状态（只有暂停中的行程可以恢复）
3. 更新状态为进行中(2)
4. 触发状态变更处理
5. 返回结果

---

### 1.11 按车辆ID查询行程

**接口**: `GET /api/trip/vehicle/{vehicleId}`  
**VO**: `List<TripVO>`

**处理流程**:

1. 根据车辆ID查询行程列表
2. 按创建时间倒序排列
3. 返回行程列表

---

### 1.12 按司机ID查询行程

**接口**: `GET /api/trip/driver/{driverId}`  
**VO**: `List<TripVO>`

**处理流程**:

1. 根据司机ID查询行程列表
2. 按创建时间倒序排列
3. 返回行程列表

---

### 1.13 获取行程统计

**接口**: `GET /api/trip/{id}/statistics`  
**VO**: `TripStatisticsVO`

**处理流程**:

1. 校验行程是否存在
2. 计算行程时长
3. 获取预计里程
4. 计算实际里程（基于轨迹点）
5. 计算平均速度
6. 返回统计数据

**行程状态枚举** (TripStatusEnum):

- 0: 待开始(PENDING)
- 1: 已接单(ACCEPTED)
- 2: 进行中(IN_PROGRESS)
- 3: 已完成(COMPLETED)
- 4: 已取消(CANCELLED)
- 5: 暂停中(PAUSED)

---

## 2. 路线服务逻辑 (RouteService)

### 2.1 创建路线

**接口**: `POST /api/route`  
**DTO**: `RouteDTO(routeName, startLocation, endLocation, startLongitude, startLatitude, endLongitude, endLatitude, distance, estimatedDuration, waypoints)`  
**返回**: `Long` (路线ID)

**处理流程**:

1. 校验路线名称是否已存在
2. 创建路线记录，默认状态为启用(1)
3. 返回路线ID

---

### 2.2 更新路线

**接口**: `PUT /api/route/{id}`  
**DTO**: `RouteDTO`  
**返回**: `void`

**处理流程**:

1. 校验路线是否存在
2. 如果修改名称，校验名称是否已被使用
3. 更新路线信息
4. 返回结果

---

### 2.3 删除路线

**接口**: `DELETE /api/route/{id}`  
**返回**: `void`

**处理流程**:

1. 校验路线是否存在
2. 删除路线记录
3. 返回结果

---

### 2.4 获取路线详情

**接口**: `GET /api/route/{id}`  
**VO**: `RouteVO`

**处理流程**:

1. 根据ID查询路线
2. 返回路线信息

---

### 2.5 分页查询路线

**接口**: `GET /api/route/page`  
**参数**: `page, size`  
**VO**: `PageResult<RouteVO>`

**处理流程**:

1. 构建分页查询条件
2. 按创建时间倒序排列
3. 返回分页结果

---

### 2.6 获取所有启用路线

**接口**: `GET /api/route/list`  
**VO**: `List<RouteVO>`

**处理流程**:

1. 查询状态为启用(1)的路线
2. 按创建时间倒序排列
3. 返回路线列表

---

### 2.7 启用路线

**接口**: `PUT /api/route/{id}/enable`  
**返回**: `void`

**处理流程**:

1. 校验路线是否存在
2. 更新状态为启用(1)
3. 返回结果

---

### 2.8 禁用路线

**接口**: `PUT /api/route/{id}/disable`  
**返回**: `void`

**处理流程**:

1. 校验路线是否存在
2. 更新状态为禁用(2)
3. 返回结果

---

## 3. 轨迹服务逻辑 (TripTrackService)

> **说明**：轨迹数据采用Redis+MongoDB双层存储架构
> - Redis：存储实时轨迹数据，高并发写入
> - MongoDB：存储历史轨迹数据，行程结束时批量写入

### 3.1 上传轨迹点

**接口**: `POST /api/trip/track`  
**DTO**: `TripTrackDTO(tripId, vehicleId, longitude, latitude, speed, direction, altitude, recordTime)`  
**返回**: `void`

**处理流程**:

1. 校验行程状态（只有进行中的行程可上传轨迹）
2. 将轨迹点存储到Redis（Key: trip:track:{tripId}）
3. 检查是否需要触发预警（如超速）
4. 返回结果

---

### 3.2 批量上传轨迹点

**接口**: `POST /api/trip/track/batch`  
**DTO**: `List<TripTrackDTO>`  
**返回**: `void`

**处理流程**:

1. 校验行程状态
2. 批量将轨迹点存储到Redis
3. 检查是否需要触发预警
4. 返回结果

---

### 3.3 获取行程轨迹

**接口**: `GET /api/trip/track/{tripId}`  
**VO**: `List<TripTrackVO>`

**处理流程**:

1. 优先从Redis获取轨迹数据
2. 如果Redis没有，从MongoDB查询
3. 按记录时间升序排列
4. 返回轨迹列表

---

### 3.4 获取最新轨迹点

**接口**: `GET /api/trip/track/{tripId}/latest`  
**VO**: `TripTrackVO`

**处理流程**:

1. 优先从Redis获取最新轨迹点
2. 如果Redis没有，从MongoDB查询
3. 返回最新轨迹点

---

### 3.5 批量写入轨迹到MongoDB

**方法**: `batchSaveTracks(List<TripTrackDTO> dtoList)`  
**触发时机**: 行程结束时

**处理流程**:

1. 将DTO转换为MongoDB文档
2. 设置创建时间
3. 批量保存到MongoDB
4. 删除Redis中的轨迹数据

---

## 4. 调度计划服务逻辑 (DispatchPlanService)

### 4.1 创建调度计划

**接口**: `POST /api/dispatch/plan`  
**DTO**: `DispatchPlanDTO(planDate, vehicleId, driverId, routeId, plannedTrips, plannedCargoWeight, startTimeSlot, remark)`  
**返回**: `Long` (计划ID)

**处理流程**:

1. 验证车辆是否存在
2. 验证司机是否存在
3. 生成计划编号（格式：DP + 日期 + 随机码）
4. 设置初始状态为待执行(1)
5. 保存调度计划
6. 返回计划ID

---

### 4.2 更新调度计划

**接口**: `PUT /api/dispatch/plan/{id}`  
**DTO**: `DispatchPlanDTO`  
**返回**: `void`

**处理流程**:

1. 校验计划是否存在
2. 校验计划状态（只有待执行的计划可修改）
3. 验证车辆和司机
4. 更新计划信息
5. 返回结果

---

### 4.3 删除调度计划

**接口**: `DELETE /api/dispatch/plan/{id}`  
**返回**: `void`

**处理流程**:

1. 校验计划是否存在
2. 校验计划状态（执行中的计划不能删除）
3. 删除计划记录
4. 返回结果

---

### 4.4 获取调度计划详情

**接口**: `GET /api/dispatch/plan/{id}`  
**VO**: `DispatchPlanVO`

**处理流程**:

1. 根据ID查询计划
2. 返回计划信息

---

### 4.5 分页查询调度计划

**接口**: `GET /api/dispatch/plan/page`  
**参数**: `page, size`  
**VO**: `PageResult<DispatchPlanVO>`

**处理流程**:

1. 构建分页查询条件
2. 按创建时间倒序排列
3. 返回分页结果

---

### 4.6 按日期查询调度计划

**接口**: `GET /api/dispatch/plan/date/{date}`  
**参数**: `date` (格式 yyyy-MM-dd)  
**VO**: `List<DispatchPlanVO>`

**处理流程**:

1. 根据日期查询计划列表
2. 按开始时段升序排列
3. 返回计划列表

---

### 4.7 执行调度计划

**接口**: `POST /api/dispatch/plan/{id}/execute`  
**返回**: `void`

**处理流程**:

1. 校验计划是否存在
2. 校验计划状态（只有待执行的计划可执行）
3. 更新状态为执行中(2)
4. 返回结果

---

### 4.8 完成调度计划

**接口**: `POST /api/dispatch/plan/{id}/complete`  
**返回**: `void`

**处理流程**:

1. 校验计划是否存在
2. 更新状态为已完成(3)
3. 返回结果

**调度计划状态枚举**:

- 1: 待执行
- 2: 执行中
- 3: 已完成

---

## 5. 行程通知服务逻辑 (TripNotificationService)

### 5.1 创建通知

**接口**: `POST /api/trip/notification`  
**DTO**: `TripNotificationDTO(tripId, userId, notificationType, notificationContent)`  
**返回**: `Long` (通知ID)

**处理流程**:

1. 创建通知记录
2. 设置状态为未读
3. 返回通知ID

---

### 5.2 获取用户通知列表

**接口**: `GET /api/trip/notification/user/{userId}`  
**VO**: `List<TripNotificationVO>`

**处理流程**:

1. 根据用户ID查询通知列表
2. 按创建时间倒序排列
3. 返回通知列表

---

### 5.3 标记通知已读

**接口**: `PUT /api/trip/notification/{id}/read`  
**返回**: `void`

**处理流程**:

1. 更新通知状态为已读
2. 设置读取时间
3. 返回结果

---

## 附录

### A. 表结构与实体类对应关系

| 表名 | 实体类 | 说明 |
| - | - | - |
| trip | Trip | 行程记录 |
| route | Route | 路线信息 |
| trip_track | TripTrack | 行程轨迹（MySQL备份） |
| trip_notification | TripNotification | 行程通知 |
| dispatch_plan | DispatchPlan | 调度计划 |

### B. MongoDB文档

| 集合名 | 文档类 | 说明 |
| - | - | - |
| trip_track | TripTrackDocument | 历史轨迹数据存储 |

### C. Redis缓存设计

| Key格式 | 说明 | 有效期 |
| - | - | - |
| trip:track:{tripId} | 实时轨迹数据列表 | 行程期间有效 |

### D. 需求文档功能覆盖

| 需求功能 | 实现状态 | 说明 |
| - | - | - |
| **3.3 调度管理模块** | | |
| 调度计划管理（日/周/月运输计划） | ✅ 已实现 | DispatchPlan表及服务 |
| 智能调度规划（动态调整） | ⚠️ 部分实现 | 需结合AI模块完善 |
| 车辆实时监控 | ✅ 已实现 | TripTrack实时轨迹 |
| **3.4 路径监管模块** | | |
| 车辆路径规划 | ✅ 已实现 | Route表及服务 |
| 车辆实时定位 | ✅ 已实现 | TripTrack实时轨迹 |
| 车辆轨迹回放 | ✅ 已实现 | MongoDB历史轨迹查询 |

### E. 发现的问题

1. **Route实体类与SQL表字段不一致**：
   - 实体类有`status`字段，但SQL表中没有定义
   - **建议**：在trip.sql的route表中添加status字段

2. **trip_track表未使用**：
   - SQL定义了trip_track表，但实际使用MongoDB存储
   - **建议**：保留作为备份或移除

3. **DispatchPlan表位置**：
   - DispatchPlan实体类在trip模块，但表定义在dispatch.sql
   - **建议**：确认模块职责划分

### F. 改进建议

1. **Route表添加status字段**：
   ```sql
   ALTER TABLE `route` ADD COLUMN `status` INT DEFAULT 1 COMMENT '状态：1-启用，2-禁用' AFTER `waypoints`;
   ```

2. **行程状态上报逻辑**：
   - 建议将车辆状态上报逻辑从vehicle模块移至trip模块
   - 行程开始时上报车辆状态为"运行中"
   - 行程结束时上报车辆状态为"空闲"
   - 行程中定期上报位置、速度、油量等

3. **预警触发机制**：
   - 当前只检查超速（>80km/h）
   - 建议增加：偏离路线、长时间停留、疲劳驾驶等检测
