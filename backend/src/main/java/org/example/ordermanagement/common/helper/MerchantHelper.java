package org.example.ordermanagement.common.helper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.exception.BusinessException;
import org.example.ordermanagement.entity.Merchant;
import org.example.ordermanagement.entity.User;
import org.example.ordermanagement.mapper.MerchantMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 商家查询公共组件 —— 消除各 Service 中重复的 "按用户查商家列表" 逻辑。
 */
@Component
@RequiredArgsConstructor
public class MerchantHelper {

    private final MerchantMapper merchantMapper;
    private final UserHelper userHelper;

    /**
     * 获取用户名对应的所有已上线商家，无则抛异常。
     */
    public List<Merchant> getActiveMerchantsByUsername(String username) {
        User user = userHelper.getByUsername(username);

        List<Merchant> merchants = merchantMapper.selectList(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getUserId, user.getId())
                        .eq(Merchant::getStatus, 1)
        );

        if (merchants == null || merchants.isEmpty()) {
            throw new BusinessException("商家不存在或未正常营业");
        }

        return merchants;
    }
}
