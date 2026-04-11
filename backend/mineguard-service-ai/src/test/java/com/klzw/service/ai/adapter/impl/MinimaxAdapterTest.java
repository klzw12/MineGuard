package com.klzw.service.ai.adapter.impl;

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
class MinimaxAdapterTest {

    private MinimaxAdapter minimaxAdapter;

    private ChatClient chatClient;

    private ChatClient.ChatClientRequestSpec requestSpec;
    private ChatClient.CallResponseSpec callResponseSpec;

    @BeforeEach
    void setUp() {
        chatClient = mock(ChatClient.class);
        requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        minimaxAdapter = new MinimaxAdapter(chatClient);
        ReflectionTestUtils.setField(minimaxAdapter, "model", "Minimax-M2.5");
    }

    @Test
    void testSendRequest_Success() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("Minimax分析结果");

        Map<String, Object> parameters = new HashMap<>();
        Map<String, Object> result = minimaxAdapter.sendRequest("测试提示词", parameters);

        assertNotNull(result);
        assertEquals("Minimax分析结果", result.get("content"));
        assertEquals("Minimax-M2.5", result.get("model"));
        assertNotNull(result.get("created"));
        verify(chatClient).prompt();
    }

    @Test
    void testSendRequest_Exception() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenThrow(new RuntimeException("Minimax API调用失败"));

        Map<String, Object> parameters = new HashMap<>();
        assertThrows(RuntimeException.class, () -> minimaxAdapter.sendRequest("测试提示词", parameters));
    }

    @Test
    void testGenerateAnalysisPrompt_CostType() {
        Map<String, Object> data = new HashMap<>();
        data.put("fuelCost", 5000.0);
        data.put("maintenanceCost", 2000.0);

        String prompt = minimaxAdapter.generateAnalysisPrompt(data, "cost");

        assertNotNull(prompt);
        assertTrue(prompt.contains("cost"));
        assertTrue(prompt.contains("fuelCost"));
        assertTrue(prompt.contains("数据概览"));
        assertTrue(prompt.contains("关键发现"));
    }

    @Test
    void testGenerateAnalysisPrompt_StatisticsType() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalTrips", 50);

        String prompt = minimaxAdapter.generateAnalysisPrompt(data, "statistics");

        assertNotNull(prompt);
        assertTrue(prompt.contains("statistics"));
        assertTrue(prompt.contains("totalTrips"));
    }

    @Test
    void testGenerateAnalysisPrompt_EmptyData() {
        Map<String, Object> data = new HashMap<>();

        String prompt = minimaxAdapter.generateAnalysisPrompt(data, "cost");

        assertNotNull(prompt);
        assertTrue(prompt.contains("cost"));
    }

    @Test
    void testParseResponse_Success() {
        Map<String, Object> response = new HashMap<>();
        response.put("content", "Minimax分析结果");
        response.put("model", "Minimax-M2.5");

        Map<String, Object> result = minimaxAdapter.parseResponse(response);

        assertNotNull(result);
        assertEquals("Minimax分析结果", result.get("content"));
        assertEquals("success", result.get("status"));
    }

    @Test
    void testParseResponse_NullContent() {
        Map<String, Object> response = new HashMap<>();
        response.put("content", null);

        Map<String, Object> result = minimaxAdapter.parseResponse(response);

        assertNotNull(result);
        assertNull(result.get("content"));
        assertEquals("success", result.get("status"));
    }

    @Test
    void testParseResponse_Exception() {
        Map<String, Object> response = null;

        assertThrows(RuntimeException.class, () -> minimaxAdapter.parseResponse(response));
    }
}
