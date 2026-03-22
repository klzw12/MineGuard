# AI服务逻辑

## 1. 服务概述

AI服务是一个纯服务模块，不依赖数据库表，通过适配器模式对接多个AI提供商（DeepSeek、Minimax），为MineGuard系统提供智能分析能力。

### 1.1 支持的AI提供商

| 提供商 | 编码 | API地址 | 说明 |
| - | - | - | - |
| DeepSeek | deepseek | https://api.deepseek.com/v1 | 默认提供商，支持中文分析 |
| Minimax | minimax | https://api.minimax.chat/v1 | 备用提供商 |

### 1.2 核心功能

| 功能 | 方法 | 对应需求 |
| - | - | - |
| 统计数据分析 | analyzeStatisticsData | 3.6 统计分析模块 |
| 成本数据分析 | analyzeCostData | 3.7 成本管理模块 |
| 财务报表生成 | generateFinancialReport | 3.7 成本管理模块 |
| 优化建议生成 | generateOptimizationSuggestions | 3.7 成本管理模块 |
| 智能调度建议 | generateDispatchSuggestions | 3.3 智能调度规划 |
| 驾驶行为分析 | analyzeDrivingBehavior | 3.5 车辆行为分析研判 |
| AI提供商管理 | switchProvider, getCurrentProvider | 系统运维 |

---

## 2. AI分析服务逻辑 (AiService)

### 2.1 分析统计数据

**接口**: `POST /api/ai/analyze/statistics`  
**DTO**: `Map<String, Object> statisticsData`  
**VO**: `AnalysisResultVO`

**处理流程**:

1. 获取当前AI适配器（根据配置的default-provider）
2. 调用适配器生成分析提示词
3. 发送请求到AI API
4. 解析AI响应
5. 返回分析结果

**提示词结构**:
```
请分析以下数据并提供详细的分析结果：
分析类型：statistics
数据：
vehicleCount: 50
driverCount: 30
...

请提供以下内容：
1. 数据概览
2. 关键发现
3. 趋势分析
4. 优化建议
```

---

### 2.2 分析成本数据

**接口**: `POST /api/ai/analyze/cost`  
**DTO**: `Map<String, Object> costData`  
**VO**: `AnalysisResultVO`

**处理流程**:

1. 获取当前AI适配器
2. 生成成本分析提示词
3. 发送请求到AI API
4. 解析AI响应
5. 返回分析结果

---

### 2.3 生成财务报表

**接口**: `POST /api/ai/generate/financial-report`  
**DTO**: `Map<String, Object> financialData`  
**VO**: `AnalysisResultVO`

**处理流程**:

1. 获取当前AI适配器
2. 生成财务报表提示词
3. 发送请求到AI API
4. 解析AI响应
5. 返回报表内容

**提示词结构**:
```
请根据以下财务数据生成详细的财务报表：
财务数据：
revenue: {...}
costs: {...}

请生成以下内容：
1. 财务概览
2. 收入分析
3. 成本分析
4. 利润分析
5. 财务建议
```

---

### 2.4 生成优化建议

**接口**: `POST /api/ai/generate/optimization-suggestions`  
**DTO**: `Map<String, Object> analysisData`  
**VO**: `AnalysisResultVO`

**处理流程**:

1. 获取当前AI适配器
2. 生成优化建议提示词
3. 发送请求到AI API
4. 解析AI响应
5. 返回优化建议

**提示词结构**:
```
请根据以下分析数据生成详细的优化建议：
分析数据：
currentIssues: [...]
metrics: {...}

请生成以下内容：
1. 问题识别
2. 优化建议
3. 预期效果
4. 实施步骤
```

---

### 2.5 生成调度建议

**接口**: `POST /api/ai/generate/dispatch-suggestions`  
**DTO**: `Map<String, Object> dispatchData`  
**VO**: `AnalysisResultVO`

**处理流程**:

1. 获取当前AI适配器
2. 生成调度建议提示词
3. 发送请求到AI API
4. 解析AI响应
5. 返回调度建议

**提示词结构**:
```
请根据以下调度数据生成智能调度建议：
调度数据：
planDate: 2024-03-21
availableVehicles: 10
availableDrivers: 8
...

请生成以下内容：
1. 调度方案
2. 车辆分配建议
3. 路线优化建议
4. 预期效果
```

**应用场景**:
- 车辆损坏时动态调整运输次数
- 运输线路堵塞时选择替代路径
- 司机请假时动态匹配其他司机
- 突发状况下的应急调度方案

---

### 2.6 分析驾驶行为

**接口**: `POST /api/ai/analyze/driving-behavior`  
**DTO**: `Map<String, Object> trackData`  
**VO**: `AnalysisResultVO`

**处理流程**:

1. 尝试调用Python服务进行数据清洗
   - 如果成功，使用清洗后的数据
   - 如果失败，直接使用原始数据
2. 获取当前AI适配器
3. 生成驾驶行为分析提示词
4. 发送请求到AI API
5. 解析AI响应
6. 整合清洗报告和分析结果
7. 返回完整分析结果

