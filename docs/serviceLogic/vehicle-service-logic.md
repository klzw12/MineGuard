# 车辆服务逻辑

## 1. 车辆基础服务逻辑 (VehicleService)

### 1.1 创建车辆

**接口**: `POST /api/vehicle`  
**DTO**: `Vehicle(vehicleNo, vehicleType, brand, model, userId)`  
**VO**: `Vehicle`

**处理流程**:

1. 校验车牌号是否已存在
2. 创建车辆记录，默认状态为空闲(0)
3. 返回创建的车辆信息

---

### 1.2 更新车辆

**接口**: `PUT /api/vehicle/{id}`  
**DTO**: `Vehicle`  
**VO**: `Vehicle`

**处理流程**:

1. 校验车辆是否存在
2. 如果修改车牌号，校验车牌号是否已被其他车辆使用
3. 更新车辆信息
4. 返回更新后的车辆信息

---

### 1.3 删除车辆

**接口**: `DELETE /api/vehicle/{id}`  
**返回**: `Boolean`

**处理流程**:

1. 校验车辆是否存在
2. 校验车辆是否绑定用户（绑定中不可删除）
3. 逻辑删除车辆记录
4. 返回删除结果

---

### 1.4 获取车辆详情

**接口**: `GET /api/vehicle/{id}`  
**VO**: `VehicleVO`

**处理流程**:

1. 根据ID查询车辆
2. 查询绑定用户信息
3. 查询保险信息
4. 返回车辆详情

---

### 1.5 分页查询车辆

**接口**: `GET /api/vehicle/page`  
**参数**: `page, size, vehicleNo, status`  
**VO**: `List<VehicleVO>`

**处理流程**:

1. 构建查询条件
2. 支持车牌号模糊查询
3. 支持状态筛选
4. 分页查询车辆列表
5. 查询每个车辆的绑定用户信息
6. 返回分页结果

---

### 1.6 绑定用户（已废弃）

**接口**: `POST /api/vehicle/{id}/bind?userId=xxx`  
**返回**: `Boolean`

**说明**: 车辆属于矿山，通过调度动态分配给司机，不再支持固定绑定用户。

---

### 1.7 解绑用户（已废弃）

**接口**: `POST /api/vehicle/{id}/unbind`  
**返回**: `Boolean`

**说明**: 车辆属于矿山，通过调度动态分配给司机，不再支持固定绑定用户。

---

### 1.8 上传车辆照片

**接口**: `POST /api/vehicle/{id}/photo`  
**请求**: `MultipartFile file`  
**返回**: `String` (照片URL)

**处理流程**:

1. 校验车辆是否存在
2. 上传照片到存储服务
3. 更新车辆照片URL
4. 返回照片URL

---

### 1.9 上传行驶证正面并进行OCR识别

**接口**: `POST /api/vehicle/{id}/license/front`  
**请求**: `MultipartFile file`  
**VO**: `Vehicle`

**处理流程**:

1. 校验车辆是否存在
2. 上传行驶证正面到存储服务
3. 更新行驶证正面URL
4. 调用OCR服务识别行驶证正面信息
5. 解析OCR结果，提取以下字段：
   - 车牌号(plateNumber)
   - 所有人(owner)
   - 住址(address)
   - 品牌型号(brandModel)
   - 车辆型号(vehicleModel)
   - 发动机号(engineNumber)
   - 车架号/ VIN码(vin)
   - 使用性质(useNature)
   - 注册日期(registerDate)
   - 发证日期(issueDate)
6. 更新车辆信息
7. 返回更新后的车辆信息

---

### 1.10 上传行驶证反面

**接口**: `POST /api/vehicle/{id}/license/back`  
**请求**: `MultipartFile file`  
**VO**: `Vehicle`

**处理流程**:

