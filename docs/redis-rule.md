# MineGuard Redis Key 规范定义

## 1. 命名规范

### 1.1 命名格式

```txt
mineguard:{module}:{key}:{identifier}
```

**命名规则**：

- 前缀：`mineguard`
- 模块：`user`、`vehicle`、`trip`、`warning`、`statistics`、`cost`
- 键名：具体业务键名
- 标识符：具体ID或参数

### 1.2 命名示例

| 模块 | Key格式 | 示例 | 说明 |
| ---- | ------- | ---- | ---- |
| 用户 | `mineguard:user:{key}:{id}` | `mineguard:user:info:1001` | 用户信息 |
| 车辆 | `mineguard:vehicle:{key}:{id}` | `mineguard:vehicle:status:1001` | 车辆状态 |
| 行程 | `mineguard:trip:{key}:{id}` | `mineguard:trip:info:3001` | 行程信息 |
| 预警 | `mineguard:warning:{key}:{id}` | `mineguard:warning:count:1001` | 预警数量 |
| 统计 | `mineguard:statistics:{key}:{date}` | `mineguard:statistics:overview:2024-01-15` | 统计概览 |
| 成本 | `mineguard:cost:{key}:{id}` | `mineguard:cost:summary:1001` | 成本汇总 |

---

## 2. 用户模块 (user)

### 2.1 用户信息

| Key | 类型 | 过期时间 | 说明 |
| ---- | ---- | ------- | ---- |
| `mineguard:user:info:{userId}` | Hash | 30分钟 | 用户基本信息 |
| `mineguard:user:token:{userId}` | String | 7天 | 用户登录Token |
| `mineguard:user:permission:{userId}` | Set | 30分钟 | 用户权限集合 |
| `mineguard:user:role:{userId}` | String | 30分钟 | 用户角色 |
| `mineguard:user:online:{userId}` | String | 30分钟 | 用户在线状态 |

**数据结构示例**：

```bash
# 用户信息
HSET mineguard:user:info:1001 id 1001
HSET mineguard:user:info:1001 username "admin"
HSET mineguard:user:info:1001 name "管理员"
HSET mineguard:user:info:1001 phone "13800138000"
HSET mineguard:user:info:1001 role "admin"
EXPIRE mineguard:user:info:1001 1800

# 用户Token
SET mineguard:user:token:1001 "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
EXPIRE mineguard:user:token:1001 604800

# 用户权限
SADD mineguard:user:permission:1001 "user:view"
SADD mineguard:user:permission:1001 "user:add"
SADD mineguard:user:permission:1001 "user:edit"
EXPIRE mineguard:user:permission:1001 1800

# 用户角色
SET mineguard:user:role:1001 "admin"
EXPIRE mineguard:user:role:1001 1800

# 用户在线状态
SET mineguard:user:online:1001 "online"
EXPIRE mineguard:user:online:1001 1800
```

---

## 3. 车辆模块 (vehicle)

### 3.1 车辆状态

| Key | 类型 | 过期时间 | 说明 |
| ---- | ---- | ------- | ---- |
| `mineguard:vehicle:status:{carId}` | Hash | 30秒 | 车辆实时状态 |
| `mineguard:vehicle:location:{carId}` | String | 30秒 | 车辆最新位置 |
| `mineguard:vehicle:online:{carId}` | String | 5分钟 | 车辆在线状态 |
| `mineguard:vehicle:info:{carId}` | Hash | 1小时 | 车辆基本信息 |
| `mineguard:vehicle:driver:{carId}` | String | 1小时 | 车辆当前司机 |

**数据结构示例**：

```bash
# 车辆实时状态
HSET mineguard:vehicle:status:1001 carId 1001
HSET mineguard:vehicle:status:1001 status "running"
HSET mineguard:vehicle:status:1001 speed 45.5
HSET mineguard:vehicle:status:1001 fuelLevel 75.2
HSET mineguard:vehicle:status:1001 direction 180
EXPIRE mineguard:vehicle:status:1001 30

# 车辆最新位置
SET mineguard:vehicle:location:1001 "114.123456,30.654321"
EXPIRE mineguard:vehicle:location:1001 30

# 车辆在线状态
SET mineguard:vehicle:online:1001 "online"
EXPIRE mineguard:vehicle:online:1001 300

# 车辆基本信息
HSET mineguard:vehicle:info:1001 carId 1001
HSET mineguard:vehicle:info:1001 carNumber "鄂A12345"
HSET mineguard:vehicle:info:1001 vehicleType "重型卡车"
HSET mineguard:vehicle:info:1001 model "东风天龙"
EXPIRE mineguard:vehicle:info:1001 3600

# 车辆当前司机
SET mineguard:vehicle:driver:1001 "2001"
EXPIRE mineguard:vehicle:driver:1001 3600
```

