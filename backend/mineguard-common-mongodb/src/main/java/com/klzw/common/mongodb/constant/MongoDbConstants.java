package com.klzw.common.mongodb.constant;

/**
 * MongoDB 常量类
 * 用于管理 MongoDB 相关的字段名、集合名和状态常量
 */
public final class MongoDbConstants {

    private MongoDbConstants() {
    }

    // ==================== 集合名称常量 ====================

    /**
     * 用户资料集合
     */
    public static final String COLLECTION_USER_PROFILE = "user_profile";

    /**
     * 车辆轨迹集合
     */
    public static final String COLLECTION_VEHICLE_TRAJECTORY = "vehicle_trajectory";

    /**
     * 车辆维护记录集合
     */
    public static final String COLLECTION_VEHICLE_MAINTENANCE = "vehicle_maintenance";

    /**
     * 行程历史集合
     */
    public static final String COLLECTION_TRIP_HISTORY = "trip_history";

    /**
     * 预警事件集合
     */
    public static final String COLLECTION_WARNING_EVENT = "warning_event";

    /**
     * 统计数据集合
     */
    public static final String COLLECTION_STATISTICS_DATA = "statistics_data";

    /**
     * 成本记录集合
     */
    public static final String COLLECTION_COST_RECORD = "cost_record";

    /**
     * 操作日志集合
     */
    public static final String COLLECTION_OPERATION_LOG = "operation_log";

    /**
     * 异常日志集合
     */
    public static final String COLLECTION_EXCEPTION_LOG = "exception_log";

    /**
     * 设备数据集合
     */
    public static final String COLLECTION_DEVICE_DATA = "device_data";

    /**
     * 消息历史集合
     */
    public static final String COLLECTION_MESSAGE_HISTORY = "message_history";

    // ==================== 字段名称常量 ====================

    /**
     * 时间字段名称
     */
    public static final String FIELD_TIMESTAMP = "timestamp";

    /**
     * 预警时间字段名称
     */
    public static final String FIELD_WARNING_TIME = "warningTime";

    /**
     * 车辆ID字段名称
     */
    public static final String FIELD_CAR_ID = "carId";

    /**
     * 预警类型字段名称
     */
    public static final String FIELD_WARNING_TYPE = "warningType";

    /**
     * 预警级别字段名称
     */
    public static final String FIELD_WARNING_LEVEL = "warningLevel";

    /**
     * 状态字段名称
     */
    public static final String FIELD_STATUS = "status";

    /**
     * 设备ID字段名称
     */
    public static final String FIELD_DEVICE_ID = "deviceId";

    /**
     * 创建时间字段名称
     */
    public static final String FIELD_CREATE_TIME = "createTime";

    /**
     * 请求时间字段名称
     */
    public static final String FIELD_REQUEST_TIME = "requestTime";

    /**
     * 发生时间字段名称
     */
    public static final String FIELD_OCCUR_TIME = "occurTime";

    /**
     * 过期时间字段名称
     */
    public static final String FIELD_EXPIRE_TIME = "expireTime";

    /**
     * 统计日期字段名称
     */
    public static final String FIELD_STAT_DATE = "statDate";

    /**
     * 成本时间字段名称
     */
    public static final String FIELD_COST_TIME = "costTime";

    // ==================== 状态值常量 ====================

    /**
     * 运行状态值
     */
    public static final String STATUS_RUNNING = "running";

    // ==================== TTL过期时间常量（秒） ====================

    /**
     * 30天（秒）
     */
    public static final long TTL_30_DAYS = 2592000L;

    /**
     * 90天（秒）
     */
    public static final long TTL_90_DAYS = 7776000L;

    /**
     * 6个月（秒）
     */
    public static final long TTL_6_MONTHS = 15552000L;

    /**
     * 1年（秒）
     */
    public static final long TTL_1_YEAR = 31536000L;

    /**
     * 2年（秒）
     */
    public static final long TTL_2_YEARS = 63072000L;

}
