package org.example.ordermanagement.service;

import org.example.ordermanagement.vo.ChatMessageVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * LLM（大语言模型）网关 Service
 * 负责调用 DeepSeek API，支持普通和流式两种模式
 */
public interface LLMGatewayService {

    /**
     * 普通（非流式）对话请求
     *
     * @param systemPrompt 系统提示词
     * @param messages     历史上下文 + 当前用户消息
     * @return AI 回复的完整文本
     */
    String chat(String systemPrompt, List<ChatMessageVO> messages);

    /**
     * 流式（SSE）对话请求
     * 每次返回一个 token 增量字符串
     *
     * @param systemPrompt 系统提示词
     * @param messages     历史上下文 + 当前用户消息
     * @return Flux<String> 字符流（每个元素是 delta content）
     */
    Flux<String> chatStream(String systemPrompt, List<ChatMessageVO> messages);
}
