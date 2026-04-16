package com.klzw.service.ai.service;

import java.util.List;
import java.util.Map;

public interface AiService {

    Map<String, Object> chat(String message, List<Map<String, String>> history);

    Map<String, Object> analyzeStatisticsData(Map<String, Object> statisticsData);

    Map<String, Object> analyzeCostData(Map<String, Object> costData);

    Map<String, Object> generateFinancialReport(Map<String, Object> financialData);

    Map<String, Object> generateOptimizationSuggestions(Map<String, Object> analysisData);

    Map<String, Object> generateDispatchSuggestions(Map<String, Object> dispatchData);

    String getCurrentProvider();

    boolean switchProvider(String provider);

    Map<String, Object> analyzeDrivingBehavior(Map<String, Object> trackData);
}