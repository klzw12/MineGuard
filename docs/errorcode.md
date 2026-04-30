# MineGuard 错误码分配规范

## 1. 错误码范围总览

| 模块 | 错误码范围 | 枚举类 |
| ---- | --------- | ------ |
| core | 200, 400-605 | `ResultCodeEnum` |
| web | 700-727 | `WebResultCode` |
| auth | 800-863 | `AuthResultCode` |
| redis | 900-955 | `RedisResultCode` |
| dispatch | 1000-1099 | `DispatchResultCode` |
| database | 1100-1164 | `DatabaseResultCode` |
| mq | 1200-1251 | `MqResultCode` |
| file | 1300-1399 | `FileResultCode` |
| mongodb | 1400-1474 | `MongoDbResultCode` |
| websocket | 1500-1519 | `WebSocketResultCode` |
| vehicle | 2000-2010 | `VehicleResultCode` |
| user | 2100-2151 | `UserResultCode` |
| warning | 2200-2221 | `WarningResultCode` |
| trip | 2300-2341 | `TripResultCode` |
| statistics | 2400-2420 | `StatisticsResultCode` |
| cost | 2500-2520 | `CostResultCode` |
| ai | 2600-2642 | `AiResultCode` |

## 2. 预留范围

| 范围 | 用途 | 状态 |
| ---- | --- | ---- |
| 700-799 | Web层扩展 | 已使用 701-727 |
| 800-899 | 认证模块 | 已使用 800-863 |
| 900-999 | Redis模块 | 已使用 900-955 |
| 1000-1099 | 调度模块 | 已使用 1000-1099 |
| 1100-1199 | 数据库模块 | 已使用 1100-1164 |
| 1200-1299 | 消息队列模块 | 已使用 1200-1251 |
| 1300-1399 | 文件存储模块 | 已使用 1300-1399 |
| 1400-1499 | MongoDB模块 | 已使用 1400-1474 |
| 1500-1599 | WebSocket模块 | 已使用 1500-1519 |
| 1600-1999 | 预留 | 未使用 |
| 2000-2099 | 车辆模块 | 已使用 2001-2010 |
| 2100-2199 | 用户模块 | 已使用 2100-2151 |
| 2200-2299 | 预警模块 | 已使用 2200-2221 |
| 2300-2399 | 行程模块 | 已使用 2300-2341 |
| 2400-2499 | 统计模块 | 已使用 2400-2420 |
| 2500-2599 | 成本模块 | 已使用 2500-2520 |
| 2600-2699 | AI模块 | 已使用 2600-2642 |
| 2700-3999 | 预留 | 未使用 |

## 3. 各模块错误码详情

### 3.1 Core 核心模块 (ResultCodeEnum)

| 错误码 | 常量名 | 说明 |
| --- | --- | --- |
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
| --- | --- | --- |
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
| --- | --- | --- |
| 800 | AUTH_ERROR | 认证操作失败 |
| 801 | PARAMETER_ERROR | 参数错误 |
| 802 | TOKEN_EXPIRED | Token已过期 |
| 803 | TOKEN_INVALID | Token无效 |
| 804 | TOKEN_MISSING | Token缺失 |
| 805 | TOKEN_SIGNATURE_ERROR | Token签名错误 |
| 806 | TOKEN_PARSE_ERROR | Token解析错误 |
| 807 | TOKEN_REVOKED | Token已被撤销 |
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
| 860 | SMS_SEND_FAILED | 短信发送失败 |
| 861 | SMS_CODE_ERROR | 验证码错误 |
| 862 | SMS_CODE_EXPIRED | 验证码已过期 |
| 863 | SMS_SEND_FREQUENCY_ERROR | 短信发送频率过高 |
| 899 | SYSTEM_ERROR | 系统错误 |

### 3.4 Redis 模块 (RedisResultCode)

| 错误码 | 常量名 | 说明 |
| --- | --- | --- |
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
| 955 | PARAMETER_ERROR | 参数错误 |

