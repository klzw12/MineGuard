# 成本服务逻辑

## 1. 服务概述

成本服务（Cost Service）提供成本明细管理、薪资配置、成本预算等功能，支持多种成本类型的记录、统计和分析。

### 1.1 数据库表

| 表名 | 说明 |
| - | - |
| cost_detail | 成本明细表 |
| salary_config | 薪资配置表 |
| cost_budget | 成本预算表 |

### 1.2 核心功能

| 功能模块 | 接口路径 | 说明 |
| - | - | - |
| 成本明细管理 | /api/cost/detail | 成本记录的增删改查 |
| 成本统计 | /api/cost/statistics | 按条件统计成本数据 |
| 薪资配置 | /api/cost/salary-config | 薪资标准配置管理 |
| 成本预算 | /api/cost/budget | 成本预算管理 |

---

## 2. 成本明细管理逻辑 (CostDetail)

### 2.1 成本类型枚举

| 编码 | 名称 | 说明 |
| - | - | - |
| 1 | 燃油成本 | 车辆燃油费用 |
| 2 | 维修成本 | 车辆维修保养费用 |
| 3 | 人工成本 | 司机及员工薪资 |
| 4 | 保险成本 | 车辆保险费用 |
| 5 | 折旧成本 | 车辆折旧费用 |
| 6 | 管理成本 | 管理费用 |
| 7 | 其他成本 | 其他费用 |

### 2.2 添加成本明细

**接口**: `POST /api/cost/detail`  
**DTO**: `CostDetailDTO`  
**VO**: `CostDetailVO`

**处理流程**:

1. 创建CostDetail实体
2. 生成成本编号（格式：COST + 时间戳）
3. 根据成本类型自动设置成本名称
4. 设置创建时间和更新时间
5. 插入数据库
6. 返回VO对象

**编号规则**: `COST + yyyyMMddHHmmss`

---

### 2.3 更新成本明细

**接口**: `PUT /api/cost/detail`  
**DTO**: `CostDetailDTO`  
**VO**: `CostDetailVO`

**处理流程**:

1. 根据ID查询成本明细
2. 校验是否存在
3. 复制属性（排除id、costNo、createTime、deleted）
4. 更新成本名称（如果成本类型变更）
5. 更新数据库
6. 返回VO对象

---

### 2.4 删除成本明细

**接口**: `DELETE /api/cost/detail/{id}`

**处理流程**:

1. 根据ID逻辑删除（MyBatis-Plus @TableLogic）
2. 记录日志

---

### 2.5 获取成本明细列表

**接口**: `GET /api/cost/detail/list`  
**DTO**: `CostQueryDTO`  
**VO**: `List<CostDetailVO>`

**查询条件**:

| 字段 | 条件 | 说明 |
| - | - | - |
| vehicleId | = | 车辆ID |
| userId | = | 用户ID |
| costType | = | 成本类型 |
| startDate | >= | 开始日期 |
| endDate | <= | 结束日期 |

**排序**: 按成本日期降序

---

## 3. 成本统计逻辑 (CostStatistics)

### 3.1 获取成本统计

**接口**: `GET /api/cost/statistics`  
**DTO**: `CostQueryDTO`  
**VO**: `CostStatisticsVO`

**处理流程**:

1. 调用getCostDetailList获取明细列表
2. 计算总金额
3. 按成本类型分组统计
4. 构建类型名称映射
5. 设置各类型成本金额
6. 返回统计结果

**返回字段**:

| 字段 | 说明 |
| - | - |
| totalAmount | 总金额 |
| recordCount | 记录数量 |
| typeAmountMap | 按类型分组的金额 |
| typeNames | 类型名称映射 |
| fuelCost | 燃油成本 |
| maintenanceCost | 维修成本 |
| laborCost | 人工成本 |
| insuranceCost | 保险成本 |
| depreciationCost | 折旧成本 |
| managementCost | 管理成本 |
| otherCost | 其他成本 |

---

## 4. 薪资配置逻辑 (SalaryConfig)

### 4.1 薪资配置字段

| 字段 | 类型 | 说明 |
| - | - | - |
| roleCode | String | 角色编码 |
| roleName | String | 角色名称 |
| baseSalary | BigDecimal | 基本工资 |
| dailySalary | BigDecimal | 日工资 |
| hourlySalary | BigDecimal | 时工资 |
| overtimeRate | BigDecimal | 加班费率 |
| performanceBonus | BigDecimal | 绩效奖金 |
| status | Integer | 状态：0-禁用 1-启用 |
| effectiveDate | LocalDate | 生效日期 |
| expiryDate | LocalDate | 失效日期 |

### 4.2 添加薪资配置

**接口**: `POST /api/cost/salary-config`  
**DTO**: `SalaryConfigDTO`  
**VO**: `SalaryConfigVO`

**处理流程**:

1. 创建SalaryConfig实体
2. 复制属性
3. 设置创建时间和更新时间
4. 插入数据库
5. 返回VO对象

---

### 4.3 获取薪资配置列表

**接口**: `GET /api/cost/salary-config/list`  
**VO**: `List<SalaryConfigVO>`

