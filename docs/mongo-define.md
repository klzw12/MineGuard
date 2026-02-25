# MineGuard MongoDB 集合定义

## 1. 数据库命名规范

### 1.1 数据库名称

```txt
mineguard
```

### 1.2 集合命名规范

```txt
{module}_{entity}
```

**命名规则**：

- 模块：`user`、`vehicle`、`trip`、`warning`、`statistics`、`cost`
- 实体：具体业务实体名
- 全部小写，使用下划线分隔

**集合示例**：

- `user_profile`：用户资料
- `vehicle_trajectory`：车辆轨迹
- `trip_history`：行程历史
- `warning_event`：预警事件
- `operation_log`：操作日志
- `device_data`：设备数据
- `message_history`：消息历史

---

## 2. 用户模块 (user)

### 2.1 用户资料集合 (user_profile)

| 字段名 | 数据类型 | 索引 | 必填 | 描述 |
| ------ | ------- | ---- | ---- | ---- |
| _id | ObjectId | PRIMARY | 是 | 文档ID |
| userId | Long | INDEXED | 是 | 用户ID |
| username | String | INDEXED | 是 | 用户名 |
| profile | Object | - | 否 | 用户资料 |
| preferences | Object | - | 否 | 用户偏好设置 |
| lastLoginTime | Date | INDEXED | 否 | 最后登录时间 |
| createTime | Date | INDEXED | 是 | 创建时间 |
| updateTime | Date | - | 是 | 更新时间 |

**索引定义**：

```javascript
// 用户ID索引
db.user_profile.createIndex({ "userId": 1 }, { unique: true })

// 用户名索引
db.user_profile.createIndex({ "username": 1 }, { unique: true })

// 最后登录时间索引
db.user_profile.createIndex({ "lastLoginTime": -1 })

// 创建时间索引
db.user_profile.createIndex({ "createTime": -1 })
```

**数据示例**：

```json
{
  "_id": ObjectId("65a1b2c3d4e5f6a7b8c9d0e1f2a3b4"),
  "userId": 1001,
  "username": "admin",
  "profile": {
    "name": "管理员",
    "phone": "13800138000",
    "email": "admin@mineguard.com",
    "avatar": "https://cdn.mineguard.com/avatar/1001.jpg",
    "department": "调度中心"
  },
  "preferences": {
    "language": "zh-CN",
    "theme": "dark",
    "notification": {
      "email": true,
      "sms": true,
      "push": true
    }
  },
  "lastLoginTime": ISODate("2024-01-15T10:30:00Z"),
  "createTime": ISODate("2024-01-01T08:00:00Z"),
  "updateTime": ISODate("2024-01-15T10:30:00Z")
}
```

---

## 3. 车辆模块 (vehicle)

### 3.1 车辆轨迹集合 (vehicle_trajectory)

| 字段名 | 数据类型 | 索引 | 必填 | 描述 |
| ------ | ------- | ---- | ---- | ---- |
| _id | ObjectId | PRIMARY | 是 | 文档ID |
| carId | Long | INDEXED | 是 | 车辆ID |
| timestamp | Date | INDEXED | 是 | 时间戳 |
| location | GeoJSON2DSphere | 2DSPHERE | 是 | 地理位置 |
| speed | Double | - | 否 | 速度（km/h） |
| direction | Double | - | 否 | 方向（度） |
| fuelLevel | Double | - | 否 | 油量（%） |
| status | String | INDEXED | 否 | 状态 |
| odometer | Double | - | 否 | 里程表读数 |
| engineStatus | Object | - | 否 | 发动机状态 |
| metadata | Object | - | 否 | 元数据 |

**索引定义**：

```javascript
// 车辆ID索引
db.vehicle_trajectory.createIndex({ "carId": 1 })

// 时间索引
db.vehicle_trajectory.createIndex({ "timestamp": -1 })

// 地理空间索引
db.vehicle_trajectory.createIndex({ "location": "2dsphere" })

// 复合索引（车辆ID + 时间）
db.vehicle_trajectory.createIndex({ "carId": 1, "timestamp": -1 })

// TTL索引（6个月后自动删除）
db.vehicle_trajectory.createIndex({ "timestamp": 1 }, { expireAfterSeconds: 15552000 })
```

