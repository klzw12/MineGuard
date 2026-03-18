package com.klzw.service.warning;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.klzw.service.warning.mapper")
public class MineguardWarningServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MineguardWarningServiceApplication.class, args);
    }

}
