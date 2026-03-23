package com.agent.agent.userstory.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;


/**
 * Provides named ChatClient beans (draftClient and criticClient). If a ChatClient
 * is available in the context (auto-configured by spring-ai starters), it will be
 * reused. This keeps the app flexible for OpenAI and Google GenAI starters.
 */
@Configuration
public class AiClientsConfig {

    @Bean(name = "draftClient")
    @ConditionalOnBean(ChatClient.Builder.class)
    public ChatClient draftClient(ObjectProvider<ChatClient.Builder> chatClientBuilders) {
        ChatClient.Builder builder = chatClientBuilders.getIfAvailable();
        return builder.build();
    }

    @Bean(name = "criticClient")
    @ConditionalOnBean(ChatClient.Builder.class)
    public ChatClient criticClient(ObjectProvider<ChatClient.Builder> chatClientBuilders) {
        ChatClient.Builder builder = chatClientBuilders.getIfAvailable();
        return builder.build();
    }
}
