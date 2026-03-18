package com.klzw.service.statistics;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.klzw.service.statistics.mapper")
public class MineguardStatisticsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MineguardStatisticsServiceApplication.class, args);
    }
}
