package org.example.ordermanagement.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ordermanagement.config.AiProperties;
import org.example.ordermanagement.service.ChatSessionService;
import org.example.ordermanagement.vo.ChatMessageVO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 聊天会话管理实现
 * 使用 Redis List 存储多轮对话历史，key 格式：chat:session:{username}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl implements ChatSessionService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;

    private static final String SESSION_KEY_PREFIX = "chat:session:";
    private static final String DAILY_COUNT_KEY_PREFIX = "chat:daily:";

    private String sessionKey(String username) {
        return SESSION_KEY_PREFIX + username;
    }

    private String dailyCountKey(String username) {
        // 每天的 key，格式：chat:daily:{username}:{yyyyMMdd}
        String today = java.time.LocalDate.now().toString().replace("-", "");
        return DAILY_COUNT_KEY_PREFIX + username + ":" + today;
    }

    @Override
    public void appendMessage(String username, String role, String content) {
        String key = sessionKey(username);
        ChatMessageVO msg = ChatMessageVO.builder()
                .role(role)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();
        try {
            String json = objectMapper.writeValueAsString(msg);
            redisTemplate.opsForList().rightPush(key, json);
            // 限制历史条数（每轮 = user + assistant，最多 maxHistoryRounds * 2 条）
            int maxSize = aiProperties.getChat().getMaxHistoryRounds() * 2;
            Long size = redisTemplate.opsForList().size(key);
            if (size != null && size > maxSize) {
                // 删除最早的一条
                redisTemplate.opsForList().leftPop(key);
            }
            // 刷新 TTL
            redisTemplate.expire(key, aiProperties.getChat().getSessionTtlSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("保存聊天消息到 Redis 失败, username={}", username, e);
        }
    }

    @Override
    public List<ChatMessageVO> getHistory(String username) {
        String key = sessionKey(username);
        try {
            Long size = redisTemplate.opsForList().size(key);
            if (size == null || size == 0) {
                return new ArrayList<>();
            }
            List<Object> rawList = redisTemplate.opsForList().range(key, 0, -1);
            List<ChatMessageVO> result = new ArrayList<>();
            if (rawList != null) {
                for (Object raw : rawList) {
                    ChatMessageVO msg = objectMapper.readValue(raw.toString(), ChatMessageVO.class);
                    result.add(msg);
                }
            }
            return result;
        } catch (Exception e) {
            log.error("读取聊天历史失败, username={}", username, e);
            return new ArrayList<>();
        }
    }

    @Override
    public void clearSession(String username) {
        redisTemplate.delete(sessionKey(username));
        log.info("已清空用户会话, username={}", username);
    }

    @Override
    public boolean checkAndIncrementDailyCount(String username, int dailyLimit) {
        String key = dailyCountKey(username);
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count == 1) {
                // 第一次发消息，设置到期时间为今天结束（24h 足够）
                redisTemplate.expire(key, 24, TimeUnit.HOURS);
            }
            return count != null && count <= dailyLimit;
        } catch (Exception e) {
            log.error("检查每日消息限制失败, username={}", username, e);
            // Redis 异常时不限流，避免影响正常业务
            return true;
        }
    }
}
