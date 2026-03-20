package com.klzw.common.mongodb;

import com.klzw.common.core.config.DotenvInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * MongoDB集成测试基类
 * <p>
 * 所有MongoDB集成测试的父类，提供统一的配置：
 * - 加载测试环境配置（application-test.yml）
 * - 标记为集成测试（@Tag("integration")）
 * - 测试前后清理数据
 * <p>
 * 子类只需继承此类即可进行集成测试
 * <p>
 * 注意：集成测试需要配置正确的MongoDB连接
 */
@SpringBootTest(classes = MineguardCommonMongodbApplication.class)
@ContextConfiguration(initializers = DotenvInitializer.class)
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("MongoDB集成测试")
public abstract class AbstractMongoDbIntegrationTest {
    
    @Autowired
    protected MongoTemplate mongoTemplate;
    
    /**
     * 测试前清理数据，确保测试环境干净
     */
    @BeforeEach
    void setUp() {
        try {
            // 清理测试相关集合
            if (mongoTemplate.collectionExists("test_timeseries")) {
                mongoTemplate.dropCollection("test_timeseries");
            }
            if (mongoTemplate.collectionExists("test_ttl")) {
                mongoTemplate.dropCollection("test_ttl");
            }
            if (mongoTemplate.collectionExists("test_collection")) {
                mongoTemplate.dropCollection("test_collection");
            }
        } catch (Exception e) {
            // 忽略清理失败
        }
    }
    
    /**
     * 测试后清理数据，确保测试环境干净
     */
    @AfterEach
    void tearDown() {
        try {
            // 清理测试相关集合
            if (mongoTemplate.collectionExists("test_timeseries")) {
                mongoTemplate.dropCollection("test_timeseries");
            }
            if (mongoTemplate.collectionExists("test_ttl")) {
                mongoTemplate.dropCollection("test_ttl");
            }
            if (mongoTemplate.collectionExists("test_collection")) {
                mongoTemplate.dropCollection("test_collection");
            }
        } catch (Exception e) {
            // 忽略清理失败
        }
    }
}
