package com.klzw.common.database.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DatabaseProperties 测试")
class DatabasePropertiesTest {

    @Test
    @DisplayName("DatabaseProperties - 默认值 - MybatisPlus.overflow")
    void defaultMybatisPlusOverflow() {
        DatabaseProperties properties = new DatabaseProperties();

        assertTrue(properties.getMybatisPlus().isOverflow());
    }

    @Test
    @DisplayName("DatabaseProperties - 默认值 - MybatisPlus.mapperLocations")
    void defaultMybatisPlusMapperLocations() {
        DatabaseProperties properties = new DatabaseProperties();

        assertEquals("classpath*:mapper/**/*.xml", properties.getMybatisPlus().getMapperLocations());
    }

    @Test
    @DisplayName("DatabaseProperties - 默认值 - MybatisPlus.typeAliasesPackage")
    void defaultMybatisPlusTypeAliasesPackage() {
        DatabaseProperties properties = new DatabaseProperties();

        assertEquals("com.klzw.**.domain", properties.getMybatisPlus().getTypeAliasesPackage());
    }

    @Test
    @DisplayName("DatabaseProperties - 默认值 - Druid.initialSize")
    void defaultDruidInitialSize() {
        DatabaseProperties properties = new DatabaseProperties();

        assertEquals(3, properties.getDruid().getInitialSize());
    }

    @Test
    @DisplayName("DatabaseProperties - 默认值 - Druid.minIdle")
    void defaultDruidMinIdle() {
        DatabaseProperties properties = new DatabaseProperties();

        assertEquals(3, properties.getDruid().getMinIdle());
    }

    @Test
    @DisplayName("DatabaseProperties - 默认值 - Druid.maxActive")
    void defaultDruidMaxActive() {
        DatabaseProperties properties = new DatabaseProperties();

        assertEquals(10, properties.getDruid().getMaxActive());
    }

    @Test
    @DisplayName("DatabaseProperties - 默认值 - Druid.maxWait")
    void defaultDruidMaxWait() {
        DatabaseProperties properties = new DatabaseProperties();

        assertEquals(60000L, properties.getDruid().getMaxWait());
    }

    @Test
    @DisplayName("DatabaseProperties - 默认值 - Druid.timeBetweenEvictionRunsMillis")
    void defaultDruidTimeBetweenEvictionRunsMillis() {
        DatabaseProperties properties = new DatabaseProperties();

        assertEquals(60000L, properties.getDruid().getTimeBetweenEvictionRunsMillis());
    }

    @Test
    @DisplayName("DatabaseProperties - 默认值 - Druid.minEvictableIdleTimeMillis")
    void defaultDruidMinEvictableIdleTimeMillis() {
        DatabaseProperties properties = new DatabaseProperties();

        assertEquals(300000L, properties.getDruid().getMinEvictableIdleTimeMillis());
    }

    @Test
    @DisplayName("DatabaseProperties - 默认值 - Druid.validationQuery")
    void defaultDruidValidationQuery() {
        DatabaseProperties properties = new DatabaseProperties();

        assertEquals("SELECT 1", properties.getDruid().getValidationQuery());
    }

    @Test
    @DisplayName("DatabaseProperties - 默认值 - Druid.testWhileIdle")
    void defaultDruidTestWhileIdle() {
        DatabaseProperties properties = new DatabaseProperties();

        assertTrue(properties.getDruid().isTestWhileIdle());
    }

    @Test
    @DisplayName("DatabaseProperties - 默认值 - Druid.testOnBorrow")
    void defaultDruidTestOnBorrow() {
        DatabaseProperties properties = new DatabaseProperties();

        assertFalse(properties.getDruid().isTestOnBorrow());
    }

    @Test
    @DisplayName("DatabaseProperties - 默认值 - Druid.testOnReturn")
    void defaultDruidTestOnReturn() {
        DatabaseProperties properties = new DatabaseProperties();

        assertFalse(properties.getDruid().isTestOnReturn());
    }

    @Test
    @DisplayName("DatabaseProperties - 默认值 - Druid.poolPreparedStatements")
    void defaultDruidPoolPreparedStatements() {
        DatabaseProperties properties = new DatabaseProperties();

        assertTrue(properties.getDruid().isPoolPreparedStatements());
    }