### 3.5 Dispatch 调度模块 (DispatchResultCode)

| 错误码 | 常量名 | 说明 |
| --- | --- | --- |
| 1000 | DISPATCH_FAILED | 调度失败 |
| 1001 | PARAMETER_ERROR | 参数错误 |
| 1002 | NO_AVAILABLE_DRIVER | 无可用司机 |
| 1003 | NO_AVAILABLE_VEHICLE | 无可用车辆 |
| 1004 | DISPATCH_ALGORITHM_ERROR | 调度算法执行失败 |
| 1010 | TASK_NOT_FOUND | 调度任务不存在 |
| 1011 | TASK_STATUS_ERROR | 任务状态错误 |
| 1012 | TASK_OPERATION_FAILED | 任务操作失败 |
| 1013 | TASK_CANCELLED | 任务已取消 |
| 1020 | ROUTE_TEMPLATE_CREATE_FAILED | 路线模板创建失败 |
| 1021 | ROUTE_TEMPLATE_NOT_FOUND | 路线模板不存在 |
| 1022 | ROUTE_BLOCK_ADJUST_FAILED | 线路堵塞调整失败 |
| 1030 | VEHICLE_NOT_FOUND | 车辆不存在 |
| 1031 | VEHICLE_NOT_AVAILABLE | 车辆不可用 |
| 1032 | VEHICLE_FAULT_ADJUST_FAILED | 车辆故障调整失败 |
| 1040 | DRIVER_NOT_FOUND | 司机不存在 |
| 1041 | DRIVER_NOT_AVAILABLE | 司机不可用 |
| 1042 | DRIVER_LEAVE_ADJUST_FAILED | 司机请假调整失败 |
| 1099 | SYSTEM_ERROR | 系统错误 |

### 3.6 Database 数据库模块 (DatabaseResultCode)

| 错误码 | 常量名 | 说明 |
| --- | --- | --- |
| 1100 | DATABASE_ERROR | 数据库操作失败 |
| 1101 | DATABASE_CONNECTION_ERROR | 数据库连接失败 |
| 1102 | CONNECTION_TIMEOUT | 数据库连接超时 |
| 1103 | CONNECTION_POOL_EXHAUSTED | 数据库连接池耗尽 |
| 1104 | CONNECTION_CLOSED | 数据库连接已关闭 |
| 1105 | INVALID_CONNECTION_CONFIG | 数据库连接配置无效 |
| 1110 | SQL_EXECUTION_ERROR | SQL执行失败 |
| 1111 | SQL_SYNTAX_ERROR | SQL语法错误 |
| 1112 | QUERY_TIMEOUT | 查询超时 |
| 1113 | UPDATE_TIMEOUT | 更新超时 |
| 1114 | BATCH_EXECUTION_ERROR | 批量执行失败 |
| 1120 | TRANSACTION_ERROR | 事务执行失败 |
| 1121 | TRANSACTION_TIMEOUT | 事务超时 |
| 1122 | TRANSACTION_ROLLBACK | 事务回滚 |
| 1123 | TRANSACTION_NOT_ACTIVE | 事务未激活 |
| 1124 | NESTED_TRANSACTION_ERROR | 嵌套事务错误 |
| 1130 | DATA_SOURCE_ERROR | 数据源配置错误 |
| 1131 | DATA_SOURCE_NOT_FOUND | 数据源不存在 |
| 1132 | DATA_SOURCE_SWITCH_ERROR | 数据源切换失败 |
| 1133 | MASTER_SLAVE_SYNC_ERROR | 主从同步错误 |
| 1134 | READ_WRITE_SPLIT_ERROR | 读写分离错误 |
| 1140 | PAGINATION_ERROR | 分页参数错误 |
| 1141 | PAGE_NUMBER_INVALID | 页码无效 |
| 1142 | PAGE_SIZE_INVALID | 每页大小无效 |
| 1143 | PAGE_SIZE_EXCEEDED | 每页大小超过限制 |
| 1144 | SORT_FIELD_INVALID | 排序字段无效 |
| 1150 | RECORD_NOT_FOUND | 记录不存在 |
| 1151 | DUPLICATE_KEY_ERROR | 主键冲突 |
| 1152 | DATA_INTEGRITY_ERROR | 数据完整性错误 |
| 1153 | FOREIGN_KEY_CONSTRAINT_ERROR | 外键约束错误 |
| 1154 | UNIQUE_CONSTRAINT_ERROR | 唯一约束错误 |
| 1155 | NULL_VALUE_ERROR | 空值错误 |
| 1160 | MAPPER_NOT_FOUND | Mapper未找到 |
| 1161 | ENTITY_NOT_FOUND | 实体类未找到 |
| 1162 | TABLE_NOT_FOUND | 表不存在 |
| 1163 | FIELD_NOT_FOUND | 字段不存在 |
| 1164 | TYPE_HANDLER_ERROR | 类型处理器错误 |

