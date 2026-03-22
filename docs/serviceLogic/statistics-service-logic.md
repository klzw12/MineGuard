# 统计服务逻辑

## 1. 服务概述

统计服务（Statistics Service）提供行程统计、成本统计、车辆统计、司机统计等功能，支持按日期范围查询统计数据，并可与AI服务集成进行智能分析。

### 1.1 数据库表

| 表名 | 说明 |
| - | - |
| trip_statistics | 行程统计表 |
| cost_statistics | 成本统计表 |
| vehicle_statistics | 车辆统计表 |
| driver_statistics | 司机统计表 |
| transport_statistics | 运输统计表 |

### 1.2 核心功能

| 功能模块 | 接口路径 | 说明 |
| - | - | - |
| 行程统计 | /api/statistics/trip | 行程数据统计 |
| 成本统计 | /api/statistics/cost | 成本数据统计 |
| 车辆统计 | /api/statistics/vehicle | 车辆运营统计 |
| 司机统计 | /api/statistics/driver | 司机绩效统计 |
| 总体统计 | /api/statistics/overall | 综合统计数据 |

---

## 2. 行程统计逻辑 (TripStatistics)

### 2.1 统计字段

| 字段 | 类型 | 说明 |
| - | - | - |
| statisticsDate | LocalDate | 统计日期 |
| tripCount | Integer | 行程数量 |
| totalDistance | BigDecimal | 总行驶距离(公里) |
| totalDuration | BigDecimal | 总行驶时长(小时) |
| completedTripCount | Integer | 完成行程数 |
| cancelledTripCount | Integer | 取消行程数 |
| averageSpeed | BigDecimal | 平均速度(km/h) |
| fuelConsumption | BigDecimal | 燃油消耗(升) |
| cargoWeight | BigDecimal | 货物运输量(吨) |

### 2.2 获取行程统计

**接口**: `GET /api/statistics/trip`  
**DTO**: `StatisticsQueryDTO`  
**VO**: `List<TripStatisticsVO>`

**查询条件**:

| 字段 | 条件 | 说明 |
| - | - | - |
| startDate | >= | 开始日期 |
| endDate | <= | 结束日期 |

**排序**: 按统计日期降序

---

### 2.3 计算行程统计

**接口**: `POST /api/statistics/trip/calculate?date={date}`  
**VO**: `TripStatisticsVO`

**处理流程**:

1. 解析日期参数
2. 创建TripStatistics实体
3. 初始化各字段为0或默认值
4. 设置创建时间和更新时间
5. 插入数据库
6. 返回VO对象

**说明**: 实际统计计算需要从行程模块获取数据进行聚合

---

## 3. 成本统计逻辑 (CostStatistics)

### 3.1 统计字段

| 字段 | 类型 | 说明 |
| - | - | - |
| statisticsDate | LocalDate | 统计日期 |
| fuelCost | BigDecimal | 燃油成本 |
| maintenanceCost | BigDecimal | 维修成本 |
| laborCost | BigDecimal | 人工成本 |
| insuranceCost | BigDecimal | 保险成本 |
| depreciationCost | BigDecimal | 折旧成本 |
| managementCost | BigDecimal | 管理成本 |
| otherCost | BigDecimal | 其他成本 |
| totalCost | BigDecimal | 总成本 |

### 3.2 获取成本统计

**接口**: `GET /api/statistics/cost`  
**DTO**: `StatisticsQueryDTO`  
**VO**: `List<CostStatisticsVO>`

**查询条件**:

| 字段 | 条件 | 说明 |
| - | - | - |
| startDate | >= | 开始日期 |
| endDate | <= | 结束日期 |

**排序**: 按统计日期降序

---

### 3.3 计算成本统计

**接口**: `POST /api/statistics/cost/calculate?date={date}`  
**VO**: `CostStatisticsVO`

**处理流程**:

1. 解析日期参数
2. 创建CostStatistics实体
3. 初始化各成本字段为0
4. 设置创建时间和更新时间
5. 插入数据库
6. 返回VO对象

**说明**: 实际统计计算需要从成本模块获取数据进行聚合

---

## 4. 车辆统计逻辑 (VehicleStatistics)

### 4.1 统计字段

