package com.klzw.service.cost;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "com.klzw.service.cost",
    "com.klzw.common.core",
    "com.klzw.common.redis",
    "com.klzw.common.database",
    "com.klzw.common.web",
    "com.klzw.common.mq"
})
@MapperScan("com.klzw.service.cost.mapper")
public class MineguardCostServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MineguardCostServiceApplication.class, args);
    }
}
