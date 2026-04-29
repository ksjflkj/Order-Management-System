package org.example.ordermanagement.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.service.CaptchaService;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CODE_PREFIX = "captcha:";
    private static final long CODE_EXPIRE_MINUTES = 5;

    @Override
    public void sendCode(String phone) {
        // 生成6位验证码
        String code = String.format("%06d", new Random().nextInt(1000000));
        
        // 存储到Redis，5分钟过期
        redisTemplate.opsForValue().set(
            CODE_PREFIX + phone,
            code,
            CODE_EXPIRE_MINUTES,
            TimeUnit.MINUTES
        );
        
        // 这里仅打印，实际生产环境需要接入短信网关
        System.out.println("验证码已发送手机号 " + phone + "，验证码: " + code);
    }

    @Override
    public boolean verifyCode(String phone, String code) {
        try {
            String savedCode = (String) redisTemplate.opsForValue().get(CODE_PREFIX + phone);
            if (savedCode == null) {
                return false;
            }
            
            // 验证成功后删除验证码
            redisTemplate.delete(CODE_PREFIX + phone);
            
            return savedCode.equals(code);
        } catch (RedisConnectionFailureException e) {
            // Redis连接失败时，返回false
            return false;
        }
    }
}