1. 校验车辆是否存在
2. 上传行驶证反面到存储服务
3. 更新行驶证反面URL
4. 调用OCR服务识别行驶证反面信息
5. 解析OCR结果，提取以下字段：
   - 核定载人数(seatingCapacity)
   - 总质量(totalMass)
   - 整备质量(curbWeight)
   - 核定载质量(ratedLoad)
   - 外廓尺寸(dimensions)
   - 备注(remarks)
   - 年检记录(inspectionRecord)
6. 更新车辆信息
7. 返回更新后的车辆信息

---

### 1.11 上传车辆保险信息

**接口**: `POST /api/vehicle/{id}/insurance`  
**参数**: `insuranceCompany, policyNo, startDate, endDate`  
**VO**: `Vehicle`

**处理流程**:

1. 校验车辆是否存在
2. 创建保险记录（调用VehicleInsuranceService）
3. 返回车辆信息

---

### 1.12 更新车辆维修状态

**接口**: `PUT /api/vehicle/{id}/maintenance?maintenanceStatus=xxx`  
**VO**: `Vehicle`

**处理流程**:

1. 校验车辆是否存在
2. 更新车辆状态
3. 返回更新后的车辆信息

---

## 2. 车辆状态服务逻辑 (VehicleStatusService)

### 2.1 获取车辆实时状态

**接口**: `GET /api/vehicle/{id}/status`  
**VO**: `VehicleStatusVO`

**处理流程**:

1. 检查车辆维修状态（是否需要保养）
2. 通过HTTP调用行程服务查询最近行程状态
3. 根据行程状态判断车辆状态：
   - 行程进行中 → 从Redis获取实时位置信息
   - 行程已完成 → 返回离线状态
4. 返回车辆状态信息

**状态枚举**:

- 0: 离线
- 1: 在线
- 2: 行驶中
- 3: 故障
- 4: 维修中

---

### 2.2 更新车辆状态

**接口**: `PUT /api/vehicle/{id}/status`  
**DTO**: `VehicleStatus`  
**VO**: `VehicleStatus`

**处理流程**:

1. 保存状态到数据库
2. 更新Redis缓存（有效期30分钟）
3. 推送状态变更（通过WebSocket）
4. 返回状态信息

---

### 2.3 获取车辆状态历史

**接口**: `GET /api/vehicle/{id}/status/history`  
**参数**: `page, size`  
**VO**: `List<VehicleStatusVO>`

**处理流程**:

1. 根据车辆ID分页查询状态历史
2. 按创建时间倒序排列
3. 返回状态历史列表

---

### 2.4 上报车辆状态

**接口**: `POST /api/vehicle/{id}/status/report`  
**DTO**: `VehicleStatusReportDTO(vehicleId, tripId, longitude, latitude, speed, direction, mileage, fuelLevel, specialStatus, reportTime)`  
**返回**: `void`

**处理流程**:

1. 设置上报时间（如果未提供）
2. 判断是否有特殊状态：
   - 有特殊状态（故障/超速等）→ 设置状态为故障(3)，暂停行程
   - 无特殊状态 → 设置状态为行驶中(2)
3. 存储状态到Redis缓存（有效期30分钟）
4. 存储状态到数据库
5. 推送状态变更（通过WebSocket）

**特殊状态枚举** (VehicleSpecialStatusEnum):

- 0: 正常(NORMAL)
- 1: 超速(OVERSPEED)
- 2: 疲劳驾驶(FATIGUE)
- 3: 偏离路线(DEVIATION)
- 4: 故障(FAULT)

---

## 3. 车辆故障服务逻辑 (VehicleFaultService)

### 3.1 报告故障

**接口**: `POST /api/vehicle/fault`  
**DTO**: `VehicleFaultDTO(vehicleId, faultType, faultDescription, faultDate, severity)`  
**VO**: `VehicleFault`

**处理流程**:

1. 校验车辆是否存在
2. 创建故障记录，默认状态为待处理(1)
3. 如果故障严重程度为紧急(4)，更新车辆状态为故障(3)
4. 返回故障记录

**故障状态枚举**:

- 0: 待处理
- 1: 处理中
- 2: 已解决
- 3: 已关闭

**严重程度枚举**:

- 1: 轻微
- 2: 中等
- 3: 严重
- 4: 紧急

---

### 3.2 处理故障

**接口**: `PUT /api/vehicle/fault/{id}/handle`  
**参数**: `repairmanId, repairContent, repairCost`  
**VO**: `VehicleFault`

**处理流程**:

1. 校验故障记录是否存在
2. 更新维修员ID
3. 更新维修内容和费用
4. 更新维修日期
5. 更新状态为已解决(3)
6. 如果车辆没有其他未解决故障，更新车辆状态为空闲(0)
7. 返回更新后的故障记录

---

### 3.3 获取车辆故障记录

**接口**: `GET /api/vehicle/fault/vehicle/{vehicleId}`  
**参数**: `status, page, size`  
**VO**: `List<VehicleFault>`

**处理流程**:

1. 根据车辆ID查询故障记录
2. 支持按状态筛选
3. 分页返回故障记录列表

---

## 4. 车辆保险服务逻辑 (VehicleInsuranceService)

### 4.1 添加保险信息

**接口**: `POST /api/vehicle/insurance`  
**DTO**: `VehicleInsuranceDTO(vehicleId, insuranceCompany, insuranceNumber, insuranceType, insuranceAmount, startDate, expiryDate, remark)`  
**VO**: `VehicleInsurance`

**处理流程**:

1. 校验车辆是否存在
2. 判断保险状态：
   - 当前日期 > 到期日期 → 状态为过期(2)
   - 当前日期 <= 到期日期 → 状态为有效(1)
3. 创建保险记录
4. 返回保险信息

**保险类型枚举**:

- 1: 交强险
- 2: 商业险
- 3: 第三者责任险
- 4: 车辆损失险
- 5: 盗抢险

**保险状态枚举**:

- 0: 未投保
- 1: 已投保/有效
- 2: 已过期
- 3: 理赔中

---

### 4.2 获取车辆保险信息

**接口**: `GET /api/vehicle/insurance/vehicle/{vehicleId}`  
**VO**: `List<VehicleInsurance>`

**处理流程**:

1. 根据车辆ID查询所有保险记录
2. 返回保险列表

---

### 4.3 获取当前有效保险信息

**接口**: `GET /api/vehicle/insurance/vehicle/{vehicleId}/current`  
**VO**: `VehicleInsurance`

**处理流程**:

1. 根据车辆ID查询有效的保险记录
2. 按到期日期排序，返回最近到期的保险
3. 返回保险信息

---

## 5. 车辆保养服务逻辑 (VehicleMaintenanceService)

### 5.1 添加保养记录

**接口**: `POST /api/vehicle/maintenance`  
**DTO**: `VehicleMaintenanceDTO(vehicleId, maintenanceType, maintenanceDate, maintenanceContent, maintenanceCost, repairmanId, nextMaintenanceDate, mileage, remark)`  
**VO**: `VehicleMaintenance`

**处理流程**:

1. 校验车辆是否存在
2. 创建保养记录
3. 返回保养信息

**保养类型枚举**:

- 1: 常规保养
- 2: 小修
- 3: 中修
- 4: 大修
- 5: 年检

---

### 5.2 获取车辆保养记录

**接口**: `GET /api/vehicle/maintenance/vehicle/{vehicleId}`  
**参数**: `page, size`  
**VO**: `List<VehicleMaintenance>`

**处理流程**:

1. 根据车辆ID分页查询保养记录
2. 按保养日期倒序排列
3. 返回保养记录列表

---

### 5.3 获取下次保养信息

**接口**: `GET /api/vehicle/maintenance/vehicle/{vehicleId}/next`  
**VO**: `VehicleMaintenance`

**处理流程**:

1. 根据车辆ID查询下次保养日期不为空的记录
2. 按下次保养日期倒序排列，取最新一条
3. 返回保养信息

---

## 6. 车辆加油服务逻辑 (VehicleRefuelingService)

### 6.1 添加加油记录