### 3.7 MQ 消息队列模块 (MqResultCode)

| 错误码 | 常量名 | 说明 |
| --- | --- | --- |
| 1200 | MQ_ERROR | 消息队列操作失败 |
| 1201 | MQ_CONNECTION_ERROR | 消息队列连接失败 |
| 1202 | MQ_TIMEOUT_ERROR | 消息队列操作超时 |
| 1210 | PRODUCER_SEND_ERROR | 消息发送失败 |
| 1211 | PRODUCER_TRANSACTION_ERROR | 事务消息处理失败 |
| 1220 | CONSUMER_PROCESS_ERROR | 消息消费失败 |
| 1221 | CONSUMER_RETRY_EXCEEDED | 消息重试次数超过限制 |
| 1230 | QUEUE_NOT_FOUND | 队列不存在 |
| 1231 | QUEUE_FULL | 队列已满 |
| 1240 | EXCHANGE_NOT_FOUND | 交换机不存在 |
| 1241 | EXCHANGE_TYPE_ERROR | 交换机类型错误 |
| 1250 | MESSAGE_FORMAT_ERROR | 消息格式错误 |
| 1251 | MESSAGE_TOO_LARGE | 消息过大 |

### 3.8 File 文件存储模块 (FileResultCode)

| 错误码 | 常量名 | 说明 |
| --- | --- | --- |
| 1300 | STORAGE_CONNECTION_FAILED | 存储连接失败 |
| 1301 | STORAGE_TIMEOUT | 存储操作超时 |
| 1302 | STORAGE_DATA_ERROR | 存储数据错误 |
| 1303 | BUCKET_INIT_FAILED | 存储桶初始化失败 |
| 1304 | BUCKET_NOT_FOUND | 存储桶不存在 |
| 1305 | BUCKET_CREATE_FAILED | 存储桶创建失败 |
| 1306 | BUCKET_DELETE_FAILED | 存储桶删除失败 |
| 1307 | FILE_NOT_FOUND | 文件不存在 |
| 1308 | FILE_UPLOAD_FAILED | 文件上传失败 |
| 1309 | FILE_DOWNLOAD_FAILED | 文件下载失败 |
| 1310 | FILE_DELETE_FAILED | 文件删除失败 |
| 1311 | FILE_OPERATION_FAILED | 文件操作失败 |
| 1312 | FILE_SIZE_EXCEEDED | 文件大小超出限制 |
| 1313 | FILE_TYPE_NOT_ALLOWED | 文件类型不允许 |
| 1314 | FILE_PATH_INVALID | 文件路径无效 |
| 1315 | URL_GENERATE_FAILED | URL生成失败 |
| 1316 | PRESIGNED_URL_FAILED | 预签名URL生成失败 |
| 1317 | STORAGE_CONFIG_ERROR | 存储配置错误 |
| 1318 | STORAGE_INIT_FAILED | 存储初始化失败 |
| 1319 | STORAGE_TYPE_NOT_SUPPORTED | 不支持的存储类型 |
| 1399 | PARAM_ERROR | 参数错误 |

