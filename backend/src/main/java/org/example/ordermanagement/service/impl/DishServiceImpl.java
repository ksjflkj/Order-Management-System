package org.example.ordermanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.exception.BusinessException;
import org.example.ordermanagement.common.helper.MerchantHelper;
import org.example.ordermanagement.dto.DishQueryDTO;
import org.example.ordermanagement.dto.DishSaveDTO;
import org.example.ordermanagement.dto.DishStatusDTO;
import org.example.ordermanagement.entity.Dish;
import org.example.ordermanagement.entity.Merchant;
import org.example.ordermanagement.mapper.DishMapper;
import org.example.ordermanagement.service.DishService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {

    private final DishMapper dishMapper;
    private final MerchantHelper merchantHelper;


    @Override
    public IPage<Dish> pageMyDishes(String username, DishQueryDTO queryDTO) {
        List<Merchant> merchants = merchantHelper.getActiveMerchantsByUsername(username);
        
        // 如果指定了merchantId，则只查询该店铺的菜品
        List<Long> merchantIds;
        if (queryDTO.getMerchantId() != null) {
            // 验证商家属于该用户
            final Long mid = queryDTO.getMerchantId();
            boolean valid = merchants.stream().anyMatch(m -> m.getId().equals(mid));
            if (!valid) {
                throw new BusinessException("无权查看该店铺的菜品");
            }
            merchantIds = java.util.Collections.singletonList(queryDTO.getMerchantId());
        } else {
            merchantIds = merchants.stream().map(Merchant::getId).collect(Collectors.toList());
        }

        if (merchantIds.isEmpty()) {
            return new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        }

        Page<Dish> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());

        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Dish::getMerchantId, merchantIds);

        if (StringUtils.hasText(queryDTO.getName())) {
            wrapper.like(Dish::getName, queryDTO.getName());
        }

        if (queryDTO.getStatus() != null) {
            wrapper.eq(Dish::getStatus, queryDTO.getStatus());
        }

        wrapper.orderByDesc(Dish::getCreateTime);

        return dishMapper.selectPage(page, wrapper);
    }

    @Override
    public void saveDish(String username, DishSaveDTO dto) {
        List<Merchant> merchants = merchantHelper.getActiveMerchantsByUsername(username);
        
        // 如果没有指定商家ID，则使用第一个商家
        final Long merchantId;
        if (dto.getMerchantId() == null) {
            merchantId = merchants.get(0).getId();
        } else {
            merchantId = dto.getMerchantId();
        }
        
        // 验证商家属于该用户
        boolean validMerchant = merchants.stream().anyMatch(m -> m.getId().equals(merchantId));
        if (!validMerchant) {
            throw new BusinessException("无权对该商家进行操作");
        }

        if (dto.getId() == null) {
            Dish dish = new Dish();
            dish.setMerchantId(merchantId);
            dish.setCategoryId(dto.getCategoryId());
            dish.setName(dto.getName());
            dish.setImage(dto.getImage());
            dish.setPrice(dto.getPrice());
            dish.setOriginalPrice(dto.getOriginalPrice());
            dish.setDescription(dto.getDescription());
            dish.setStock(dto.getStock());
            dish.setSales(0);
            dish.setStatus(1);
            dishMapper.insert(dish);
            return;
        }

        Dish dish = dishMapper.selectById(dto.getId());
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }

        if (!dish.getMerchantId().equals(merchantId)) {
            throw new BusinessException("无权操作该菜品");
        }

        dish.setCategoryId(dto.getCategoryId());
        dish.setName(dto.getName());
        dish.setImage(dto.getImage());
        dish.setPrice(dto.getPrice());
        dish.setOriginalPrice(dto.getOriginalPrice());
        dish.setDescription(dto.getDescription());
        dish.setStock(dto.getStock());

        dishMapper.updateById(dish);
    }

    @Override
    public void updateDishStatus(String username, DishStatusDTO dto) {
        List<Merchant> merchants = merchantHelper.getActiveMerchantsByUsername(username);

        if (dto.getStatus() != 0 && dto.getStatus() != 1) {
            throw new BusinessException("状态非法");
        }

        Dish dish = dishMapper.selectById(dto.getId());
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }

        boolean validMerchant = merchants.stream().anyMatch(m -> m.getId().equals(dish.getMerchantId()));
        if (!validMerchant) {
            throw new BusinessException("无权操作该菜品");
        }

        dish.setStatus(dto.getStatus());
        dishMapper.updateById(dish);
    }
}
