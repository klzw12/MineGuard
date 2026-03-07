package com.klzw.service.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.klzw.service.user.mapper")
public class MineguardUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MineguardUserServiceApplication.class, args);
    }
}