### 3.2 车辆列表

| Key | 类型 | 过期时间 | 说明 |
| ---- | ---- | ------- | ---- |
| `mineguard:vehicle:list:all` | Set | 10分钟 | 所有车辆ID集合 |
| `mineguard:vehicle:list:online` | Set | 5分钟 | 在线车辆ID集合 |
| `mineguard:vehicle:list:offline` | Set | 5分钟 | 离线车辆ID集合 |
| `mineguard:vehicle:list:running` | Set | 5分钟 | 运行中车辆ID集合 |

**数据结构示例**：

```bash
# 所有车辆ID集合
SADD mineguard:vehicle:list:all 1001 1002 1003 1004
EXPIRE mineguard:vehicle:list:all 600

# 在线车辆ID集合
SADD mineguard:vehicle:list:online 1001 1002 1003
EXPIRE mineguard:vehicle:list:online 300

# 离线车辆ID集合
SADD mineguard:vehicle:list:offline 1004
EXPIRE mineguard:vehicle:list:offline 300

# 运行中车辆ID集合
SADD mineguard:vehicle:list:running 1001 1002
EXPIRE mineguard:vehicle:list:running 300
```

---

## 4. 行程模块 (trip)

### 4.1 行程信息

| Key | 类型 | 过期时间 | 说明 |
| ---- | ---- | ------- | ---- |
| `mineguard:trip:info:{tripId}` | Hash | 2小时 | 行程基本信息 |
| `mineguard:trip:status:{tripId}` | String | 2小时 | 行程状态 |
| `mineguard:trip:route:{tripId}` | String | 2小时 | 行程路线 |
| `mineguard:trip:driver:{tripId}` | String | 2小时 | 行程司机 |

**数据结构示例**：

```bash
# 行程基本信息
HSET mineguard:trip:info:3001 tripId 3001
HSET mineguard:trip:info:3001 carId 1001
HSET mineguard:trip:info:3001 driverId 2001
HSET mineguard:trip:info:3001 startPoint "武汉市"
HSET mineguard:trip:info:3001 endPoint "宜昌市"
EXPIRE mineguard:trip:info:3001 7200

# 行程状态
SET mineguard:trip:status:3001 "in_progress"
EXPIRE mineguard:trip:status:3001 7200

# 行程路线
SET mineguard:trip:route:3001 "114.123456,30.654321|114.234567,30.7654321"
EXPIRE mineguard:trip:route:3001 7200

# 行程司机
SET mineguard:trip:driver:3001 "2001"
EXPIRE mineguard:trip:driver:3001 7200
```

---

## 5. 预警模块 (warning)

### 5.1 预警信息

| Key | 类型 | 过期时间 | 说明 |
| ---- | ---- | ------- | ---- |
| `mineguard:warning:count:{carId}` | String | 1小时 | 车辆预警数量 |
| `mineguard:warning:latest:{carId}` | String | 1小时 | 车辆最新预警 |
| `mineguard:warning:unhandled:count` | String | 10分钟 | 未处理预警总数 |
| `mineguard:warning:level:{level}` | Set | 10分钟 | 按级别分类预警ID |

**数据结构示例**：

```bash
# 车辆预警数量
SET mineguard:warning:count:1001 "5"
EXPIRE mineguard:warning:count:1001 3600

# 车辆最新预警
SET mineguard:warning:latest:1001 "speed_exceed"
EXPIRE mineguard:warning:latest:1001 3600

# 未处理预警总数
SET mineguard:warning:unhandled:count "12"
EXPIRE mineguard:warning:unhandled:count 600

# 按级别分类预警ID
SADD mineguard:warning:level:high 5001 5002 5003
SADD mineguard:warning:level:medium 5004 5005
SADD mineguard:warning:level:low 5006 5007
EXPIRE mineguard:warning:level:high 600
EXPIRE mineguard:warning:level:medium 600
EXPIRE mineguard:warning:level:low 600
```

---

## 6. 统计模块 (statistics)

### 6.1 统计数据

