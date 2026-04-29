package org.example.ordermanagement.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.entity.User;
import org.example.ordermanagement.mapper.UserMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 自定义用户详细信息服务类
 * <p>
 * 实现 Spring Security 的 {@link UserDetailsService} 接口，
 * 用于在认证过程中根据用户名从数据库中加载用户详细信息（如密码、权限、账号状态等）。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    /**
     * 根据用户名加载用户详细信息
     *
     * @param username 用户名
     * @return 返回封装了用户信息的 {@link UserDetails} 对象，供 Spring Security 进行身份验证和权限校验
     * @throws UsernameNotFoundException 如果数据库中找不到指定的用户名，抛出此异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        // 与系统既有约定保持一致：status=1 正常，status=0/null 禁用/未激活
        boolean enabled = Integer.valueOf(1).equals(user.getStatus());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                enabled,          // isEnabled
                true,             // isAccountNonExpired
                true,             // isCredentialsNonExpired
                true,             // isAccountNonLocked
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
}