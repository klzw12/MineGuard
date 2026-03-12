package com.klzw.common.mongodb.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AggregationUtil 聚合查询工具测试
 */
@DisplayName("AggregationUtil 聚合查询工具测试")
class AggregationUtilTest {

    @Test
    @DisplayName("构建行驶里程统计聚合应成功")
    void buildMileageAggregation_shouldBuildAggregation() {
        // 准备
        long startTime = System.currentTimeMillis() - 86400000; // 24小时前
        long endTime = System.currentTimeMillis();

        // 执行
        List<AggregationOperation> operations = AggregationUtil.buildMileageAggregation(startTime, endTime);

        // 验证
        assertNotNull(operations);
        assertFalse(operations.isEmpty());
        assertEquals(3, operations.size()); // match, group, sort
    }

    @Test
    @DisplayName("构建预警趋势分析聚合应成功")
    void buildWarningTrendAggregation_shouldBuildAggregation() {
        // 准备
        long startTime = System.currentTimeMillis() - 86400000; // 24小时前
        long endTime = System.currentTimeMillis();

        // 执行
        List<AggregationOperation> operations = AggregationUtil.buildWarningTrendAggregation(startTime, endTime);

        // 验证
        assertNotNull(operations);
        assertFalse(operations.isEmpty());
        assertEquals(3, operations.size()); // match, group, sort
    }

    @Test
    @DisplayName("构建车辆活跃度统计聚合应成功")
    void buildVehicleActivityAggregation_shouldBuildAggregation() {
        // 准备
        long startTime = System.currentTimeMillis() - 86400000; // 24小时前
        long endTime = System.currentTimeMillis();

        // 执行
        List<AggregationOperation> operations = AggregationUtil.buildVehicleActivityAggregation(startTime, endTime);

        // 验证
        assertNotNull(operations);
        assertFalse(operations.isEmpty());
        assertEquals(2, operations.size()); // match, group
    }

    @Test
    @DisplayName("构建按字段分组统计聚合应成功")
    void buildGroupByAggregation_shouldBuildAggregation() {
        // 准备
        String field = "category";
        String countField = "amount";
        Criteria matchCriteria = Criteria.where("status").is("active");

        // 执行
        List<AggregationOperation> operations = AggregationUtil.buildGroupByAggregation(field, countField, matchCriteria);

        // 验证
        assertNotNull(operations);
        assertFalse(operations.isEmpty());
        assertEquals(3, operations.size()); // match, group, sort
    }

    @Test
    @DisplayName("私有构造函数应无法实例化")
    void privateConstructor_shouldNotBeInstantiable() throws Exception {
        // 准备
        var constructor = AggregationUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        // 执行和验证
        assertDoesNotThrow(() -> constructor.newInstance());
    }
}