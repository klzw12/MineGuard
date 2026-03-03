package com.klzw.common.mongodb.util;

import com.klzw.common.mongodb.constant.MongoDbConstants;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

/**
 * MongoDB 时序数据工具类
 * 用于处理时序数据相关的操作
 */
public class TimeSeriesUtil {

    /**
     * 构建时序数据查询
     * @param timeField 时间字段名
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param otherCriteria 其他查询条件
     * @return 查询对象
     */
    public static Query buildTimeSeriesQuery(String timeField, long startTime, long endTime, Criteria... otherCriteria) {
        Query query = new Query();
        query.addCriteria(Criteria.where(timeField).gte(startTime).lte(endTime));
        
        // 添加其他查询条件
        for (Criteria criteria : otherCriteria) {
            query.addCriteria(criteria);
        }
        
        // 按时间排序
        query.with(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, timeField));
        
        return query;
    }

    /**
     * 构建车辆轨迹时序查询
     * @param carId 车辆ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 查询对象
     */
    public static Query buildVehicleTrajectoryQuery(long carId, long startTime, long endTime) {
        return buildTimeSeriesQuery(MongoDbConstants.FIELD_TIMESTAMP, startTime, endTime, 
                Criteria.where(MongoDbConstants.FIELD_CAR_ID).is(carId));
    }

    /**
     * 构建设备数据时序查询
     * @param deviceId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 查询对象
     */
    public static Query buildDeviceDataQuery(String deviceId, long startTime, long endTime) {
        return buildTimeSeriesQuery(MongoDbConstants.FIELD_TIMESTAMP, startTime, endTime, 
                Criteria.where(MongoDbConstants.FIELD_DEVICE_ID).is(deviceId));
    }

    /**
     * 构建预警信息时序查询
     * @param warningType 预警类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 查询对象
     */
    public static Query buildWarningQuery(String warningType, long startTime, long endTime) {
        return buildTimeSeriesQuery(MongoDbConstants.FIELD_TIMESTAMP, startTime, endTime, 
                Criteria.where(MongoDbConstants.FIELD_WARNING_TYPE).is(warningType));
    }

    /**
     * 构建行程历史时序查询
     * @param carId 车辆ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 查询对象
     */
    public static Query buildTripHistoryQuery(long carId, long startTime, long endTime) {
        return buildTimeSeriesQuery(MongoDbConstants.FIELD_TIMESTAMP, startTime, endTime, 
                Criteria.where(MongoDbConstants.FIELD_CAR_ID).is(carId));
    }

    /**
     * 批量插入时序数据
     * @param mongoTemplate MongoTemplate 实例
     * @param collectionName 集合名称
     * @param dataList 数据集合
     * @param <T> 数据类型
     */
    public static <T> void batchInsertTimeSeriesData(MongoTemplate mongoTemplate, 
                                                   String collectionName, List<T> dataList) {
        if (dataList != null && !dataList.isEmpty()) {
            mongoTemplate.insert(dataList, collectionName);
        }
    }

    /**
     * 清理过期的时序数据
     * @param mongoTemplate MongoTemplate 实例
     * @param collectionName 集合名称
     * @param timeField 时间字段名
     * @param expireTime 过期时间（毫秒）
     * @return 删除的记录数
     */
    public static long cleanExpiredTimeSeriesData(MongoTemplate mongoTemplate, 
                                               String collectionName, String timeField, long expireTime) {
        Query query = new Query();
        query.addCriteria(Criteria.where(timeField).lt(System.currentTimeMillis() - expireTime));
        return mongoTemplate.remove(query, collectionName).getDeletedCount();
    }

}