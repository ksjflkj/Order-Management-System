package org.example.ordermanagement.service;

import org.example.ordermanagement.vo.ChatMessageVO;

import java.util.List;

/**
 * 聊天会话管理 Service
 * 负责将多轮对话上下文存储到 Redis 中
 */
public interface ChatSessionService {

    /**
     * 追加一条消息到会话历史
     *
     * @param username 用户名（作为 Redis key 的一部分）
     * @param role     "user" 或 "assistant"
     * @param content  消息内容
     */
    void appendMessage(String username, String role, String content);

    /**
     * 获取当前会话的历史消息列表（用于传递给 LLM 的 messages 数组）
     *
     * @param username 用户名
     * @return 消息列表，格式为 [{role, content}, ...]
     */
    List<ChatMessageVO> getHistory(String username);

    /**
     * 清空指定用户的会话历史
     *
     * @param username 用户名
     */
    void clearSession(String username);

    /**
     * 检查并增加用户今日消息计数；超限时返回 false
     *
     * @param username   用户名
     * @param dailyLimit 每日上限
     * @return true = 未超限可继续发送
     */
    boolean checkAndIncrementDailyCount(String username, int dailyLimit);
}
