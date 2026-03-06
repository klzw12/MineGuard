# MineGuard 错误码分配规范

## 1. 错误码范围总览

| 模块 | 错误码范围 | 枚举类 |
| ---- | --------- | ------ |
| core | 200, 400-605 | `ResultCodeEnum` |
| web | 701-727 | `WebResultCode` |
| auth | 800-854 | `AuthResultCode` |
| redis | 900-954 | `RedisResultCode` |
| database | 1000-1064 | `DatabaseResultCode` |
| file | 1200-1219 | `FileResultCode` |
| mq | 1100-1151 | `MqResultCode` |
| mongodb | 1300-1374 | `MongoDbResultCode` |
| websocket | 1500-1519 | `WebSocketResultCode` |

## 2. 预留范围

| 范围 | 用途 | 状态 |
|------|------|------|
| 700-799 | Web层扩展 | 已使用 701-727 |
| 800-899 | 认证模块 | 已使用 800-854 |
| 900-999 | Redis模块 | 已使用 900-954 |
| 1000-1099 | 数据库模块 | 已使用 1000-1064 |
| 1100-1199 | 消息队列模块 | 已使用 1100-1151 |
| 1200-1299 | 文件存储模块 | 已使用 1200-1219 |
| 1300-1399 | MongoDB模块 | 已使用 1300-1374 |
| 1400-1499 | 预留 | 未使用 |
| 1500-1599 | WebSocket模块 | 已使用 1500-1519 |
| 1600-1999 | 预留 | 未使用 |
| 2000-3999 | 业务模块预留 | 未使用 |

## 3. 各模块错误码详情

### 3.1 Core 核心模块 (ResultCodeEnum)

| 错误码 | 常量名 | 说明 |
|--------|--------|------|
| 200 | SUCCESS | 操作成功 |
| 400 | FAIL | 操作失败 |
| 401 | UNAUTHORIZED | 未授权 |
| 403 | FORBIDDEN | 禁止访问 |
| 404 | NOT_FOUND | 资源不存在 |
| 500 | INTERNAL_ERROR | 系统内部错误 |
| 600 | BUSINESS_ERROR | 业务错误 |
| 601 | PARAM_ERROR | 参数错误 |
| 602 | DATA_ERROR | 数据错误 |
| 603 | PERMISSION_ERROR | 权限错误 |
| 604 | UNKNOWN_ERROR | 未知错误 |
| 605 | SERVICE_UNAVAILABLE | 服务不可用 |

### 3.2 Web 模块 (WebResultCode)

| 错误码 | 常量名 | 说明 |
|--------|--------|------|
| 701 | PARAM_MISSING | 参数缺失 |
| 702 | PARAM_TYPE_ERROR | 参数类型错误 |
| 703 | PARAM_VALUE_ERROR | 参数值错误 |
| 704 | PARAM_FORMAT_ERROR | 参数格式错误 |
| 705 | PARAM_VALIDATION_ERROR | 参数验证错误 |
| 706 | FILE_UPLOAD_ERROR | 文件上传错误 |
| 707 | FILE_SIZE_EXCEEDED | 文件大小超限 |
| 708 | FILE_TYPE_NOT_ALLOWED | 文件类型不允许 |
| 709 | FILE_NOT_FOUND | 文件不存在 |
| 710 | REQUEST_TIMEOUT | 请求超时 |
| 711 | NETWORK_ERROR | 网络错误 |
| 712 | SERVER_ERROR | 服务器内部错误 |
| 713 | SERVICE_UNAVAILABLE | 服务不可用 |
| 714 | GATEWAY_ERROR | 网关错误 |
| 721 | METHOD_NOT_ALLOWED | 方法不允许 |
| 722 | UNSUPPORTED_MEDIA_TYPE | 不支持的媒体类型 |
| 724 | TOO_MANY_REQUESTS | 请求过于频繁 |
| 727 | CORS_ERROR | 跨域请求错误 |

### 3.3 Auth 认证模块 (AuthResultCode)

