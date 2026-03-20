package com.klzw.common.redis;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Redis集成测试基类
 * <p>
 * 所有Redis集成测试的父类，提供统一的配置：
 * - 加载测试环境配置（application-test.yml）
 * - 标记为集成测试（@Tag("integration")）
 * - 测试前后清理数据
 * <p>
 * 子类只需继承此类即可进行集成测试
 * <p>
 * 注意：集成测试需要配置正确的Redis连接
 */
@SpringBootTest(classes = MineguardCommonRedisApplication.class)
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("Redis集成测试")
public abstract class AbstractRedisIntegrationTest {
    
    @Autowired
    protected RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 测试前清理数据，确保测试环境干净
     */
    @BeforeEach
    void setUp() {
        try {
            // 清理测试相关键
            redisTemplate.delete("test:lock:distributed");
            redisTemplate.delete("test:lock:redisson");
            redisTemplate.delete("test:permission:user:*");
            redisTemplate.delete("test:*");
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
            // 清理测试相关键
            redisTemplate.delete("test:lock:distributed");
            redisTemplate.delete("test:lock:redisson");
            redisTemplate.delete("test:permission:user:*");
            redisTemplate.delete("test:*");
        } catch (Exception e) {
            // 忽略清理失败
        }
    }
}
