package com.klzw.service.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klzw.common.core.client.PythonClient;
import com.klzw.service.ai.dto.ProviderSwitchDTO;
import com.klzw.service.ai.service.AiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;
import static org.mockito.quality.Strictness.LENIENT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.mockito.junit.jupiter.MockitoSettings;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class AiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AiService aiService;

    @Mock
    private PythonClient pythonClient;

    @InjectMocks
    private AiController aiController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(aiController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testAnalyzeStatisticsData() throws Exception {
        Map<String, Object> analysisResult = new HashMap<>();
        analysisResult.put("content", "统计分析结果");
        analysisResult.put("status", "success");

        when(aiService.analyzeStatisticsData(anyMap())).thenReturn(analysisResult);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("totalTrips", 100);
        requestBody.put("avgSpeed", 60.5);

        mockMvc.perform(post("/ai/analyze/statistics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("success"));
    }

    @Test
    void testAnalyzeCostData() throws Exception {
        Map<String, Object> analysisResult = new HashMap<>();
        analysisResult.put("content", "成本分析结果");
        analysisResult.put("status", "success");

        when(aiService.analyzeCostData(anyMap())).thenReturn(analysisResult);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("fuelCost", 5000.0);
        requestBody.put("maintenanceCost", 2000.0);

        mockMvc.perform(post("/ai/analyze/cost")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("success"));
    }

    @Test
    void testGenerateFinancialReport() throws Exception {
        Map<String, Object> reportResult = new HashMap<>();
        reportResult.put("content", "财务报表结果");
        reportResult.put("status", "success");

        when(aiService.generateFinancialReport(anyMap())).thenReturn(reportResult);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("revenue", 100000.0);

        mockMvc.perform(post("/ai/generate/financial-report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("success"));
    }

    @Test
    void testGenerateOptimizationSuggestions() throws Exception {
        Map<String, Object> suggestionsResult = new HashMap<>();
        suggestionsResult.put("content", "优化建议");
        suggestionsResult.put("status", "success");

        when(aiService.generateOptimizationSuggestions(anyMap())).thenReturn(suggestionsResult);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("issues", "油耗偏高");

        mockMvc.perform(post("/ai/generate/optimization-suggestions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("success"));
    }

    @Test
    void testGenerateDispatchSuggestions() throws Exception {
        Map<String, Object> dispatchResult = new HashMap<>();
        dispatchResult.put("content", "调度建议");
        dispatchResult.put("status", "success");

        when(aiService.generateDispatchSuggestions(anyMap())).thenReturn(dispatchResult);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("vehicles", 10);

        mockMvc.perform(post("/ai/generate/dispatch-suggestions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("success"));
    }

    @Test
    void testGetCurrentProvider() throws Exception {
        when(aiService.getCurrentProvider()).thenReturn("deepseek");

        mockMvc.perform(get("/ai/provider/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.currentProvider").value("deepseek"))
                .andExpect(jsonPath("$.data.status").value("success"));
    }

    @Test
    void testSwitchProvider_Success() throws Exception {
        when(aiService.switchProvider("minimax")).thenReturn(true);
        when(aiService.getCurrentProvider()).thenReturn("minimax");

        ProviderSwitchDTO dto = new ProviderSwitchDTO();
        dto.setProvider("minimax");

        mockMvc.perform(post("/ai/provider/switch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.currentProvider").value("minimax"))
                .andExpect(jsonPath("$.data.status").value("success"));
    }

    @Test
    void testSwitchProvider_Failed() throws Exception {
        when(aiService.switchProvider("unsupported")).thenReturn(false);
        when(aiService.getCurrentProvider()).thenReturn("deepseek");

        ProviderSwitchDTO dto = new ProviderSwitchDTO();
        dto.setProvider("unsupported");

        mockMvc.perform(post("/ai/provider/switch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void testAnalyzeDrivingBehavior() throws Exception {
        Map<String, Object> behaviorResult = new HashMap<>();
        behaviorResult.put("status", "success");
        behaviorResult.put("message", "驾驶行为分析完成");
        behaviorResult.put("analysis", Map.of("driving_score", 85));
        behaviorResult.put("cleaning_report", Map.of("removed_outliers", 2));

        when(aiService.analyzeDrivingBehavior(anyMap())).thenReturn(behaviorResult);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("speed", 80.0);
        requestBody.put("duration", 120);

        mockMvc.perform(post("/ai/analyze/driving-behavior")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("success"))
                .andExpect(jsonPath("$.data.message").value("驾驶行为分析完成"));
    }

    @Test
    void testCleanDrivingData() throws Exception {
        Map<String, Object> cleanResult = new HashMap<>();
        cleanResult.put("cleaned_data", Map.of("speed", 78.5));
        cleanResult.put("cleaning_report", Map.of("quality_score", 0.95));

        when(pythonClient.cleanDrivingData(anyMap())).thenReturn(cleanResult);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("speed", 80.0);

        mockMvc.perform(post("/ai/clean/driving-data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.cleaned_data").isNotEmpty());
    }

    @Test
    void testCleanStatisticsData() throws Exception {
        Map<String, Object> cleanResult = new HashMap<>();
        cleanResult.put("cleaned_data", Map.of("totalTrips", 98));

        when(pythonClient.cleanStatisticsData(anyMap())).thenReturn(cleanResult);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("totalTrips", 100);

        mockMvc.perform(post("/ai/clean/statistics-data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.cleaned_data").isNotEmpty());
    }

    @Test
    void testCleanCostData() throws Exception {
        Map<String, Object> cleanResult = new HashMap<>();
        cleanResult.put("cleaned_data", Map.of("fuelCost", 4800.0));

        when(pythonClient.cleanCostData(anyMap())).thenReturn(cleanResult);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("fuelCost", 5000.0);

        mockMvc.perform(post("/ai/clean/cost-data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.cleaned_data").isNotEmpty());
    }
}
