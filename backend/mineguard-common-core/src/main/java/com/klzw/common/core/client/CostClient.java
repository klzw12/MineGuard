package com.klzw.common.core.client;

import com.klzw.common.core.domain.dto.CostStatisticsResponseDTO;
import com.klzw.common.core.result.Result;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;
import java.util.Map;

@HttpExchange
public interface CostClient {

    @PostExchange("/cost/detail")
    Result<Map<String, Object>> addCostDetail(@RequestBody Map<String, Object> dto);

    @GetExchange("/cost/detail/{id}")
    Result<Map<String, Object>> getCostDetail(@PathVariable("id") Long id);

    @GetExchange("/cost/detail/trip/{tripId}")
    Result<List<Map<String, Object>>> getCostDetailList(@PathVariable("tripId") Long tripId);

    @PostExchange("/cost/commission")
    Result<Map<String, Object>> recordTripCommission(@RequestBody Map<String, Object> commissionData);

    @GetExchange("/cost/statistics/internal")
    Result<CostStatisticsResponseDTO> getCostStatistics(@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate);

    @GetExchange("/cost/statistics/driver/{userId}")
    Result<Map<String, Object>> getDriverCostStatistics(
            @PathVariable("userId") Long userId,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate);
    
    @PostExchange("/cost/salary/calculate")
    Result<Map<String, Object>> calculateSalaries(@RequestParam("month") String month);
}
