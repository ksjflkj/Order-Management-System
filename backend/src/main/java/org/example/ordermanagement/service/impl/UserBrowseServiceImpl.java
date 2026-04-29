package org.example.ordermanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.exception.BusinessException;
import org.example.ordermanagement.vo.UserDishListVO;
import org.example.ordermanagement.vo.UserMerchantDetailVO;
import org.example.ordermanagement.vo.UserMerchantVO;
import org.example.ordermanagement.dto.UserMerchantQueryDTO;
import org.example.ordermanagement.entity.Dish;
import org.example.ordermanagement.entity.Merchant;
import org.example.ordermanagement.mapper.DishMapper;
import org.example.ordermanagement.mapper.DishCategoryMapper;
import org.example.ordermanagement.entity.DishCategory;
import org.example.ordermanagement.mapper.MerchantMapper;
import org.example.ordermanagement.service.UserBrowseService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserBrowseServiceImpl implements UserBrowseService {

    private final MerchantMapper merchantMapper;
    private final DishMapper dishMapper;
    private final DishCategoryMapper dishCategoryMapper;

    @Override
    public IPage<UserMerchantVO> pageMerchants(UserMerchantQueryDTO queryDTO) {
        Page<Merchant> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());

        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Merchant::getStatus, 1);   // 只显示审核通过的
        // 注意：打烊的店铺仍然在列表显示（isOpen=0），但前端会展示"打烊中"标记，且不能下单

        if (StringUtils.hasText(queryDTO.getShopName())) {
            wrapper.like(Merchant::getShopName, queryDTO.getShopName());
        }

        // 营业中的优先展示
        wrapper.orderByDesc(Merchant::getIsOpen)
               .orderByDesc(Merchant::getRating)
               .orderByDesc(Merchant::getCreateTime);

        merchantMapper.selectPage(page, wrapper);

        Page<UserMerchantVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        
        List<UserMerchantVO> voList = page.getRecords().stream().map(merchant -> {
            UserMerchantVO vo = new UserMerchantVO();
            vo.setId(merchant.getId());
            vo.setShopName(merchant.getShopName());
            vo.setShopLogo(merchant.getShopLogo());
            vo.setAddress(merchant.getAddress());
            vo.setPhone(merchant.getPhone());
            vo.setDescription(merchant.getDescription());
            vo.setIsOpen(merchant.getIsOpen());
            vo.setRating(merchant.getRating());
            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public UserMerchantDetailVO getMerchantDetail(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);

        if (merchant == null || merchant.getStatus() == null || merchant.getStatus() != 1) {
            throw new BusinessException("商家不存在或已下线");
        }

        return new UserMerchantDetailVO(
                merchant.getId(),
                merchant.getShopName(),
                merchant.getShopLogo(),
                merchant.getAddress(),
                merchant.getPhone(),
                merchant.getDescription(),
                merchant.getRating(),
                merchant.getIsOpen() != null ? merchant.getIsOpen() : 1
        );
    }

    @Override
    public List<UserDishListVO> listMerchantDishes(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);

        if (merchant == null || merchant.getStatus() == null || merchant.getStatus() != 1) {
            throw new BusinessException("商家不存在或未通过审核");
        }
        // 打烊店铺不允许查看菜品（防止加购后绕过前端限制下单）
        if (merchant.getIsOpen() == null || merchant.getIsOpen() != 1) {
            throw new BusinessException("该商家当前已打烊，暂不支持浏览菜品");
        }

        List<Dish> dishList = dishMapper.selectList(
                new LambdaQueryWrapper<Dish>()
                        .eq(Dish::getMerchantId, merchantId)
                        .eq(Dish::getStatus, 1)
                        .orderByDesc(Dish::getCreateTime)
        );

        List<DishCategory> categories = dishCategoryMapper.selectList(
                new LambdaQueryWrapper<DishCategory>()
                        .eq(DishCategory::getMerchantId, merchantId)
        );
        Map<Long, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(DishCategory::getId, DishCategory::getName));

        return dishList.stream().map(dish -> new UserDishListVO(
                dish.getId(),
                dish.getCategoryId(),
                dish.getCategoryId() != null ? categoryMap.get(dish.getCategoryId()) : null,
                dish.getName(),
                dish.getImage(),
                dish.getPrice(),
                dish.getOriginalPrice(),
                dish.getDescription(),
                dish.getSales(),
                dish.getStock()
        )).collect(Collectors.toList());
    }
}