**数据示例**：

```json
{
  "_id": ObjectId("65a1b2c3d4e5f6a7b8c9d0e1f2a3b4"),
  "carId": 1001,
  "timestamp": ISODate("2024-01-15T10:30:00Z"),
  "location": {
    "type": "Point",
    "coordinates": [114.123456, 30.654321]
  },
  "speed": 45.5,
  "direction": 180.0,
  "fuelLevel": 75.2,
  "status": "running",
  "odometer": 123456.7,
  "engineStatus": {
    "temperature": 85.5,
    "rpm": 2500,
    "load": 60.5
  },
  "metadata": {
    "driverId": 2001,
    "tripId": 3001,
    "source": "gps"
  }
}
```

### 3.2 车辆维护记录集合 (vehicle_maintenance)

| 字段名 | 数据类型 | 索引 | 必填 | 描述 |
| ------ | ------- | ---- | ---- | ---- |
| _id | ObjectId | PRIMARY | 是 | 文档ID |
| maintenanceId | Long | INDEXED | 是 | 维护ID |
| carId | Long | INDEXED | 是 | 车辆ID |
| maintenanceType | String | INDEXED | 是 | 维护类型 |
| description | String | - | 否 | 维护描述 |
| cost | Double | - | 是 | 维护费用 |
| mileage | Double | - | 是 | 维护时里程 |
| maintenanceTime | Date | INDEXED | 是 | 维护时间 |
| operatorId | Long | INDEXED | 否 | 操作员ID |
| createTime | Date | INDEXED | 是 | 创建时间 |

**索引定义**：

```javascript
// 维护ID索引
db.vehicle_maintenance.createIndex({ "maintenanceId": 1 }, { unique: true })

// 车辆ID索引
db.vehicle_maintenance.createIndex({ "carId": 1 })

// 维护类型索引
db.vehicle_maintenance.createIndex({ "maintenanceType": 1 })

// 维护时间索引
db.vehicle_maintenance.createIndex({ "maintenanceTime": -1 })

// 复合索引（车辆ID + 维护时间）
db.vehicle_maintenance.createIndex({ "carId": 1, "maintenanceTime": -1 })
```

**数据示例**：

```json
{
  "_id": ObjectId("65a1b2c3d4e5f6a7b8c9d0e1f2a3b4"),
  "maintenanceId": 4001,
  "carId": 1001,
  "maintenanceType": "oil_change",
  "description": "更换机油",
  "cost": 500.00,
  "mileage": 120000.5,
  "maintenanceTime": ISODate("2024-01-15T10:30:00Z"),
  "operatorId": 1001,
  "createTime": ISODate("2024-01-15T10:30:00Z")
}
```

---

## 4. 行程模块 (trip)

### 4.1 行程历史集合 (trip_history)

| 字段名 | 数据类型 | 索引 | 必填 | 描述 |
| ------ | ------- | ---- | ---- | ---- |
| _id | ObjectId | PRIMARY | 是 | 文档ID |
| tripId | Long | INDEXED | 是 | 行程ID |
| carId | Long | INDEXED | 是 | 车辆ID |
| driverId | Long | INDEXED | 是 | 司机ID |
| routeId | Long | - | 否 | 路线ID |
| startTime | Date | INDEXED | 是 | 开始时间 |
| endTime | Date | INDEXED | 否 | 结束时间 |
| startPoint | Object | - | 是 | 起点 |
| endPoint | Object | - | 是 | 终点 |
| distance | Double | - | 否 | 距离（km） |
| loadWeight | Double | - | 否 | 载重（吨） |
| status | String | INDEXED | 是 | 状态 |
| routePoints | Array | - | 否 | 路线点集合 |
| summary | Object | - | 否 | 行程汇总 |
| createTime | Date | INDEXED | 是 | 创建时间 |
| updateTime | Date | - | 是 | 更新时间 |

**索引定义**：

