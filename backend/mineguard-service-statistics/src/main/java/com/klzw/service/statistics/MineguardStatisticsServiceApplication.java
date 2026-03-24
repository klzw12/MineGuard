package com.klzw.service.statistics;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = {
    "com.klzw.service.statistics",
    "com.klzw.common.core",
    "com.klzw.common.redis",
    "com.klzw.common.database",
    "com.klzw.common.web",
    "com.klzw.common.mq"
})

@MapperScan("com.klzw.service.statistics.mapper")
public class MineguardStatisticsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MineguardStatisticsServiceApplication.class, args);
    }
}