| 字段 | 类型 | 说明 |
| - | - | - |
| vehicleId | Long | 车辆ID |
| statisticsDate | LocalDate | 统计日期 |
| tripCount | Integer | 行程数量 |
| totalDistance | BigDecimal | 总行驶距离(公里) |
| totalDuration | BigDecimal | 总行驶时长(小时) |
| cargoWeight | BigDecimal | 货物运输量(吨) |
| fuelConsumption | BigDecimal | 燃油消耗(升) |
| fuelCost | BigDecimal | 燃油成本 |
| maintenanceCount | Integer | 维修次数 |
| maintenanceCost | BigDecimal | 维修成本 |
| warningCount | Integer | 预警次数 |
| violationCount | Integer | 违规次数 |
| idleDuration | BigDecimal | 怠速时长(小时) |
| idleDistance | BigDecimal | 空驶距离(公里) |

### 4.2 获取车辆统计

**接口**: `GET /api/statistics/vehicle`  
**DTO**: `StatisticsQueryDTO`  
**VO**: `List<VehicleStatisticsVO>`

**查询条件**:

| 字段 | 条件 | 说明 |
| - | - | - |
| vehicleId | = | 车辆ID |
| startDate | >= | 开始日期 |
| endDate | <= | 结束日期 |

**排序**: 按统计日期降序

---

### 4.3 计算车辆统计

**接口**: `POST /api/statistics/vehicle/calculate?vehicleId={vehicleId}&date={date}`  
**VO**: `VehicleStatisticsVO`

**处理流程**:

1. 解析车辆ID和日期参数
2. 创建VehicleStatistics实体
3. 初始化各字段为0或默认值
4. 设置创建时间和更新时间
5. 插入数据库
6. 返回VO对象

**利用率计算**:
```
utilizationRate = (totalDistance / totalDuration) * 100
```

---

## 5. 司机统计逻辑 (DriverStatistics)

### 5.1 统计字段

| 字段 | 类型 | 说明 |
| - | - | - |
| userId | Long | 用户ID |
| statisticsDate | LocalDate | 统计日期 |
| attendanceDays | Integer | 出勤天数 |
| attendanceHours | BigDecimal | 出勤时长(小时) |
| tripCount | Integer | 行程数量 |
| totalDistance | BigDecimal | 总行驶距离(公里) |
| cargoWeight | BigDecimal | 货物运输量(吨) |
| lateCount | Integer | 迟到次数 |
| earlyLeaveCount | Integer | 早退次数 |
| warningCount | Integer | 预警次数 |
| violationCount | Integer | 违规次数 |
| overSpeedCount | Integer | 超速次数 |
| routeDeviationCount | Integer | 路线偏离次数 |
| performanceScore | BigDecimal | 绩效评分 |

### 5.2 获取司机统计

**接口**: `GET /api/statistics/driver`  
**DTO**: `StatisticsQueryDTO`  
**VO**: `List<DriverStatisticsVO>`

**查询条件**:

| 字段 | 条件 | 说明 |
| - | - | - |
| userId | = | 用户ID |
| startDate | >= | 开始日期 |
| endDate | <= | 结束日期 |

**排序**: 按统计日期降序

---

### 5.3 计算司机统计

**接口**: `POST /api/statistics/driver/calculate?userId={userId}&date={date}`  
**VO**: `DriverStatisticsVO`

**处理流程**:

1. 解析用户ID和日期参数
2. 创建DriverStatistics实体
3. 初始化各字段为0或默认值
4. 设置绩效评分为100
5. 设置创建时间和更新时间
6. 插入数据库
7. 返回VO对象

**绩效评分规则**:
- 初始分数：100分
- 根据违规、预警等情况扣分
- 具体扣分规则由业务配置

---

## 6. 总体统计逻辑 (OverallStatistics)

### 6.1 获取总体统计

**接口**: `GET /api/statistics/overall`  
**DTO**: `StatisticsQueryDTO`  
**VO**: `OverallStatisticsVO`

**处理流程**:

1. 调用getTripStatistics获取行程统计列表
2. 调用getCostStatistics获取成本统计列表
3. 遍历行程统计，累加各项指标
4. 遍历成本统计，累加各项成本
5. 构建OverallStatisticsVO返回

**返回字段**:

| 字段 | 说明 |
| - | - |
| startDate | 开始日期 |
| endDate | 结束日期 |
| totalTripCount | 总行程数 |
| totalDistance | 总行驶距离 |
| totalDuration | 总行驶时长 |
| totalCompletedTripCount | 完成行程数 |
| totalCancelledTripCount | 取消行程数 |
| totalCargoWeight | 总货运量 |
| totalFuelCost | 总燃油成本 |
| totalMaintenanceCost | 总维修成本 |
| totalLaborCost | 总人工成本 |
| totalOtherCost | 总其他成本 |
| totalCost | 总成本 |
| tripStatisticsList | 行程统计明细列表 |
| costStatisticsList | 成本统计明细列表 |