```javascript
// 行程ID索引
db.trip_history.createIndex({ "tripId": 1 }, { unique: true })

// 车辆ID索引
db.trip_history.createIndex({ "carId": 1 })

// 司机ID索引
db.trip_history.createIndex({ "driverId": 1 })

// 开始时间索引
db.trip_history.createIndex({ "startTime": -1 })

// 状态索引
db.trip_history.createIndex({ "status": 1 })

// 复合索引（车辆ID + 开始时间）
db.trip_history.createIndex({ "carId": 1, "startTime": -1 })

// TTL索引（1年后自动删除）
db.trip_history.createIndex({ "createTime": 1 }, { expireAfterSeconds: 31536000 })
```

**数据示例**：

```json
{
  "_id": ObjectId("65a1b2c3d4e5f6a7b8c9d0e1f2a3b4"),
  "tripId": 3001,
  "carId": 1001,
  "driverId": 2001,
  "routeId": 5001,
  "startTime": ISODate("2024-01-15T08:00:00Z"),
  "endTime": ISODate("2024-01-15T12:30:00Z"),
  "startPoint": {
    "name": "武汉市江夏区",
    "location": {
      "type": "Point",
      "coordinates": [114.123456, 30.654321]
    }
  },
  "endPoint": {
    "name": "宜昌市西陵区",
    "location": {
      "type": "Point",
      "coordinates": [111.234567, 30.765432]
    }
  },
  "distance": 125.5,
  "loadWeight": 25.0,
  "status": "completed",
  "routePoints": [
    {
      "timestamp": ISODate("2024-01-15T08:00:00Z"),
      "location": {
        "type": "Point",
        "coordinates": [114.123456, 30.654321]
      }
    },
    {
      "timestamp": ISODate("2024-01-15T09:00:00Z"),
      "location": {
        "type": "Point",
        "coordinates": [114.234567, 30.765432]
      }
    }
  ],
  "summary": {
    "duration": 270,
    "avgSpeed": 45.5,
    "maxSpeed": 65.0,
    "fuelConsumption": 35.5
  },
  "createTime": ISODate("2024-01-15T08:00:00Z"),
  "updateTime": ISODate("2024-01-15T12:30:00Z")
}
```

---

## 5. 预警模块 (warning)

### 5.1 预警事件集合 (warning_event)

| 字段名 | 数据类型 | 索引 | 必填 | 描述 |
| ------ | ------- | ---- | ---- | ---- |
| _id | ObjectId | PRIMARY | 是 | 文档ID |
| warningId | Long | INDEXED | 是 | 预警ID |
| carId | Long | INDEXED | 是 | 车辆ID |
| driverId | Long | INDEXED | 否 | 司机ID |
| warningType | String | INDEXED | 是 | 预警类型 |
| warningLevel | String | INDEXED | 是 | 预警级别 |
| warningTime | Date | INDEXED | 是 | 预警时间 |
| location | GeoJSON2DSphere | 2DSPHERE | 否 | 预警位置 |
| description | String | - | 否 | 预警描述 |
| details | Object | - | 否 | 详细信息 |
| status | String | INDEXED | 是 | 状态 |
| handleInfo | Object | - | 否 | 处理信息 |
| createTime | Date | INDEXED | 是 | 创建时间 |
| updateTime | Date | - | 是 | 更新时间 |

**索引定义**：

```javascript
// 预警ID索引
db.warning_event.createIndex({ "warningId": 1 }, { unique: true })

// 车辆ID索引
db.warning_event.createIndex({ "carId": 1 })

// 司机ID索引
db.warning_event.createIndex({ "driverId": 1 })

// 预警类型索引
db.warning_event.createIndex({ "warningType": 1 })

// 预警级别索引
db.warning_event.createIndex({ "warningLevel": 1 })

// 预警时间索引
db.warning_event.createIndex({ "warningTime": -1 })

// 状态索引
db.warning_event.createIndex({ "status": 1 })

// 复合索引（车辆ID + 预警时间）
db.warning_event.createIndex({ "carId": 1, "warningTime": -1 })

// TTL索引（1年后自动删除）
db.warning_event.createIndex({ "warningTime": 1 }, { expireAfterSeconds: 31536000 })
```

