package com.klzw.common.core.client;

import com.klzw.common.core.domain.dto.VehicleInfo;
import com.klzw.common.core.domain.dto.VehicleStatus;
import com.klzw.common.core.result.Result;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@HttpExchange
public interface VehicleClient {

    @GetExchange("/vehicle/{id}")
    Result<VehicleInfo> getById(@PathVariable("id") Long id);

    @GetExchange("/vehicle/{id}/exists")
    Result<Boolean> existsById(@PathVariable("id") Long id);

    @GetExchange("/vehicle/{id}/status")
    Result<VehicleStatus> getStatus(@PathVariable("id") Long id);

    @PostExchange("/vehicle/{id}/status")
    Result<VehicleStatus> updateStatus(@PathVariable("id") Long id, @RequestBody VehicleStatus status);

    @GetExchange("/vehicle/available")
    Result<List<VehicleInfo>> getAvailableVehicles();

    @PostExchange("/vehicle/best")
    Result<List<VehicleInfo>> selectBestVehicle(
        @RequestParam(required = false) Long driverId,
        @RequestParam(required = false) BigDecimal startLongitude,
        @RequestParam(required = false) BigDecimal startLatitude,
        @RequestParam(required = false) BigDecimal cargoWeight,
        @RequestParam(required = false) String scheduledTime);
    
    @GetExchange("/vehicle/count")
    Result<Integer> getVehicleCount();
    
    @GetExchange("/vehicle/ids")
    Result<List<Long>> getVehicleIds();
    
    @GetExchange("/vehicle/fault/statistics")
    Result<Map<String, Object>> getFaultStatistics(
        @RequestParam("vehicleId") Long vehicleId,
        @RequestParam("date") String date
    );
}
