package com.klzw.common.database.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.klzw.common.database.properties.DatabaseProperties;
import com.klzw.common.database.util.DruidConfigUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Slf4j
@AutoConfiguration
@ConditionalOnClass(name = "com.alibaba.druid.pool.DruidDataSource")
@ConditionalOnProperty(prefix = "mineguard.database.datasource.master", name = "url")
@EnableConfigurationProperties(DatabaseProperties.class)
public class DataSourceAutoConfiguration {

    private final DatabaseProperties databaseProperties;

    public DataSourceAutoConfiguration(DatabaseProperties databaseProperties) {
        this.databaseProperties = databaseProperties;
    }

    @Bean(name = "masterDataSource")
    @ConditionalOnMissingBean(name = "masterDataSource")
    public DataSource masterDataSource() {
        log.info("开始配置主数据源");
        
        DruidDataSource dataSource = new DruidDataSource();
        
        DatabaseProperties.Datasource.Master masterConfig = databaseProperties.getDatasource().getMaster();
        dataSource.setUrl(masterConfig.getUrl());
        dataSource.setUsername(masterConfig.getUsername());
        dataSource.setPassword(masterConfig.getPassword());
        dataSource.setDriverClassName(masterConfig.getDriverClassName());
        
        DruidConfigUtils.configureDruidDataSource(dataSource, databaseProperties);
        
        log.info("主数据源配置完成");
        return dataSource;
    }

    @Bean(name = "slaveDataSource")
    @ConditionalOnMissingBean(name = "slaveDataSource")
    @ConditionalOnProperty(prefix = "mineguard.database.datasource", name = "slave.url")
    public DataSource slaveDataSource() {
        log.info("开始配置从数据源");
        
        DruidDataSource dataSource = new DruidDataSource();
        
        DatabaseProperties.Datasource.Slave slaveConfig = databaseProperties.getDatasource().getSlave();
        dataSource.setUrl(slaveConfig.getUrl());
        dataSource.setUsername(slaveConfig.getUsername());
        dataSource.setPassword(slaveConfig.getPassword());
        dataSource.setDriverClassName(slaveConfig.getDriverClassName());
        
        DruidConfigUtils.configureDruidDataSource(dataSource, databaseProperties);
        
        log.info("从数据源配置完成");
        return dataSource;
    }

    @Bean
    @ConditionalOnMissingBean
    public JdbcTemplate jdbcTemplate(@Qualifier("dynamicDataSource") DataSource dataSource) {
        log.info("开始配置JdbcTemplate");
        return new JdbcTemplate(dataSource);
    }
}
