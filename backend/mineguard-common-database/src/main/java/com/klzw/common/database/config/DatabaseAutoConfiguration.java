package com.klzw.common.database.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.klzw.common.core.properties.PaginationProperties;
import com.klzw.common.database.handler.EntityMetaObjectHandler;
import com.klzw.common.database.interceptor.CustomPaginationInnerInterceptor;
import com.klzw.common.database.properties.DatabaseProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(name = "com.baomidou.mybatisplus.core.MybatisConfiguration")
@ConditionalOnProperty(prefix = "mineguard.database", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({DatabaseProperties.class, PaginationProperties.class})
public class DatabaseAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor(DatabaseProperties databaseProperties, PaginationProperties paginationProperties) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        CustomPaginationInnerInterceptor paginationInterceptor =
                new CustomPaginationInnerInterceptor(databaseProperties, paginationProperties);
        interceptor.addInnerInterceptor(paginationInterceptor);
        return interceptor;
    }

    @Bean
    @ConditionalOnMissingBean
    public EntityMetaObjectHandler entityMetaObjectHandler() {
        return new EntityMetaObjectHandler();
    }
}