    @Test
    @DisplayName("DatabaseProperties - 默认值 - Druid.maxPoolPreparedStatementPerConnectionSize")
    void defaultDruidMaxPoolPreparedStatementPerConnectionSize() {
        DatabaseProperties properties = new DatabaseProperties();

        assertEquals(20, properties.getDruid().getMaxPoolPreparedStatementPerConnectionSize());
    }

    @Test
    @DisplayName("DatabaseProperties - 默认值 - Druid.filters")
    void defaultDruidFilters() {
        DatabaseProperties properties = new DatabaseProperties();

        assertEquals("stat,wall,slf4j", properties.getDruid().getFilters());
    }

    @Test
    @DisplayName("DatabaseProperties - 默认值 - Druid.connectionProperties")
    void defaultDruidConnectionProperties() {
        DatabaseProperties properties = new DatabaseProperties();

        assertEquals("druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000", properties.getDruid().getConnectionProperties());
    }

    @Test
    @DisplayName("DatabaseProperties - 默认值 - Druid.statViewUsername")
    void defaultDruidStatViewUsername() {
        DatabaseProperties properties = new DatabaseProperties();

        assertEquals("admin", properties.getDruid().getStatViewUsername());
    }

    @Test
    @DisplayName("DatabaseProperties - 默认值 - Druid.statViewPassword")
    void defaultDruidStatViewPassword() {
        DatabaseProperties properties = new DatabaseProperties();

        assertEquals("", properties.getDruid().getStatViewPassword());
    }

    @Test
    @DisplayName("DatabaseProperties - 默认值 - Druid.statViewAllow")
    void defaultDruidStatViewAllow() {
        DatabaseProperties properties = new DatabaseProperties();

        assertEquals("", properties.getDruid().getStatViewAllow());
    }

    @Test
    @DisplayName("DatabaseProperties - 默认值 - Dynamic.enabled")
    void defaultDynamicEnabled() {
        DatabaseProperties properties = new DatabaseProperties();

        assertFalse(properties.getDynamic().isEnabled());
    }

    @Test
    @DisplayName("DatabaseProperties - 默认值 - Dynamic.primary")
    void defaultDynamicPrimary() {
        DatabaseProperties properties = new DatabaseProperties();

        assertEquals("master", properties.getDynamic().getPrimary());
    }

    @Test
    @DisplayName("DatabaseProperties - 默认值 - Dynamic.slaves")
    void defaultDynamicSlaves() {
        DatabaseProperties properties = new DatabaseProperties();

        assertNotNull(properties.getDynamic().getSlaves());
        assertEquals(0, properties.getDynamic().getSlaves().length);
    }

    @Test
    @DisplayName("DatabaseProperties - 设置MybatisPlus.overflow")
    void setMybatisPlusOverflow() {
        DatabaseProperties properties = new DatabaseProperties();
        properties.getMybatisPlus().setOverflow(false);

        assertFalse(properties.getMybatisPlus().isOverflow());
    }

    @Test
    @DisplayName("DatabaseProperties - 设置MybatisPlus.mapperLocations")
    void setMybatisPlusMapperLocations() {
        DatabaseProperties properties = new DatabaseProperties();
        properties.getMybatisPlus().setMapperLocations("classpath*:custom/mapper/**/*.xml");

        assertEquals("classpath*:custom/mapper/**/*.xml", properties.getMybatisPlus().getMapperLocations());
    }

    @Test
    @DisplayName("DatabaseProperties - 设置MybatisPlus.typeAliasesPackage")
    void setMybatisPlusTypeAliasesPackage() {
        DatabaseProperties properties = new DatabaseProperties();
        properties.getMybatisPlus().setTypeAliasesPackage("com.klzw.custom.domain");

        assertEquals("com.klzw.custom.domain", properties.getMybatisPlus().getTypeAliasesPackage());
    }

    @Test
    @DisplayName("DatabaseProperties - 设置Druid.initialSize")
    void setDruidInitialSize() {
        DatabaseProperties properties = new DatabaseProperties();
        properties.getDruid().setInitialSize(10);

        assertEquals(10, properties.getDruid().getInitialSize());
    }

    @Test
    @DisplayName("DatabaseProperties - 设置Druid.minIdle")
    void setDruidMinIdle() {
        DatabaseProperties properties = new DatabaseProperties();
        properties.getDruid().setMinIdle(10);

        assertEquals(10, properties.getDruid().getMinIdle());
    }