### 3.9 MongoDB 模块 (MongoDbResultCode)

| 错误码 | 常量名 | 说明 |
| --- | --- | --- |
| 1400 | MONGODB_ERROR | MongoDB操作失败 |
| 1401 | CONNECTION_ERROR | MongoDB连接失败 |
| 1402 | CONNECTION_TIMEOUT | MongoDB连接超时 |
| 1403 | CONNECTION_POOL_EXHAUSTED | MongoDB连接池耗尽 |
| 1404 | INVALID_CONNECTION_STRING | 无效的MongoDB连接字符串 |
| 1405 | AUTHENTICATION_ERROR | MongoDB认证失败 |
| 1410 | COLLECTION_ERROR | 集合操作失败 |
| 1411 | COLLECTION_NOT_FOUND | 集合不存在 |
| 1412 | COLLECTION_ALREADY_EXISTS | 集合已存在 |
| 1413 | CREATE_COLLECTION_ERROR | 创建集合失败 |
| 1414 | DROP_COLLECTION_ERROR | 删除集合失败 |
| 1420 | DOCUMENT_ERROR | 文档操作失败 |
| 1421 | DOCUMENT_NOT_FOUND | 文档不存在 |
| 1422 | INSERT_DOCUMENT_ERROR | 插入文档失败 |
| 1423 | UPDATE_DOCUMENT_ERROR | 更新文档失败 |
| 1424 | DELETE_DOCUMENT_ERROR | 删除文档失败 |
| 1425 | DUPLICATE_KEY_ERROR | 文档键值冲突 |
| 1430 | INDEX_ERROR | 索引操作失败 |
| 1431 | CREATE_INDEX_ERROR | 创建索引失败 |
| 1432 | DROP_INDEX_ERROR | 删除索引失败 |
| 1433 | INDEX_NOT_FOUND | 索引不存在 |
| 1434 | INVALID_INDEX_SPEC | 无效的索引规范 |
| 1435 | TTL_INDEX_ERROR | TTL索引操作失败 |
| 1440 | AGGREGATION_ERROR | 聚合操作失败 |
| 1441 | PIPELINE_ERROR | 聚合管道错误 |
| 1442 | AGGREGATION_TIMEOUT | 聚合操作超时 |
| 1443 | INVALID_AGGREGATION_STAGE | 无效的聚合阶段 |
| 1450 | GEO_ERROR | 地理空间操作失败 |
| 1451 | INVALID_GEO_JSON | 无效的GeoJSON格式 |
| 1452 | GEO_INDEX_MISSING | 缺少地理空间索引 |
| 1453 | GEO_QUERY_ERROR | 地理空间查询失败 |
| 1454 | COORDINATE_OUT_OF_RANGE | 坐标值超出范围 |
| 1460 | TIME_SERIES_ERROR | 时序数据操作失败 |
| 1461 | INVALID_TIME_FIELD | 无效的时间字段 |
| 1462 | TIME_SERIES_COLLECTION_ERROR | 时序集合操作失败 |
| 1470 | QUERY_ERROR | 查询操作失败 |
| 1471 | QUERY_TIMEOUT | 查询超时 |
| 1472 | BULK_OPERATION_ERROR | 批量操作失败 |
| 1473 | TRANSACTION_ERROR | 事务操作失败 |
| 1474 | SESSION_ERROR | 会话操作失败 |

### 3.10 WebSocket 模块 (WebSocketResultCode)

| 错误码 | 常量名 | 说明 |
| --- | --- | --- |
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

### 3.11 Vehicle 车辆模块 (VehicleResultCode)

