package com.klzw.common.core.client;

import com.klzw.common.core.domain.dto.TripCreateRequest;
import com.klzw.common.core.domain.dto.TripResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

@HttpExchange
public interface TripClient {

    @GetExchange("/api/trip/active/{vehicleId}")
    Mono<TripResponse> hasActiveTrip(@PathVariable("vehicleId") Long vehicleId);

    @GetExchange("/api/trip/latest/{vehicleId}")
    Mono<TripResponse> getLatestTrip(@PathVariable("vehicleId") Long vehicleId);

    @PostExchange("/api/trip/{id}/pause")
    Mono<Void> pauseTrip(@PathVariable("id") Long id);
    
    @PostExchange("/api/trip/dispatch/create")
    Mono<Long> createTrip(@RequestBody TripCreateRequest request);
    
    @PostExchange("/api/trip/{id}/end")
    Mono<Void> endTrip(@PathVariable("id") Long id);
}
}
