# MineGuard Redis Key 规范定义

## 1. 命名规范

### 1.1 命名格式

```txt
mineguard:{module}:{id}:{key}:{identifier}
```

**命名规则**：

- 前缀：`mineguard`
- 模块：`user`、`vehicle`、`trip`、`warning`、`rate`、`lock`、`cache`
- ID：具体业务ID（如用户ID、车辆ID）
- 键名：具体业务键名
- 标识符：具体参数

### 1.2 命名示例

| 模块 | Key格式 | 示例 | 说明 |
| ---- | ------- | ---- | ---- |
| 用户 | `mineguard:user:{userId}:{key}:{identifier}` | `mineguard:user:1001:token:abc123` | 用户Token |
| 用户 | `mineguard:user:{userId}:{key}:{identifier}` | `mineguard:user:1001:permission:perm_001` | 用户权限 |
| 用户 | `mineguard:user:{userId}:{key}:{identifier}` | `mineguard:user:1001:role:role_001` | 用户角色 |
| 车辆 | `mineguard:vehicle:{vehicleId}:{key}:{identifier}` | `mineguard:vehicle:2001:status:status_001` | 车辆状态 |
| 车辆 | `mineguard:vehicle:{vehicleId}:{key}:{identifier}` | `mineguard:vehicle:2001:location:loc_001` | 车辆位置 |
| 行程 | `mineguard:trip:{tripId}:{key}:{identifier}` | `mineguard:trip:3001:status:status_001` | 行程状态 |
| 预警 | `mineguard:warning:{warningId}:{key}:{identifier}` | `mineguard:warning:4001:info:info_001` | 预警信息 |
| 分布式锁 | `mineguard:lock:{lockName}` | `mineguard:lock:order_123` | 分布式锁 |
| 限流 | `mineguard:rate:limit:{limitName}` | `mineguard:rate:limit:api_user_1001` | API限流 |
| 缓存刷新 | `mineguard:cache:refresh:{cacheName}` | `mineguard:cache:refresh:user_info` | 缓存刷新锁 |

---

## 2. 键名格式常量

### 2.1 RedisConstants.java 定义

```java
public class RedisConstants {
    
    // 键前缀
    public static final String PREFIX = "mineguard:";
    
    // 用户Token键: mineguard:user:{userId}:token:{token}
    public static final String USER_TOKEN_KEY = PREFIX + "user:%d:token:%s";
    
    // 用户权限键: mineguard:user:{userId}:permission:{permissionId}
    public static final String USER_PERMISSION_KEY = PREFIX + "user:%d:permission:%s";
    
    // 用户角色键: mineguard:user:{userId}:role:{roleId}
    public static final String USER_ROLE_KEY = PREFIX + "user:%d:role:%s";
    
    // 车辆实时状态键: mineguard:vehicle:{vehicleId}:status:{statusId}
    public static final String VEHICLE_STATUS_KEY = PREFIX + "vehicle:%d:status:%s";
    
    // 车辆位置键: mineguard:vehicle:{vehicleId}:location:{locationId}
    public static final String VEHICLE_LOCATION_KEY = PREFIX + "vehicle:%d:location:%s";
    
    // 行程状态键: mineguard:trip:{tripId}:status:{statusId}
    public static final String TRIP_STATUS_KEY = PREFIX + "trip:%d:status:%s";
    
    // 预警信息键: mineguard:warning:{warningId}:info:{infoId}
    public static final String WARNING_INFO_KEY = PREFIX + "warning:%d:info:%s";
    
    // 分布式锁键: mineguard:lock:{lockName}
    public static final String DISTRIBUTED_LOCK_KEY = PREFIX + "lock:%s";
    
    // 限流键: mineguard:rate:limit:{limitName}
    public static final String RATE_LIMIT_KEY = PREFIX + "rate:limit:%s";
    
    // 缓存刷新锁键: mineguard:cache:refresh:{cacheName}
    public static final String CACHE_REFRESH_LOCK_KEY = PREFIX + "cache:refresh:%s";
}
```

---

## 3. 过期时间常量

### 3.1 RedisConstants.java 定义

```java
public class RedisConstants {
    
    // 预设过期时间（秒）
    public static final long EXPIRE_SHORT = 600;      // 10分钟
    public static final long EXPIRE_DEFAULT = 3600;   // 1小时
    public static final long EXPIRE_LONG = 86400;     // 1天
    
    // 时间常量（秒）
    public static final long MINUTE_1 = 60;           // 1分钟
    public static final long MINUTE_5 = 300;          // 5分钟
    public static final long MINUTE_10 = 600;         // 10分钟
    public static final long MINUTE_30 = 1800;        // 30分钟
    public static final long HOUR_1 = 3600;           // 1小时
    public static final long HOUR_2 = 7200;           // 2小时
    public static final long HOUR_6 = 21600;          // 6小时
    public static final long HOUR_12 = 43200;         // 12小时
    public static final long DAY_1 = 86400;           // 1天
    public static final long DAY_3 = 259200;          // 3天
    public static final long DAY_7 = 604800;          // 7天
    public static final long DAY_30 = 2592000;        // 30天
}
```

