package com.klzw.common.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * 集成测试基类
 * <p>
 * 所有集成测试的父类，提供统一的配置：
 * - 加载测试环境配置（application-test.yml）
 * - 标记为集成测试（@Tag("integration")）
 * - 测试前清理数据
 * <p>
 * 子类只需继承此类即可进行集成测试
 * <p>
 * 注意：集成测试需要配置正确的数据库连接
 */
@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("集成测试")
public abstract class AbstractIntegrationTest {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private DataSource dataSource;
    
    /**
     * 测试前清理数据，确保测试环境干净
     */
    @BeforeEach
    void setUp() throws InterruptedException {
        try {
            // 清理主数据源数据
            jdbcTemplate.execute("DELETE FROM test_table");
            
            // 等待主从同步完成
            Thread.sleep(1000);
        } catch (Exception e) {
            // 忽略清理失败的情况，确保测试能够继续执行
            e.printStackTrace();
        }
    }
}