| 错误码 | 常量名 | 说明 |
| --- | --- | --- |
| 2001 | VEHICLE_NOT_FOUND | 车辆不存在 |
| 2002 | VEHICLE_NO_EXISTS | 车牌号已存在 |
| 2003 | VEHICLE_STATUS_NOT_ALLOWED | 车辆状态不允许该操作 |
| 2004 | FAULT_NOT_FOUND | 故障记录不存在 |
| 2005 | INSURANCE_NOT_FOUND | 保险记录不存在 |
| 2006 | MAINTENANCE_NOT_FOUND | 保养记录不存在 |
| 2007 | REFUELING_NOT_FOUND | 加油记录不存在 |
| 2008 | PARAMETER_ERROR | 参数错误 |
| 2009 | OPERATION_FAILED | 操作失败 |
| 2010 | VEHICLE_NO_MISMATCH | 车牌号与行驶证识别结果不一致 |

### 3.12 User 用户模块 (UserResultCode)

| 错误码 | 常量名 | 说明 |
| --- | --- | --- |
| 2100 | USER_ERROR | 用户操作失败 |
| 2101 | USER_NOT_FOUND | 用户不存在 |
| 2102 | USERNAME_EXISTS | 用户名已存在 |
| 2103 | PHONE_EXISTS | 手机号已被注册 |
| 2104 | EMAIL_EXISTS | 邮箱已被注册 |
| 2105 | USER_DISABLED | 用户已被禁用 |
| 2106 | USER_TYPE_ERROR | 用户类型错误 |
| 2110 | PASSWORD_ERROR | 密码错误 |
| 2111 | PASSWORD_INVALID | 密码无效 |
| 2112 | OLD_PASSWORD_ERROR | 原密码错误 |
| 2115 | SMS_SEND_FAILED | 短信发送失败 |
| 2116 | SMS_VERIFY_FAILED | 短信验证失败 |
| 2120 | ROLE_NOT_FOUND | 角色不存在 |
| 2121 | ROLE_DISABLED | 角色已禁用 |
| 2122 | ROLE_ASSIGN_FAILED | 角色分配失败 |
| 2123 | USER_ROLE_NOT_FOUND | 用户角色不存在 |
| 2130 | PERMISSION_DENIED | 权限不足 |
| 2131 | PERMISSION_NOT_FOUND | 权限不存在 |
| 2140 | ID_CARD_EXISTS | 身份证号已被注册 |
| 2141 | ID_CARD_INVALID | 身份证号无效 |
| 2142 | OCR_FAILED | OCR识别失败 |
| 2143 | CERTIFICATE_EXPIRED | 证书已过期 |
| 2144 | CERTIFICATE_INVALID | 证书无效 |
| 2145 | QUALIFICATION_NOT_VERIFIED | 资格未认证 |
| 2146 | QUALIFICATION_VERIFY_FAILED | 资格认证失败 |
| 2150 | DATA_ERROR | 数据错误 |
| 2151 | OPERATION_FAILED | 操作失败 |

### 3.13 Warning 预警模块 (WarningResultCode)

| 错误码 | 常量名 | 说明 |
| --- | --- | --- |
| 2200 | WARNING_RULE_NOT_FOUND | 预警规则不存在 |
| 2201 | WARNING_RULE_CODE_EXISTS | 规则编码已存在 |
| 2202 | WARNING_RULE_DISABLED | 预警规则已禁用 |
| 2210 | WARNING_RECORD_NOT_FOUND | 预警记录不存在 |
| 2211 | WARNING_ALREADY_HANDLED | 预警已处理 |
| 2212 | WARNING_ALREADY_CLOSED | 预警已关闭 |
| 2220 | WARNING_THRESHOLD_INVALID | 预警阈值无效 |
| 2221 | WARNING_NOTIFY_FAILED | 预警通知发送失败 |

### 3.14 Trip 行程模块 (TripResultCode)

