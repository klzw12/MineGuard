package com.klzw.common.database.config;

import com.klzw.common.database.datasource.DynamicDataSource;
import com.klzw.common.database.properties.DatabaseProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
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
@AutoConfiguration
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@AutoConfigureBefore(name = "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration")
@ConditionalOnBean(name = {"masterDataSource", "slaveDataSource"})
@EnableConfigurationProperties(DatabaseProperties.class)
public class DynamicDataSourceConfig {

    private final DataSource masterDataSource;
    private final DataSource slaveDataSource;

    public DynamicDataSourceConfig(
            @Qualifier("masterDataSource") DataSource masterDataSource,
            @Qualifier("slaveDataSource") DataSource slaveDataSource) {
        this.masterDataSource = masterDataSource;
        this.slaveDataSource = slaveDataSource;
    }

    /**
     * 配置动态数据源
     *
     * @return 动态数据源
     */
    @Primary
    @Bean(name = "dataSource")
    public DataSource dynamicDataSource() {
        log.info("开始配置动态数据源");

        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DynamicDataSource.MASTER, masterDataSource);
        targetDataSources.put(DynamicDataSource.SLAVE, slaveDataSource);

        DynamicDataSource dynamicDataSource = new DynamicDataSource(masterDataSource, targetDataSources);

        log.info("动态数据源配置完成，主数据源: {}, 从数据源: {}",
                DynamicDataSource.MASTER, DynamicDataSource.SLAVE);

        return dynamicDataSource;
    }

}
