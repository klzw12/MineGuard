package com.klzw.common.database;

import com.klzw.common.core.config.DotenvInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import javax.sql.DataSource;
import java.sql.Connection;


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
@ContextConfiguration(initializers = DotenvInitializer.class)
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("集成测试")
public abstract class AbstractIntegrationTest {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    @SuppressWarnings("unused")
    private DataSource dataSource;
    
    /**
     * 测试前清理数据，确保测试环境干净
     */
    @BeforeEach
    void setUp() {
        try {
            jdbcTemplate.execute("DELETE FROM test_table WHERE 1=1");
        } catch (Exception e) {
            // 忽略清理失败
        }
    }
}
