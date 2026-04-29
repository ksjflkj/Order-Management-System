package org.example.ordermanagement.service;

import org.example.ordermanagement.dto.ChatMessageDTO;
import org.example.ordermanagement.vo.ChatMessageVO;
import org.example.ordermanagement.vo.ChatResponseVO;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 智能客服核心业务 Service
 * 安全说明：角色鉴权通过 Authentication 对象从服务端 JWT 中解析，
 * 拒绝任何来自请求体的 userRole 字段，防止权限伪造攻击。
 */
public interface ChatService {

    /**
     * 发送消息（普通模式，同步返回完整回复）
     *
     * @param username       当前用户名
     * @param authentication Spring Security 认证对象，用于服务端角色解析
     * @param dto            消息请求体
     * @return AI 回复
     */
    ChatResponseVO sendMessage(String username, Authentication authentication, ChatMessageDTO dto);

    /**
     * 发送消息（流式模式，SSE 逐 token 返回）
     *
     * @param username       当前用户名
     * @param authentication Spring Security 认证对象，用于服务端角色解析
     * @param dto            消息请求体
     * @return 字符流
     */
    Flux<String> sendMessageStream(String username, Authentication authentication, ChatMessageDTO dto);

    /**
     * 获取当前用户的会话历史
     *
     * @param username 当前用户名
     * @return 消息列表
     */
    List<ChatMessageVO> getHistory(String username);

    /**
     * 清空当前用户的会话
     *
     * @param username 当前用户名
     */
    void clearSession(String username);
}
