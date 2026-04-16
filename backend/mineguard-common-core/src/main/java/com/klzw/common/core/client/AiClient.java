package com.klzw.common.core.client;

import com.klzw.common.core.result.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.Map;

@HttpExchange
public interface AiClient {

    @PostExchange("/ai/chat")
    Result<Map<String, Object>> chat(@RequestBody Map<String, Object> request);

    @PostExchange("/ai/analyze/statistics")
    Result<Map<String, Object>> analyzeStatisticsData(@RequestBody Map<String, Object> statisticsData);

    @PostExchange("/ai/analyze/cost")
    Result<Map<String, Object>> analyzeCostData(@RequestBody Map<String, Object> costData);

    @PostExchange("/ai/generate/financial-report")
    Result<Map<String, Object>> generateFinancialReport(@RequestBody Map<String, Object> financialData);

    @PostExchange("/ai/generate/optimization-suggestions")
    Result<Map<String, Object>> generateOptimizationSuggestions(@RequestBody Map<String, Object> analysisData);

    @PostExchange("/ai/generate/dispatch-suggestions")
    Result<Map<String, Object>> generateDispatchSuggestions(@RequestBody Map<String, Object> dispatchData);

    @GetExchange("/ai/provider/current")
    Result<Map<String, Object>> getCurrentProvider();

    @PostExchange("/ai/analyze/driving-behavior")
    Result<Map<String, Object>> analyzeDrivingBehavior(@RequestBody Map<String, Object> trackData);

    @PostExchange("/ai/clean/driving-data")
    Result<Map<String, Object>> cleanDrivingData(@RequestBody Map<String, Object> drivingData);

    @PostExchange("/ai/clean/statistics-data")
    Result<Map<String, Object>> cleanStatisticsData(@RequestBody Map<String, Object> statisticsData);

    @PostExchange("/ai/clean/cost-data")
    Result<Map<String, Object>> cleanCostData(@RequestBody Map<String, Object> costData);
}
