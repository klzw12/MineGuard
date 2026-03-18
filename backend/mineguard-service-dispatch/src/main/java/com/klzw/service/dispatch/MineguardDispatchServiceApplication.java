package com.klzw.service.dispatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MineguardDispatchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MineguardDispatchServiceApplication.class, args);
    }
}