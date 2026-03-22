package com.klzw.common.core.config;

import com.klzw.common.core.client.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpExchangeConfig {

    @Value("${service.user.url:http://localhost:8081}")
    private String userServiceUrl;

    @Value("${service.vehicle.url:http://localhost:8082}")
    private String vehicleServiceUrl;

    @Value("${service.trip.url:http://localhost:8083}")
    private String tripServiceUrl;

    @Value("${service.dispatch.url:http://localhost:8084}")
    private String dispatchServiceUrl;

    @Value("${service.cost.url:http://localhost:8085}")
    private String costServiceUrl;

    @Value("${service.ai.url:http://localhost:8086}")
    private String aiServiceUrl;

    @Value("${service.statistics.url:http://localhost:8087}")
    private String statisticsServiceUrl;

    private <T> T createClient(Class<T> clientClass, String baseUrl) {
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        WebClientAdapter adapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(clientClass);
    }

    @Bean
    public UserClient userClient() {
        return createClient(UserClient.class, userServiceUrl);
    }

    @Bean
    public DriverClient driverClient() {
        return createClient(DriverClient.class, userServiceUrl);
    }

    @Bean
    public MessageClient messageClient() {
        return createClient(MessageClient.class, userServiceUrl);
    }

    @Bean
    public VehicleClient vehicleClient() {
        return createClient(VehicleClient.class, vehicleServiceUrl);
    }

    @Bean
    public TripClient tripClient() {
        return createClient(TripClient.class, tripServiceUrl);
    }

    @Bean
    public DispatchClient dispatchClient() {
        return createClient(DispatchClient.class, dispatchServiceUrl);
    }
    
    @Bean
    public StatisticsClient statisticsClient() {
        return createClient(StatisticsClient.class, tripServiceUrl);
    }
    
    @Bean
    public CostClient costClient() {
        return createClient(CostClient.class, costServiceUrl);
    }
    
    @Bean
    public TransportClient transportClient() {
        return createClient(TransportClient.class, statisticsServiceUrl);
    }
}
