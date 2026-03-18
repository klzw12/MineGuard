package com.klzw.service.vehicle.client;

import com.klzw.common.core.domain.dto.TripResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

public interface TripServiceClient {
    
    @GetExchange("/api/trip/active/{vehicleId}")
    Mono<TripResponse> hasActiveTrip(@PathVariable("vehicleId") Long vehicleId);
    
    @GetExchange("/api/trip/latest/{vehicleId}")
    Mono<TripResponse> getLatestTrip(@PathVariable("vehicleId") Long vehicleId);
    
    @PostExchange("/api/trip/{id}/pause")
    Mono<Void> pauseTrip(@PathVariable("id") Long id);
}