**查询条件**:
- status = 1（仅返回启用的配置）
- 按roleCode升序排序

---

## 5. 成本预算逻辑 (CostBudget)

### 5.1 预算类型枚举

| 编码 | 名称 | 说明 |
| - | - | - |
| 1 | 月度 | 月度预算 |
| 2 | 季度 | 季度预算 |
| 3 | 年度 | 年度预算 |

### 5.2 预算状态枚举

| 编码 | 名称 | 说明 |
| - | - | - |
| 0 | 草稿 | 新建预算 |
| 1 | 已审批 | 审批通过 |
| 2 | 执行中 | 正在执行 |
| 3 | 已完成 | 执行完成 |

### 5.3 添加成本预算

**接口**: `POST /api/cost/budget`  
**DTO**: `CostBudgetDTO`  
**VO**: `CostBudgetVO`

**处理流程**:

1. 创建CostBudget实体
2. 生成预算编号（格式：BUDGET + 时间戳）
3. 计算总预算（各项预算之和）
4. 设置状态为草稿
5. 插入数据库
6. 返回VO对象

**编号规则**: `BUDGET + yyyyMMddHHmmss`

**预算计算**:
```
totalBudget = fuelBudget + maintenanceBudget + laborBudget 
             + insuranceBudget + depreciationBudget 
             + managementBudget + otherBudget
```

---

### 5.4 获取成本预算列表

**接口**: `GET /api/cost/budget/list`  
**VO**: `List<CostBudgetVO>`

**查询条件**:

| 参数 | 条件 | 说明 |
| - | - | - |
| budgetType | = | 预算类型 |
| budgetYear | = | 预算年份 |

**排序**: 按创建时间降序

---

## 6. 与其他模块的集成

### 6.1 与车辆模块集成

- 成本明细可关联车辆ID（vehicleId）
- 按车辆统计成本数据

### 6.2 与行程模块集成

- 成本明细可关联行程ID（tripId）
- 行程相关的燃油、过路费等成本

### 6.3 与统计模块集成

- 统计模块可查询成本统计数据
- 生成成本分析报表

### 6.4 与AI模块集成

- AI模块可分析成本数据
- 生成成本优化建议
- 生成财务报表

---

## 7. 业务规则

### 7.1 成本编号规则

- 格式：`COST + yyyyMMddHHmmss`
- 示例：`COST20240321143000`

### 7.2 预算编号规则

- 格式：`BUDGET + yyyyMMddHHmmss`
- 示例：`BUDGET20240321143000`

### 7.3 状态转换

```
草稿(0) -> 已审批(1) -> 执行中(2) -> 已完成(3)
```

### 7.4 金额精度

- 所有金额字段使用BigDecimal
- 精度：12位整数，2位小数

---

## 8. 成本分析功能

### 8.1 能耗分析

**接口**: `GET /api/cost/analysis/energy`

**分析指标**:
- 总燃油成本
- 总燃油消耗量
- 总货运量
- 总行驶里程
- 每吨公里燃油消耗
- 每吨公里燃油成本

### 8.2 车辆利用率分析

**接口**: `GET /api/cost/analysis/utilization`

**分析指标**:
- 总车辆数
- 活跃车辆数
- 出勤率
- 平均利用率
- 平均空驶率

### 8.3 空载率分析

**接口**: `GET /api/cost/analysis/idle-rate`

**分析指标**:
- 总行驶里程
- 重载里程
- 空载里程
- 空载率
- 重载率

### 8.4 总体成本分析

**接口**: `GET /api/cost/analysis/overall`

**汇总内容**:
- 能耗分析结果
- 利用率分析结果
- 空载率分析结果
- 成本报表
- 预算预警

---

## 9. 定时任务

### 9.1 月度预算检查

- Cron: `0 0 2 1 * ?`
- 每月1日02:00执行
- 检查上月预算使用情况

### 9.2 月度成本报表

- Cron: `0 0 3 1 * ?`
- 每月1日03:00执行
- 生成上月成本报表

### 9.3 预算状态更新

- Cron: `0 0 4 * * ?`
- 每天04:00执行
- 更新过期预算状态

### 9.4 季度预算检查

- Cron: `0 0 5 1 1,4,7,10 ?`
- 每季度首月1日05:00执行

### 9.5 年度预算检查

- Cron: `0 0 6 1 1 ?`
- 每年1月1日06:00执行

---

## 10. 与need.txt需求对应

### 10.1 车辆能耗管理 (need.txt 3.7.1)

- ✅ 运输矿石吨位统计
- ✅ 油料消耗统计
- ✅ 能耗成本核算分析
- ✅ 每吨公里成本计算

### 10.2 车辆空载管理 (need.txt 3.7.2)

- ✅ 车辆出勤率统计
- ✅ 空驶率统计
- ✅ 利用率分析
- ✅ 提高经济效益建议

### 10.3 总体成本分析 (need.txt 3.7.3)

- ✅ 能耗管理汇总
- ✅ 空载管理汇总
- ✅ 运输量统计汇总
- ✅ 安全事故统计
- ✅ 人员成本统计
- ✅ 决策依据支持
