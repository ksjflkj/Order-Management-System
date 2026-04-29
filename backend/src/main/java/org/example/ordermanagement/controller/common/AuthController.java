package org.example.ordermanagement.controller.common;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.result.Result;
import org.example.ordermanagement.dto.LoginDTO;
import org.example.ordermanagement.dto.RegisterDTO;
import org.example.ordermanagement.dto.ResetPasswordDTO;
import org.example.ordermanagement.dto.SendCodeDTO;
import org.example.ordermanagement.security.JwtUtils;
import org.example.ordermanagement.service.CaptchaService;
import org.example.ordermanagement.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统级认证授权中心 - 掌管所有角色的登录、注册、以及基于 JWT 的权限凭证下发。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final CaptchaService captchaService;
    private final JwtUtils jwtUtils;

    /**
     * 健康检查探测端口
     */
    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.successMessage("backend running");
    }

    /**
     * 注册时发送手机验证码（无需登录，permitAll）
     * 发码前先检查手机号是否已被注册，避免浪费短信
     */
    @PostMapping("/send-code")
    public Result<String> sendRegisterCode(@Valid @RequestBody SendCodeDTO dto) {
        userService.checkPhoneForRegister(dto.getPhone());
        captchaService.sendCode(dto.getPhone());
        return Result.successMessage("验证码已发送，请注意查收");
    }

    /**
     * 签发游客 Token（无需登录，permitAll - 供未登录者浏览大厅使用）
     */
    @PostMapping("/guest")
    public Result<String> guestToken() {
        String token = jwtUtils.generateGuestToken();
        return Result.success(token);
    }

    /**
     * 新用户注册接口
     */
    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.successMessage("注册成功");
    }

    /**
     * 用户账号密码登录入口
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success(userService.login(loginDTO));
    }

    /**
     * 找回密码：发送验证码（只向已注册手机号发送）
     */
    @PostMapping("/reset-send-code")
    public Result<String> sendResetCode(@Valid @RequestBody SendCodeDTO dto) {
        userService.sendResetPasswordCode(dto.getPhone());
        return Result.successMessage("验证码已发送，请注意查收");
    }

    /**
     * 找回密码：通过手机验证码设置新密码
     */
    @PostMapping("/reset-password")
    public Result<String> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        userService.resetPassword(dto);
        return Result.successMessage("密码重置成功，请使用新密码登录");
    }
}