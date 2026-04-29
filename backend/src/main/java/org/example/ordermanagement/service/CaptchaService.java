package org.example.ordermanagement.service;

import java.util.concurrent.TimeUnit;

public interface CaptchaService {

    /**
     * 发送验证码
     * @param phone 手机号
     */
    void sendCode(String phone);

    /**
     * 验证验证码
     * @param phone 手机号
     * @param code 验证码
     * @return 验证结果
     */
    boolean verifyCode(String phone, String code);
}