| 错误码 | 常量名 | 说明 |
|--------|--------|------|
| 800 | AUTH_ERROR | 认证操作失败 |
| 801 | TOKEN_EXPIRED | Token已过期 |
| 802 | TOKEN_INVALID | Token无效 |
| 803 | TOKEN_MISSING | Token缺失 |
| 804 | TOKEN_SIGNATURE_ERROR | Token签名错误 |
| 805 | TOKEN_PARSE_ERROR | Token解析错误 |
| 806 | TOKEN_REVOKED | Token已被撤销 |
| 810 | PERMISSION_DENIED | 权限不足 |
| 811 | PERMISSION_NOT_FOUND | 权限不存在 |
| 812 | PERMISSION_EXPIRED | 权限已过期 |
| 813 | PERMISSION_DISABLED | 权限已禁用 |
| 820 | ROLE_DENIED | 角色不足 |
| 821 | ROLE_NOT_FOUND | 角色不存在 |
| 822 | ROLE_DISABLED | 角色已禁用 |
| 823 | ROLE_EXPIRED | 角色已过期 |
| 830 | USER_NOT_FOUND | 用户不存在 |
| 831 | PASSWORD_ERROR | 密码错误 |
| 832 | PASSWORD_EXPIRED | 密码已过期 |
| 833 | PASSWORD_TOO_WEAK | 密码强度不足 |
| 834 | USERNAME_TAKEN | 用户名已被使用 |
| 835 | EMAIL_TAKEN | 邮箱已被使用 |
| 836 | PHONE_TAKEN | 手机号已被使用 |
| 840 | ACCOUNT_LOCKED | 账户已锁定 |
| 841 | ACCOUNT_DISABLED | 账户已禁用 |
| 842 | ACCOUNT_EXPIRED | 账户已过期 |
| 843 | ACCOUNT_NOT_ACTIVATED | 账户未激活 |
| 844 | ACCOUNT_TOO_MANY_ATTEMPTS | 账户尝试次数过多 |
| 850 | LOGIN_FAILED | 登录失败 |
| 851 | LOGOUT_FAILED | 登出失败 |
| 852 | REGISTER_FAILED | 注册失败 |
| 853 | VERIFICATION_FAILED | 验证失败 |
| 854 | SESSION_EXPIRED | 会话已过期 |

### 3.4 Redis 模块 (RedisResultCode)

| 错误码 | 常量名 | 说明 |
|--------|--------|------|
| 900 | REDIS_ERROR | Redis操作失败 |
| 901 | CACHE_MISS | 缓存未命中 |
| 902 | CACHE_OPERATION_FAILED | 缓存操作失败 |
| 903 | CACHE_GET_FAILED | 缓存获取失败 |
| 904 | CACHE_SET_FAILED | 缓存设置失败 |
| 905 | CACHE_DELETE_FAILED | 缓存删除失败 |
| 906 | CACHE_EXPIRE_FAILED | 缓存过期设置失败 |
| 907 | CACHE_CLEAR_FAILED | 缓存清理失败 |
| 910 | LOCK_ACQUIRE_FAILED | 获取锁失败 |
| 911 | LOCK_RELEASE_FAILED | 释放锁失败 |
| 912 | LOCK_EXPIRED | 锁已过期 |
| 913 | LOCK_NOT_OWNER | 不是锁的所有者 |
| 914 | LOCK_TIMEOUT | 获取锁超时 |
| 920 | RATE_LIMIT_EXCEEDED | 超过限流阈值 |
| 921 | RATE_LIMIT_CONFIG_ERROR | 限流配置错误 |
| 922 | RATE_LIMIT_RESET_FAILED | 限流重置失败 |
| 930 | CONNECTION_ERROR | 连接错误 |
| 931 | CONNECTION_TIMEOUT | 连接超时 |
| 932 | CONNECTION_CLOSED | 连接已关闭 |
| 933 | CONNECTION_POOL_EXHAUSTED | 连接池耗尽 |
| 940 | SERIALIZATION_ERROR | 序列化错误 |
| 941 | DESERIALIZATION_ERROR | 反序列化错误 |
| 942 | SERIALIZER_NOT_FOUND | 序列化器未找到 |
| 950 | COMMAND_EXECUTION_ERROR | 命令执行错误 |
| 951 | KEY_NOT_FOUND | 键不存在 |
| 952 | INVALID_KEY | 无效的键 |
| 953 | INVALID_VALUE | 无效的值 |
| 954 | OPERATION_NOT_SUPPORTED | 不支持的操作 |

