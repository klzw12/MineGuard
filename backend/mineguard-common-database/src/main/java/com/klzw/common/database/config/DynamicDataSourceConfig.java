package com.klzw.common.database.config;

import com.klzw.common.database.datasource.DynamicDataSource;
import com.klzw.common.database.properties.DatabaseProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 动态数据源配置类
 * 用于配置动态数据源的切换和管理
 * <p>
 * 注意：此配置需要主从数据源都存在才会生效
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(DatabaseProperties.class)
@ConditionalOnBean(name = {"masterDataSource", "slaveDataSource"})
public class DynamicDataSourceConfig {

    private final DataSource masterDataSource;
    private final DataSource slaveDataSource;
    private final DatabaseProperties databaseProperties;

    public DynamicDataSourceConfig(
            @Qualifier("masterDataSource") DataSource masterDataSource,
            @Qualifier("slaveDataSource") DataSource slaveDataSource,
            DatabaseProperties databaseProperties) {
        this.masterDataSource = masterDataSource;
        this.slaveDataSource = slaveDataSource;
        this.databaseProperties = databaseProperties;
    }

    /**
     * 配置动态数据源
     * @return 动态数据源
     */
    @Primary
    @Bean(name = "dynamicDataSource")
    public DataSource dynamicDataSource() {
        log.info("开始配置动态数据源");
        
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DynamicDataSource.MASTER, masterDataSource);
        targetDataSources.put(DynamicDataSource.SLAVE, slaveDataSource);
        
        dynamicDataSource.setTargetDataSources(targetDataSources);
        dynamicDataSource.setDefaultTargetDataSource(masterDataSource);
        
        log.info("动态数据源配置完成，主数据源: {}, 从数据源: {}", 
                 DynamicDataSource.MASTER, DynamicDataSource.SLAVE);
        
        return dynamicDataSource;
    }

}