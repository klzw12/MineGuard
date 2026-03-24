package com.klzw.service.user;

import com.klzw.service.user.service.AdminInitService;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = {
    "com.klzw.service.user",
    "com.klzw.common.core",
    "com.klzw.common.auth",
    "com.klzw.common.redis",
    "com.klzw.common.database",
    "com.klzw.common.file",
    "com.klzw.common.mq",
    "com.klzw.common.websocket",
    "com.klzw.common.mongodb",
        "com.klzw.common.web"
})
@MapperScan("com.klzw.service.user.mapper")
@RequiredArgsConstructor
public class MineguardUserServiceApplication {

    private final AdminInitService adminInitService;

    public static void main(String[] args) {
        SpringApplication.run(MineguardUserServiceApplication.class, args);
    }

    @Bean
    public ApplicationRunner adminInitRunner() {
        return args -> adminInitService.initAdmin();
    }
}
