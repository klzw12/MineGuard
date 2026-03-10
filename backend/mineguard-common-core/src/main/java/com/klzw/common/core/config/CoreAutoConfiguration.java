package com.klzw.common.core.config;

import com.klzw.common.core.properties.PaginationProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(prefix = "mineguard.pagination", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(PaginationProperties.class)
public class CoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PaginationProperties paginationProperties() {
        return new PaginationProperties();
    }
}
