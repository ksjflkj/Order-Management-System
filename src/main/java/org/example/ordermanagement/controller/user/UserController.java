package org.example.ordermanagement.controller.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.result.Result;
import org.example.ordermanagement.dto.PasswordChangeDTO;
import org.example.ordermanagement.dto.PhoneUpdateDTO;
import org.example.ordermanagement.dto.SendCodeDTO;
import org.example.ordermanagement.dto.UpdateUserDTO;
import org.example.ordermanagement.vo.UserInfoVO;
import org.example.ordermanagement.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个人中心控制器 - 负责管理当前登录用户的私有账号资料、安全设置、敏感操作鉴权。
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 个人面板（Profile）：获取用户信息、头像展示、昵称、手机号及身份标记
     */
    @GetMapping("/me")
    public Result<UserInfoVO> me(Authentication authentication) {
        String username = authentication.getName();
        return Result.success(userService.getCurrentUserInfo(username));
    }

    /**
     * 编辑更新用户非敏感的基本资料（如网名、性别、头像URL）
     */
    @PutMapping("/update")
    public Result<String> update(Authentication authentication, @Valid @RequestBody UpdateUserDTO dto) {
        userService.updateUserInfo(authentication.getName(), dto);
        return Result.successMessage("更新成功");
    }

    /**
     * 修改密码（安全操作：需提供原密码才能变更）
     */
    @PostMapping("/change-password")
    public Result<String> changePassword(Authentication authentication, @Valid @RequestBody PasswordChangeDTO dto) {
        userService.changePassword(authentication.getName(), dto);
        return Result.successMessage("密码修改成功");
    }

    /**
     * （换绑手机号业务预备）给将要绑定的新手机号发送验证码
     */
    @PostMapping("/send-code")
    public Result<String> sendCode(@Valid @RequestBody SendCodeDTO dto) {
        userService.sendPhoneCode(dto.getPhone());
        return Result.successMessage("验证码已发送");
    }

    /**
     * 修改/绑定手机号（需校验验证码正确性，同时验证手机号未被占有）
     */
    @PostMapping("/update-phone")
    public Result<String> updatePhone(Authentication authentication, @Valid @RequestBody PhoneUpdateDTO dto) {
        userService.updatePhone(authentication.getName(), dto);
        return Result.successMessage("手机号更新成功");
    }
}
