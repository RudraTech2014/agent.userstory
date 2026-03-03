package com.agent.agent.userstory.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.ObjectProvider;


/**
 * Provides named ChatClient beans (draftClient and criticClient). If a ChatClient
 * is available in the context (auto-configured by spring-ai starters), it will be
 * reused. This keeps the app flexible for OpenAI and Google GenAI starters.
 */
@Configuration
public class AiClientsConfig {

    @Bean
    public ChatClient draftClient(ObjectProvider<ChatClient> chatClients) {
        return chatClients.getIfAvailable();
    }

    @Bean
    public ChatClient criticClient(ObjectProvider<ChatClient> chatClients) {
        return chatClients.getIfAvailable();
    }
}