**数据示例**：

```json
{
  "_id": ObjectId("65a1b2c3d4e5f6a7b8c9d0e1f2a3b4"),
  "warningId": 5001,
  "carId": 1001,
  "driverId": 2001,
  "warningType": "speed_exceed",
  "warningLevel": "high",
  "warningTime": ISODate("2024-01-15T10:30:00Z"),
  "location": {
    "type": "Point",
    "coordinates": [114.123456, 30.654321]
  },
  "description": "车辆速度超过限速值",
  "details": {
    "currentSpeed": 85.5,
    "limitSpeed": 80.0,
    "exceedDuration": 30,
    "roadType": "highway"
  },
  "status": "handled",
  "handleInfo": {
    "handlerId": 1001,
    "handlerName": "调度员张三",
    "handleTime": ISODate("2024-01-15T10:35:00Z"),
    "handleResult": "已通知司机减速",
    "handleMethod": "phone_call"
  },
  "createTime": ISODate("2024-01-15T10:30:00Z"),
  "updateTime": ISODate("2024-01-15T10:35:00Z")
}
```

---

## 6. 统计模块 (statistics)

### 6.1 统计数据集合 (statistics_data)

| 字段名 | 数据类型 | 索引 | 必填 | 描述 |
| ------ | ------- | ---- | ---- | ---- |
| _id | ObjectId | PRIMARY | 是 | 文档ID |
| statId | Long | INDEXED | 是 | 统计ID |
| statType | String | INDEXED | 是 | 统计类型 |
| statDate | Date | INDEXED | 是 | 统计日期 |
| statData | Object | - | 是 | 统计数据 |
| createTime | Date | INDEXED | 是 | 创建时间 |

**索引定义**：

```javascript
// 统计ID索引
db.statistics_data.createIndex({ "statId": 1 }, { unique: true })

// 统计类型索引
db.statistics_data.createIndex({ "statType": 1 })

// 统计日期索引
db.statistics_data.createIndex({ "statDate": -1 })

// 复合索引（统计类型 + 统计日期）
db.statistics_data.createIndex({ "statType": 1, "statDate": -1 })

// TTL索引（2年后自动删除）
db.statistics_data.createIndex({ "statDate": 1 }, { expireAfterSeconds: 63072000 })
```

**数据示例**：

```json
{
  "_id": ObjectId("65a1b2c3d4e5f6a7b8c9d0e1f2a3b4"),
  "statId": 6001,
  "statType": "daily_overview",
  "statDate": ISODate("2024-01-15T00:00:00Z"),
  "statData": {
    "vehicleCount": 128,
    "onlineCount": 96,
    "offlineCount": 32,
    "tripCount": 45,
    "warningCount": 12,
    "totalDistance": 12500.5,
    "totalDuration": 5400
  },
  "createTime": ISODate("2024-01-16T00:00:00Z")
}
```

---

## 7. 成本模块 (cost)

### 7.1 成本记录集合 (cost_record)

| 字段名 | 数据类型 | 索引 | 必填 | 描述 |
| ------ | ------- | ---- | ---- | ---- |
| _id | ObjectId | PRIMARY | 是 | 文档ID |
| costId | Long | INDEXED | 是 | 成本ID |
| carId | Long | INDEXED | 是 | 车辆ID |
| tripId | Long | INDEXED | 否 | 行程ID |
| costType | String | INDEXED | 是 | 成本类型 |
| amount | Double | - | 是 | 金额 |
| description | String | - | 否 | 描述 |
| costTime | Date | INDEXED | 是 | 成本时间 |
| operatorId | Long | INDEXED | 否 | 操作员ID |
| createTime | Date | INDEXED | 是 | 创建时间 |

**索引定义**：

```javascript
// 成本ID索引
db.cost_record.createIndex({ "costId": 1 }, { unique: true })

// 车辆ID索引
db.cost_record.createIndex({ "carId": 1 })

// 行程ID索引
db.cost_record.createIndex({ "tripId": 1 })

// 成本类型索引
db.cost_record.createIndex({ "costType": 1 })

// 成本时间索引
db.cost_record.createIndex({ "costTime": -1 })

// 复合索引（车辆ID + 成本时间）
db.cost_record.createIndex({ "carId": 1, "costTime": -1 })

// TTL索引（2年后自动删除）
db.cost_record.createIndex({ "costTime": 1 }, { expireAfterSeconds: 63072000 })
```

