package com.klzw.common.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * Knife4j 配置类
 * 用于配置 API 接口文档
 */
@Configuration
public class Knife4jConfig {
    
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MineGuard API 文档")
                        .description("MineGuard 系统 API 接口文档")
                        .version("1.0.0"));
    }
    
}
