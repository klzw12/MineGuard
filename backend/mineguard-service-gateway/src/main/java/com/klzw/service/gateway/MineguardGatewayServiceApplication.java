package com.klzw.service.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = {"com.klzw.service.gateway", "com.klzw.common.core"})
@EnableConfigurationProperties
public class MineguardGatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MineguardGatewayServiceApplication.class, args);
    }

}
