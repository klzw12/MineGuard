package com.klzw.service.dispatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"com.klzw"})
public class MineguardDispatchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MineguardDispatchServiceApplication.class, args);
    }
}