    @Test
    @DisplayName("DatabaseProperties - 设置Druid.maxActive")
    void setDruidMaxActive() {
        DatabaseProperties properties = new DatabaseProperties();
        properties.getDruid().setMaxActive(50);

        assertEquals(50, properties.getDruid().getMaxActive());
    }

    @Test
    @DisplayName("DatabaseProperties - 设置Druid.maxWait")
    void setDruidMaxWait() {
        DatabaseProperties properties = new DatabaseProperties();
        properties.getDruid().setMaxWait(120000L);

        assertEquals(120000L, properties.getDruid().getMaxWait());
    }

    @Test
    @DisplayName("DatabaseProperties - 设置Druid.validationQuery")
    void setDruidValidationQuery() {
        DatabaseProperties properties = new DatabaseProperties();
        properties.getDruid().setValidationQuery("SELECT 1");

        assertEquals("SELECT 1", properties.getDruid().getValidationQuery());
    }

    @Test
    @DisplayName("DatabaseProperties - 设置Druid.testWhileIdle")
    void setDruidTestWhileIdle() {
        DatabaseProperties properties = new DatabaseProperties();
        properties.getDruid().setTestWhileIdle(false);

        assertFalse(properties.getDruid().isTestWhileIdle());
    }

    @Test
    @DisplayName("DatabaseProperties - 设置Druid.testOnBorrow")
    void setDruidTestOnBorrow() {
        DatabaseProperties properties = new DatabaseProperties();
        properties.getDruid().setTestOnBorrow(true);

        assertTrue(properties.getDruid().isTestOnBorrow());
    }

    @Test
    @DisplayName("DatabaseProperties - 设置Druid.testOnReturn")
    void setDruidTestOnReturn() {
        DatabaseProperties properties = new DatabaseProperties();
        properties.getDruid().setTestOnReturn(true);

        assertTrue(properties.getDruid().isTestOnReturn());
    }

    @Test
    @DisplayName("DatabaseProperties - 设置Druid.poolPreparedStatements")
    void setDruidPoolPreparedStatements() {
        DatabaseProperties properties = new DatabaseProperties();
        properties.getDruid().setPoolPreparedStatements(false);

        assertFalse(properties.getDruid().isPoolPreparedStatements());
    }

    @Test
    @DisplayName("DatabaseProperties - 设置Druid.filters")
    void setDruidFilters() {
        DatabaseProperties properties = new DatabaseProperties();
        properties.getDruid().setFilters("stat,wall");

        assertEquals("stat,wall", properties.getDruid().getFilters());
    }

    @Test
    @DisplayName("DatabaseProperties - 设置Druid.connectionProperties")
    void setDruidConnectionProperties() {
        DatabaseProperties properties = new DatabaseProperties();
        properties.getDruid().setConnectionProperties("druid.stat.mergeSql=false");

        assertEquals("druid.stat.mergeSql=false", properties.getDruid().getConnectionProperties());
    }

    @Test
    @DisplayName("DatabaseProperties - 设置Druid.statViewUsername")
    void setDruidStatViewUsername() {
        DatabaseProperties properties = new DatabaseProperties();
        properties.getDruid().setStatViewUsername("customUser");

        assertEquals("customUser", properties.getDruid().getStatViewUsername());
    }

    @Test
    @DisplayName("DatabaseProperties - 设置Druid.statViewPassword")
    void setDruidStatViewPassword() {
        DatabaseProperties properties = new DatabaseProperties();
        properties.getDruid().setStatViewPassword("customPassword");

        assertEquals("customPassword", properties.getDruid().getStatViewPassword());
    }

    @Test
    @DisplayName("DatabaseProperties - 设置Druid.statViewAllow")
    void setDruidStatViewAllow() {
        DatabaseProperties properties = new DatabaseProperties();
        properties.getDruid().setStatViewAllow("127.0.0.1");

        assertEquals("127.0.0.1", properties.getDruid().getStatViewAllow());
    }

    @Test
    @DisplayName("DatabaseProperties - 设置Dynamic.enabled")
    void setDynamicEnabled() {
        DatabaseProperties properties = new DatabaseProperties();
        properties.getDynamic().setEnabled(true);

        assertTrue(properties.getDynamic().isEnabled());
    }

