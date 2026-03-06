package com.klzw.common.file;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;

/**
 * File模块测试应用启动类
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@ComponentScan(basePackages = "com.klzw.common.file")
public class MineguardCommonFileApplication {
    public static void main(String[] args) {
        SpringApplication.run(MineguardCommonFileApplication.class, args);
    }
}
