package com.klzw.service.trip.config;

import com.klzw.service.trip.client.UserServiceClient;
import com.klzw.service.trip.client.VehicleServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ClientConfig {

    @Value("${user-service.url:http://localhost:8081}")
    private String userServiceUrl;

    @Value("${vehicle-service.url:http://localhost:8082}")
    private String vehicleServiceUrl;

    @Bean
    public UserServiceClient userServiceClient() {
        WebClient webClient = WebClient.builder()
                .baseUrl(userServiceUrl)
                .build();
        WebClientAdapter adapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(UserServiceClient.class);
    }

    @Bean
    public VehicleServiceClient vehicleServiceClient() {
        WebClient webClient = WebClient.builder()
                .baseUrl(vehicleServiceUrl)
                .build();
        WebClientAdapter adapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(VehicleServiceClient.class);
    }
}