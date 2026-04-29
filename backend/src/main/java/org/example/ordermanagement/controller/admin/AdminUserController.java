package org.example.ordermanagement.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.exception.BusinessException;
import org.example.ordermanagement.common.result.Result;
import org.example.ordermanagement.dto.UserQueryDTO;
import org.example.ordermanagement.entity.User;
import org.example.ordermanagement.mapper.UserMapper;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 平台级用户管理中心 - 用于治理 C 端用户账号，惩戒恶意刷单、羊毛党账号等。
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserMapper userMapper;

    /**
     * 分页查询用户列表
     */
    @GetMapping("/page")
    public Result<IPage<User>> page(UserQueryDTO queryDTO) {
        Page<User> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getUsername())) {
            wrapper.like(User::getUsername, queryDTO.getUsername());
        }
        if (StringUtils.hasText(queryDTO.getPhone())) {
            wrapper.like(User::getPhone, queryDTO.getPhone());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(User::getStatus, queryDTO.getStatus());
        }
        // 隐藏管理员账号，防止被误操作
        wrapper.ne(User::getRole, "ADMIN");
        wrapper.orderByDesc(User::getCreateTime);

        IPage<User> result = userMapper.selectPage(page, wrapper);

        // 脱敏：清除密码字段
        result.getRecords().forEach(u -> u.setPassword(null));

        return Result.success(result);
    }

    /**
     * 禁用 / 启用用户
     * @param id 用户ID
     * @param status 1=正常 0=禁用（与系统约定一致）
     */
    @PostMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        if (status != 0 && status != 1) {
            throw new BusinessException("状态值非法");
        }

        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if ("ADMIN".equals(user.getRole())) {
            throw new BusinessException("不能操作管理员账号");
        }

        user.setStatus(status);
        userMapper.updateById(user);

        return Result.successMessage(status == 0 ? "用户已禁用" : "用户已恢复正常");
    }
}
