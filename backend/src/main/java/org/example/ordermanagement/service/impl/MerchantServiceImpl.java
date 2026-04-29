package org.example.ordermanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.exception.BusinessException;
import org.example.ordermanagement.common.helper.UserHelper;
import org.example.ordermanagement.dto.MerchantApplyDTO;
import org.example.ordermanagement.dto.MerchantAuditDTO;
import org.example.ordermanagement.dto.MerchantQueryDTO;
import org.example.ordermanagement.dto.MerchantStatusDTO;
import org.example.ordermanagement.entity.Merchant;
import org.example.ordermanagement.entity.User;
import org.example.ordermanagement.mapper.MerchantMapper;
import org.example.ordermanagement.service.MerchantService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {

    private final MerchantMapper merchantMapper;
    private final UserHelper userHelper;

    @Override
    public IPage<Merchant> pageMerchants(MerchantQueryDTO queryDTO) {
        Page<Merchant> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());

        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getShopName())) {
            wrapper.like(Merchant::getShopName, queryDTO.getShopName());
        }

        if (queryDTO.getStatus() != null) {
            wrapper.eq(Merchant::getStatus, queryDTO.getStatus());
        }

        wrapper.orderByDesc(Merchant::getCreateTime);

        return merchantMapper.selectPage(page, wrapper);
    }

    @Override
    public void auditMerchant(MerchantAuditDTO dto) {
        if (dto.getStatus() != 1 && dto.getStatus() != 3) {
            throw new BusinessException("审核状态非法");
        }

        Merchant merchant = merchantMapper.selectById(dto.getId());
        if (merchant == null) {
            throw new BusinessException("商家不存在");
        }

        merchant.setStatus(dto.getStatus());
        merchantMapper.updateById(merchant);
    }

    @Override
    public void updateMerchantStatus(MerchantStatusDTO dto) {
        if (dto.getStatus() != 1 && dto.getStatus() != 2) {
            throw new BusinessException("状态非法");
        }

        Merchant merchant = merchantMapper.selectById(dto.getId());
        if (merchant == null) {
            throw new BusinessException("商家不存在");
        }

        merchant.setStatus(dto.getStatus());
        merchantMapper.updateById(merchant);
    }

    @Override
    public void applyMerchant(String username, MerchantApplyDTO dto) {
        User user = userHelper.getByUsername(username);

        Merchant merchant = new Merchant();
        merchant.setUserId(user.getId());
        merchant.setShopName(dto.getShopName());
        merchant.setShopLogo(dto.getShopLogo());
        merchant.setAddress(dto.getAddress());
        merchant.setPhone(dto.getPhone());
        merchant.setDescription(dto.getDescription());
        merchant.setBusinessLicense(dto.getBusinessLicense());
        merchant.setStatus(0);
        merchant.setRating(java.math.BigDecimal.ZERO);

        merchantMapper.insert(merchant);
    }

    @Override
    public List<Merchant> getMyMerchants(String username) {
        User user = userHelper.getByUsername(username);

        return merchantMapper.selectList(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getUserId, user.getId())
                        .eq(Merchant::getStatus, 1)
        );
    }

    @Override
    public List<Merchant> getAllMyMerchants(String username) {
        User user = userHelper.getByUsername(username);

        return merchantMapper.selectList(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getUserId, user.getId())
                        .orderByDesc(Merchant::getCreateTime)
        );
    }

    @Override
    public void updateMyMerchant(String username, Merchant dto) {
        User user = userHelper.getByUsername(username);

        Merchant merchant = merchantMapper.selectById(dto.getId());
        if (merchant == null) {
            throw new BusinessException("店铺不存在");
        }

        // 验证店铺属于该用户
        if (!merchant.getUserId().equals(user.getId())) {
            throw new BusinessException("无权操作该店铺");
        }

        // 只更新基本信息字段，营业状态由 toggleShopOpen 专门接口处理
        if (dto.getShopName() != null)       merchant.setShopName(dto.getShopName());
        if (dto.getShopLogo() != null)       merchant.setShopLogo(dto.getShopLogo());
        if (dto.getPhone() != null)          merchant.setPhone(dto.getPhone());
        if (dto.getAddress() != null)        merchant.setAddress(dto.getAddress());
        if (dto.getDescription() != null)    merchant.setDescription(dto.getDescription());

        merchantMapper.updateById(merchant);
    }

    @Override
    public void toggleShopOpen(String username, Long merchantId, boolean open) {
        User user = userHelper.getByUsername(username);

        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException("店铺不存在");
        }
        if (!merchant.getUserId().equals(user.getId())) {
            throw new BusinessException("无权操作该店铺");
        }
        if (merchant.getStatus() != 1) {
            throw new BusinessException("只有审核通过的店铺才能切换营业状态");
        }

        merchant.setIsOpen(open ? 1 : 0);
        merchantMapper.updateById(merchant);
    }
}