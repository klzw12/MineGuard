package com.klzw.common.database.datasource;

import com.klzw.common.database.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 主从数据源集成测试
 * <p>
 * 此测试类用于验证数据源配置是否正确加载。
 * 注意：动态数据源测试需要配置主从数据库，此测试仅验证基本数据源配置。
 */
@DisplayName("数据源集成测试")
class MasterSlaveIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("验证数据源配置加载")
    void testDataSourceConfiguration() {
        assertNotNull(dataSource, "数据源应该被正确配置");
    }

    @Test
    @DisplayName("验证数据源类型")
    void testDataSourceType() {
        String dataSourceClassName = dataSource.getClass().getName();
        assertTrue(
            dataSourceClassName.contains("Druid") || 
            dataSourceClassName.contains("Hikari") ||
            dataSourceClassName.contains("Dynamic"),
            "数据源应该是Druid、Hikari或Dynamic类型，实际类型: " + dataSourceClassName
        );
    }
}
