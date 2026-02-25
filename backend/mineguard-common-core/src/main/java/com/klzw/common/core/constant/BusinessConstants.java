package com.klzw.common.core.constant;

public class BusinessConstants {
    // 分页默认值
    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;
    
    // 状态常量
    public static final int STATUS_ENABLE = 1;
    public static final int STATUS_DISABLE = 0;
    
    // 删除状态
    public static final int DEL_FLAG_NORMAL = 0;
    public static final int DEL_FLAG_DELETED = 1;
    
    // 用户类型
    public static final int USER_TYPE_ADMIN = 1;
    public static final int USER_TYPE_DRIVER = 2;
    public static final int USER_TYPE_MAINTENANCE = 3;
    public static final int USER_TYPE_SAFETY = 4;
    
    // 车辆状态
    public static final int VEHICLE_STATUS_IDLE = 1;
    public static final int VEHICLE_STATUS_BUSY = 2;
    public static final int VEHICLE_STATUS_MAINTAIN = 3;
    public static final int VEHICLE_STATUS_FAULT = 4;
    
    // 行程状态
    public static final int TRIP_STATUS_PENDING = 1;
    public static final int TRIP_STATUS_IN_PROGRESS = 2;
    public static final int TRIP_STATUS_COMPLETED = 3;
    public static final int TRIP_STATUS_CANCELLED = 4;
    
    // 预警级别
    public static final int WARNING_LEVEL_INFO = 1;
    public static final int WARNING_LEVEL_WARN = 2;
    public static final int WARNING_LEVEL_ERROR = 3;
    public static final int WARNING_LEVEL_FATAL = 4;
    
    // 排序方向
    public static final String SORT_ASC = "asc";
    public static final String SORT_DESC = "desc";
}