### 3.2 过期时间使用建议

| 过期时间 | 常量 | 适用场景 |
| ------- | ---- | ------- |
| 10分钟 | `EXPIRE_SHORT` | 短期缓存、验证码 |
| 1小时 | `EXPIRE_DEFAULT` | 默认缓存时间 |
| 1天 | `EXPIRE_LONG` | 长期缓存 |
| 7天 | `DAY_7` | 用户Token |
| 30分钟 | `MINUTE_30` | 用户会话 |
| 5分钟 | `MINUTE_5` | 在线状态 |

---

## 4. 错误码定义

### 4.1 RedisResultCode.java 定义

错误码范围：**900-999**

| 错误码 | 枚举值 | 描述 |
| ------ | ------ | ---- |
| **通用错误** | | |
| 900 | `REDIS_ERROR` | Redis操作失败 |
| **缓存操作错误 (901-909)** | | |
| 901 | `CACHE_MISS` | 缓存未命中 |
| 902 | `CACHE_OPERATION_FAILED` | 缓存操作失败 |
| 903 | `CACHE_GET_FAILED` | 缓存获取失败 |
| 904 | `CACHE_SET_FAILED` | 缓存设置失败 |
| 905 | `CACHE_DELETE_FAILED` | 缓存删除失败 |
| 906 | `CACHE_EXPIRE_FAILED` | 缓存过期设置失败 |
| 907 | `CACHE_CLEAR_FAILED` | 缓存清理失败 |
| **分布式锁错误 (910-919)** | | |
| 910 | `LOCK_ACQUIRE_FAILED` | 获取锁失败 |
| 911 | `LOCK_RELEASE_FAILED` | 释放锁失败 |
| 912 | `LOCK_EXPIRED` | 锁已过期 |
| 913 | `LOCK_NOT_OWNER` | 不是锁的所有者 |
| 914 | `LOCK_TIMEOUT` | 获取锁超时 |
| **限流错误 (920-929)** | | |
| 920 | `RATE_LIMIT_EXCEEDED` | 超过限流阈值 |
| 921 | `RATE_LIMIT_CONFIG_ERROR` | 限流配置错误 |
| 922 | `RATE_LIMIT_RESET_FAILED` | 限流重置失败 |
| **连接错误 (930-939)** | | |
| 930 | `CONNECTION_ERROR` | 连接错误 |
| 931 | `CONNECTION_TIMEOUT` | 连接超时 |
| 932 | `CONNECTION_CLOSED` | 连接已关闭 |
| 933 | `CONNECTION_POOL_EXHAUSTED` | 连接池耗尽 |
| **序列化错误 (940-949)** | | |
| 940 | `SERIALIZATION_ERROR` | 序列化错误 |
| 941 | `DESERIALIZATION_ERROR` | 反序列化错误 |
| 942 | `SERIALIZER_NOT_FOUND` | 序列化器未找到 |
| **其他错误 (950-959)** | | |
| 950 | `COMMAND_EXECUTION_ERROR` | 命令执行错误 |
| 951 | `KEY_NOT_FOUND` | 键不存在 |
| 952 | `INVALID_KEY` | 无效的键 |
| 953 | `INVALID_VALUE` | 无效的值 |
| 954 | `OPERATION_NOT_SUPPORTED` | 不支持的操作 |

---

## 5. 用户模块 (user)

### 5.1 用户信息

| Key | 类型 | 过期时间 | 说明 |
| ---- | ---- | ------- | ---- |
| `mineguard:user:{userId}:token:{token}` | String | 7天 | 用户登录Token |
| `mineguard:user:{userId}:permission:{permissionId}` | Set | 30分钟 | 用户权限集合 |
| `mineguard:user:{userId}:role:{roleId}` | String | 30分钟 | 用户角色 |

**数据结构示例**：

```bash
# 用户Token
SET mineguard:user:1001:token:abc123 "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
EXPIRE mineguard:user:1001:token:abc123 604800

# 用户权限
SADD mineguard:user:1001:permission:perm_001 "user:view"
SADD mineguard:user:1001:permission:perm_001 "user:add"
EXPIRE mineguard:user:1001:permission:perm_001 1800

# 用户角色
SET mineguard:user:1001:role:role_001 "admin"
EXPIRE mineguard:user:1001:role:role_001 1800
```

---

## 6. 车辆模块 (vehicle)

### 6.1 车辆状态