| Key | 类型 | 过期时间 | 说明 |
| ---- | ---- | ------- | ---- |
| `mineguard:statistics:overview:{date}` | Hash | 1小时 | 数据概览 |
| `mineguard:statistics:trip:{date}` | Hash | 1小时 | 行程统计 |
| `mineguard:statistics:warning:{date}` | Hash | 1小时 | 预警统计 |
| `mineguard:statistics:cost:{date}` | Hash | 1小时 | 成本统计 |
| `mineguard:statistics:vehicle:{date}` | Hash | 1小时 | 车辆统计 |

**数据结构示例**：

```bash
# 数据概览
HSET mineguard:statistics:overview:2024-01-15 vehicleCount 128
HSET mineguard:statistics:overview:2024-01-15 onlineCount 96
HSET mineguard:statistics:overview:2024-01-15 offlineCount 32
HSET mineguard:statistics:overview:2024-01-15 tripCount 45
EXPIRE mineguard:statistics:overview:2024-01-15 3600

# 行程统计
HSET mineguard:statistics:trip:2024-01-15 totalTrips 1000
HSET mineguard:statistics:trip:2024-01-15 completedTrips 950
HSET mineguard:statistics:trip:2024-01-15 inProgressTrips 45
HSET mineguard:statistics:trip:2024-01-15 totalDistance 125000
EXPIRE mineguard:statistics:trip:2024-01-15 3600

# 预警统计
HSET mineguard:statistics:warning:2024-01-15 totalWarnings 50
HSET mineguard:statistics:warning:2024-01-15 handledWarnings 45
HSET mineguard:statistics:warning:2024-01-15 unhandledWarnings 5
HSET mineguard:statistics:warning:2024-01-15 highLevelWarnings 10
EXPIRE mineguard:statistics:warning:2024-01-15 3600

# 成本统计
HSET mineguard:statistics:cost:2024-01-15 totalCost 100000
HSET mineguard:statistics:cost:2024-01-15 fuelCost 60000
HSET mineguard:statistics:cost:2024-01-15 maintenanceCost 25000
HSET mineguard:statistics:cost:2024-01-15 laborCost 15000
EXPIRE mineguard:statistics:cost:2024-01-15 3600

# 车辆统计
HSET mineguard:statistics:vehicle:2024-01-15 totalVehicles 128
HSET mineguard:statistics:vehicle:2024-01-15 activeVehicles 96
HSET mineguard:statistics:vehicle:2024-01-15 idleVehicles 32
EXPIRE mineguard:statistics:vehicle:2024-01-15 3600
```

---

## 7. 成本模块 (cost)

### 7.1 成本信息

| Key | 类型 | 过期时间 | 说明 |
| ---- | ---- | ------- | ---- |
| `mineguard:cost:summary:{carId}` | Hash | 1天 | 车辆成本汇总 |
| `mineguard:cost:summary:{tripId}` | Hash | 1天 | 行程成本汇总 |
| `mineguard:cost:monthly:{carId}:{month}` | Hash | 7天 | 车辆月度成本 |

**数据结构示例**：

```bash
# 车辆成本汇总
HSET mineguard:cost:summary:1001 totalCost 50000
HSET mineguard:cost:summary:1001 fuelCost 30000
HSET mineguard:cost:summary:1001 maintenanceCost 15000
HSET mineguard:cost:summary:1001 otherCost 5000
EXPIRE mineguard:cost:summary:1001 86400

# 行程成本汇总
HSET mineguard:cost:summary:3001 totalCost 500
HSET mineguard:cost:summary:3001 fuelCost 300
HSET mineguard:cost:summary:3001 tollCost 150
HSET mineguard:cost:summary:3001 otherCost 50
EXPIRE mineguard:cost:summary:3001 86400

# 车辆月度成本
HSET mineguard:cost:monthly:1001:2024-01 totalCost 100000
HSET mineguard:cost:monthly:1001:2024-01 fuelCost 60000
HSET mineguard:cost:monthly:1001:2024-01 maintenanceCost 30000
HSET mineguard:cost:monthly:1001:2024-01 otherCost 10000
EXPIRE mineguard:cost:monthly:1001:2024-01 604800
```

---

## 8. 分布式锁

### 8.1 锁定义

| Key | 类型 | 过期时间 | 说明 |
| ---- | ---- | ------- | ---- |
| `mineguard:lock:vehicle:{carId}` | String | 30秒 | 车辆操作锁 |
| `mineguard:lock:trip:{tripId}` | String | 30秒 | 行程操作锁 |
| `mineguard:lock:warning:{warningId}` | String | 30秒 | 预警操作锁 |
| `mineguard:lock:user:{userId}` | String | 30秒 | 用户操作锁 |