| 错误码 | 常量名 | 说明 |
| --- | --- | --- |
| 2300 | TRIP_NOT_FOUND | 行程不存在 |
| 2301 | TRIP_ALREADY_STARTED | 行程已开始 |
| 2302 | TRIP_ALREADY_ENDED | 行程已结束 |
| 2303 | TRIP_NOT_STARTED | 行程未开始 |
| 2304 | TRIP_STATUS_ERROR | 行程状态错误 |
| 2310 | ROUTE_NOT_FOUND | 路线不存在 |
| 2311 | ROUTE_NAME_EXISTS | 路线名称已存在 |
| 2320 | DISPATCH_PLAN_NOT_FOUND | 调度计划不存在 |
| 2321 | DISPATCH_PLAN_EXECUTED | 调度计划已执行 |
| 2330 | TRACK_UPLOAD_FAILED | 轨迹上传失败 |
| 2331 | TRACK_NOT_FOUND | 轨迹记录不存在 |
| 2340 | VEHICLE_NOT_AVAILABLE | 车辆不可用 |
| 2341 | DRIVER_NOT_AVAILABLE | 司机不可用 |

### 3.15 Statistics 统计模块 (StatisticsResultCode)

| 错误码 | 常量名 | 说明 |
| --- | --- | --- |
| 2400 | STATISTICS_DATA_NOT_FOUND | 统计数据不存在 |
| 2410 | REPORT_NOT_FOUND | 报表不存在 |
| 2411 | REPORT_GENERATE_FAILED | 报表生成失败 |
| 2420 | EXPORT_FAILED | 导出失败 |

### 3.16 Cost 成本模块 (CostResultCode)

| 错误码 | 常量名 | 说明 |
| --- | --- | --- |
| 2500 | COST_RECORD_NOT_FOUND | 成本记录不存在 |
| 2510 | BUDGET_NOT_FOUND | 预算不存在 |
| 2511 | BUDGET_EXCEEDED | 预算已超支 |
| 2512 | BUDGET_DISABLED | 预算已禁用 |
| 2520 | BILLING_RULE_NOT_FOUND | 计费规则不存在 |

### 3.17 AI 模块 (AiResultCode)

| 错误码 | 常量名 | 说明 |
| --- | --- | --- |
| 2600 | AI_MODEL_NOT_FOUND | AI模型不存在 |
| 2601 | AI_MODEL_DISABLED | AI模型已禁用 |
| 2610 | AI_ANALYSIS_FAILED | AI分析失败 |
| 2620 | AI_PREDICTION_FAILED | AI预测失败 |
| 2630 | AI_ANOMALY_DETECTION_FAILED | 异常检测失败 |
| 2640 | AI_PROVIDER_NOT_AVAILABLE | AI服务提供商不可用 |
| 2641 | AI_API_KEY_INVALID | AI API密钥无效 |
| 2642 | AI_REQUEST_TIMEOUT | AI请求超时 |

## 4. 变更记录

| 日期 | 变更内容 |
| --- | --- |
| 2026-03-03 | 创建错误码分配规范文档 |
| 2026-03-07 | 将文件模块错误码从 4001-4018 调整为 1200-1218 |
| 2026-03-07 | 统一错误码常量命名风格，单码对应单错误 |
| 2026-03-07 | 简化文档结构，与代码完全同步 |
| 2026-03-12 | Redis模块增加PARAMETER_ERROR(955)错误码 |
| 2026-03-12 | Auth模块增加PARAMETER_ERROR(801)错误码 |
| 2026-04-08 | 修复错误码重复问题，重新分配各模块错误码范围 |
| 2026-04-08 | 新增业务模块错误码：Dispatch(1000-1099)、Vehicle(2000-2099)、User(2100-2199)、Warning(2200-2299)、Trip(2300-2399)、Statistics(2400-2499)、Cost(2500-2599)、AI(2600-2699) |
| 2026-04-08 | 调整基础模块错误码范围：Database(1100-1199)、MQ(1200-1299)、File(1300-1399)、MongoDB(1400-1499) |
