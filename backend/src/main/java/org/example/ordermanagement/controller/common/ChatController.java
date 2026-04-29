package org.example.ordermanagement.controller.common;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.result.Result;
import org.example.ordermanagement.dto.ChatMessageDTO;
import org.example.ordermanagement.service.ChatService;
import org.example.ordermanagement.vo.ChatMessageVO;
import org.example.ordermanagement.vo.ChatResponseVO;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 全局智能 AI 客服通道 - 借助大语言模型提供 7x24 智能问答。
 * 核心机制：依据拦截器传入的不同角色（用户/商家），动态织入不同的 Prompt 及业务上下文。
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 判断当前认证用户是否为游客或未登录
     */
    private boolean isGuestOrAnonymous(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return true;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_GUEST".equals(a.getAuthority()));
    }

    /**
     * 发送消息（普通模式，同步返回完整 AI 回复）
     * POST /api/chat/send
     */
    @PostMapping("/send")
    public Result<ChatResponseVO> sendMessage(
            Authentication authentication,
            @Valid @RequestBody ChatMessageDTO dto) {
        if (isGuestOrAnonymous(authentication)) {
            return Result.fail("请先登录后再使用智能客服～");
        }
        // 将 authentication 透传给 Service，角色在服务端从 JWT 中解析
        ChatResponseVO response = chatService.sendMessage(authentication.getName(), authentication, dto);
        return Result.success(response);
    }

    /**
     * 发送消息（SSE 流式模式，逐 token 推送 AI 回复）
     * POST /api/chat/stream
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> sendMessageStream(
            Authentication authentication,
            @Valid @RequestBody ChatMessageDTO dto) {
        if (isGuestOrAnonymous(authentication)) {
            return Flux.just(ServerSentEvent.<String>builder()
                    .data("请先登录后再使用智能客服～").build());
        }
        // 将 authentication 透传给 Service，角色在服务端从 JWT 中解析
        return chatService.sendMessageStream(authentication.getName(), authentication, dto)
                .map(chunk -> ServerSentEvent.<String>builder().data(chunk).build());
    }

    /**
     * 获取当前用户的会话历史
     * GET /api/chat/history
     */
    @GetMapping("/history")
    public Result<List<ChatMessageVO>> getHistory(Authentication authentication) {
        if (isGuestOrAnonymous(authentication)) {
            return Result.success(List.of());
        }
        List<ChatMessageVO> history = chatService.getHistory(authentication.getName());
        return Result.success(history);
    }

    /**
     * 清空当前用户的会话
     * DELETE /api/chat/session
     */
    @DeleteMapping("/session")
    public Result<String> clearSession(Authentication authentication) {
        if (isGuestOrAnonymous(authentication)) {
            return Result.successMessage("会话已清空");
        }
        chatService.clearSession(authentication.getName());
        return Result.successMessage("会话已清空");
    }
}
