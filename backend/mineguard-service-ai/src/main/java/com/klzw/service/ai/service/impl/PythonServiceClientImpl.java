package com.klzw.service.ai.service.impl;

import com.klzw.common.core.util.HttpUtils;
import com.klzw.common.core.util.JsonUtils;
import com.klzw.service.ai.service.PythonServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PythonServiceClientImpl implements PythonServiceClient {

    @Value("${python-service.url:http://localhost:8008}")
    private String pythonServiceUrl;

    @Override
    public Map<String, Object> cleanDrivingData(Map<String, Object> drivingData) {
        try {
            String url = pythonServiceUrl + "/api/clean/driving-data";
            String jsonBody = JsonUtils.toJson(drivingData);
            String response = HttpUtils.postJson(url, jsonBody);
            return JsonUtils.parseObject(response, Map.class);
        } catch (Exception e) {
            log.error("调用Python服务清洗驾驶数据失败", e);
            return createErrorResponse("清洗驾驶数据失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> cleanStatisticsData(Map<String, Object> statisticsData) {
        try {
            String url = pythonServiceUrl + "/api/clean/statistics-data";
            String jsonBody = JsonUtils.toJson(statisticsData);
            String response = HttpUtils.postJson(url, jsonBody);
            return JsonUtils.parseObject(response, Map.class);
        } catch (Exception e) {
            log.error("调用Python服务清洗统计数据失败", e);
            return createErrorResponse("清洗统计数据失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> cleanCostData(Map<String, Object> costData) {
        try {
            String url = pythonServiceUrl + "/api/clean/cost-data";
            String jsonBody = JsonUtils.toJson(costData);
            String response = HttpUtils.postJson(url, jsonBody);
            return JsonUtils.parseObject(response, Map.class);
        } catch (Exception e) {
            log.error("调用Python服务清洗成本数据失败", e);
            return createErrorResponse("清洗成本数据失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> analyzeDrivingBehavior(Map<String, Object> trackData) {
        try {
            String url = pythonServiceUrl + "/api/analysis/driving-behavior";
            Map<String, Object> request = new HashMap<>();
            request.put("analysis_type", "driving");
            request.put("data", trackData);
            String jsonBody = JsonUtils.toJson(request);
            String response = HttpUtils.postJson(url, jsonBody);
            return JsonUtils.parseObject(response, Map.class);
        } catch (Exception e) {
            log.error("调用Python服务分析驾驶行为失败", e);
            return createErrorResponse("分析驾驶行为失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> analyzeCost(Map<String, Object> costData) {
        try {
            String url = pythonServiceUrl + "/api/analysis/cost-analysis";
            Map<String, Object> request = new HashMap<>();
            request.put("analysis_type", "cost");
            request.put("data", costData);
            String jsonBody = JsonUtils.toJson(request);
            String response = HttpUtils.postJson(url, jsonBody);
            return JsonUtils.parseObject(response, Map.class);
        } catch (Exception e) {
            log.error("调用Python服务分析成本失败", e);
            return createErrorResponse("分析成本失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> analyzeVehicleEfficiency(Map<String, Object> vehicleData) {
        try {
            String url = pythonServiceUrl + "/api/analysis/vehicle-efficiency";
            Map<String, Object> request = new HashMap<>();
            request.put("analysis_type", "vehicle");
            request.put("data", vehicleData);
            String jsonBody = JsonUtils.toJson(request);
            String response = HttpUtils.postJson(url, jsonBody);
            return JsonUtils.parseObject(response, Map.class);
        } catch (Exception e) {
            log.error("调用Python服务分析车辆效率失败", e);
            return createErrorResponse("分析车辆效率失败: " + e.getMessage());
        }
    }

    @Override
    public String generateAiPrompt(String analysisType, Map<String, Object> data) {
        try {
            String url = pythonServiceUrl + "/api/analysis/ai-prompt";
            Map<String, Object> request = new HashMap<>();
            request.put("analysis_type", analysisType);
            request.put("data", data);
            String jsonBody = JsonUtils.toJson(request);
            String response = HttpUtils.postJson(url, jsonBody);
            Map<String, Object> result = JsonUtils.parseObject(response, Map.class);
            return (String) result.get("prompt");
        } catch (Exception e) {
            log.error("调用Python服务生成AI提示词失败", e);
            return "请分析以下数据: " + JsonUtils.toJson(data);
        }
    }

    @Override
    public byte[] exportStatistics(Map<String, Object> exportRequest) {
        try {
            String url = pythonServiceUrl + "/api/export/statistics";
            String jsonBody = JsonUtils.toJson(exportRequest);
            return HttpUtils.postJsonForBytes(url, jsonBody);
        } catch (Exception e) {
            log.error("调用Python服务导出统计报表失败", e);
            return null;
        }
    }

    @Override
    public byte[] exportTripReport(Map<String, Object> exportRequest) {
        try {
            String url = pythonServiceUrl + "/api/export/trip-report";
            String jsonBody = JsonUtils.toJson(exportRequest);
            return HttpUtils.postJsonForBytes(url, jsonBody);
        } catch (Exception e) {
            log.error("调用Python服务导出行程报表失败", e);
            return null;
        }
    }

    @Override
    public byte[] exportCostReport(Map<String, Object> exportRequest) {
        try {
            String url = pythonServiceUrl + "/api/export/cost-report";
            String jsonBody = JsonUtils.toJson(exportRequest);
            return HttpUtils.postJsonForBytes(url, jsonBody);
        } catch (Exception e) {
            log.error("调用Python服务导出成本报表失败", e);
            return null;
        }
    }

    @Override
    public byte[] exportVehicleReport(Map<String, Object> exportRequest) {
        try {
            String url = pythonServiceUrl + "/api/export/vehicle-report";
            String jsonBody = JsonUtils.toJson(exportRequest);
            return HttpUtils.postJsonForBytes(url, jsonBody);
        } catch (Exception e) {
            log.error("调用Python服务导出车辆报表失败", e);
            return null;
        }
    }

    @Override
    public byte[] exportDriverReport(Map<String, Object> exportRequest) {
        try {
            String url = pythonServiceUrl + "/api/export/driver-report";
            String jsonBody = JsonUtils.toJson(exportRequest);
            return HttpUtils.postJsonForBytes(url, jsonBody);
        } catch (Exception e) {
            log.error("调用Python服务导出司机报表失败", e);
            return null;
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", message);
        return response;
    }
}