### 3.5 Database 数据库模块 (DatabaseResultCode)

| 错误码 | 常量名 | 说明 |
|--------|--------|------|
| 1000 | DATABASE_ERROR | 数据库操作失败 |
| 1001 | DATABASE_CONNECTION_ERROR | 数据库连接失败 |
| 1002 | CONNECTION_TIMEOUT | 数据库连接超时 |
| 1003 | CONNECTION_POOL_EXHAUSTED | 数据库连接池耗尽 |
| 1004 | CONNECTION_CLOSED | 数据库连接已关闭 |
| 1005 | INVALID_CONNECTION_CONFIG | 数据库连接配置无效 |
| 1010 | SQL_EXECUTION_ERROR | SQL执行失败 |
| 1011 | SQL_SYNTAX_ERROR | SQL语法错误 |
| 1012 | QUERY_TIMEOUT | 查询超时 |
| 1013 | UPDATE_TIMEOUT | 更新超时 |
| 1014 | BATCH_EXECUTION_ERROR | 批量执行失败 |
| 1020 | TRANSACTION_ERROR | 事务执行失败 |
| 1021 | TRANSACTION_TIMEOUT | 事务超时 |
| 1022 | TRANSACTION_ROLLBACK | 事务回滚 |
| 1023 | TRANSACTION_NOT_ACTIVE | 事务未激活 |
| 1024 | NESTED_TRANSACTION_ERROR | 嵌套事务错误 |
| 1030 | DATA_SOURCE_ERROR | 数据源配置错误 |
| 1031 | DATA_SOURCE_NOT_FOUND | 数据源不存在 |
| 1032 | DATA_SOURCE_SWITCH_ERROR | 数据源切换失败 |
| 1033 | MASTER_SLAVE_SYNC_ERROR | 主从同步错误 |
| 1034 | READ_WRITE_SPLIT_ERROR | 读写分离错误 |
| 1040 | PAGINATION_ERROR | 分页参数错误 |
| 1041 | PAGE_NUMBER_INVALID | 页码无效 |
| 1042 | PAGE_SIZE_INVALID | 每页大小无效 |
| 1043 | PAGE_SIZE_EXCEEDED | 每页大小超过限制 |
| 1044 | SORT_FIELD_INVALID | 排序字段无效 |
| 1050 | RECORD_NOT_FOUND | 记录不存在 |
| 1051 | DUPLICATE_KEY_ERROR | 主键冲突 |
| 1052 | DATA_INTEGRITY_ERROR | 数据完整性错误 |
| 1053 | FOREIGN_KEY_CONSTRAINT_ERROR | 外键约束错误 |
| 1054 | UNIQUE_CONSTRAINT_ERROR | 唯一约束错误 |
| 1055 | NULL_VALUE_ERROR | 空值错误 |
| 1060 | MAPPER_NOT_FOUND | Mapper未找到 |
| 1061 | ENTITY_NOT_FOUND | 实体类未找到 |
| 1062 | TABLE_NOT_FOUND | 表不存在 |
| 1063 | FIELD_NOT_FOUND | 字段不存在 |
| 1064 | TYPE_HANDLER_ERROR | 类型处理器错误 |

### 3.6 MQ 消息队列模块 (MqResultCode)

