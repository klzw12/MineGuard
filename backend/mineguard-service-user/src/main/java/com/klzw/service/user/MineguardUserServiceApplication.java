package com.klzw.service.user;

import com.klzw.service.user.service.AdminInitService;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = {
    "com.klzw.service.user",
    "com.klzw.common.core",
    "com.klzw.common.auth",
    "com.klzw.common.redis",
    "com.klzw.common.database",
    "com.klzw.common.file",
    "com.klzw.common.mq",
    "com.klzw.common.websocket",
    "com.klzw.common.mongodb"
})
@MapperScan("com.klzw.service.user.mapper")
@EnableMongoRepositories(basePackages = "com.klzw.common.websocket.repository")
@RequiredArgsConstructor
public class MineguardUserServiceApplication {

    private final AdminInitService adminInitService;

    public static void main(String[] args) {
        SpringApplication.run(MineguardUserServiceApplication.class, args);
    }

    @Bean
    public ApplicationRunner adminInitRunner() {
        return args -> {
            adminInitService.initAdmin();
        };
    }
}
