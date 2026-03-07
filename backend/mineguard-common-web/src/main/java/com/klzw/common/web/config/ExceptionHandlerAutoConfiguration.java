package com.klzw.common.web.config;

import com.klzw.common.core.exception.ExceptionHandlerRegistry;
import com.klzw.common.core.exception.ExceptionHandlerStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@Configuration
public class ExceptionHandlerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ExceptionHandlerRegistry exceptionHandlerRegistry(List<ExceptionHandlerStrategy> strategies) {
        log.info("初始化 ExceptionHandlerRegistry，已发现异常处理策略数量={}", strategies.size());
        return new ExceptionHandlerRegistry(strategies);
    }
}