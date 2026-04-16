package com.klzw.common.core.client;

import com.klzw.common.core.domain.dto.WarningCreateDTO;
import com.klzw.common.core.result.Result;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;
import org.springframework.web.service.annotation.GetExchange;

import java.util.List;
import java.util.Map;

@HttpExchange
public interface WarningClient {

    @PostExchange("/warning/record")
    Result<Long> createWarning(@RequestBody WarningCreateDTO dto);

    @GetExchange("/warning/record/trip")
    Result<List<Map<String, Object>>> getRecordsByTripId(@RequestParam("tripId") Long tripId);

    @PutExchange("/warning/record/{id}/handle")
    Result<Void> handleWarning(@PathVariable("id") Long id, @RequestBody Map<String, Object> handleData);

    @PostExchange("/warning/process-track")
    Result<Map<String, Object>> processTrack(@RequestBody Map<String, Object> trackData);

    @PostExchange("/warning/process-event")
    Result<Map<String, Object>> processEventTrigger(@RequestBody Map<String, Object> eventData);

    @GetExchange("/warning/statistics")
    Result<Map<String, Object>> getStatistics(
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime);

    @GetExchange("/warning/trend")
    Result<List<Map<String, Object>>> getTrend(@RequestParam(value = "days", required = false, defaultValue = "7") int days);

    @PostExchange("/warning/trip/stop-warning")
    Result<Void> stopTripWarningCheck(@RequestParam("tripId") Long tripId);

    @PutExchange("/warning/record/vehicle/{vehicleId}/handle")
    Result<Void> handleWarningsByVehicleId(
            @PathVariable("vehicleId") Long vehicleId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "handleResult", required = false) String handleResult);
}
