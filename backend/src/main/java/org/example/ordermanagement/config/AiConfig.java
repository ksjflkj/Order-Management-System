package org.example.ordermanagement.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI ChatClient 配置类。
 * <p>
 * Spring AI 自动配置会注册 {@link ChatClient.Builder}，
 * 此处通过 Builder 构建单例 {@link ChatClient} Bean，
 * 供 {@code LLMGatewayServiceImpl} 注入使用。
 * 连接参数（api-key / base-url / model）由 {@code application.yaml}
 * 中的 {@code spring.ai.openai.*} 自动绑定。
 */
@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
