package org.example.ordermanagement.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI 客服业务配置属性。
 * <p>
 * 注意：底层大模型连接参数（api-key / base-url / model 等）
 * 已迁移至 Spring AI 标准配置路径 {@code spring.ai.openai.*}，
 * 本类仅保留与业务逻辑相关的自定义参数。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    private Chat chat = new Chat();

    @Data
    public static class Chat {
        /** 每个用户 Redis 会话中保留的最大历史轮数（1轮 = 用户消息 + AI 回复） */
        private Integer maxHistoryRounds = 10;
        /** Redis 会话过期时间（秒），默认 2 小时 */
        private Long sessionTtlSeconds = 7200L;
        /** 每用户每日最多发送消息数（防刷限额） */
        private Integer dailyLimit = 100;
    }
}
