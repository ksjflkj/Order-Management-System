package org.example.ordermanagement.common.helper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.exception.BusinessException;
import org.example.ordermanagement.entity.User;
import org.example.ordermanagement.mapper.UserMapper;
import org.springframework.stereotype.Component;

/**
 * 用户查询公共组件 —— 消除各 Service 中重复的 "按用户名查用户" 逻辑。
 */
@Component
@RequiredArgsConstructor
public class UserHelper {

    private final UserMapper userMapper;

    /**
     * 根据用户名查询用户，不存在则抛出 BusinessException。
     */
    public User getByUsername(String username) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    /**
     * 根据用户名查询用户 ID，不存在则抛出 BusinessException。
     */
    public Long getIdByUsername(String username) {
        return getByUsername(username).getId();
    }
}
