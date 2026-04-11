package com.klzw.service.ai.adapter.impl;

import com.klzw.service.ai.adapter.AiAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeepSeekAdapterTest {

    private DeepSeekAdapter deepSeekAdapter;

    private ChatClient chatClient;

    private ChatClient.ChatClientRequestSpec requestSpec;
    private ChatClient.CallResponseSpec callResponseSpec;

    @BeforeEach
    void setUp() {
        chatClient = mock(ChatClient.class);
        requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        deepSeekAdapter = new DeepSeekAdapter(chatClient);
        ReflectionTestUtils.setField(deepSeekAdapter, "model", "deepseek-V3.2");
    }

    @Test
    void testSendRequest_Success() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("AI分析结果内容");

        Map<String, Object> parameters = new HashMap<>();
        Map<String, Object> result = deepSeekAdapter.sendRequest("测试提示词", parameters);

        assertNotNull(result);
        assertEquals("AI分析结果内容", result.get("content"));
        assertEquals("deepseek-V3.2", result.get("model"));
        assertNotNull(result.get("created"));
        verify(chatClient).prompt();
    }

    @Test
    void testSendRequest_Exception() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenThrow(new RuntimeException("API调用失败"));

        Map<String, Object> parameters = new HashMap<>();
        assertThrows(RuntimeException.class, () -> deepSeekAdapter.sendRequest("测试提示词", parameters));
    }

    @Test
    void testGenerateAnalysisPrompt_StatisticsType() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalTrips", 100);
        data.put("avgSpeed", 60.5);

        String prompt = deepSeekAdapter.generateAnalysisPrompt(data, "statistics");

        assertNotNull(prompt);
        assertTrue(prompt.contains("statistics"));
        assertTrue(prompt.contains("totalTrips"));
        assertTrue(prompt.contains("100"));
        assertTrue(prompt.contains("数据概览"));
        assertTrue(prompt.contains("关键发现"));
        assertTrue(prompt.contains("趋势分析"));
        assertTrue(prompt.contains("优化建议"));
    }

    @Test
    void testGenerateAnalysisPrompt_CostType() {
        Map<String, Object> data = new HashMap<>();
        data.put("fuelCost", 5000.0);

        String prompt = deepSeekAdapter.generateAnalysisPrompt(data, "cost");

        assertNotNull(prompt);
        assertTrue(prompt.contains("cost"));
        assertTrue(prompt.contains("fuelCost"));
    }

    @Test
    void testGenerateAnalysisPrompt_EmptyData() {
        Map<String, Object> data = new HashMap<>();

        String prompt = deepSeekAdapter.generateAnalysisPrompt(data, "statistics");

        assertNotNull(prompt);
        assertTrue(prompt.contains("statistics"));
    }

    @Test
    void testParseResponse_Success() {
        Map<String, Object> response = new HashMap<>();
        response.put("content", "AI分析结果");
        response.put("model", "deepseek-V3.2");

        Map<String, Object> result = deepSeekAdapter.parseResponse(response);

        assertNotNull(result);
        assertEquals("AI分析结果", result.get("content"));
        assertEquals("success", result.get("status"));
    }

    @Test
    void testParseResponse_NullContent() {
        Map<String, Object> response = new HashMap<>();
        response.put("content", null);

        Map<String, Object> result = deepSeekAdapter.parseResponse(response);

        assertNotNull(result);
        assertNull(result.get("content"));
        assertEquals("success", result.get("status"));
    }

    @Test
    void testParseResponse_Exception() {
        Map<String, Object> response = null;

        assertThrows(RuntimeException.class, () -> deepSeekAdapter.parseResponse(response));
    }
}