**接口**: `POST /api/vehicle/refueling`  
**DTO**: `VehicleRefuelingDTO(vehicleId, driverId, refuelingDate, fuelType, fuelAmount, fuelPrice, totalCost, mileage, gasStation, remark)`  
**VO**: `VehicleRefueling`

**处理流程**:

1. 校验车辆是否存在
2. 校验司机是否存在
3. 创建加油记录
4. 返回加油信息

---

### 6.2 获取车辆加油记录

**接口**: `GET /api/vehicle/refueling/vehicle/{vehicleId}`  
**参数**: `page, size`  
**VO**: `List<VehicleRefueling>`

**处理流程**:

1. 根据车辆ID分页查询加油记录
2. 按加油日期倒序排列
3. 返回加油记录列表

---

## 7. 车辆状态推送服务逻辑 (VehicleStatusPushService)

### 7.1 推送状态变更

**方法**: `pushStatusChange(Long vehicleId, VehicleStatusVO statusVO)`  
**返回**: `void`

**处理流程**:

1. 构建状态变更消息
2. 通过WebSocket推送给订阅该车辆的用户
3. 记录推送日志

---

## 附录

### A. 表结构与实体类对应关系

| 表名 | 实体类 | 说明 |
| - | - | - |
| vehicle | Vehicle | 车辆基础信息 |
| vehicle_status | VehicleStatus | 车辆实时状态 |
| vehicle_fault | VehicleFault | 车辆故障记录 |
| vehicle_insurance | VehicleInsurance | 车辆保险信息 |
| vehicle_maintenance | VehicleMaintenance | 车辆保养记录 |
| vehicle_refueling | VehicleRefueling | 车辆加油记录 |

### B. 需求文档功能覆盖

| 需求功能 | 实现状态 | 说明 |
| - | - | - |
| **3.3 车辆管理模块** | | |
| 车辆信息管理（车牌号、品牌型号、绑定司机） | ✅ 已实现 | Vehicle表及服务 |
| 车辆状态管理（在线、离线、行驶中、故障、维修中） | ✅ 已实现 | VehicleStatus表及服务 |
| 车辆保险管理（保险公司、保单号、到期日期） | ✅ 已实现 | VehicleInsurance表及服务 |
| 车辆维护管理（保养记录、维修记录） | ✅ 已实现 | VehicleMaintenance、VehicleFault表及服务 |
| 车辆加油管理（加油记录、油耗统计） | ✅ 已实现 | VehicleRefueling表及服务 |
| 行驶证OCR识别 | ✅ 已实现 | 上传行驶证自动填充车辆信息 |

### C. 已修复的问题

1. **Vehicle实体类userId字段已移除**：车辆属于矿山，通过调度动态分配，不再绑定用户。

2. **VehicleRefueling实体类字段已统一**：
   - 实体类与SQL表字段一致：`refuelingStation`、`refuelingAmount`、`unitPrice`

3. **VehicleMaintenance实体类字段已补全**：
   - 新增：`maintenanceShop`、`maintenanceResult`、`nextMaintenanceMileage`
   - 类型修正：`mileage` 改为 Integer

4. **VehicleStatus实体类字段已补全**：
   - 新增：`statusTime`、`altitude`

5. **Vehicle表新增fuel_level字段**：用于缓存当前油量百分比

### D. 功能完整性确认

| 需求功能 | 实现状态 | 说明 |
|---------|---------|------|
| 车辆信息管理 | ✅ 已实现 | CRUD、行驶证OCR识别 |
| 车辆状态管理 | ✅ 已实现 | 实时状态上报、历史查询 |
| 车辆保险管理 | ✅ 已实现 | 添加、查询、当前有效保险 |
| 车辆维护管理 | ✅ 已实现 | 保养记录、下次保养提醒 |
| 车辆故障管理 | ✅ 已实现 | 报告、处理、查询 |
| 车辆加油管理 | ✅ 已实现 | 加油记录、油耗统计 |
| 最佳车辆选择 | ✅ 已实现 | 按油量、载重评分推荐 |

**车辆模块已定档** ✅