**数据示例**：

```json
{
  "_id": ObjectId("65a1b2c3d4e5f6a7b8c9d0e1f2a3b4"),
  "costId": 7001,
  "carId": 1001,
  "tripId": 3001,
  "costType": "fuel",
  "amount": 300.00,
  "description": "行程燃料费用",
  "costTime": ISODate("2024-01-15T12:30:00Z"),
  "operatorId": 1001,
  "createTime": ISODate("2024-01-15T12:30:00Z")
}
```

---

## 8. 日志模块 (log)

### 8.1 操作日志集合 (operation_log)

| 字段名 | 数据类型 | 索引 | 必填 | 描述 |
| ------ | ------- | ---- | ---- | ---- |
| _id | ObjectId | PRIMARY | 是 | 文档ID |
| logId | Long | INDEXED | 是 | 日志ID |
| userId | Long | INDEXED | 是 | 用户ID |
| username | String | INDEXED | 是 | 用户名 |
| operation | String | INDEXED | 是 | 操作类型 |
| module | String | INDEXED | 是 | 模块名称 |
| ip | String | - | 否 | IP地址 |
| userAgent | String | - | 否 | 用户代理 |
| requestTime | Date | INDEXED | 是 | 请求时间 |
| requestBody | Object | - | 否 | 请求体 |
| responseTime | Date | - | 否 | 响应时间 |
| duration | Long | - | 否 | 耗时（ms） |
| result | String | - | 是 | 结果 |
| errorMessage | String | - | 否 | 错误信息 |

**索引定义**：

```javascript
// 日志ID索引
db.operation_log.createIndex({ "logId": 1 }, { unique: true })

// 用户ID索引
db.operation_log.createIndex({ "userId": 1 })

// 用户名索引
db.operation_log.createIndex({ "username": 1 })

// 操作类型索引
db.operation_log.createIndex({ "operation": 1 })

// 模块名称索引
db.operation_log.createIndex({ "module": 1 })

// 请求时间索引
db.operation_log.createIndex({ "requestTime": -1 })

// 复合索引（用户ID + 请求时间）
db.operation_log.createIndex({ "userId": 1, "requestTime": -1 })

// TTL索引（30天后自动删除）
db.operation_log.createIndex({ "requestTime": 1 }, { expireAfterSeconds: 2592000 })
```

**数据示例**：

```json
{
  "_id": ObjectId("65a1b2c3d4e5f6a7b8c9d0e1f2a3b4"),
  "logId": 8001,
  "userId": 1001,
  "username": "admin",
  "operation": "update_vehicle",
  "module": "vehicle-service",
  "ip": "192.168.1.100",
  "userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
  "requestTime": ISODate("2024-01-15T10:30:00Z"),
  "requestBody": {
    "carId": 1001,
    "status": "idle"
  },
  "responseTime": ISODate("2024-01-15T10:30:00.123Z"),
  "duration": 123,
  "result": "success"
}
```

### 8.2 异常日志集合 (exception_log)

| 字段名 | 数据类型 | 索引 | 必填 | 描述 |
| ------ | ------- | ---- | ---- | ---- |
| _id | ObjectId | PRIMARY | 是 | 文档ID |
| exceptionId | Long | INDEXED | 是 | 异常ID |
| userId | Long | INDEXED | 否 | 用户ID |
| exceptionType | String | INDEXED | 是 | 异常类型 |
| exceptionMessage | String | - | 是 | 异常信息 |
| stackTrace | String | - | 否 | 堆栈信息 |
| requestUrl | String | - | 否 | 请求URL |
| requestMethod | String | - | 否 | 请求方法 |
| requestParams | Object | - | 否 | 请求参数 |
| occurTime | Date | INDEXED | 是 | 发生时间 |

**索引定义**：

