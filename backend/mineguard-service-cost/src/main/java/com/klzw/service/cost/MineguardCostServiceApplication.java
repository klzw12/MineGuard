package com.klzw.service.cost;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.klzw.service.cost.mapper")
public class MineguardCostServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MineguardCostServiceApplication.class, args);
    }
}