| 错误码 | 常量名 | 说明 |
|--------|--------|------|
| 1100 | MQ_ERROR | 消息队列操作失败 |
| 1101 | MQ_CONNECTION_ERROR | 消息队列连接失败 |
| 1102 | MQ_TIMEOUT_ERROR | 消息队列操作超时 |
| 1110 | PRODUCER_SEND_ERROR | 消息发送失败 |
| 1111 | PRODUCER_TRANSACTION_ERROR | 事务消息处理失败 |
| 1120 | CONSUMER_PROCESS_ERROR | 消息消费失败 |
| 1121 | CONSUMER_RETRY_EXCEEDED | 消息重试次数超过限制 |
| 1130 | QUEUE_NOT_FOUND | 队列不存在 |
| 1131 | QUEUE_FULL | 队列已满 |
| 1140 | EXCHANGE_NOT_FOUND | 交换机不存在 |
| 1141 | EXCHANGE_TYPE_ERROR | 交换机类型错误 |
| 1150 | MESSAGE_FORMAT_ERROR | 消息格式错误 |
| 1151 | MESSAGE_TOO_LARGE | 消息过大 |

### 3.7 File 文件存储模块 (FileResultCode)

| 错误码 | 常量名 | 说明 |
|--------|--------|------|
| 1200 | STORAGE_CONNECTION_FAILED | 存储连接失败 |
| 1201 | STORAGE_TIMEOUT | 存储操作超时 |
| 1202 | STORAGE_DATA_ERROR | 存储数据错误 |
| 1203 | BUCKET_INIT_FAILED | 存储桶初始化失败 |
| 1204 | BUCKET_NOT_FOUND | 存储桶不存在 |
| 1205 | BUCKET_CREATE_FAILED | 存储桶创建失败 |
| 1206 | BUCKET_DELETE_FAILED | 存储桶删除失败 |
| 1207 | FILE_NOT_FOUND | 文件不存在 |
| 1208 | FILE_UPLOAD_FAILED | 文件上传失败 |
| 1209 | FILE_DOWNLOAD_FAILED | 文件下载失败 |
| 1210 | FILE_DELETE_FAILED | 文件删除失败 |
| 1211 | FILE_OPERATION_FAILED | 文件操作失败 |
| 1212 | FILE_SIZE_EXCEEDED | 文件大小超出限制 |
| 1213 | FILE_TYPE_NOT_ALLOWED | 文件类型不允许 |
| 1214 | FILE_PATH_INVALID | 文件路径无效 |
| 1215 | URL_GENERATE_FAILED | URL生成失败 |
| 1216 | PRESIGNED_URL_FAILED | 预签名URL生成失败 |
| 1217 | STORAGE_CONFIG_ERROR | 存储配置错误 |
| 1218 | STORAGE_INIT_FAILED | 存储初始化失败 |
| 1219 | STORAGE_TYPE_NOT_SUPPORTED | 不支持的存储类型 |

### 3.8 MongoDB 模块 (MongoDbResultCode)

