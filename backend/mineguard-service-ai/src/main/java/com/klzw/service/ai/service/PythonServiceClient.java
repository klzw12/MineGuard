package com.klzw.service.ai.service;

import java.util.Map;

public interface PythonServiceClient {
    
    Map<String, Object> cleanDrivingData(Map<String, Object> drivingData);
    
    Map<String, Object> cleanStatisticsData(Map<String, Object> statisticsData);
    
    Map<String, Object> cleanCostData(Map<String, Object> costData);
    
    Map<String, Object> analyzeDrivingBehavior(Map<String, Object> trackData);
    
    Map<String, Object> analyzeCost(Map<String, Object> costData);
    
    Map<String, Object> analyzeVehicleEfficiency(Map<String, Object> vehicleData);
    
    String generateAiPrompt(String analysisType, Map<String, Object> data);
    
    byte[] exportStatistics(Map<String, Object> exportRequest);
    
    byte[] exportTripReport(Map<String, Object> exportRequest);
    
    byte[] exportCostReport(Map<String, Object> exportRequest);
    
    byte[] exportVehicleReport(Map<String, Object> exportRequest);
    
    byte[] exportDriverReport(Map<String, Object> exportRequest);
}