    @Test
    @DisplayName("DatabaseProperties - 设置Dynamic.primary")
    void setDynamicPrimary() {
        DatabaseProperties properties = new DatabaseProperties();
        properties.getDynamic().setPrimary("customMaster");

        assertEquals("customMaster", properties.getDynamic().getPrimary());
    }

    @Test
    @DisplayName("DatabaseProperties - 设置Dynamic.slaves")
    void setDynamicSlaves() {
        DatabaseProperties properties = new DatabaseProperties();
        properties.getDynamic().setSlaves(new String[]{"slave1", "slave2"});

        assertArrayEquals(new String[]{"slave1", "slave2"}, properties.getDynamic().getSlaves());
    }

    @Test
    @DisplayName("DatabaseProperties - MybatisPlus对象不为null")
    void mybatisPlusNotNull() {
        DatabaseProperties properties = new DatabaseProperties();

        assertNotNull(properties.getMybatisPlus());
    }

    @Test
    @DisplayName("DatabaseProperties - Druid对象不为null")
    void druidNotNull() {
        DatabaseProperties properties = new DatabaseProperties();

        assertNotNull(properties.getDruid());
    }

    @Test
    @DisplayName("DatabaseProperties - Dynamic对象不为null")
    void dynamicNotNull() {
        DatabaseProperties properties = new DatabaseProperties();

        assertNotNull(properties.getDynamic());
    }

    @Test
    @DisplayName("DatabaseProperties - 设置零值")
    void setZeroValues() {
        DatabaseProperties properties = new DatabaseProperties();
        properties.getDruid().setInitialSize(0);
        properties.getDruid().setMinIdle(0);
        properties.getDruid().setMaxActive(0);

        assertEquals(0, properties.getDruid().getInitialSize());
        assertEquals(0, properties.getDruid().getMinIdle());
        assertEquals(0, properties.getDruid().getMaxActive());
    }

    @Test
    @DisplayName("DatabaseProperties - 设置负值")
    void setNegativeValues() {
        DatabaseProperties properties = new DatabaseProperties();
        properties.getDruid().setInitialSize(-1);
        properties.getDruid().setMaxWait(-1L);

        assertEquals(-1, properties.getDruid().getInitialSize());
        assertEquals(-1L, properties.getDruid().getMaxWait());
    }

    @Test
    @DisplayName("DatabaseProperties - 设置大值")
    void setLargeValues() {
        DatabaseProperties properties = new DatabaseProperties();
        properties.getDruid().setMaxActive(Integer.MAX_VALUE);
        properties.getDruid().setMaxWait(Long.MAX_VALUE);

        assertEquals(Integer.MAX_VALUE, properties.getDruid().getMaxActive());
        assertEquals(Long.MAX_VALUE, properties.getDruid().getMaxWait());
    }

    @Test
    @DisplayName("DatabaseProperties - 设置空字符串")
    void setEmptyStrings() {
        DatabaseProperties properties = new DatabaseProperties();
        properties.getMybatisPlus().setMapperLocations("");
        properties.getDruid().setValidationQuery("");

        assertEquals("", properties.getMybatisPlus().getMapperLocations());
        assertEquals("", properties.getDruid().getValidationQuery());
    }

    @Test
    @DisplayName("DatabaseProperties - Dynamic.slaves为空数组")
    void dynamicSlavesEmptyArray() {
        DatabaseProperties properties = new DatabaseProperties();
        properties.getDynamic().setSlaves(new String[]{});

        assertNotNull(properties.getDynamic().getSlaves());
        assertEquals(0, properties.getDynamic().getSlaves().length);
    }

    @Test
    @DisplayName("DatabaseProperties - Dynamic.slaves包含多个元素")
    void dynamicSlavesMultipleElements() {
        DatabaseProperties properties = new DatabaseProperties();
        properties.getDynamic().setSlaves(new String[]{"slave1", "slave2", "slave3"});

        assertEquals(3, properties.getDynamic().getSlaves().length);
        assertEquals("slave1", properties.getDynamic().getSlaves()[0]);
        assertEquals("slave2", properties.getDynamic().getSlaves()[1]);
        assertEquals("slave3", properties.getDynamic().getSlaves()[2]);
    }
}
