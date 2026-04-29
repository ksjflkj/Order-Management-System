package org.example.ordermanagement.service;

import org.example.ordermanagement.dto.LoginDTO;
import org.example.ordermanagement.dto.PasswordChangeDTO;
import org.example.ordermanagement.dto.PhoneUpdateDTO;
import org.example.ordermanagement.dto.RegisterDTO;
import org.example.ordermanagement.dto.ResetPasswordDTO;
import org.example.ordermanagement.dto.UpdateUserDTO;
import org.example.ordermanagement.vo.UserInfoVO;

import java.util.Map;

public interface UserService {

    void register(RegisterDTO registerDTO);

    Map<String, Object> login(LoginDTO loginDTO);

    UserInfoVO getCurrentUserInfo(String username);

    /**
     * 更新用户信息
     * @param username 用户名
     * @param dto 更新内容
     */
    void updateUserInfo(String username, UpdateUserDTO dto);

    /**
     * 修改密码
     * @param username 用户名
     * @param dto 新密码
     */
    void changePassword(String username, PasswordChangeDTO dto);

    /**
     * 发送手机验证码
     * @param phone 手机号
     */
    void sendPhoneCode(String phone);

    /**
     * 更新手机号
     * @param username 用户名
     * @param dto 手机号和验证码
     */
    void updatePhone(String username, PhoneUpdateDTO dto);

    /**
     * 注册前检查手机号是否可用（未被注册）
     * 若已被注册则抛出 BusinessException
     * @param phone 手机号
     */
    void checkPhoneForRegister(String phone);

    /**
     * 根据用户名获取用户ID
     * @param username 用户名
     * @return 用户ID
     */
    Long getIdByUsername(String username);

    /**
     * 发送找回密码验证码（仅向已注册手机号发送）
     * @param phone 手机号
     */
    void sendResetPasswordCode(String phone);

    /**
     * 通过手机验证码重置密码（无需登录）
     * @param dto 手机号、验证码、新密码
     */
    void resetPassword(ResetPasswordDTO dto);
}