| 错误码 | 常量名 | 说明 |
|--------|--------|------|
| 1300 | MONGODB_ERROR | MongoDB操作失败 |
| 1301 | CONNECTION_ERROR | MongoDB连接失败 |
| 1302 | CONNECTION_TIMEOUT | MongoDB连接超时 |
| 1303 | CONNECTION_POOL_EXHAUSTED | MongoDB连接池耗尽 |
| 1304 | INVALID_CONNECTION_STRING | 无效的MongoDB连接字符串 |
| 1305 | AUTHENTICATION_ERROR | MongoDB认证失败 |
| 1310 | COLLECTION_ERROR | 集合操作失败 |
| 1311 | COLLECTION_NOT_FOUND | 集合不存在 |
| 1312 | COLLECTION_ALREADY_EXISTS | 集合已存在 |
| 1313 | CREATE_COLLECTION_ERROR | 创建集合失败 |
| 1314 | DROP_COLLECTION_ERROR | 删除集合失败 |
| 1320 | DOCUMENT_ERROR | 文档操作失败 |
| 1321 | DOCUMENT_NOT_FOUND | 文档不存在 |
| 1322 | INSERT_DOCUMENT_ERROR | 插入文档失败 |
| 1323 | UPDATE_DOCUMENT_ERROR | 更新文档失败 |
| 1324 | DELETE_DOCUMENT_ERROR | 删除文档失败 |
| 1325 | DUPLICATE_KEY_ERROR | 文档键值冲突 |
| 1330 | INDEX_ERROR | 索引操作失败 |
| 1331 | CREATE_INDEX_ERROR | 创建索引失败 |
| 1332 | DROP_INDEX_ERROR | 删除索引失败 |
| 1333 | INDEX_NOT_FOUND | 索引不存在 |
| 1334 | INVALID_INDEX_SPEC | 无效的索引规范 |
| 1335 | TTL_INDEX_ERROR | TTL索引操作失败 |
| 1340 | AGGREGATION_ERROR | 聚合操作失败 |
| 1341 | PIPELINE_ERROR | 聚合管道错误 |
| 1342 | AGGREGATION_TIMEOUT | 聚合操作超时 |
| 1343 | INVALID_AGGREGATION_STAGE | 无效的聚合阶段 |
| 1350 | GEO_ERROR | 地理空间操作失败 |
| 1351 | INVALID_GEO_JSON | 无效的GeoJSON格式 |
| 1352 | GEO_INDEX_MISSING | 缺少地理空间索引 |
| 1353 | GEO_QUERY_ERROR | 地理空间查询失败 |
| 1354 | COORDINATE_OUT_OF_RANGE | 坐标值超出范围 |
| 1360 | TIME_SERIES_ERROR | 时序数据操作失败 |
| 1361 | INVALID_TIME_FIELD | 无效的时间字段 |
| 1362 | TIME_SERIES_COLLECTION_ERROR | 时序集合操作失败 |
| 1370 | QUERY_ERROR | 查询操作失败 |
| 1371 | QUERY_TIMEOUT | 查询超时 |
| 1372 | BULK_OPERATION_ERROR | 批量操作失败 |
| 1373 | TRANSACTION_ERROR | 事务操作失败 |
| 1374 | SESSION_ERROR | 会话操作失败 |

### 3.9 WebSocket 模块 (WebSocketResultCode)

| 错误码 | 常量名 | 说明 |
|--------|--------|------|
| 1500 | CONNECTION_FAILED | WebSocket连接失败 |
| 1501 | CONNECTION_TIMEOUT | WebSocket连接超时 |
| 1502 | CONNECTION_CLOSED | WebSocket连接已关闭 |
| 1503 | AUTHENTICATION_FAILED | WebSocket认证失败 |
| 1504 | TOKEN_INVALID | Token无效或已过期 |
| 1505 | TOKEN_MISSING | Token缺失 |
| 1506 | MESSAGE_SEND_FAILED | 消息发送失败 |
| 1507 | MESSAGE_FORMAT_INVALID | 消息格式无效 |
| 1508 | MESSAGE_TYPE_UNKNOWN | 未知的消息类型 |
| 1509 | SUBSCRIBE_FAILED | 订阅失败 |
| 1510 | UNSUBSCRIBE_FAILED | 取消订阅失败 |
| 1511 | USER_NOT_ONLINE | 用户不在线 |
| 1512 | USER_ALREADY_ONLINE | 用户已在线 |
| 1513 | SESSION_NOT_FOUND | 会话不存在 |
| 1514 | HEARTBEAT_TIMEOUT | 心跳超时 |
| 1515 | RATE_LIMIT_EXCEEDED | 消息频率超限 |
| 1516 | TOPIC_NOT_FOUND | 主题不存在 |
| 1517 | PERMISSION_DENIED | 权限不足 |
| 1518 | ENCRYPTION_ERROR | 消息加密/解密失败 |
| 1519 | INTERNAL_ERROR | WebSocket内部错误 |

## 4. 变更记录

| 日期 | 变更内容 |
|------|---------|
| 2026-03-03 | 创建错误码分配规范文档 |
| 2026-03-07 | 将文件模块错误码从 4001-4018 调整为 1200-1218 |
| 2026-03-07 | 统一错误码常量命名风格，单码对应单错误 |
| 2026-03-07 | 简化文档结构，与代码完全同步 |
