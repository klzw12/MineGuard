package com.klzw.service.vehicle.config;

import com.klzw.service.vehicle.client.TripServiceClient;
import com.klzw.service.vehicle.client.UserServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * 车辆服务配置类
 */
@Configuration
public class VehicleServiceConfig {
    
    @Value("${trip-service.url:http://localhost:8083}")
    private String tripServiceUrl;

    @Value("${user-service.url:http://localhost:8081}")
    private String userServiceUrl;

    @Bean
    public TripServiceClient tripServiceClient() {
        WebClient webClient = WebClient.builder()
                .baseUrl(tripServiceUrl)
                .build();
        WebClientAdapter adapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(TripServiceClient.class);
    }

    @Bean
    public UserServiceClient userServiceClient() {
        WebClient webClient = WebClient.builder()
                .baseUrl(userServiceUrl)
                .build();
        WebClientAdapter adapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(UserServiceClient.class);
    }
    
}