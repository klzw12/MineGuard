package com.klzw.common.database.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.klzw.common.database.properties.DatabaseProperties;
import com.klzw.common.database.util.DruidConfigUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * 数据源配置类
 * 配置数据源和连接池
 * <p>
 * 主要功能：
 * 1. 配置主数据源和从数据源
 * 2. 应用 Druid 连接池性能配置
 * 3. 支持主从数据源切换
 * <p>
 * 注意：此配置需要 mineguard.database.datasource.master 配置存在才会生效
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(DatabaseProperties.class)
@ConditionalOnProperty(prefix = "mineguard.database.datasource", name = "master.url")
public class DataSourceConfig {

    /**
     * 数据库配置属性
     */
    private final DatabaseProperties databaseProperties;

    /**
     * 构造函数
     * @param databaseProperties 数据库配置属性
     */
    public DataSourceConfig(DatabaseProperties databaseProperties) {
        this.databaseProperties = databaseProperties;
    }

    /**
     * 配置主数据源
     * <p>
     * 应用 Druid 连接池性能配置，提高数据库连接效率
     * 
     * @return 主数据源
     */
    @Primary
    @Bean(name = "masterDataSource")
    public DataSource masterDataSource() {
        log.info("开始配置主数据源");
        
        DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
        
        // 设置主数据源连接信息
        DatabaseProperties.Datasource.Master masterConfig = databaseProperties.getDatasource().getMaster();
        dataSource.setUrl(masterConfig.getUrl());
        dataSource.setUsername(masterConfig.getUsername());
        dataSource.setPassword(masterConfig.getPassword());
        dataSource.setDriverClassName(masterConfig.getDriverClassName());
        
        // 应用Druid连接池配置
        DruidConfigUtils.configureDruidDataSource(dataSource, databaseProperties);
        
        log.info("主数据源配置完成");
        return dataSource;
    }

    /**
     * 配置从数据源
     * <p>
     * 应用 Druid 连接池性能配置，提高数据库连接效率
     * 
     * @return 从数据源
     */
    @Bean(name = "slaveDataSource")
    @ConditionalOnProperty(prefix = "mineguard.database.datasource", name = "slave.url")
    public DataSource slaveDataSource() {
        log.info("开始配置从数据源");
        
        DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
        
        // 设置从数据源连接信息
        DatabaseProperties.Datasource.Slave slaveConfig = databaseProperties.getDatasource().getSlave();
        dataSource.setUrl(slaveConfig.getUrl());
        dataSource.setUsername(slaveConfig.getUsername());
        dataSource.setPassword(slaveConfig.getPassword());
        dataSource.setDriverClassName(slaveConfig.getDriverClassName());
        
        // 应用Druid连接池配置
        DruidConfigUtils.configureDruidDataSource(dataSource, databaseProperties);
        
        log.info("从数据源配置完成");
        return dataSource;
    }
}
