package org.example.ordermanagement.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 聊天接口统一响应对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseVO {

    /**
     * AI 回复的消息内容
     */
    private String content;

    /**
     * 当前会话的消息总数
     */
    private Integer messageCount;

    /**
     * 历史消息列表（仅在 /history 接口返回）
     */
    private List<ChatMessageVO> history;
}
