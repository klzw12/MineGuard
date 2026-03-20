package com.klzw.common.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(
    scanBasePackages = {
        "com.klzw.common.websocket",
        "com.klzw.common.auth",
        "com.klzw.common.redis",
        "com.klzw.common.mongodb",
        "com.klzw.common.mq",
        "com.klzw.common.core"
    },
    exclude = {RedissonAutoConfigurationV2.class}
)
public class WebSocketTestApplication {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
