package com.klzw.common.database.util;

import com.alibaba.druid.pool.DruidDataSource;
import com.klzw.common.database.exception.DatabaseException;
import com.klzw.common.database.properties.DatabaseProperties;
import com.klzw.common.database.constant.DatabaseResultCode;
import lombok.extern.slf4j.Slf4j;

/**
 * Druid配置工具类
 * <p>
 * 主要功能：
 * 1. 统一Druid数据源配置逻辑
 * 2. 消除代码重复
 * 3. 提供配置复用
 */
@Slf4j
public class DruidConfigUtils {

    /**
     * 配置Druid连接池参数
     * <p>
     * 应用DatabaseProperties中的Druid配置，确保连接池性能最优
     * 
     * @param dataSource 数据源
     * @param databaseProperties 数据库配置属性
     */
    public static void configureDruidDataSource(DruidDataSource dataSource, DatabaseProperties databaseProperties) {
        DatabaseProperties.Druid druidConfig = databaseProperties.getDruid();
        
        dataSource.setInitialSize(druidConfig.getInitialSize());
        dataSource.setMinIdle(druidConfig.getMinIdle());
        dataSource.setMaxActive(druidConfig.getMaxActive());
        dataSource.setMaxWait(druidConfig.getMaxWait());
        dataSource.setTimeBetweenEvictionRunsMillis(druidConfig.getTimeBetweenEvictionRunsMillis());
        dataSource.setMinEvictableIdleTimeMillis(druidConfig.getMinEvictableIdleTimeMillis());
        dataSource.setValidationQuery(druidConfig.getValidationQuery());
        dataSource.setTestWhileIdle(druidConfig.isTestWhileIdle());
        dataSource.setTestOnBorrow(druidConfig.isTestOnBorrow());
        dataSource.setTestOnReturn(druidConfig.isTestOnReturn());
        dataSource.setPoolPreparedStatements(druidConfig.isPoolPreparedStatements());
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(druidConfig.getMaxPoolPreparedStatementPerConnectionSize());
        
        try {
            dataSource.setFilters(druidConfig.getFilters());
            dataSource.setConnectionProperties(druidConfig.getConnectionProperties());
        } catch (Exception e) {
            log.error("配置Druid数据源失败: {}", e.getMessage(), e);
            throw new DatabaseException(DatabaseResultCode.DATA_SOURCE_ERROR, e);
        }
    }
}