**提示词结构**:
```
请根据以下轨迹数据分析司机的驾驶行为：
轨迹数据：
driverId: 123
trackData: [...]
drivingDuration: 120
...

请生成以下内容：
1. 驾驶行为分析
2. 超速情况
3. 急加速/急减速情况
4. 驾驶行为评分
5. 改进建议
```

**分析维度**:
- 行驶速度分析（超速检测）
- 急加速/急减速检测
- 连续驾驶时长（疲劳驾驶检测）
- 轨迹偏移分析
- 综合驾驶评分

**返回结果**:
```json
{
  "status": "success",
  "message": "驾驶行为分析完成",
  "analysis": {
    "drivingScore": 85,
    "speedingAnalysis": "...",
    "accelerationAnalysis": "...",
    "fatigueAnalysis": "...",
    "recommendations": [...]
  },
  "cleaningReport": {
    "originalPoints": 100,
    "cleanedPoints": 95,
    "removedPoints": 5,
    "reason": "移除了5个异常GPS点"
  }
}
```

---

### 2.7 获取当前AI提供商

**接口**: `GET /api/ai/provider/current`  
**VO**: `ProviderVO`

**处理流程**:

1. 返回配置的default-provider值
2. 返回状态信息

---

### 2.8 切换AI提供商

**接口**: `POST /api/ai/provider/switch`  
**DTO**: `ProviderSwitchDTO(provider)`  
**VO**: `ProviderVO`

**处理流程**:

1. 检查提供商是否在支持列表中（配置的providers）
2. 检查对应的适配器是否存在
3. 如果都通过，更新当前提供商
4. 返回切换结果

---

## 3. AI适配器逻辑 (AiAdapter)

### 3.1 适配器接口

```java
public interface AiAdapter {
    Map<String, Object> sendRequest(String prompt, Map<String, Object> parameters);
    String generateAnalysisPrompt(Map<String, Object> data, String analysisType);
    Map<String, Object> parseResponse(Map<String, Object> response);
}
```

### 3.2 DeepSeek适配器 (DeepSeekAdapter)

**配置**:
```yaml
ai:
  deepseek:
    api-key: ${DEEPSEEK_API_KEY}
    model: deepseek-chat
```

**实现**:
- 使用Spring AI的ChatClient
- API地址: https://api.deepseek.com/v1
- 支持中文分析，适合国内使用

### 3.3 Minimax适配器 (MinimaxAdapter)

**配置**:
```yaml
ai:
  minimax:
    api-key: ${MINIMAX_API_KEY}
    model: abab5.5-chat
```

**实现**:
- 使用Spring AI的ChatClient
- API地址: https://api.minimax.chat/v1
- 作为备用提供商

---

## 4. 配置说明

### 4.1 完整配置示例

```yaml
ai:
  default-provider: deepseek
  providers:
    - deepseek
    - minimax
  deepseek:
    api-key: ${DEEPSEEK_API_KEY}
    model: deepseek-chat
  minimax:
    api-key: ${MINIMAX_API_KEY}
    model: abab5.5-chat
```

### 4.2 环境变量

| 变量名 | 说明 |
| - | - |
| DEEPSEEK_API_KEY | DeepSeek API密钥 |
| MINIMAX_API_KEY | Minimax API密钥 |

---

## 5. 错误处理

### 5.1 AI请求异常

当AI请求失败时：
1. 记录错误日志
2. 抛出RuntimeException
3. 由全局异常处理器返回错误响应

### 5.2 Python服务不可用

当Python数据清洗服务不可用时：
1. 记录警告日志
2. 降级使用原始数据
3. 返回默认分析结果（驾驶行为分析）

---

## 6. 扩展指南

### 6.1 添加新的AI提供商

1. 创建新的适配器类实现AiAdapter接口
2. 添加@Component注解，命名格式：`{provider}Adapter`
3. 在配置文件中添加提供商配置
4. 在providers列表中添加新提供商

### 6.2 添加新的分析类型

1. 在AiService中添加新方法
2. 创建对应的提示词生成方法
3. 在AiController中添加新接口
4. 更新api.md文档

---

## 7. 与其他模块的集成

### 7.1 与调度模块集成

调度模块可调用`generateDispatchSuggestions`获取智能调度建议：
- 输入：当前可用车辆、司机、货物信息、约束条件
- 输出：调度方案、车辆分配建议、路线优化建议

### 7.2 与预警模块集成

预警模块可调用`analyzeDrivingBehavior`进行驾驶行为分析：
- 输入：轨迹数据、驾驶时长、速度信息
- 输出：驾驶评分、异常行为识别、改进建议

### 7.3 与统计模块集成

统计模块可调用`analyzeStatisticsData`进行数据分析：
- 输入：统计数据（车辆数、行程数、里程等）
- 输出：数据概览、关键发现、趋势分析、优化建议

### 7.4 与成本模块集成

成本模块可调用以下接口：
- `analyzeCostData`: 成本分析
- `generateFinancialReport`: 财务报表生成
- `generateOptimizationSuggestions`: 优化建议

---

## 8. Python数据清洗服务

### 8.1 服务架构

Python服务作为独立进程运行，提供数据清洗、报表导出、数据分析功能。

