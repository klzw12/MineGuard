package com.klzw.service.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringAiConfig {

    @Value("${ai.deepseek.api-key}")
    private String deepSeekApiKey;

    @Value("${ai.deepseek.model:deepseek-V3.2}")
    private String deepSeekModel;

    @Value("${ai.deepseek.base-url:https://api.deepseek.com}")
    private String deepSeekBaseUrl;

    @Value("${ai.minimax.api-key}")
    private String minimaxApiKey;

    @Value("${ai.minimax.model:minimax-m2.5}")
    private String minimaxModel;

    @Value("${ai.minimax.base-url:https://api.minimax.chat/v1}")
    private String minimaxBaseUrl;

    @Bean(name = "deepSeekApi")
    public OpenAiApi deepSeekApi() {
        return OpenAiApi.builder()
                .apiKey(deepSeekApiKey)
                .baseUrl(deepSeekBaseUrl)
                .build();
    }

    @Bean(name = "deepSeekChatModel")
    public OpenAiChatModel deepSeekChatModel() {
        OpenAiApi api = deepSeekApi();
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(deepSeekModel)
                .temperature(0.7)
                .build();
        
        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(options)
                .build();
    }

    @Bean(name = "deepSeekChatClient")
    public ChatClient deepSeekChatClient() {
        return ChatClient.builder(deepSeekChatModel()).build();
    }

    @Bean(name = "minimaxApi")
    public OpenAiApi minimaxApi() {
        return OpenAiApi.builder()
                .apiKey(minimaxApiKey)
                .baseUrl(minimaxBaseUrl)
                .build();
    }

    @Bean(name = "minimaxChatModel")
    public OpenAiChatModel minimaxChatModel() {
        OpenAiApi api = minimaxApi();
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(minimaxModel)
                .temperature(0.7)
                .build();
        
        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(options)
                .build();
    }

    @Bean(name = "minimaxChatClient")
    public ChatClient minimaxChatClient() {
        return ChatClient.builder(minimaxChatModel()).build();
    }

    @Bean
    public ChatClient chatClient() {
        return deepSeekChatClient();
    }
}
