package com.klzw.common.database;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 测试应用启动类
 * 用于集成测试
 */
@SpringBootApplication
@MapperScan("com.klzw.common.database.mapper")
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
