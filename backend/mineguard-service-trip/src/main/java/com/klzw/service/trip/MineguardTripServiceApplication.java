package com.klzw.service.trip;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.klzw.service.trip.mapper")
@ComponentScan({"com.klzw.service.trip", "com.klzw.common"})
public class MineguardTripServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MineguardTripServiceApplication.class, args);
    }

}