---

## 7. 与其他模块的集成

### 7.1 与行程模块集成

- 从行程模块获取行程数据
- 计算行程统计数据
- 关联车辆和司机统计

### 7.2 与成本模块集成

- 从成本模块获取成本数据
- 计算成本统计数据
- 关联车辆成本统计

### 7.3 与车辆模块集成

- 从车辆模块获取车辆信息
- 计算车辆运营统计
- 车辆利用率分析

### 7.4 与用户模块集成

- 从用户模块获取司机信息
- 计算司机绩效统计
- 出勤数据分析

### 7.5 与AI模块集成

- AI模块可调用统计数据进行分析
- 生成统计报表
- 提供优化建议

---

## 8. 数据聚合策略

### 8.1 日统计

- 每日凌晨执行定时任务
- 聚合前一天的数据
- 插入统计表

### 8.2 唯一约束

| 表 | 唯一约束 |
| - | - |
| trip_statistics | statistics_date |
| cost_statistics | statistics_date |
| vehicle_statistics | vehicle_id + statistics_date |
| driver_statistics | user_id + statistics_date |

### 8.3 数据更新策略

- 统计数据生成后一般不修改
- 如需重新计算，先删除后插入
- 支持手动触发重新计算

---

## 9. 性能优化

### 9.1 索引设计

- 所有统计表按日期建立索引
- 车辆统计表按vehicle_id建立索引
- 司机统计表按user_id建立索引

### 9.2 查询优化

- 使用日期范围查询
- 避免全表扫描
- 支持分页查询

### 9.3 缓存策略

- 高频查询的统计数据可缓存
- 缓存过期时间：1小时
- 支持手动刷新缓存

---

## 10. 故障统计逻辑 (FaultStatistics)

### 10.1 统计字段

| 字段 | 类型 | 说明 |
| - | - | - |
| vehicleId | Long | 车辆ID |
| statisticsDate | LocalDate | 统计日期 |
| faultCount | Integer | 故障总数 |
| minorFaultCount | Integer | 轻微故障数 |
| majorFaultCount | Integer | 一般故障数 |
| criticalFaultCount | Integer | 严重故障数 |
| totalRepairCost | BigDecimal | 总维修成本 |
| avgRepairTime | BigDecimal | 平均维修时间 |
| topFaultType | String | 最常见故障类型 |
| topFaultCount | Integer | 最常见故障数量 |
| repairedCount | Integer | 已修复数量 |
| pendingCount | Integer | 待维修数量 |

### 10.2 获取故障统计

**接口**: `GET /api/statistics/fault`  
**DTO**: `StatisticsQueryDTO`  
**VO**: `List<FaultStatisticsVO>`

### 10.3 故障分布分析

- 按故障类型分布统计
- 按故障级别分布统计
- 故障趋势分析（按日期）

---

## 11. 定时任务配置

### 11.1 每日统计任务

| 任务 | Cron表达式 | 执行时间 | 说明 |
| - | - | - | - |
| 行程统计 | 0 5 0 * * ? | 每天00:05 | 统计前一天行程数据 |
| 成本统计 | 0 5 0 * * ? | 每天00:05 | 统计前一天成本数据 |
| 车辆统计 | 0 10 0 * * ? | 每天00:10 | 统计每辆车前一天运营数据 |
| 司机统计 | 0 15 0 * * ? | 每天00:15 | 统计每位司机前一天绩效数据 |
| 运输统计 | 0 30 0 * * ? | 每天00:30 | 统计前一天运输汇总数据 |
| 故障统计 | 每日执行 | - | 统计每辆车前一天故障数据 |

### 11.2 月度统计任务

| 任务 | Cron表达式 | 执行时间 | 说明 |
| - | - | - | - |
| 月度汇总 | 0 0 1 1 * ? | 每月1日01:00 | 汇总上月行程和成本数据 |

---

## 12. 与need.txt需求对应

### 12.1 车辆实时统计分析 (need.txt 3.6.1)

- ✅ 每日出车数量统计
- ✅ 每周/每月出车数量统计
- ✅ 司机数量统计
- ✅ 运输吨位统计
- ✅ 曲线图、饼图、柱状图展示

### 12.2 车辆运量趋势分析 (need.txt 3.6.2)

- ✅ 运量趋势分析
- ✅ 按日期范围查询
- ✅ 支持导出报表

### 12.3 车辆故障统计分析 (need.txt 3.6.3)

- ✅ 故障统计汇总
- ✅ 故障类型分布
- ✅ 故障级别分布
- ✅ 维修成本分析
- ✅ 为备件管理提供依据