**数据结构示例**：

```bash
# 车辆操作锁
SET mineguard:lock:vehicle:1001 "locked" NX EX 30

# 行程操作锁
SET mineguard:lock:trip:3001 "locked" NX EX 30

# 预警操作锁
SET mineguard:lock:warning:5001 "locked" NX EX 30

# 用户操作锁
SET mineguard:lock:user:1001 "locked" NX EX 30
```

---

## 9. 限流器

### 9.1 限流定义

| Key | 类型 | 过期时间 | 说明 |
| ---- | ---- | ------- | ---- |
| `mineguard:ratelimit:api:{userId}:{endpoint}` | String | 1分钟 | API限流 |
| `mineguard:ratelimit:login:{ip}` | String | 5分钟 | 登录限流 |
| `mineguard:ratelimit:sms:{phone}` | String | 1小时 | 短信限流 |

**数据结构示例**：

```bash
# API限流
INCR mineguard:ratelimit:api:1001:/api/vehicle/list
EXPIRE mineguard:ratelimit:api:1001:/api/vehicle/list 60

# 登录限流
INCR mineguard:ratelimit:login:192.168.1.100
EXPIRE mineguard:ratelimit:login:192.168.1.100 300

# 短信限流
INCR mineguard:ratelimit:sms:13800138000
EXPIRE mineguard:ratelimit:sms:13800138000 3600
```

---

## 10. 消息队列

### 10.1 消息主题

| 主题 | 说明 |
| ---- | ---- |
| `mineguard:mq:vehicle:status` | 车辆状态变更 |
| `mineguard:mq:vehicle:location` | 车辆位置上报 |
| `mineguard:mq:warning:notification` | 预警通知 |
| `mineguard:mq:trip:completed` | 行程完成 |
| `mineguard:mq:trip:updated` | 行程更新 |
| `mineguard:mq:statistics:update` | 统计数据更新 |
| `mineguard:mq:cost:update` | 成本数据更新 |

---

## 11. 缓存策略

### 11.1 缓存更新策略

| 策略 | 说明 | 适用场景 |
| ---- | ---- | ------- |
| Cache Aside | 先查缓存，缓存未命中再查数据库，更新数据库后更新缓存 | 读多写少 |
| Write Through | 写入时同时更新缓存和数据库 | 写入频繁 |
| Write Behind | 写入时先更新缓存，异步批量写入数据库 | 高并发写入 |

### 11.2 缓存失效策略

| 策略 | 说明 | 适用场景 |
| ---- | ---- | ------- |
| 主动失效 | 数据变更时主动删除缓存 | 数据一致性要求高 |
| 被动失效 | 缓存过期后重新加载 | 数据一致性要求一般 |
| 定时刷新 | 定时任务刷新缓存 | 数据更新频率固定 |

---

## 12. 监控指标

### 12.1 监控Key

| 指标 | 说明 | 告警阈值 |
| ---- | ---- | ------- |
| Key数量 | Redis中Key的总数 | > 1000000 |
| 内存使用 | Redis内存使用量 | > 8GB |
| 命中率 | 缓存命中率 | < 80% |
| 连接数 | Redis连接数 | > 1000 |
| 慢查询 | 慢查询数量 | > 100/min |

### 12.2 监控命令

```bash
# 查看Key数量
DBSIZE

# 查看内存使用
INFO memory

# 查看命中率
INFO stats

# 查看慢查询
SLOWLOG GET 10
```

---

## 13. 最佳实践

### 13.1 Key设计原则

- 使用冒号分隔层级，提高可读性
- Key长度控制在100字符以内
- 避免使用特殊字符
- 使用统一前缀，便于管理
- 合理设置过期时间，避免内存泄漏

### 13.2 数据类型选择

| 数据类型 | 适用场景 | 示例 |
| ------- | ------- | ---- |
| String | 简单值、计数器 | Token、状态、计数 |
| Hash | 对象属性 | 用户信息、车辆信息 |
| List | 有序列表 | 消息队列、排行榜 |
| Set | 去重集合 | 权限集合、标签集合 |
| Sorted Set | 排行榜、范围查询 | 排行榜、时间序列 |

### 13.3 性能优化

- 使用Pipeline批量操作，减少网络开销
- 合理设置过期时间，避免内存泄漏
- 使用Lua脚本保证原子性
- 避免使用KEYS命令，使用SCAN替代
- 合理使用数据结构，减少内存占用
