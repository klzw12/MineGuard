package com.klzw.common.file;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * File模块测试应用启动类
 */
@SpringBootApplication(scanBasePackages = {
    "com.klzw.common.file",
    "com.klzw.common.core"
})
@ConfigurationPropertiesScan
public class MineguardCommonFileApplication {
    public static void main(String[] args) {
        SpringApplication.run(MineguardCommonFileApplication.class, args);
    }
}
