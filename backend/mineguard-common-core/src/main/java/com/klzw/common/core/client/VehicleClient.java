package com.klzw.common.core.client;

import com.klzw.common.core.domain.dto.VehicleInfo;
import com.klzw.common.core.domain.dto.VehicleStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@HttpExchange
public interface VehicleClient {

    @GetExchange("/api/vehicle/{id}")
    VehicleInfo getById(@PathVariable("id") Long id);

    @GetExchange("/api/vehicle/{id}/exists")
    Boolean existsById(@PathVariable("id") Long id);

    @GetExchange("/api/vehicle/{id}/status")
    VehicleStatus getStatus(@PathVariable("id") Long id);

    @GetExchange("/api/vehicle/available")
    List<VehicleInfo> getAvailableVehicles();

    @PostExchange("/api/vehicle/best")
    VehicleInfo selectBestVehicle(
        @RequestParam(required = false) BigDecimal startLongitude,
        @RequestParam(required = false) BigDecimal startLatitude,
        @RequestParam(required = false) BigDecimal cargoWeight,
        @RequestParam(required = false) String scheduledTime);
    
    @GetExchange("/api/vehicle/count")
    Integer getVehicleCount();
    
    @GetExchange("/api/vehicle/ids")
    List<Long> getVehicleIds();
    
    @GetExchange("/api/vehicle/fault/statistics")
    Map<String, Object> getFaultStatistics(
        @RequestParam("vehicleId") Long vehicleId,
        @RequestParam("date") String date
    );
}
