package org.example.ordermanagement.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ordermanagement.common.exception.BusinessException;
import org.example.ordermanagement.config.AiProperties;
import org.example.ordermanagement.dto.ChatMessageDTO;
import org.example.ordermanagement.service.*;
import org.example.ordermanagement.vo.ChatMessageVO;
import org.example.ordermanagement.vo.ChatResponseVO;
import org.example.ordermanagement.vo.OrderDetailVO;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 智能客服核心业务实现（Spring AI 重构版）。
 * <p>
 * 使用 {@link SystemPromptTemplate} 替代裸 {@code String.format}，
 * 实现类型安全、防注入的动态系统提示词组装。
 * 支持用户端、商家端、管理员端三套差异化提示词策略。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatSessionService chatSessionService;
    private final LLMGatewayService llmGatewayService;
    private final BusinessContextService businessContextService;
    private final OrderService orderService;
    private final AiProperties aiProperties;

    // ==================== 用户端 System Prompt 模板 ====================
    private static final String USER_SYSTEM_PROMPT = """
            你是"小饿"，一位专业、友善的外卖平台智能客服助手。
            当前时间：{currentTime}
            用户账号：{username}
            
            你的职责：
            1. 帮助用户查询订单状态、处理售后问题
            2. 解答平台使用规则和优惠券相关问题
            3. 在订单异常时引导用户进行投诉或联系人工客服
            4. 提供菜品和商家推荐建议
            
            行为准则：
            - 回答简洁友好，使用口语化中文，适当使用表情符号
            - 当用户问及订单相关问题时，优先使用下方提供的真实订单数据来回答
            - 当用户问及优惠券时，根据下方可用优惠券列表进行回答
            - 涉及退款等敏感操作时，需要先向用户确认
            - 无法处理的问题，委婉引导用户拨打客服热线
            - 严禁泄露其他用户的任何信息
            - 如果用户问你可以做什么，列出你的能力
            
            ===== 以下是该用户的真实业务数据 =====
            {userContext}
            {orderDetailContext}
            """;

    // ==================== 商家端 System Prompt 模板 ====================
    private static final String MERCHANT_SYSTEM_PROMPT = """
            你是"商家助手"，一位专业的外卖平台商家运营客服。
            当前时间：{currentTime}
            商家账号：{username}
            
            你的职责：
            1. 帮助商家查看订单情况、营业数据、热销菜品
            2. 解答平台运营规则、活动参与方式
            3. 提供经营建议（如菜品定价、促销策略）
            4. 协助处理用户投诉和差评
            
            行为准则：
            - 回答专业简洁，语气友好但偏商务
            - 基于下方提供的真实经营数据回答
            - 可以主动提供运营建议
            - 涉及平台政策变更时，引导商家咨询平台运营专线
            
            ===== 以下是该商家的真实经营数据 =====
            {merchantContext}
            """;

    // ==================== 管理员端 System Prompt 模板 ====================
    private static final String ADMIN_SYSTEM_PROMPT = """
            你是"平台管理助手"，一位专业的外卖平台运营分析师。
            当前时间：{currentTime}
            管理员账号：{username}
            
            你的职责：
            1. 提供平台整体运营数据分析（用户增长、订单趋势、营收统计）
            2. 协助管理商家审核、优惠券/活动策略制定
            3. 分析平台异常情况（投诉集中的商家、高退款率订单等）
            4. 提供运营建议和行业趋势洞察
            
            行为准则：
            - 回答数据导向、专业严谨
            - 主动用表格或列表形式呈现数据分析结果
            - 可以基于下方真实数据给出运营策略建议
            - 涉及系统操作（如封禁商家、退款审批）时，仅提供建议，提醒管理员到对应页面操作
            
            ===== 以下是平台真实运营数据 =====
            {adminContext}
            """;

    // ─────────────────────────────────────────────────
    //  角色解析
    // ─────────────────────────────────────────────────

    /**
     * 从 Spring Security Authentication 对象中解析用户真实角色。
     * 角色来源于 JWT 签发时写入的权限，客户端无法伪造。
     */
    private String resolveRole(Authentication authentication) {
        if (authentication == null) return "USER";
        return authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .filter(r -> "ADMIN".equals(r) || "MERCHANT".equals(r) || "USER".equals(r))
                .findFirst()
                .orElse("USER");
    }

    // ─────────────────────────────────────────────────
    //  提示词组装（SystemPromptTemplate）
    // ─────────────────────────────────────────────────

    /**
     * 根据服务端解析的角色，使用 {@link SystemPromptTemplate} 构建系统提示词。
     * 绝不接受前端传入的角色值，防止权限越级。
     */
    private String buildSystemPrompt(String username, ChatMessageDTO dto, String resolvedRole) {
        String currentTime = LocalDateTime.now().toString().substring(0, 16);

        // ─── 管理员角色 ───
        if ("ADMIN".equals(resolvedRole)) {
            String adminContext = "";
            try {
                adminContext = businessContextService.buildAdminContext();
            } catch (Exception e) {
                log.warn("构建管理员上下文失败", e);
            }
            return new SystemPromptTemplate(ADMIN_SYSTEM_PROMPT).render(Map.of(
                            "currentTime", currentTime,
                            "username", username,
                            "adminContext", adminContext
                    ));
        }

        // ─── 商家角色 ───
        if ("MERCHANT".equals(resolvedRole)) {
            String merchantContext = "";
            try {
                merchantContext = businessContextService.buildMerchantContext(username);
            } catch (Exception e) {
                log.warn("构建商家上下文失败, username={}", username, e);
            }
            return new SystemPromptTemplate(MERCHANT_SYSTEM_PROMPT).render(Map.of(
                            "currentTime", currentTime,
                            "username", username,
                            "merchantContext", merchantContext
                    ));
        }

        // ─── 普通用户角色（默认） ───
        String userContext = "";
        try {
            userContext = businessContextService.buildUserContext(username);
        } catch (Exception e) {
            log.warn("构建用户上下文失败, username={}", username, e);
        }

        String orderDetailContext = "";
        if (dto.getOrderId() != null) {
            try {
                OrderDetailVO order = orderService.getOrderDetail(username, dto.getOrderId());
                if (order != null) {
                    orderDetailContext = """

                            【⭐ 用户当前正在查看的订单详情】
                            订单号：%s | 状态：%s | 商家：%s
                            下单时间：%s | 金额：¥%s
                            配送地址：%s | 联系人：%s %s
                            备注：%s
                            用户正在此订单详情页，请优先针对此订单回答。
                            """.formatted(
                            order.getOrderNo(), order.getStatus(), order.getShopName(),
                            order.getCreateTime(), order.getTotalAmount(),
                            order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "无",
                            order.getContactName() != null ? order.getContactName() : "无",
                            order.getContactPhone() != null ? order.getContactPhone() : "",
                            order.getRemark() != null ? order.getRemark() : "无");
                }
            } catch (Exception e) {
                log.warn("注入订单详情上下文失败, orderId={}", dto.getOrderId(), e);
            }
        }

        return new SystemPromptTemplate(USER_SYSTEM_PROMPT).render(Map.of(
                        "currentTime", currentTime,
                        "username", username,
                        "userContext", userContext,
                        "orderDetailContext", orderDetailContext
                ));
    }

    // ─────────────────────────────────────────────────
    //  对外接口实现
    // ─────────────────────────────────────────────────

    @Override
    public ChatResponseVO sendMessage(String username, Authentication authentication, ChatMessageDTO dto) {
        int dailyLimit = aiProperties.getChat().getDailyLimit();
        if (!chatSessionService.checkAndIncrementDailyCount(username, dailyLimit)) {
            throw new BusinessException("您今日的咨询次数已达上限（" + dailyLimit + "次），请明天再来～");
        }

        List<ChatMessageVO> history = chatSessionService.getHistory(username);
        ChatMessageVO userMsg = ChatMessageVO.builder()
                .role("user").content(dto.getContent()).createdAt(LocalDateTime.now()).build();
        chatSessionService.appendMessage(username, "user", dto.getContent());

        List<ChatMessageVO> messages = new ArrayList<>(history);
        messages.add(userMsg);

        // 角色从服务端 Security 上下文解析，前端传值被彻底忽略
        String resolvedRole = resolveRole(authentication);
        String systemPrompt = buildSystemPrompt(username, dto, resolvedRole);
        String aiReply = llmGatewayService.chat(systemPrompt, messages);
        chatSessionService.appendMessage(username, "assistant", aiReply);

        return ChatResponseVO.builder().content(aiReply).messageCount(messages.size() + 1).build();
    }

    @Override
    public Flux<String> sendMessageStream(String username, Authentication authentication, ChatMessageDTO dto) {
        int dailyLimit = aiProperties.getChat().getDailyLimit();
        if (!chatSessionService.checkAndIncrementDailyCount(username, dailyLimit)) {
            return Flux.just("您今日的咨询次数已达上限（" + dailyLimit + "次），请明天再来～");
        }

        List<ChatMessageVO> history = chatSessionService.getHistory(username);
        chatSessionService.appendMessage(username, "user", dto.getContent());

        List<ChatMessageVO> messages = new ArrayList<>(history);
        messages.add(ChatMessageVO.builder()
                .role("user").content(dto.getContent()).createdAt(LocalDateTime.now()).build());

        // 角色从服务端 Security 上下文解析，前端传值被彻底忽略
        String resolvedRole = resolveRole(authentication);
        String systemPrompt = buildSystemPrompt(username, dto, resolvedRole);

        StringBuilder fullReply = new StringBuilder();
        return llmGatewayService.chatStream(systemPrompt, messages)
                .doOnNext(fullReply::append)
                .doOnComplete(() -> {
                    String reply = fullReply.toString();
                    if (!reply.isEmpty()) {
                        chatSessionService.appendMessage(username, "assistant", reply);
                    }
                });
    }

    @Override
    public List<ChatMessageVO> getHistory(String username) {
        return chatSessionService.getHistory(username);
    }

    @Override
    public void clearSession(String username) {
        chatSessionService.clearSession(username);
    }
}