```javascript
// 异常ID索引
db.exception_log.createIndex({ "exceptionId": 1 }, { unique: true })

// 用户ID索引
db.exception_log.createIndex({ "userId": 1 })

// 异常类型索引
db.exception_log.createIndex({ "exceptionType": 1 })

// 发生时间索引
db.exception_log.createIndex({ "occurTime": -1 })

// TTL索引（90天后自动删除）
db.exception_log.createIndex({ "occurTime": 1 }, { expireAfterSeconds: 7776000 })
```

**数据示例**：

```json
{
  "_id": ObjectId("65a1b2c3d4e5f6a7b8c9d0e1f2a3b4"),
  "exceptionId": 9001,
  "userId": 1001,
  "exceptionType": "BusinessException",
  "exceptionMessage": "车辆状态更新失败",
  "stackTrace": "com.mineguard.vehicle.exception.VehicleException: 车辆状态更新失败\n\tat com.mineguard.vehicle.service.VehicleService.updateStatus(VehicleService.java:125)",
  "requestUrl": "/api/vehicle/update",
  "requestMethod": "PUT",
  "requestParams": {
    "carId": 1001,
    "status": "idle"
  },
  "occurTime": ISODate("2024-01-15T10:30:00Z")
}
```

---

## 9. 设备模块 (device)

### 9.1 设备上报数据集合 (device_data)

| 字段名 | 数据类型 | 索引 | 必填 | 描述 |
| ------ | ------- | ---- | ---- | ---- |
| _id | ObjectId | PRIMARY | 是 | 文档ID |
| deviceId | String | INDEXED | 是 | 设备ID |
| carId | Long | INDEXED | 是 | 车辆ID |
| timestamp | Date | INDEXED | 是 | 时间戳 |
| sensors | Object | - | 是 | 传感器数据 |
| status | String | INDEXED | 否 | 状态 |

**索引定义**：

```javascript
// 设备ID索引
db.device_data.createIndex({ "deviceId": 1 })

// 车辆ID索引
db.device_data.createIndex({ "carId": 1 })

// 时间索引
db.device_data.createIndex({ "timestamp": -1 })

// 复合索引（设备ID + 时间）
db.device_data.createIndex({ "deviceId": 1, "timestamp": -1 })

// TTL索引（3个月后自动删除）
db.device_data.createIndex({ "timestamp": 1 }, { expireAfterSeconds: 7776000 })
```

**数据示例**：

```json
{
  "_id": ObjectId("65a1b2c3d4e5f6a7b8c9d0e1f2a3b4"),
  "deviceId": "DEVICE_001",
  "carId": 1001,
  "timestamp": ISODate("2024-01-15T10:30:00Z"),
  "sensors": {
    "speed": 45.5,
    "fuel": 75.2,
    "temperature": 85.5,
    "pressure": 2.5,
    "mileage": 123456.7,
    "engineRpm": 2500,
    "engineLoad": 60.5,
    "batteryVoltage": 13.8
  },
  "status": "normal"
}
```

---

## 10. 消息模块 (message)

### 10.1 消息历史集合 (message_history)

| 字段名 | 数据类型 | 索引 | 必填 | 描述 |
| ------ | ------- | ---- | ---- | ---- |
| _id | ObjectId | PRIMARY | 是 | 文档ID |
| messageId | String | INDEXED | 是 | 消息ID |
| messageType | String | INDEXED | 是 | 消息类型 |
| sender | String | INDEXED | 是 | 发送者 |
| receiver | String | INDEXED | 是 | 接收者 |
| timestamp | Date | INDEXED | 是 | 时间戳 |
| content | Object | - | 是 | 消息内容 |
| status | String | INDEXED | 是 | 状态 |
| readTime | Date | - | 否 | 已读时间 |
| expireTime | Date | - | 是 | 过期时间 |

**索引定义**：

