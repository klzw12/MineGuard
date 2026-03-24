package com.klzw.service.vehicle;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
    "com.klzw.service.vehicle",
    "com.klzw.common.core",
    "com.klzw.common.redis",
    "com.klzw.common.database",
    "com.klzw.common.file",
    "com.klzw.common.mq",
    "com.klzw.common.websocket",
    "com.klzw.common.mongodb",
    "com.klzw.common.web"
})
@MapperScan("com.klzw.service.vehicle.mapper")
@EnableScheduling
public class MineguardVehicleServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MineguardVehicleServiceApplication.class, args);
    }
    
}