| Key | 类型 | 过期时间 | 说明 |
| ---- | ---- | ------- | ---- |
| `mineguard:vehicle:{vehicleId}:status:{statusId}` | Hash | 30秒 | 车辆实时状态 |
| `mineguard:vehicle:{vehicleId}:location:{locationId}` | String | 30秒 | 车辆最新位置 |

**数据结构示例**：

```bash
# 车辆实时状态
HSET mineguard:vehicle:2001:status:status_001 status "running"
HSET mineguard:vehicle:2001:status:status_001 speed 45.5
HSET mineguard:vehicle:2001:status:status_001 fuelLevel 75.2
EXPIRE mineguard:vehicle:2001:status:status_001 30

# 车辆最新位置
SET mineguard:vehicle:2001:location:loc_001 "114.123456,30.654321"
EXPIRE mineguard:vehicle:2001:location:loc_001 30
```

---

## 7. 行程模块 (trip)

### 7.1 行程信息

| Key | 类型 | 过期时间 | 说明 |
| ---- | ---- | ------- | ---- |
| `mineguard:trip:{tripId}:status:{statusId}` | String | 2小时 | 行程状态 |

**数据结构示例**：

```bash
# 行程状态
SET mineguard:trip:3001:status:status_001 "in_progress"
EXPIRE mineguard:trip:3001:status:status_001 7200
```

---

## 8. 预警模块 (warning)

### 8.1 预警信息

| Key | 类型 | 过期时间 | 说明 |
| ---- | ---- | ------- | ---- |
| `mineguard:warning:{warningId}:info:{infoId}` | Hash | 1小时 | 预警详情 |

**数据结构示例**：

```bash
# 预警详情
HSET mineguard:warning:4001:info:info_001 type "speed_exceed"
HSET mineguard:warning:4001:info:info_001 level "high"
HSET mineguard:warning:4001:info:info_001 description "车辆速度超过限速值"
EXPIRE mineguard:warning:4001:info:info_001 3600
```

---

## 9. 分布式锁

### 9.1 锁定义

| Key | 类型 | 过期时间 | 说明 |
| ---- | ---- | ------- | ---- |
| `mineguard:lock:{lockName}` | String | 30秒 | 分布式锁 |

**使用示例**：

```java
// Redisson锁示例
@Autowired
private RedissonLockService redissonLockService;

public void processOrder(String orderId) {
    String lockKey = RedisKeyUtil.generateDistributedLockKey("order:" + orderId);
    boolean locked = redissonLockService.tryLock(lockKey, 5, 30, TimeUnit.SECONDS);
    if (locked) {
        try {
            // 处理订单
        } finally {
            redissonLockService.unlock(lockKey);
        }
    }
}
```

---

## 10. 限流器

### 10.1 限流定义

| Key | 类型 | 过期时间 | 说明 |
| ---- | ---- | ------- | ---- |
| `mineguard:rate:limit:{limitName}` | ZSet | 按窗口 | 滑动窗口限流 |
| `mineguard:rate:limit:token:{limitName}` | String | 按窗口 | 令牌桶限流 |

**使用示例**：

```java
// 声明式限流
@RateLimit(keyPrefix = "api", keySuffix = "#userId", limit = 10, window = 60)
public void submitOrder(Long userId, Order order) {
    // 业务逻辑
}

// 编程式限流
@Autowired
private RedisRateLimiter redisRateLimiter;

public boolean checkRateLimit(String key, int limit, long window) {
    return redisRateLimiter.tryAcquire(key, limit, window, TimeUnit.SECONDS);
}
```

---

## 11. 缓存策略

### 11.1 声明式缓存

```java
// 缓存示例
@Cacheable(keyPrefix = "user", keySuffix = "#userId", expire = 3600)
public User getUserById(Long userId) {
    return userMapper.selectById(userId);
}

// 清除缓存示例
@CacheEvict(keyPrefix = "user", keySuffix = "#userId")
public void updateUser(Long userId, User user) {
    userMapper.updateById(user);
}
```

### 11.2 编程式缓存

```java
@Autowired
private RedisCacheService redisCacheService;

// 设置缓存
redisCacheService.set("user:1001", user, RedisConstants.HOUR_1, TimeUnit.SECONDS);

// 获取缓存
User user = redisCacheService.get("user:1001", User.class);

// 删除缓存
redisCacheService.delete("user:1001");
```

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
- 使用统一前缀`mineguard:`，便于管理
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

---

## 14. 变更记录

| 日期 | 变更内容 |
| ---- | ------- |
| 2026-03-04 | 更新键名格式，与RedisConstants.java保持一致 |
| 2026-03-04 | 添加过期时间常量定义 |
| 2026-03-04 | 添加错误码定义（900-999范围） |
| 2026-03-04 | 添加声明式缓存和限流使用示例 |
