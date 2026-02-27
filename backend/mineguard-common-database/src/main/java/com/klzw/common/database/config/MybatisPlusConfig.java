package com.klzw.common.database.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
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
@EnableConfigurationProperties(DatabaseProperties.class)
public class MybatisPlusConfig {

    /**
     * 数据库配置属性
     */
    private final DatabaseProperties databaseProperties;

    /**
     * 构造函数
     * @param databaseProperties 数据库配置属性
     */
    public MybatisPlusConfig(DatabaseProperties databaseProperties) {
        this.databaseProperties = databaseProperties;
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
        
        // 添加分页插件
        CustomPaginationInnerInterceptor paginationInterceptor = new CustomPaginationInnerInterceptor(databaseProperties);
        interceptor.addInnerInterceptor(paginationInterceptor);
        
        return interceptor;
    }

}