```javascript
// 消息ID索引
db.message_history.createIndex({ "messageId": 1 }, { unique: true })

// 接收者索引
db.message_history.createIndex({ "receiver": 1, "timestamp": -1 })

// 消息类型索引
db.message_history.createIndex({ "messageType": 1 })

// 发送者索引
db.message_history.createIndex({ "sender": 1 })

// 时间索引
db.message_history.createIndex({ "timestamp": -1 })

// 状态索引
db.message_history.createIndex({ "status": 1 })

// TTL索引（30天后自动删除）
db.message_history.createIndex({ "expireTime": 1 }, { expireAfterSeconds: 2592000 })
```

**数据示例**：

```json
{
  "_id": ObjectId("65a1b2c3d4e5f6a7b8c9d0e1f2a3b4"),
  "messageId": "MSG_001",
  "messageType": "vehicle_status",
  "sender": "system",
  "receiver": "user_001",
  "timestamp": ISODate("2024-01-15T10:30:00Z"),
  "content": {
    "carId": 1001,
    "status": "running",
    "location": {
      "longitude": 114.123456,
      "latitude": 30.654321
    }
  },
  "status": "delivered",
  "readTime": ISODate("2024-01-15T10:30:05Z"),
  "expireTime": ISODate("2024-02-14T10:30:00Z")
}
```

---

## 11. 地理空间查询

### 11.1 附近车辆查询

```javascript
// 查询指定半径内的车辆
db.vehicle_trajectory.find({
  "location": {
    "$near": {
      "$geometry": {
        "type": "Point",
        "coordinates": [114.123456, 30.654321]
      },
      "$maxDistance": 5000
    }
  },
  "timestamp": {
    "$gte": ISODate("2024-01-15T00:00:00Z"),
    "$lte": ISODate("2024-01-15T23:59:59Z")
  }
}).sort({ "timestamp": -1 }).limit(100)
```

### 11.2 轨迹回放查询

```javascript
// 查询指定时间段的车辆轨迹
db.vehicle_trajectory.find({
  "carId": 1001,
  "timestamp": {
    "$gte": ISODate("2024-01-15T08:00:00Z"),
    "$lte": ISODate("2024-01-15T12:30:00Z")
  }
}).sort({ "timestamp": 1 })
```

### 11.3 区域监控查询

```javascript
// 查询指定区域内的车辆
db.vehicle_trajectory.find({
  "location": {
    "$geoWithin": {
      "$polygon": [
        [114.123456, 30.654321],
        [114.234567, 30.765432],
        [114.345678, 30.876543],
        [114.123456, 30.654321]
      ]
    }
  },
  "timestamp": {
    "$gte": ISODate("2024-01-15T00:00:00Z")
  }
}).sort({ "timestamp": -1 })
```

---

## 12. 聚合查询

### 12.1 行驶里程统计

```javascript
// 按时间、车辆统计行驶里程
db.vehicle_trajectory.aggregate([
  {
    "$match": {
      "timestamp": {
        "$gte": ISODate("2024-01-01T00:00:00Z"),
        "$lte": ISODate("2024-01-31T23:59:59Z")
      }
    }
  },
  {
    "$group": {
      "_id": {
        "carId": "$carId",
        "date": {
          "$dateToString": {
            "format": "%Y-%m-%d",
            "date": "$timestamp"
          }
        }
      },
      "totalDistance": {
        "$sum": {
          "$cond": {
            "if": { "$gt": ["$odometer", 0] },
            "then": {
              "$subtract": [
                { "$arrayElemAt": ["$odometer", -1] },
                { "$arrayElemAt": ["$odometer", 0] }
              ]
            },
            "else": 0
          }
        }
      }
    }
  },
  {
    "$sort": { "_id.date": 1 }
  }
])
```

### 12.2 预警趋势分析

```javascript
// 按时间、类型统计预警数量
db.warning_event.aggregate([
  {
    "$match": {
      "warningTime": {
        "$gte": ISODate("2024-01-01T00:00:00Z"),
        "$lte": ISODate("2024-01-31T23:59:59Z")
      }
    }
  },
  {
    "$group": {
      "_id": {
        "warningType": "$warningType",
        "warningLevel": "$warningLevel",
        "date": {
          "$dateToString": {
            "format": "%Y-%m-%d",
            "date": "$warningTime"
          }
        }
      },
      "count": { "$sum": 1 }
    }
  },
  {
    "$sort": { "_id.date": 1 }
  }
])
```

