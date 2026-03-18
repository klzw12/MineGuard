package com.klzw.service.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.klzw")
public class MineguardUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MineguardUserServiceApplication.class, args);
    }
}
