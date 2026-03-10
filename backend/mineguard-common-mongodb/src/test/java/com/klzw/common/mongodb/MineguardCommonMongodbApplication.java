package com.klzw.common.mongodb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MongoDB测试应用启动类
 */
@SpringBootApplication(scanBasePackages = {"com.klzw.common.mongodb", "com.klzw.common.core"})
public class MineguardCommonMongodbApplication {
    public static void main(String[] args) {
        SpringApplication.run(MineguardCommonMongodbApplication.class, args);
    }
}
