package com.klzw.service.ai.config;

import com.klzw.service.ai.tool.MineGuardToolProvider;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolConfig {

    @Autowired
    private MineGuardToolProvider toolProvider;

    @Bean
    public ToolCallbackProvider mineGuardTools() {
        return ToolCallbackProvider.builder()
                .toolObjects(toolProvider)
                .build();
    }

    @Bean
    public ChatClient chatClientWithTools(@Qualifier("deepSeekChatModel") ChatModel chatModel, 
                                          ToolCallbackProvider mineGuardTools) {
        return ChatClient.builder(chatModel)
                .defaultTools(mineGuardTools)
                .build();
    }
}
