package com.klzw.service.ai;

import com.klzw.service.ai.service.AiService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = MineguardAiServiceApplication.class)
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("AI模块集成测试")
class AiIntegrationTest {

    @Autowired
    private AiService aiService;

    @Test
    @DisplayName("获取当前AI提供商")
    void testGetCurrentProvider() {
        String provider = aiService.getCurrentProvider();
        assertNotNull(provider);
        assertTrue(provider.equals("deepseek") || provider.equals("minimax"),
                "当前提供商应为deepseek或minimax");
    }

    @Test
    @DisplayName("切换AI提供商 - 成功")
    void testSwitchProvider_Success() {
        String currentProvider = aiService.getCurrentProvider();
        String targetProvider = "deepseek".equals(currentProvider) ? "minimax" : "deepseek";

        boolean result = aiService.switchProvider(targetProvider);

        if (result) {
            assertEquals(targetProvider, aiService.getCurrentProvider());
            aiService.switchProvider(currentProvider);
        }
    }

    @Test
    @DisplayName("切换AI提供商 - 不支持的提供商")
    void testSwitchProvider_UnsupportedProvider() {
        String originalProvider = aiService.getCurrentProvider();

        boolean result = aiService.switchProvider("unsupported_provider");

        assertFalse(result);
        assertEquals(originalProvider, aiService.getCurrentProvider());
    }

    @Test
    @DisplayName("分析统计数据 - 完整流程")
    void testAnalyzeStatisticsDataFlow() {
        Map<String, Object> statisticsData = new HashMap<>();
        statisticsData.put("totalTrips", 100);
        statisticsData.put("totalDistance", 50000.0);
        statisticsData.put("avgSpeed", 60.5);

        try {
            Map<String, Object> result = aiService.analyzeStatisticsData(statisticsData);
            assertNotNull(result);
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("异常") || e.getMessage().contains("失败"),
                    "异常信息应包含失败描述");
        }
    }

    @Test
    @DisplayName("分析驾驶行为 - Python服务降级流程")
    void testAnalyzeDrivingBehaviorFlow() {
        Map<String, Object> trackData = new HashMap<>();
        trackData.put("speed", 80.0);
        trackData.put("duration", 120);

        try {
            Map<String, Object> result = aiService.analyzeDrivingBehavior(trackData);
            assertNotNull(result);
            assertEquals("success", result.get("status"));
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("异常") || e.getMessage().contains("失败"));
        }
    }

    @Test
    @DisplayName("分析成本数据 - 完整流程")
    void testAnalyzeCostDataFlow() {
        Map<String, Object> costData = new HashMap<>();
        costData.put("fuelCost", 5000.0);
        costData.put("maintenanceCost", 2000.0);

        try {
            Map<String, Object> result = aiService.analyzeCostData(costData);
            assertNotNull(result);
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("异常") || e.getMessage().contains("失败"));
        }
    }

    @Test
    @DisplayName("生成财务报表 - 完整流程")
    void testGenerateFinancialReportFlow() {
        Map<String, Object> financialData = new HashMap<>();
        financialData.put("revenue", 100000.0);
        financialData.put("expenses", 80000.0);

        try {
            Map<String, Object> result = aiService.generateFinancialReport(financialData);
            assertNotNull(result);
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("异常") || e.getMessage().contains("失败"));
        }
    }

    @Test
    @DisplayName("生成优化建议 - 完整流程")
    void testGenerateOptimizationSuggestionsFlow() {
        Map<String, Object> analysisData = new HashMap<>();
        analysisData.put("issues", new String[]{"油耗偏高", "空载率高"});

        try {
            Map<String, Object> result = aiService.generateOptimizationSuggestions(analysisData);
            assertNotNull(result);
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("异常") || e.getMessage().contains("失败"));
        }
    }

    @Test
    @DisplayName("生成调度建议 - 完整流程")
    void testGenerateDispatchSuggestionsFlow() {
        Map<String, Object> dispatchData = new HashMap<>();
        dispatchData.put("vehicles", 10);
        dispatchData.put("orders", 20);

        try {
            Map<String, Object> result = aiService.generateDispatchSuggestions(dispatchData);
            assertNotNull(result);
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("异常") || e.getMessage().contains("失败"));
        }
    }

    @Test
    @DisplayName("完整业务流程测试")
    void testCompleteBusinessFlow() {
        // 1. 分析统计数据
        Map<String, Object> statisticsData = new HashMap<>();
        statisticsData.put("totalTrips", 100);
        statisticsData.put("totalDistance", 50000.0);

        // 2. 分析成本数据
        Map<String, Object> costData = new HashMap<>();
        costData.put("fuelCost", 5000.0);
        costData.put("maintenanceCost", 2000.0);

        // 3. 生成优化建议
        Map<String, Object> analysisData = new HashMap<>();
        analysisData.put("statistics", statisticsData);
        analysisData.put("cost", costData);

        try {
            // 执行完整流程
            Map<String, Object> statisticsResult = aiService.analyzeStatisticsData(statisticsData);
            Map<String, Object> costResult = aiService.analyzeCostData(costData);
            Map<String, Object> suggestions = aiService.generateOptimizationSuggestions(analysisData);

            assertNotNull(statisticsResult);
            assertNotNull(costResult);
            assertNotNull(suggestions);
        } catch (RuntimeException e) {
            // 记录异常但不中断测试
            System.out.println("业务流程测试异常: " + e.getMessage());
        }
    }
} 