### 12.3 车辆活跃度统计

```javascript
// 统计车辆在线时长和行驶次数
db.vehicle_trajectory.aggregate([
  {
    "$match": {
      "timestamp": {
        "$gte": ISODate("2024-01-01T00:00:00Z"),
        "$lte": ISODate("2024-01-31T23:59:59Z")
      }
    }
  },
  {
    "$group": {
      "_id": "$carId",
      "onlineDuration": {
        "$sum": {
          "$cond": {
            "if": { "$eq": ["$status", "running"] },
            "then": 1,
            "else": 0
          }
        }
      },
      "tripCount": {
        "$sum": {
          "$cond": {
            "if": { "$eq": ["$status", "running"] },
            "then": 1,
            "else": 0
          }
        }
      }
    }
  }
])
```

---

## 13. 数据保留策略

### 13.1 TTL索引配置

| 集合 | 保留时间 | TTL字段 | 说明 |
| ---- | ------- | ------- | ---- |
| vehicle_trajectory | 6个月 | timestamp | 车辆轨迹数据 |
| vehicle_maintenance | 2年 | createTime | 车辆维护记录 |
| trip_history | 1年 | createTime | 行程历史数据 |
| warning_event | 1年 | warningTime | 预警事件数据 |
| statistics_data | 2年 | statDate | 统计数据 |
| cost_record | 2年 | costTime | 成本记录 |
| operation_log | 30天 | requestTime | 操作日志 |
| exception_log | 90天 | occurTime | 异常日志 |
| device_data | 3个月 | timestamp | 设备上报数据 |
| message_history | 30天 | expireTime | 消息历史 |

### 13.2 数据清理策略

| 策略 | 说明 | 执行频率 |
| ---- | ---- | ------- |
| TTL自动清理 | 利用MongoDB TTL索引自动删除过期数据 | 持续 |
| 手动归档 | 将历史数据归档到冷存储 | 每月 |
| 定时清理 | 定时任务清理未设置TTL的过期数据 | 每周 |

---

## 14. 性能优化

### 14.1 索引优化

- 为常用查询字段创建索引
- 使用复合索引优化多条件查询
- 为地理空间查询创建2dsphere索引
- 定期分析慢查询，优化索引

### 14.2 查询优化

- 使用投影只返回需要的字段
- 使用limit限制返回结果数量
- 使用sort优化排序
- 避免使用$or，使用$in替代

### 14.3 写入优化

- 使用批量插入提高写入性能
- 合理设置writeConcern
- 使用分片提高写入吞吐量
- 避免频繁更新同一文档

---

## 15. 监控指标

### 15.1 集合监控

| 指标 | 说明 | 告警阈值 |
| ---- | ---- | ------- |
| 文档数量 | 集合中文档总数 | > 10000000 |
| 存储大小 | 集合占用存储空间 | > 100GB |
| 索引大小 | 索引占用存储空间 | > 10GB |
| 查询延迟 | 平均查询响应时间 | > 100ms |
| 写入延迟 | 平均写入响应时间 | > 50ms |

### 15.2 慢查询监控

```javascript
// 查看慢查询
db.setProfilingLevel(1)
db.system.profile.find({ "millis": { "$gt": 100 } }).sort({ "ts": -1 }).limit(10)

// 查看索引使用情况
db.collection.getIndexes()
db.collection.stats()
```

---

## 16. 最佳实践

### 16.1 文档设计原则

- 合理设计文档结构，避免过大文档
- 使用嵌套文档组织相关数据
- 使用数组存储列表数据
- 避免使用过多的嵌套层级
- 合理使用引用和嵌入

### 16.2 索引设计原则

- 为常用查询字段创建索引
- 避免创建过多索引
- 使用复合索引优化多条件查询
- 定期分析索引使用情况
- 删除未使用的索引

### 16.3 查询优化原则

- 使用投影减少数据传输
- 使用limit限制返回结果
- 避免使用$or，使用$in替代
- 合理使用聚合管道
- 避免全表扫描
