package com.klzw.service.python;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "com.klzw.service.python",
    "com.klzw.common.core",
    "com.klzw.common.web"
})
public class MineguardPythonServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MineguardPythonServiceApplication.class, args);
    }

}