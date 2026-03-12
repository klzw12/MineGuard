package com.klzw.common.core.config;

import com.klzw.common.core.exception.ExceptionHandlerRegistry;
import com.klzw.common.core.exception.ExceptionHandlerStrategy;
import com.klzw.common.core.handler.GlobalExceptionHandler;
import com.klzw.common.core.properties.PaginationProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties(PaginationProperties.class)
public class CoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PaginationProperties paginationProperties() {
        return new PaginationProperties();
    }
    
    @Bean
    @ConditionalOnMissingBean
    public ExceptionHandlerRegistry exceptionHandlerRegistry(List<ExceptionHandlerStrategy> strategies) {
        return new ExceptionHandlerRegistry(strategies);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler(ExceptionHandlerRegistry registry) {
        return new GlobalExceptionHandler(registry);
    }
}
