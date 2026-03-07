package com.klzw.common.mongodb.util;

import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;


/**
 * MongoDB 地理空间查询工具类
 * 用于处理地理空间相关的查询操作
 */
public class GeoQueryUtil {

    /**
     * 构建附近查询条件
     * @param locationField 位置字段名
     * @param point 中心点
     * @param maxDistance 最大距离（米）
     * @return 查询条件
     */
    public static Criteria near(String locationField, GeoJsonPoint point, double maxDistance) {
        return Criteria.where(locationField).near(point).maxDistance(maxDistance / 6371000.0 * 2 * Math.PI);
    }

    /**
     * 构建附近查询条件
     * @param locationField 位置字段名
     * @param longitude 经度
     * @param latitude 纬度
     * @param maxDistance 最大距离（米）
     * @return 查询条件
     */
    public static Criteria near(String locationField, double longitude, double latitude, double maxDistance) {
        GeoJsonPoint point = new GeoJsonPoint(longitude, latitude);
        return near(locationField, point, maxDistance);
    }

    /**
     * 构建附近车辆查询
     * @param locationField 位置字段名
     * @param longitude 经度
     * @param latitude 纬度
     * @param maxDistance 最大距离（米）
     * @param timestampField 时间戳字段名
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 查询对象
     */
    public static Query nearVehiclesQuery(String locationField, double longitude, double latitude, double maxDistance, 
                                         String timestampField, long startTime, long endTime) {
        Query query = new Query();
        query.addCriteria(near(locationField, longitude, latitude, maxDistance));
        query.addCriteria(Criteria.where(timestampField).gte(startTime).lte(endTime));
        query.with(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, timestampField));
        return query;
    }

}