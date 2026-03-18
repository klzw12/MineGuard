package com.klzw.service.trip.client;

import com.klzw.common.core.domain.dto.VehicleInfo;
import com.klzw.common.core.domain.dto.VehicleStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import reactor.core.publisher.Mono;

public interface VehicleServiceClient {
    
    @GetExchange("http://vehicle-service:8087/api/vehicle/exists/{id}")
    Mono<Boolean> existsById(@PathVariable("id") Long vehicleId);
    
    @GetExchange("http://vehicle-service:8087/api/vehicle/{id}")
    Mono<VehicleInfo> getVehicleById(@PathVariable("id") Long vehicleId);
    
    @GetExchange("http://vehicle-service:8087/api/vehicle/{id}/status")
    Mono<VehicleStatus> getVehicleStatus(@PathVariable("id") Long vehicleId);
}