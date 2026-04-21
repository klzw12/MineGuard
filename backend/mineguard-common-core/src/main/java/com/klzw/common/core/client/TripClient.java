package com.klzw.common.core.client;

import com.klzw.common.core.domain.dto.TripCreateRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

@HttpExchange
public interface TripClient {

    @GetExchange("/trip/active/{vehicleId}")
    Mono<com.klzw.common.core.result.Result<com.klzw.common.core.domain.dto.TripResponse>> hasActiveTrip(@PathVariable("vehicleId") Long vehicleId);

    @GetExchange("/trip/latest/{vehicleId}")
    Mono<com.klzw.common.core.result.Result<com.klzw.common.core.domain.dto.TripResponse>> getLatestTrip(@PathVariable("vehicleId") Long vehicleId);

    @PostExchange("/trip/{id}/pause")
    Mono<com.klzw.common.core.result.Result<Void>> pauseTrip(@PathVariable("id") Long id);
    
    @PostExchange("/trip/dispatch/create")
    Mono<com.klzw.common.core.result.Result<Long>> createTrip(@RequestBody TripCreateRequest request);
    
    @PostExchange("/trip/{id}/end")
    Mono<com.klzw.common.core.result.Result<Void>> endTrip(@PathVariable("id") Long id);
    
    @GetExchange("/trip/{id}")
    Mono<com.klzw.common.core.result.Result<com.klzw.common.core.domain.dto.TripResponse>> getTripById(@PathVariable("id") Long id);
    
    @GetExchange("/trip/statistics")
    com.klzw.common.core.result.Result<java.util.Map<String, Object>> getStatistics(
        @RequestParam("startDate") String startDate,
        @RequestParam("endDate") String endDate
    );
    
    @GetExchange("/trip/statistics/driver/{driverId}")
    com.klzw.common.core.result.Result<java.util.Map<String, Object>> getDriverStatistics(
        @PathVariable("driverId") Long driverId,
        @RequestParam("startDate") String startDate,
        @RequestParam("endDate") String endDate
    );
    
    @PostExchange("/trip/cancel-by-dispatch/{dispatchTaskId}")
    com.klzw.common.core.result.Result<Void> cancelTripByDispatchTaskId(
        @PathVariable("dispatchTaskId") Long dispatchTaskId,
        @RequestParam(value = "reason", required = false) String reason
    );
}
