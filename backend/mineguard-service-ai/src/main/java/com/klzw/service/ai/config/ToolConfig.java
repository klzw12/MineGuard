package com.klzw.service.ai.config;

import com.klzw.service.ai.tool.MineGuardToolProvider;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolConfig {

    @Autowired
    private MineGuardToolProvider toolProvider;

    @Bean
    public ChatClient chatClientWithTools(@Qualifier("deepSeekChatModel") ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultTools(toolProvider)
                .build();
    }
}
