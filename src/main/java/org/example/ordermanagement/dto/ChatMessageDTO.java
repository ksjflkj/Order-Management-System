package org.example.ordermanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户发送聊天消息的请求体
 * 注意：用户角色不通过请求体传递，由服务端从 Spring Security 上下文中解析，防止权限伪造攻击。
 */
@Data
public class ChatMessageDTO {

    /**
     * 用户输入的消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "消息内容不能超过2000字")
    private String content;

    /**
     * 可选：关联的订单ID（在订单详情页打开客服时自动传入，提供上下文）
     */
    private Long orderId;

    /**
     * 可选：关联的商家ID（在商家详情页打开客服时自动传入）
     */
    private Long merchantId;
}
