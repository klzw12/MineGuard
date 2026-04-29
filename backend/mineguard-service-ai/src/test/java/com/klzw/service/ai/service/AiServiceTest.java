package com.klzw.service.ai.service;

import com.klzw.common.core.client.PythonClient;
import com.klzw.service.ai.adapter.AiAdapter;
import com.klzw.service.ai.service.impl.AiServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @InjectMocks
    private AiServiceImpl aiService;

    @Mock
    private Map<String, AiAdapter> aiAdapterMap;

    @Mock
    private PythonClient pythonClient;

    @Mock
    private AiAdapter deepSeekAdapter;

    @Mock
    private AiAdapter minimaxAdapter;

    private Map<String, Object> testData;

    @BeforeEach
    void setUp() {
        testData = new HashMap<>();
        testData.put("totalTrips", 100);
        testData.put("totalDistance", 50000.0);
        testData.put("avgSpeed", 60.5);

        ReflectionTestUtils.setField(aiService, "aiProvider", "deepseek");
        ReflectionTestUtils.setField(aiService, "providers", Arrays.asList("deepseek", "minimax"));
    }

    @AfterEach
    void tearDown() {
        reset(aiAdapterMap, pythonClient, deepSeekAdapter, minimaxAdapter);
    }

    @Test
    void testAnalyzeStatisticsData_Success() {
        Map<String, Object> response = new HashMap<>();
        response.put("content", "统计分析结果");
        response.put("model", "deepseek-V3.2");

        when(aiAdapterMap.get("deepseekAdapter")).thenReturn(deepSeekAdapter);
        when(deepSeekAdapter.generateAnalysisPrompt(anyMap(), anyString())).thenReturn("分析提示词");
        when(deepSeekAdapter.sendRequest(anyString(), anyMap())).thenReturn(response);
        when(deepSeekAdapter.parseResponse(anyMap())).thenReturn(Map.of("content", "统计分析结果", "status", "success"));

        Map<String, Object> result = aiService.analyzeStatisticsData(testData);

        assertNotNull(result);
        assertEquals("success", result.get("status"));
        verify(deepSeekAdapter).generateAnalysisPrompt(testData, "statistics");
        verify(deepSeekAdapter).sendRequest(anyString(), anyMap());
        verify(deepSeekAdapter).parseResponse(anyMap());
    }

    @Test
    void testAnalyzeStatisticsData_Exception() {
        when(aiAdapterMap.get("deepseekAdapter")).thenReturn(deepSeekAdapter);
        when(deepSeekAdapter.generateAnalysisPrompt(anyMap(), anyString())).thenThrow(new RuntimeException("API异常"));

        assertThrows(RuntimeException.class, () -> aiService.analyzeStatisticsData(testData));
    }

    @Test
    void testAnalyzeCostData_Success() {
        Map<String, Object> costData = new HashMap<>();
        costData.put("fuelCost", 5000.0);
        costData.put("maintenanceCost", 2000.0);

        Map<String, Object> response = new HashMap<>();
        response.put("content", "成本分析结果");

        when(aiAdapterMap.get("deepseekAdapter")).thenReturn(deepSeekAdapter);
        when(deepSeekAdapter.generateAnalysisPrompt(anyMap(), anyString())).thenReturn("成本分析提示词");
        when(deepSeekAdapter.sendRequest(anyString(), anyMap())).thenReturn(response);
        when(deepSeekAdapter.parseResponse(anyMap())).thenReturn(Map.of("content", "成本分析结果", "status", "success"));

        Map<String, Object> result = aiService.analyzeCostData(costData);

        assertNotNull(result);
        assertEquals("success", result.get("status"));
        verify(deepSeekAdapter).generateAnalysisPrompt(costData, "cost");
    }

    @Test
    void testAnalyzeCostData_Exception() {
        when(aiAdapterMap.get("deepseekAdapter")).thenReturn(deepSeekAdapter);
        when(deepSeekAdapter.generateAnalysisPrompt(anyMap(), anyString())).thenThrow(new RuntimeException("API异常"));

        assertThrows(RuntimeException.class, () -> aiService.analyzeCostData(testData));
    }

    @Test
    void testGenerateFinancialReport_Success() {
        Map<String, Object> financialData = new HashMap<>();
        financialData.put("revenue", 100000.0);
        financialData.put("expenses", 80000.0);

        Map<String, Object> response = new HashMap<>();
        response.put("content", "财务报表结果");

        when(aiAdapterMap.get("deepseekAdapter")).thenReturn(deepSeekAdapter);
        when(deepSeekAdapter.sendRequest(anyString(), anyMap())).thenReturn(response);
        when(deepSeekAdapter.parseResponse(anyMap())).thenReturn(Map.of("content", "财务报表结果", "status", "success"));

        Map<String, Object> result = aiService.generateFinancialReport(financialData);

        assertNotNull(result);
        assertEquals("success", result.get("status"));
        verify(deepSeekAdapter).sendRequest(anyString(), anyMap());
    }

    @Test
    void testGenerateFinancialReport_Exception() {
        when(aiAdapterMap.get("deepseekAdapter")).thenReturn(deepSeekAdapter);
        when(deepSeekAdapter.sendRequest(anyString(), anyMap())).thenThrow(new RuntimeException("API异常"));

        assertThrows(RuntimeException.class, () -> aiService.generateFinancialReport(testData));
    }

    @Test
    void testGenerateOptimizationSuggestions_Success() {
        Map<String, Object> analysisData = new HashMap<>();
        analysisData.put("issues", List.of("油耗偏高", "空载率高"));

        Map<String, Object> response = new HashMap<>();
        response.put("content", "优化建议结果");

        when(aiAdapterMap.get("deepseekAdapter")).thenReturn(deepSeekAdapter);
        when(deepSeekAdapter.sendRequest(anyString(), anyMap())).thenReturn(response);
        when(deepSeekAdapter.parseResponse(anyMap())).thenReturn(Map.of("content", "优化建议结果", "status", "success"));

        Map<String, Object> result = aiService.generateOptimizationSuggestions(analysisData);

        assertNotNull(result);
        assertEquals("success", result.get("status"));
    }

    @Test
    void testGenerateOptimizationSuggestions_Exception() {
        when(aiAdapterMap.get("deepseekAdapter")).thenReturn(deepSeekAdapter);
        when(deepSeekAdapter.sendRequest(anyString(), anyMap())).thenThrow(new RuntimeException("API异常"));

        assertThrows(RuntimeException.class, () -> aiService.generateOptimizationSuggestions(testData));
    }

    @Test
    void testGenerateDispatchSuggestions_Success() {
        Map<String, Object> dispatchData = new HashMap<>();
        dispatchData.put("vehicles", 10);
        dispatchData.put("orders", 20);

        Map<String, Object> response = new HashMap<>();
        response.put("content", "调度建议结果");

        when(aiAdapterMap.get("deepseekAdapter")).thenReturn(deepSeekAdapter);
        when(deepSeekAdapter.sendRequest(anyString(), anyMap())).thenReturn(response);
        when(deepSeekAdapter.parseResponse(anyMap())).thenReturn(Map.of("content", "调度建议结果", "status", "success"));

        Map<String, Object> result = aiService.generateDispatchSuggestions(dispatchData);

        assertNotNull(result);
        assertEquals("success", result.get("status"));
    }

    @Test
    void testGenerateDispatchSuggestions_Exception() {
        when(aiAdapterMap.get("deepseekAdapter")).thenReturn(deepSeekAdapter);
        when(deepSeekAdapter.sendRequest(anyString(), anyMap())).thenThrow(new RuntimeException("API异常"));

        assertThrows(RuntimeException.class, () -> aiService.generateDispatchSuggestions(testData));
    }

    @Test
    void testAnalyzeDrivingBehavior_SuccessWithCleaning() {
        Map<String, Object> trackData = new HashMap<>();
        trackData.put("speed", 80.0);
        trackData.put("duration", 120);

        Map<String, Object> cleanResult = new HashMap<>();
        cleanResult.put("cleaned_data", Map.of("speed", 78.5, "duration", 118));
        cleanResult.put("cleaning_report", Map.of("removed_outliers", 2, "quality_score", 0.95));

        Map<String, Object> aiResponse = new HashMap<>();
        aiResponse.put("content", "驾驶行为分析结果");

        when(pythonClient.cleanDrivingData(trackData)).thenReturn(cleanResult);
        when(aiAdapterMap.get("deepseekAdapter")).thenReturn(deepSeekAdapter);
        when(deepSeekAdapter.sendRequest(anyString(), anyMap())).thenReturn(aiResponse);
        when(deepSeekAdapter.parseResponse(anyMap())).thenReturn(Map.of("content", "驾驶行为分析结果", "status", "success"));

        Map<String, Object> result = aiService.analyzeDrivingBehavior(trackData);

        assertNotNull(result);
        assertEquals("success", result.get("status"));
        assertEquals("驾驶行为分析完成", result.get("message"));
        assertNotNull(result.get("analysis"));
        assertNotNull(result.get("cleaning_report"));
        verify(pythonClient).cleanDrivingData(trackData);
    }

    @Test
    void testAnalyzeDrivingBehavior_FallbackWhenPythonFails() {
        Map<String, Object> trackData = new HashMap<>();
        trackData.put("speed", 80.0);

        Map<String, Object> aiResponse = new HashMap<>();
        aiResponse.put("content", "驾驶行为分析结果");

        when(pythonClient.cleanDrivingData(trackData)).thenThrow(new RuntimeException("Python服务不可用"));
        when(aiAdapterMap.get("deepseekAdapter")).thenReturn(deepSeekAdapter);
        when(deepSeekAdapter.sendRequest(anyString(), anyMap())).thenReturn(aiResponse);
        when(deepSeekAdapter.parseResponse(anyMap())).thenReturn(Map.of("content", "驾驶行为分析结果", "status", "success"));

        Map<String, Object> result = aiService.analyzeDrivingBehavior(trackData);

        assertNotNull(result);
        assertEquals("success", result.get("status"));
        assertEquals("驾驶行为分析完成（直接使用原始数据）", result.get("message"));
        assertNotNull(result.get("analysis"));
        assertNull(result.get("cleaning_report"));
    }

    @Test
    void testAnalyzeDrivingBehavior_DefaultResultWhenAllFail() {
        Map<String, Object> trackData = new HashMap<>();
        trackData.put("speed", 80.0);
        // 添加预警记录以计算分数
        List<Map<String, Object>> warningRecords = new ArrayList<>();
        Map<String, Object> warning = new HashMap<>();
        warning.put("warningLevel", 2); // 中危预警，扣10分
        warningRecords.add(warning);
        trackData.put("warningRecords", warningRecords);

        when(pythonClient.cleanDrivingData(trackData)).thenThrow(new RuntimeException("Python服务不可用"));
        when(aiAdapterMap.get("deepseekAdapter")).thenReturn(deepSeekAdapter);
        when(deepSeekAdapter.sendRequest(anyString(), anyMap())).thenThrow(new RuntimeException("AI服务不可用"));

        Map<String, Object> result = aiService.analyzeDrivingBehavior(trackData);

        assertNotNull(result);
        assertEquals("success", result.get("status"));
        assertNotNull(result.get("analysis"));
        // 基础分100分，中危预警扣10分，所以是90分
        assertEquals(90, ((Map<?, ?>) result.get("analysis")).get("driving_score"));
    }

    @Test
    void testGetCurrentProvider() {
        String provider = aiService.getCurrentProvider();
        assertEquals("deepseek", provider);
    }

    @Test
    void testSwitchProvider_Success() {
        when(aiAdapterMap.get("minimaxAdapter")).thenReturn(minimaxAdapter);

        boolean result = aiService.switchProvider("minimax");

        assertTrue(result);
        assertEquals("minimax", aiService.getCurrentProvider());
    }

    @Test
    void testSwitchProvider_UnsupportedProvider() {
        boolean result = aiService.switchProvider("unsupported");

        assertFalse(result);
        assertEquals("deepseek", aiService.getCurrentProvider());
    }

    @Test
    void testSwitchProvider_AdapterNotFound() {
        when(aiAdapterMap.get("minimaxAdapter")).thenReturn(null);

        boolean result = aiService.switchProvider("minimax");

        assertFalse(result);
        assertEquals("deepseek", aiService.getCurrentProvider());
    }

    @Test
    void testGetAiAdapter_FallbackToDeepSeek() {
        ReflectionTestUtils.setField(aiService, "aiProvider", "unknown");
        when(aiAdapterMap.get("unknownAdapter")).thenReturn(null);
        when(aiAdapterMap.get("deepSeekAdapter")).thenReturn(deepSeekAdapter);

        Map<String, Object> response = new HashMap<>();
        response.put("content", "结果");
        when(deepSeekAdapter.generateAnalysisPrompt(anyMap(), anyString())).thenReturn("提示词");
        when(deepSeekAdapter.sendRequest(anyString(), anyMap())).thenReturn(response);
        when(deepSeekAdapter.parseResponse(anyMap())).thenReturn(Map.of("content", "结果", "status", "success"));

        Map<String, Object> result = aiService.analyzeStatisticsData(testData);

        assertNotNull(result);
        verify(aiAdapterMap).get("unknownAdapter");
        verify(aiAdapterMap).get("deepSeekAdapter");
    }

    @Test
    void testSwitchProvider_Exception() {
        when(aiAdapterMap.get(anyString())).thenThrow(new RuntimeException("异常"));

        boolean result = aiService.switchProvider("minimax");

        assertFalse(result);
    }
}
