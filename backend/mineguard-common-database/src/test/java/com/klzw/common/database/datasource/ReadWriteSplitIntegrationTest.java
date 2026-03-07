package com.klzw.common.database.datasource;

import com.klzw.common.database.AbstractIntegrationTest;
import com.klzw.common.database.mapper.TestMapper;
import com.klzw.common.database.service.TestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 读写分离集成测试
 * <p>
 * 此测试类用于验证MyBatis-Plus基本功能。
 * 注意：完整的读写分离测试需要配置主从数据库。
 */
@DisplayName("MyBatis-Plus集成测试")
class ReadWriteSplitIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestMapper testMapper;

    @Autowired
    private TestService testService;

    @Test
    @DisplayName("验证TestMapper注入")
    void testMapperInjection() {
        assertNotNull(testMapper, "TestMapper应该被正确注入");
    }

    @Test
    @DisplayName("验证TestService注入")
    void testServiceInjection() {
        assertNotNull(testService, "TestService应该被正确注入");
    }

    @Test
    @DisplayName("验证数据库连接")
    void testDatabaseConnection() {
        Long count = testMapper.selectCount(null);
        assertNotNull(count, "查询应该返回结果");
        assertTrue(count >= 0, "查询应该返回非负数");
    }
}
