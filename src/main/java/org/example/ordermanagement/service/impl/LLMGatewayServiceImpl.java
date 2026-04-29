package org.example.ordermanagement.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ordermanagement.service.LLMGatewayService;
import org.example.ordermanagement.vo.ChatMessageVO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * DeepSeek LLM 网关实现（Spring AI 重构版）。
 * <p>
 * 使用 {@link ChatClient} 替代手动 WebClient + SSE 解析，
 * 底层 HTTP 通讯、JSON 构造、SSE 分块解析全部由 Spring AI 框架接管。
 * 支持同步阻塞调用与 Reactor Flux 流式调用两种模式。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LLMGatewayServiceImpl implements LLMGatewayService {

    /** Spring AI 自动配置注入，读取 spring.ai.openai.* */
    private final ChatClient chatClient;

    // ─────────────────────────────────────────────────
    //  同步调用（用于非流式场景）
    // ─────────────────────────────────────────────────

    @Override
    public String chat(String systemPrompt, List<ChatMessageVO> messages) {
        try {
            log.info("调用大模型 API (non-stream)");
            Prompt prompt = buildPrompt(systemPrompt, messages);
            return chatClient.prompt(prompt).call().content();
        } catch (Exception e) {
            log.error("大模型同步调用失败", e);
            return "抱歉，AI 服务暂时不可用，请稍后重试。如有紧急问题请联系人工客服。";
        }
    }

    // ─────────────────────────────────────────────────
    //  流式调用（SSE，用于打字机效果）
    // ─────────────────────────────────────────────────

    @Override
    public Flux<String> chatStream(String systemPrompt, List<ChatMessageVO> messages) {
        log.info("调用大模型 API (stream)");
        Prompt prompt = buildPrompt(systemPrompt, messages);
        // Spring AI 原生返回 Flux<String>，内部自动完成 SSE 帧解析
        return chatClient.prompt(prompt).stream().content()
                .doOnError(e -> log.error("大模型流式调用异常", e))
                .onErrorReturn("抱歉，AI 服务暂时不可用，请稍后重试。");
    }

    // ─────────────────────────────────────────────────
    //  私有工具方法
    // ─────────────────────────────────────────────────

    /**
     * 将 System Prompt 与历史对话消息组装为 Spring AI {@link Prompt} 对象。
     *
     * @param systemPrompt 系统提示词（含业务上下文）
     * @param messages     历史对话消息列表（role: user / assistant）
     * @return Spring AI Prompt
     */
    private Prompt buildPrompt(String systemPrompt, List<ChatMessageVO> messages) {
        List<Message> aiMessages = new ArrayList<>();
        aiMessages.add(new SystemMessage(systemPrompt));
        for (ChatMessageVO msg : messages) {
            if ("user".equals(msg.getRole())) {
                aiMessages.add(new UserMessage(msg.getContent()));
            } else if ("assistant".equals(msg.getRole())) {
                aiMessages.add(new AssistantMessage(msg.getContent()));
            }
        }
        return new Prompt(aiMessages);
    }
}
