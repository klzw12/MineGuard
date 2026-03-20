package com.klzw.common.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j 配置类
 */
@Configuration
@ConditionalOnProperty(value = "knife4j.enable", havingValue = "true", matchIfMissing = true)
public class Knife4jConfig {

    @Bean
    public OpenAPI mineguardOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MineGuard API文档")
                        .description("MineGuard系统API接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("MineGuard Team")
                                .email("")
                                .url("")));
    }
}
