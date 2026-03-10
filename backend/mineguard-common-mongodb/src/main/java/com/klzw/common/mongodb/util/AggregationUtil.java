package com.klzw.common.mongodb.util;

import com.klzw.common.mongodb.constant.MongoDbConstants;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

/**
 * MongoDB 聚合查询工具类
 * 用于处理聚合管道相关的查询操作
 */
public class AggregationUtil {

    /**
     * 执行聚合查询
     * @param mongoTemplate MongoTemplate 实例
     * @param collectionName 集合名称
     * @param operations 聚合操作列表
     * @param outputType 输出类型
     * @param <T> 输出类型泛型
     * @return 聚合结果
     */
    public static <T> AggregationResults<T> aggregate(MongoTemplate mongoTemplate, String collectionName, 
                                                     List<AggregationOperation> operations, Class<T> outputType) {
        Aggregation aggregation = Aggregation.newAggregation(operations);
        return mongoTemplate.aggregate(aggregation, collectionName, outputType);
    }

    /**
     * 构建行驶里程统计聚合
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 聚合操作列表
     */
    public static List<AggregationOperation> buildMileageAggregation(long startTime, long endTime) {
        // 匹配时间范围
        MatchOperation matchOperation = Aggregation.match(
                org.springframework.data.mongodb.core.query.Criteria.where(MongoDbConstants.FIELD_TIMESTAMP)
                        .gte(startTime)
                        .lte(endTime)
        );

        // 按车辆分组
        GroupOperation groupOperation = Aggregation.group(MongoDbConstants.FIELD_CAR_ID)
                .count().as("totalDistance");

        // 排序
        SortOperation sortOperation = Aggregation.sort(
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "_id")
        );

        return List.of(matchOperation, groupOperation, sortOperation);
    }

    /**
     * 构建预警趋势分析聚合
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 聚合操作列表
     */
    public static List<AggregationOperation> buildWarningTrendAggregation(long startTime, long endTime) {
        // 匹配时间范围
        MatchOperation matchOperation = Aggregation.match(
                org.springframework.data.mongodb.core.query.Criteria.where(MongoDbConstants.FIELD_WARNING_TIME)
                        .gte(startTime)
                        .lte(endTime)
        );

        // 按预警类型、级别分组
        GroupOperation groupOperation = Aggregation.group(MongoDbConstants.FIELD_WARNING_TYPE, MongoDbConstants.FIELD_WARNING_LEVEL)
                .count().as("count");

        // 排序
        SortOperation sortOperation = Aggregation.sort(
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "_id.warningType", "_id.warningLevel")
        );

        return List.of(matchOperation, groupOperation, sortOperation);
    }

    /**
     * 构建车辆活跃度统计聚合
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 聚合操作列表
     */
    public static List<AggregationOperation> buildVehicleActivityAggregation(long startTime, long endTime) {
        // 匹配时间范围
        MatchOperation matchOperation = Aggregation.match(
                org.springframework.data.mongodb.core.query.Criteria.where(MongoDbConstants.FIELD_TIMESTAMP)
                        .gte(startTime)
                        .lte(endTime)
        );

        // 按车辆分组
        GroupOperation groupOperation = Aggregation.group(MongoDbConstants.FIELD_CAR_ID)
                .sum(org.springframework.data.mongodb.core.aggregation.ConditionalOperators.Cond
                        .when(org.springframework.data.mongodb.core.query.Criteria.where(MongoDbConstants.FIELD_STATUS).is(MongoDbConstants.STATUS_RUNNING))
                        .then(1)
                        .otherwise(0)
                ).as("onlineDuration")
                .count().as("tripCount");

        return List.of(matchOperation, groupOperation);
    }

    /**
     * 构建按字段分组统计聚合
     * @param field 分组字段
     * @param countField 计数字段
     * @param matchCriteria 匹配条件
     * @return 聚合操作列表
     */
    public static List<AggregationOperation> buildGroupByAggregation(String field, String countField, 
                                                                   org.springframework.data.mongodb.core.query.Criteria matchCriteria) {
        // 匹配条件
        MatchOperation matchOperation = Aggregation.match(matchCriteria);

        // 按字段分组
        GroupOperation groupOperation = Aggregation.group(field)
                .count().as("count")
                .sum(countField).as("total");

        // 排序
        SortOperation sortOperation = Aggregation.sort(
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "count")
        );

        return List.of(matchOperation, groupOperation, sortOperation);
    }

}