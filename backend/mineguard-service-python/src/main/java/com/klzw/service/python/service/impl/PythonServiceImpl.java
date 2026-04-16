package com.klzw.service.python.service.impl;

import com.klzw.service.python.config.PythonServiceConfig;
import com.klzw.service.python.service.PythonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PythonServiceImpl implements PythonService {

    private final RestTemplate restTemplate;
    private final PythonServiceConfig.PythonServiceProperties pythonServiceProperties;

    @Override
    public Map<String, Object> cleanDrivingData(Map<String, Object> drivingData) {
        String url = pythonServiceProperties.getUrl() + "/api/clean/driving-data";
        log.info("调用Python服务清洗驾驶数据: {}", url);
        return restTemplate.postForObject(url, drivingData, Map.class);
    }

    @Override
    public Map<String, Object> cleanStatisticsData(Map<String, Object> statisticsData) {
        String url = pythonServiceProperties.getUrl() + "/api/clean/statistics-data";
        log.info("调用Python服务清洗统计数据: {}", url);
        return restTemplate.postForObject(url, statisticsData, Map.class);
    }

    @Override
    public Map<String, Object> cleanCostData(Map<String, Object> costData) {
        String url = pythonServiceProperties.getUrl() + "/api/clean/cost-data";
        log.info("调用Python服务清洗成本数据: {}", url);
        return restTemplate.postForObject(url, costData, Map.class);
    }

    @Override
    public Map<String, Object> analyzeDrivingBehavior(Map<String, Object> trackData) {
        String url = pythonServiceProperties.getUrl() + "/api/analysis/driving-behavior";
        log.info("调用Python服务分析驾驶行为: {}", url);
        return restTemplate.postForObject(url, trackData, Map.class);
    }

    @Override
    public int analyzeDrivingBehavior(Long tripId) {
        String url = pythonServiceProperties.getUrl() + "/api/analysis/driving-behavior/" + tripId;
        log.info("调用Python服务分析驾驶行为(按行程ID): {}", url);
        try {
            Integer score = restTemplate.getForObject(url, Integer.class);
            return score != null ? score : 0;
        } catch (Exception e) {
            log.error("调用Python服务分析驾驶行为失败: {}", e.getMessage());
        }
        return 0;
    }

    @Override
    public Map<String, Object> analyzeCost(Map<String, Object> costData) {
        String url = pythonServiceProperties.getUrl() + "/api/analysis/cost";
        log.info("调用Python服务分析成本: {}", url);
        return restTemplate.postForObject(url, costData, Map.class);
    }

    @Override
    public Map<String, Object> analyzeVehicleEfficiency(Map<String, Object> vehicleData) {
        String url = pythonServiceProperties.getUrl() + "/api/analysis/vehicle-efficiency";
        log.info("调用Python服务分析车辆效率: {}", url);
        return restTemplate.postForObject(url, vehicleData, Map.class);
    }

    @Override
    public byte[] exportStatistics(Map<String, Object> exportRequest) {
        String url = pythonServiceProperties.getUrl() + "/api/export/statistics";
        log.info("调用Python服务导出统计报表: {}", url);
        return restTemplate.postForObject(url, exportRequest, byte[].class);
    }

    @Override
    public byte[] exportTripReport(Map<String, Object> exportRequest) {
        String url = pythonServiceProperties.getUrl() + "/api/export/trip-report";
        log.info("调用Python服务导出行程报表: {}", url);
        return restTemplate.postForObject(url, exportRequest, byte[].class);
    }

    @Override
    public byte[] exportCostReport(Map<String, Object> exportRequest) {
        String url = pythonServiceProperties.getUrl() + "/api/export/cost-report";
        log.info("调用Python服务导出成本报表: {}", url);
        return restTemplate.postForObject(url, exportRequest, byte[].class);
    }

    @Override
    public byte[] exportVehicleReport(Map<String, Object> exportRequest) {
        String url = pythonServiceProperties.getUrl() + "/api/export/vehicle-report";
        log.info("调用Python服务导出车辆报表: {}", url);
        return restTemplate.postForObject(url, exportRequest, byte[].class);
    }

    @Override
    public byte[] exportDriverReport(Map<String, Object> exportRequest) {
        String url = pythonServiceProperties.getUrl() + "/api/export/driver-report";
        log.info("调用Python服务导出司机报表: {}", url);
        return restTemplate.postForObject(url, exportRequest, byte[].class);
    }

    @Override
    public Map<String, Object> healthCheck() {
        String url = pythonServiceProperties.getUrl() + "/health";
        log.info("调用Python服务健康检查: {}", url);
        return restTemplate.getForObject(url, Map.class);
    }

}