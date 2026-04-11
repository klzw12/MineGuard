package com.klzw.common.database;

import com.klzw.common.core.config.DotenvInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import javax.sql.DataSource;


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
    private static boolean tablesInitialized = false;

    @BeforeEach
    void setUp() {
        if (!tablesInitialized) {
            initTables();
            tablesInitialized = true;
        }
        System.out.println("=== 开始清理测试数据 ===");
        try {
            jdbcTemplate.execute("TRUNCATE TABLE test_table");
            System.out.println("清理 test_table 表数据");
        } catch (Exception e) {
            System.out.println("清理数据时发生异常: " + e.getMessage());
        }
    }

    private void initTables() {
        System.out.println("=== 初始化测试表 ===");
        try {
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS test_table (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    age INT,
                    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
            System.out.println("test_table 表创建成功");
        } catch (Exception e) {
            System.out.println("创建表时发生异常: " + e.getMessage());
        }
    }
    
    /**
     * 测试后清理数据，确保测试环境干净
     */
    @AfterEach
    void tearDown() {
        System.out.println("=== 开始清理测试数据 ===");
        try {
            jdbcTemplate.execute("TRUNCATE TABLE test_table");
            System.out.println("清理 test_table 表数据");
        } catch (Exception e) {
            System.out.println("清理数据时发生异常: " + e.getMessage());
        }
    }
}
