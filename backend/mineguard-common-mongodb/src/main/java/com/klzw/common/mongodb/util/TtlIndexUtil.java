package com.klzw.common.mongodb.util;

import com.klzw.common.mongodb.constant.MongoDbConstants;
import com.klzw.common.mongodb.constant.MongoDbResultCode;
import com.klzw.common.mongodb.exception.MongoDbException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.concurrent.TimeUnit;

/**
 * MongoDB TTL 索引工具类
 * 用于配置和管理 TTL 索引，确保数据能够自动过期删除
 */
public final class TtlIndexUtil {

    private TtlIndexUtil() {
    }

    /**
     * 创建 TTL 索引
     * @param mongoTemplate MongoTemplate 实例
     * @param collectionName 集合名称
     * @param fieldName 字段名称
     * @param expireAfterSeconds 过期时间（秒）
     */
    public static void createTtlIndex(MongoTemplate mongoTemplate, String collectionName, 
                                     String fieldName, long expireAfterSeconds) {
        try {
            MongoDatabase database = mongoTemplate.getDb();
            MongoCollection<Document> collection = database.getCollection(collectionName);

            boolean indexExists = false;
            for (Document index : collection.listIndexes()) {
                Document key = (Document) index.get("key");
                if (key.containsKey(fieldName)) {
                    indexExists = true;
                    break;
                }
            }

            if (!indexExists) {
                IndexOptions indexOptions = new IndexOptions()
                        .expireAfter(expireAfterSeconds, TimeUnit.SECONDS);
                collection.createIndex(Indexes.ascending(fieldName), indexOptions);
            }
        } catch (Exception e) {
            throw new MongoDbException(
                    MongoDbResultCode.TTL_INDEX_ERROR.getCode(),
                    "创建TTL索引失败: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * 创建 TTL 索引（使用时间单位）
     * @param mongoTemplate MongoTemplate 实例
     * @param collectionName 集合名称
     * @param fieldName 字段名称
     * @param expireAfter 过期时间
     * @param timeUnit 时间单位
     */
    public static void createTtlIndex(MongoTemplate mongoTemplate, String collectionName, 
                                     String fieldName, long expireAfter, TimeUnit timeUnit) {
        try {
            long expireAfterSeconds = timeUnit.toSeconds(expireAfter);
            createTtlIndex(mongoTemplate, collectionName, fieldName, expireAfterSeconds);
        } catch (Exception e) {
            throw new MongoDbException(
                    MongoDbResultCode.TTL_INDEX_ERROR.getCode(),
                    "创建TTL索引失败: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * 为车辆轨迹集合创建 TTL 索引（6个月）
     * @param mongoTemplate MongoTemplate 实例
     */
    public static void createVehicleTrajectoryTtlIndex(MongoTemplate mongoTemplate) {
        createTtlIndex(mongoTemplate, 
                MongoDbConstants.COLLECTION_VEHICLE_TRAJECTORY, 
                MongoDbConstants.FIELD_TIMESTAMP, 
                MongoDbConstants.TTL_6_MONTHS);
    }

    /**
     * 为行程历史集合创建 TTL 索引（1年）
     * @param mongoTemplate MongoTemplate 实例
     */
    public static void createTripHistoryTtlIndex(MongoTemplate mongoTemplate) {
        createTtlIndex(mongoTemplate, 
                MongoDbConstants.COLLECTION_TRIP_HISTORY, 
                MongoDbConstants.FIELD_CREATE_TIME, 
                MongoDbConstants.TTL_1_YEAR);
    }

    /**
     * 为预警事件集合创建 TTL 索引（1年）
     * @param mongoTemplate MongoTemplate 实例
     */
    public static void createWarningEventTtlIndex(MongoTemplate mongoTemplate) {
        createTtlIndex(mongoTemplate, 
                MongoDbConstants.COLLECTION_WARNING_EVENT, 
                MongoDbConstants.FIELD_WARNING_TIME, 
                MongoDbConstants.TTL_1_YEAR);
    }

    /**
     * 为操作日志集合创建 TTL 索引（30天）
     * @param mongoTemplate MongoTemplate 实例
     */
    public static void createOperationLogTtlIndex(MongoTemplate mongoTemplate) {
        createTtlIndex(mongoTemplate, 
                MongoDbConstants.COLLECTION_OPERATION_LOG, 
                MongoDbConstants.FIELD_REQUEST_TIME, 
                MongoDbConstants.TTL_30_DAYS);
    }

    /**
     * 为异常日志集合创建 TTL 索引（90天）
     * @param mongoTemplate MongoTemplate 实例
     */
    public static void createExceptionLogTtlIndex(MongoTemplate mongoTemplate) {
        createTtlIndex(mongoTemplate, 
                MongoDbConstants.COLLECTION_EXCEPTION_LOG, 
                MongoDbConstants.FIELD_OCCUR_TIME, 
                MongoDbConstants.TTL_90_DAYS);
    }

    /**
     * 为设备数据集合创建 TTL 索引（3个月）
     * @param mongoTemplate MongoTemplate 实例
     */
    public static void createDeviceDataTtlIndex(MongoTemplate mongoTemplate) {
        createTtlIndex(mongoTemplate, 
                MongoDbConstants.COLLECTION_DEVICE_DATA, 
                MongoDbConstants.FIELD_TIMESTAMP, 
                MongoDbConstants.TTL_90_DAYS);
    }

    /**
     * 为消息历史集合创建 TTL 索引（30天）
     * @param mongoTemplate MongoTemplate 实例
     */
    public static void createMessageHistoryTtlIndex(MongoTemplate mongoTemplate) {
        createTtlIndex(mongoTemplate, 
                MongoDbConstants.COLLECTION_MESSAGE_HISTORY, 
                MongoDbConstants.FIELD_EXPIRE_TIME, 
                MongoDbConstants.TTL_30_DAYS);
    }

    /**
     * 为统计数据集合创建 TTL 索引（2年）
     * @param mongoTemplate MongoTemplate 实例
     */
    public static void createStatisticsDataTtlIndex(MongoTemplate mongoTemplate) {
        createTtlIndex(mongoTemplate, 
                MongoDbConstants.COLLECTION_STATISTICS_DATA, 
                MongoDbConstants.FIELD_STAT_DATE, 
                MongoDbConstants.TTL_2_YEARS);
    }

    /**
     * 为成本记录集合创建 TTL 索引（2年）
     * @param mongoTemplate MongoTemplate 实例
     */
    public static void createCostRecordTtlIndex(MongoTemplate mongoTemplate) {
        createTtlIndex(mongoTemplate, 
                MongoDbConstants.COLLECTION_COST_RECORD, 
                MongoDbConstants.FIELD_COST_TIME, 
                MongoDbConstants.TTL_2_YEARS);
    }

    /**
     * 为车辆维护记录集合创建 TTL 索引（2年）
     * @param mongoTemplate MongoTemplate 实例
     */
    public static void createVehicleMaintenanceTtlIndex(MongoTemplate mongoTemplate) {
        createTtlIndex(mongoTemplate, 
                MongoDbConstants.COLLECTION_VEHICLE_MAINTENANCE, 
                MongoDbConstants.FIELD_CREATE_TIME, 
                MongoDbConstants.TTL_2_YEARS);
    }

    /**
     * 初始化所有 TTL 索引
     * @param mongoTemplate MongoTemplate 实例
     */
    public static void initAllTtlIndexes(MongoTemplate mongoTemplate) {
        createVehicleTrajectoryTtlIndex(mongoTemplate);
        createTripHistoryTtlIndex(mongoTemplate);
        createWarningEventTtlIndex(mongoTemplate);
        createOperationLogTtlIndex(mongoTemplate);
        createExceptionLogTtlIndex(mongoTemplate);
        createDeviceDataTtlIndex(mongoTemplate);
        createMessageHistoryTtlIndex(mongoTemplate);
        createStatisticsDataTtlIndex(mongoTemplate);
        createCostRecordTtlIndex(mongoTemplate);
        createVehicleMaintenanceTtlIndex(mongoTemplate);
    }

}
