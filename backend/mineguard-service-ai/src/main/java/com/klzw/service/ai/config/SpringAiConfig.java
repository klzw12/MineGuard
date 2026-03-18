package com.klzw.service.ai.config;

import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class SpringAiConfig {

    // DeepSeek配置
    @Value("${ai.deepseek.api-key}")
    private String deepSeekApiKey;

    @Value("${ai.deepseek.model}")
    private String deepSeekModel;

    // Minimax配置
    @Value("${ai.minimax.api-key}")
    private String minimaxApiKey;

    @Value("${ai.minimax.model}")
    private String minimaxModel;

    // DeepSeek API配置
    @Bean(name = "deepSeekApi")
    public OpenAiApi deepSeekApi() {
        // 配置RestClient指向DeepSeek API
        RestClient restClient = RestClient.builder()
                .baseUrl("https://api.deepseek.com/v1")
                .defaultHeader("Authorization", "Bearer " + deepSeekApiKey)
                .build();
        
        return new OpenAiApi(restClient);
    }

    // DeepSeek ChatClient
    @Bean(name = "deepSeekChatClient")
    public OpenAiChatClient deepSeekChatClient() {
        OpenAiApi api = deepSeekApi();
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withModel(deepSeekModel)
                .withTemperature(0.7f)
                .build();
        
        return new OpenAiChatClient(api, options);
    }

    // Minimax API配置
    @Bean(name = "minimaxApi")
    public OpenAiApi minimaxApi() {
        // 配置RestClient指向Minimax API
        RestClient restClient = RestClient.builder()
                .baseUrl("https://api.minimax.chat/v1")
                .defaultHeader("Authorization", "Bearer " + minimaxApiKey)
                .build();
        
        return new OpenAiApi(restClient);
    }

    // Minimax ChatClient
    @Bean(name = "minimaxChatClient")
    public OpenAiChatClient minimaxChatClient() {
        OpenAiApi api = minimaxApi();
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withModel(minimaxModel)
                .withTemperature(0.7f)
                .build();
        
        return new OpenAiChatClient(api, options);
    }

    // 默认ChatClient (DeepSeek)
    @Bean
    public OpenAiChatClient chatClient() {
        return deepSeekChatClient();
    }
}
