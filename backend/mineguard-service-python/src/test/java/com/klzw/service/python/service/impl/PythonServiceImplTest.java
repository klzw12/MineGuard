package com.klzw.service.python.service.impl;

import com.klzw.service.python.config.PythonServiceConfig;
import com.klzw.service.python.service.PythonService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PythonServiceImplTest {

    @InjectMocks
    private PythonServiceImpl pythonService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private PythonServiceConfig.PythonServiceProperties pythonServiceProperties;

    private Map<String, Object> testData;

    @BeforeEach
    void setUp() {
        testData = new HashMap<>();
        testData.put("key", "value");

        when(pythonServiceProperties.getUrl()).thenReturn("http://localhost:8000");
    }

    @AfterEach
    void tearDown() {
        reset(restTemplate, pythonServiceProperties);
    }

    @Test
    void testCleanDrivingData_Success() {
        Map<String, Object> expectedResult = Map.of("status", "success", "cleaned_data", testData);
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(expectedResult);

        Map<String, Object> result = pythonService.cleanDrivingData(testData);

        assertNotNull(result);
        assertEquals("success", result.get("status"));
        verify(restTemplate).postForObject("http://localhost:8000/api/clean/driving-data", testData, Map.class);
    }

    @Test
    void testCleanDrivingData_Exception() {
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenThrow(new RuntimeException("API异常"));

        assertThrows(RuntimeException.class, () -> pythonService.cleanDrivingData(testData));
    }

    @Test
    void testCleanStatisticsData_Success() {
        Map<String, Object> expectedResult = Map.of("status", "success", "cleaned_data", testData);
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(expectedResult);

        Map<String, Object> result = pythonService.cleanStatisticsData(testData);

        assertNotNull(result);
        assertEquals("success", result.get("status"));
        verify(restTemplate).postForObject("http://localhost:8000/api/clean/statistics-data", testData, Map.class);
    }

    @Test
    void testCleanCostData_Success() {
        Map<String, Object> expectedResult = Map.of("status", "success", "cleaned_data", testData);
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(expectedResult);

        Map<String, Object> result = pythonService.cleanCostData(testData);

        assertNotNull(result);
        assertEquals("success", result.get("status"));
        verify(restTemplate).postForObject("http://localhost:8000/api/clean/cost-data", testData, Map.class);
    }

    @Test
    void testAnalyzeDrivingBehavior_MapParam_Success() {
        Map<String, Object> expectedResult = Map.of("status", "success", "score", 85);
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(expectedResult);

        Map<String, Object> result = pythonService.analyzeDrivingBehavior(testData);

        assertNotNull(result);
        assertEquals("success", result.get("status"));
        verify(restTemplate).postForObject("http://localhost:8000/api/analysis/driving-behavior", testData, Map.class);
    }

    @Test
    void testAnalyzeDrivingBehavior_LongParam_Success() {
        Integer expectedResult = 90;
        when(restTemplate.postForObject(anyString(), any(), eq(Integer.class))).thenReturn(expectedResult);

        int result = pythonService.analyzeDrivingBehavior(1L);

        assertEquals(90, result);
        verify(restTemplate).postForObject("http://localhost:8000/api/analysis/driving-behavior/1", null, Integer.class);
    }

    @Test
    void testAnalyzeDrivingBehavior_LongParam_NullResult() {
        when(restTemplate.postForObject(anyString(), any(), eq(Integer.class))).thenReturn(null);

        int result = pythonService.analyzeDrivingBehavior(1L);

        assertEquals(0, result);
    }

    @Test
    void testAnalyzeCost_Success() {
        Map<String, Object> expectedResult = Map.of("status", "success", "analysis", testData);
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(expectedResult);

        Map<String, Object> result = pythonService.analyzeCost(testData);

        assertNotNull(result);
        assertEquals("success", result.get("status"));
        verify(restTemplate).postForObject("http://localhost:8000/api/analysis/cost", testData, Map.class);
    }

    @Test
    void testAnalyzeVehicleEfficiency_Success() {
        Map<String, Object> expectedResult = Map.of("status", "success", "efficiency", 0.85);
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(expectedResult);

        Map<String, Object> result = pythonService.analyzeVehicleEfficiency(testData);

        assertNotNull(result);
        assertEquals("success", result.get("status"));
        verify(restTemplate).postForObject("http://localhost:8000/api/analysis/vehicle-efficiency", testData, Map.class);
    }

    @Test
    void testExportStatistics_Success() {
        byte[] expectedResult = "test".getBytes();
        when(restTemplate.postForObject(anyString(), any(), eq(byte[].class))).thenReturn(expectedResult);

        byte[] result = pythonService.exportStatistics(testData);

        assertNotNull(result);
        assertEquals(4, result.length);
        verify(restTemplate).postForObject("http://localhost:8000/api/export/statistics", testData, byte[].class);
    }

    @Test
    void testExportTripReport_Success() {
        byte[] expectedResult = "test".getBytes();
        when(restTemplate.postForObject(anyString(), any(), eq(byte[].class))).thenReturn(expectedResult);

        byte[] result = pythonService.exportTripReport(testData);

        assertNotNull(result);
        verify(restTemplate).postForObject("http://localhost:8000/api/export/trip-report", testData, byte[].class);
    }

    @Test
    void testExportCostReport_Success() {
        byte[] expectedResult = "test".getBytes();
        when(restTemplate.postForObject(anyString(), any(), eq(byte[].class))).thenReturn(expectedResult);

        byte[] result = pythonService.exportCostReport(testData);

        assertNotNull(result);
        verify(restTemplate).postForObject("http://localhost:8000/api/export/cost-report", testData, byte[].class);
    }

    @Test
    void testExportVehicleReport_Success() {
        byte[] expectedResult = "test".getBytes();
        when(restTemplate.postForObject(anyString(), any(), eq(byte[].class))).thenReturn(expectedResult);

        byte[] result = pythonService.exportVehicleReport(testData);

        assertNotNull(result);
        verify(restTemplate).postForObject("http://localhost:8000/api/export/vehicle-report", testData, byte[].class);
    }

    @Test
    void testExportDriverReport_Success() {
        byte[] expectedResult = "test".getBytes();
        when(restTemplate.postForObject(anyString(), any(), eq(byte[].class))).thenReturn(expectedResult);

        byte[] result = pythonService.exportDriverReport(testData);

        assertNotNull(result);
        verify(restTemplate).postForObject("http://localhost:8000/api/export/driver-report", testData, byte[].class);
    }

    @Test
    void testHealthCheck_Success() {
        Map<String, Object> expectedResult = Map.of("status", "healthy", "service", "python");
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(expectedResult);

        Map<String, Object> result = pythonService.healthCheck();

        assertNotNull(result);
        assertEquals("healthy", result.get("status"));
        verify(restTemplate).getForObject("http://localhost:8000/health", Map.class);
    }

    @Test
    void testHealthCheck_Exception() {
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenThrow(new RuntimeException("API异常"));

        assertThrows(RuntimeException.class, () -> pythonService.healthCheck());
    }
}