**服务地址**: `http://localhost:8008`

### 8.2 数据清洗接口

| 接口 | 路径 | 说明 |
| - | - | - |
| 驾驶数据清洗 | POST /api/clean/driving-data | 清洗GPS轨迹数据 |
| 统计数据清洗 | POST /api/clean/statistics-data | 清洗统计数据 |
| 成本数据清洗 | POST /api/clean/cost-data | 清洗成本数据 |

**驾驶数据清洗流程**:
1. 移除缺失关键数据的点
2. 移除异常GPS坐标（经纬度超出合理范围）
3. 移除速度异常点（速度超过200km/h）
4. 按时间排序
5. 移除重复点
6. 计算统计数据

### 8.3 报表导出接口

| 接口 | 路径 | 说明 |
| - | - | - |
| 统计报表 | POST /api/export/statistics | 导出统计数据报表 |
| 行程报表 | POST /api/export/trip-report | 导出行程报表（多sheet） |
| 成本报表 | POST /api/export/cost-report | 导出成本报表（多sheet） |
| 车辆报表 | POST /api/export/vehicle-report | 导出车辆运营报表 |
| 司机报表 | POST /api/export/driver-report | 导出司机绩效报表 |

**报表格式**: 支持 xlsx、csv

**行程报表结构**:
- Sheet 1: 行程明细
- Sheet 2: 车辆汇总
- Sheet 3: 司机汇总
- Sheet 4: 日期汇总

### 8.4 数据分析接口

| 接口 | 路径 | 说明 |
| - | - | - |
| 驾驶行为分析 | POST /api/analysis/driving-behavior | 分析驾驶行为数据 |
| 成本分析 | POST /api/analysis/cost-analysis | 分析成本数据 |
| 车辆效率分析 | POST /api/analysis/vehicle-efficiency | 分析车辆效率 |
| AI提示词生成 | POST /api/analysis/ai-prompt | 生成AI分析提示词 |

---

## 9. 两种AI分析方案对比

### 9.1 方案一: Python清洗 + AI分析

**流程**:
1. Java服务收集原始数据
2. 调用Python服务进行数据清洗
3. Python返回清洗后的结构化数据
4. Java调用AI服务进行分析

**优点**:
- Token消耗低（数据已压缩）
- 分析质量高（数据预处理精准）
- 可复用清洗逻辑

**缺点**:
- 响应较慢（多一次网络调用）
- 运维复杂（多一个服务）

**适用场景**:
- 大量重复分析
- 标准化分析流程
- 驾驶行为分析

### 9.2 方案二: 数据直接给AI

**流程**:
1. Java服务收集数据
2. 直接调用AI服务进行分析

**优点**:
- 响应较快
- 实现简单
- 运维成本低

**缺点**:
- Token消耗高
- 依赖AI理解能力

**适用场景**:
- 临时性分析
- 小规模数据
- 调度建议/优化建议

### 9.3 推荐方案

| 分析类型 | 推荐方案 | 原因 |
| - | - | - |
| 驾驶行为分析 | 方案一 | 轨迹数据量大，清洗效果好 |
| 调度建议 | 方案二 | 文本类数据，AI理解能力强 |
| 成本分析 | 方案一 | 数据规范，清洗后更精准 |
| 统计分析 | 方案二 | 统计数据本身较规范 |
| 优化建议 | 方案二 | 依赖AI推理能力 |

---

## 10. Java服务集成接口

### 10.1 PythonServiceClient

```java
public interface PythonServiceClient {
    // 数据清洗
    Map<String, Object> cleanDrivingData(Map<String, Object> drivingData);
    Map<String, Object> cleanStatisticsData(Map<String, Object> statisticsData);
    Map<String, Object> cleanCostData(Map<String, Object> costData);
    
    // 数据分析
    Map<String, Object> analyzeDrivingBehavior(Map<String, Object> trackData);
    Map<String, Object> analyzeCost(Map<String, Object> costData);
    Map<String, Object> analyzeVehicleEfficiency(Map<String, Object> vehicleData);
    
    // 报表导出
    byte[] exportStatistics(Map<String, Object> exportRequest);
    byte[] exportTripReport(Map<String, Object> exportRequest);
    byte[] exportCostReport(Map<String, Object> exportRequest);
    byte[] exportVehicleReport(Map<String, Object> exportRequest);
    byte[] exportDriverReport(Map<String, Object> exportRequest);
}
```

### 10.2 导出接口调用示例

```java
// 导出行程报表
Map<String, Object> request = new HashMap<>();
request.put("data_type", "trip");
request.put("records", tripList);
request.put("format", "xlsx");
request.put("filename", "trip_report_20240321");

byte[] data = pythonServiceClient.exportTripReport(request);
// 返回给前端下载
```

---

## 11. 配置说明

### 11.1 Python服务配置

```yaml
python-service:
  url: http://localhost:8008
```

### 11.2 Python服务启动

```bash
cd python-service
pip install -r requirements.txt
python main.py
```

服务将在 `http://localhost:8008` 启动，可通过 `/health` 接口检查健康状态。
