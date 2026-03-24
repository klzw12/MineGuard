package com.klzw.service.warning;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "com.klzw.service.warning",
    "com.klzw.common.core",
    "com.klzw.common.redis",
    "com.klzw.common.database",
    "com.klzw.common.web",
    "com.klzw.common.mq",
    "com.klzw.common.map"
})
@MapperScan("com.klzw.service.warning.mapper")
public class MineguardWarningServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MineguardWarningServiceApplication.class, args);
    }

}
