package com.klzw.service.ai.adapter.impl;

import com.klzw.service.ai.adapter.AiAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component("deepSeekAdapter")
public class DeepSeekAdapter implements AiAdapter {

    private final ChatClient chatClient;

    @Value("${ai.deepseek.model}")
    private String model;

    @Autowired
    public DeepSeekAdapter(@Qualifier("deepSeekChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public Map<String, Object> sendRequest(String prompt, Map<String, Object> parameters) {
        try {
            log.debug("发送请求到DeepSeek API: {}", prompt);
            
            // 使用Spring AI的ChatClient发送请求
            Prompt aiPrompt = new Prompt(new UserMessage(prompt));
            ChatClientResponse response = chatClient.call(aiPrompt);
            
            // 构建响应映射
            Map<String, Object> result = new HashMap<>();
            result.put("content", response.getResult().getOutput().getContent());
            result.put("model", model);
            result.put("created", System.currentTimeMillis() / 1000);
            
            log.debug("收到DeepSeek API响应");
            return result;
        } catch (Exception e) {
            log.error("发送请求到DeepSeek API异常", e);
            throw new RuntimeException("发送请求到DeepSeek API异常", e);
        }
    }

    @Override
    public String generateAnalysisPrompt(Map<String, Object> data, String analysisType) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请分析以下数据并提供详细的分析结果：\n");
        prompt.append("分析类型：").append(analysisType).append("\n");
        prompt.append("数据：\n");
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            prompt.append(entry.getKey()).append(": " ).append(entry.getValue()).append("\n");
        }
        
        prompt.append("\n请提供以下内容：\n");
        prompt.append("1. 数据概览\n");
        prompt.append("2. 关键发现\n");
        prompt.append("3. 趋势分析\n");
        prompt.append("4. 优化建议\n");
        
        return prompt.toString();
    }

    @Override
    public Map<String, Object> parseResponse(Map<String, Object> response) {
        try {
            log.debug("解析DeepSeek API响应");
            
            // 解析DeepSeek API响应
            Map<String, Object> result = new HashMap<>();
            result.put("content", response.get("content"));
            result.put("status", "success");
            return result;
        } catch (Exception e) {
            log.error("解析DeepSeek API响应异常", e);
            throw new RuntimeException("解析DeepSeek API响应异常", e);
        }
    }
}