package com.klzw.common.mq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MQ集成测试启动类
 */
@SpringBootApplication(scanBasePackages = {"com.klzw.common.mq", "com.klzw.common.core"})
public class MineguardCommonMqApplication {
    public static void main(String[] args) {
        SpringApplication.run(MineguardCommonMqApplication.class, args);
    }
}
