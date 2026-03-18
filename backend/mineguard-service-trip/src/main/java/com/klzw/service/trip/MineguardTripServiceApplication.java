package com.klzw.service.trip;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.klzw.service.trip.mapper")
public class MineguardTripServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MineguardTripServiceApplication.class, args);
    }

}
