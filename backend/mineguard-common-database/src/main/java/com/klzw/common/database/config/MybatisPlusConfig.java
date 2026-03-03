package com.klzw.common.database.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.klzw.common.core.properties.PaginationProperties;
import com.klzw.common.database.interceptor.CustomPaginationInnerInterceptor;
import com.klzw.common.database.properties.DatabaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 * 配置分页插件等核心功能
 * <p>
 * 主要功能：
 * 1. 配置 MyBatis-Plus 拦截器链
 * 2. 集成自定义分页拦截器
 * 3. 统一管理 MyBatis-Plus 相关配置
 */
@Configuration
@EnableConfigurationProperties({DatabaseProperties.class, PaginationProperties.class})
public class MybatisPlusConfig {

    private final DatabaseProperties databaseProperties;
    private final PaginationProperties paginationProperties;

    public MybatisPlusConfig(DatabaseProperties databaseProperties, PaginationProperties paginationProperties) {
        this.databaseProperties = databaseProperties;
        this.paginationProperties = paginationProperties;
    }

    /**
     * 配置 MyBatis-Plus 拦截器
     * <p>
     * 拦截器链配置：
     * 1. 自定义分页拦截器：处理分页逻辑，与 common-core 分页常量保持一致
     * 
     * @return MyBatis-Plus 拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        CustomPaginationInnerInterceptor paginationInterceptor = 
                new CustomPaginationInnerInterceptor(databaseProperties, paginationProperties);
        interceptor.addInnerInterceptor(paginationInterceptor);
        
        return interceptor;
    }

}
