package org.example.ordermanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.exception.BusinessException;
import org.example.ordermanagement.dto.LoginDTO;
import org.example.ordermanagement.dto.PasswordChangeDTO;
import org.example.ordermanagement.dto.PhoneUpdateDTO;
import org.example.ordermanagement.dto.RegisterDTO;
import org.example.ordermanagement.dto.ResetPasswordDTO;
import org.example.ordermanagement.dto.UpdateUserDTO;
import org.example.ordermanagement.vo.UserInfoVO;
import org.example.ordermanagement.entity.User;
import org.example.ordermanagement.mapper.UserMapper;
import org.example.ordermanagement.security.JwtUtils;
import org.example.ordermanagement.service.CaptchaService;
import org.example.ordermanagement.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final CaptchaService captchaService;

    @Override
    public void register(RegisterDTO registerDTO) {
        // 校验用户名唯一
        User existingUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, registerDTO.getUsername())
        );
        if (existingUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 校验手机号唯一
        User phoneUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, registerDTO.getPhone())
        );
        if (phoneUser != null) {
            throw new BusinessException("该手机号已被注册");
        }

        // 校验短信验证码
        if (!captchaService.verifyCode(registerDTO.getPhone(), registerDTO.getCode())) {
            throw new BusinessException("验证码错误或已过期");
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        user.setRole("USER");
        user.setStatus(1);

        userMapper.insert(user);
    }

    @Override
    public Map<String, Object> login(LoginDTO loginDTO) {
        String account = loginDTO.getAccount();

        // 先尝试用户名匹配，再尝试手机号匹配
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, account)
        );
        if (user == null) {
            user = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getPhone, account)
            );
        }

        if (user == null) {
            throw new BusinessException("用户不存在，请检查用户名或手机号");
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }

        String token = jwtUtils.generateToken(user.getUsername(), user.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("username", user.getUsername());
        result.put("role", user.getRole());

        return result;
    }
    @Override
    public UserInfoVO getCurrentUserInfo(String username) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        return new UserInfoVO(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getPhone(),
                user.getEmail(),
                user.getAvatar(),
                user.getRole(),
                user.getStatus()
        );
    }

    @Override
    public void updateUserInfo(String username, UpdateUserDTO dto) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }

        userMapper.updateById(user);
    }

    @Override
    public void changePassword(String username, PasswordChangeDTO dto) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    public void sendPhoneCode(String phone) {
        // 检查手机号是否已被其他用户绑定
        User existingUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone)
        );
        
        if (existingUser != null) {
            throw new BusinessException("该手机号已被绑定");
        }
        
        captchaService.sendCode(phone);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePhone(String username, PhoneUpdateDTO dto) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 校验原手机号是否匹配
        if (user.getPhone() == null || !user.getPhone().equals(dto.getOldPhone())) {
            throw new BusinessException("原手机号不正确");
        }

        // 验证验证码
        if (!captchaService.verifyCode(dto.getPhone(), dto.getCode())) {
            throw new BusinessException("验证码错误或已过期");
        }

        // 检查新手机号是否已被其他用户绑定
        User phoneUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getPhone())
        );
        
        if (phoneUser != null && !phoneUser.getId().equals(user.getId())) {
            throw new BusinessException("该手机号已被其他用户绑定");
        }

        user.setPhone(dto.getPhone());
        userMapper.updateById(user);
    }

    @Override
    public Long getIdByUsername(String username) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
        return user != null ? user.getId() : null;
    }

    @Override
    public void checkPhoneForRegister(String phone) {
        User existingUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone)
        );
        if (existingUser != null) {
            throw new BusinessException("该手机号已被注册，请直接登录或使用其他手机号");
        }
    }

    @Override
    public void sendResetPasswordCode(String phone) {
        // 必须是已注册用户的手机号才可以发送
        User existingUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone)
        );
        if (existingUser == null) {
            throw new BusinessException("该手机号尚未注册，请检查手机号是否正确");
        }
        // 利用现有短信验证码基础设施发送
        captchaService.sendCode(phone);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordDTO dto) {
        // 校验验证码
        if (!captchaService.verifyCode(dto.getPhone(), dto.getCode())) {
            throw new BusinessException("验证码错误或已过期，请重新获取");
        }

        // 查找用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getPhone())
        );
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }
}