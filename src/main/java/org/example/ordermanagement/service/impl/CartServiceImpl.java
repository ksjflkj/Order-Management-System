package org.example.ordermanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.exception.BusinessException;
import org.example.ordermanagement.common.helper.UserHelper;
import org.example.ordermanagement.dto.CartAddDTO;
import org.example.ordermanagement.dto.CartUpdateDTO;
import org.example.ordermanagement.entity.Cart;
import org.example.ordermanagement.entity.Dish;
import org.example.ordermanagement.entity.Merchant;
import org.example.ordermanagement.entity.User;
import org.example.ordermanagement.mapper.CartMapper;
import org.example.ordermanagement.mapper.DishMapper;
import org.example.ordermanagement.mapper.MerchantMapper;
import org.example.ordermanagement.service.CartService;
import org.example.ordermanagement.common.utils.PriceUtil;
import org.example.ordermanagement.vo.CartVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;
    private final UserHelper userHelper;
    private final DishMapper dishMapper;
    private final MerchantMapper merchantMapper;

    @Override
    public void addToCart(String username, CartAddDTO dto) {
        User user = userHelper.getByUsername(username);

        Dish dish = dishMapper.selectById(dto.getDishId());
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }

        if (dish.getStatus() == null || dish.getStatus() != 1) {
            throw new BusinessException("菜品已下架");
        }

        if (dish.getStock() == null || dish.getStock() < dto.getQuantity()) {
            throw new BusinessException("库存不足");
        }

        Merchant merchant = merchantMapper.selectById(dish.getMerchantId());
        if (merchant == null || merchant.getStatus() == null || merchant.getStatus() != 1) {
            throw new BusinessException("商家不存在或未营业");
        }

        // 允许购物车中存放多家商家的菜品，按商家分组展示和结算

        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, user.getId())
                .eq(Cart::getDishId, dto.getDishId());

        if (dto.getDishTags() != null) {
            queryWrapper.eq(Cart::getDishTags, dto.getDishTags());
        } else {
            queryWrapper.isNull(Cart::getDishTags);
        }

        Cart existingCart = cartMapper.selectOne(queryWrapper);

        if (existingCart != null) {
            int newQuantity = existingCart.getQuantity() + dto.getQuantity();
            if (newQuantity > dish.getStock()) {
                throw new BusinessException("加入后数量超过库存");
            }
            existingCart.setQuantity(newQuantity);
            cartMapper.updateById(existingCart);
            return;
        }

        Cart cart = new Cart();
        cart.setUserId(user.getId());
        cart.setMerchantId(dish.getMerchantId());
        cart.setDishId(dish.getId());
        cart.setQuantity(dto.getQuantity());
        cart.setDishTags(dto.getDishTags());

        cartMapper.insert(cart);
    }

    @Override
    public List<CartVO> listCart(String username) {
        User user = userHelper.getByUsername(username);

        List<Cart> cartList = cartMapper.selectList(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, user.getId())
                        .orderByDesc(Cart::getCreateTime)
        );

        return cartList.stream().map(cart -> {
            Dish dish = dishMapper.selectById(cart.getDishId());
            Merchant merchant = merchantMapper.selectById(cart.getMerchantId());

            if (dish == null || merchant == null) {
                return null;
            }

            return new CartVO(
                    cart.getId(),
                    dish.getId(),
                    merchant.getId(),
                    dish.getName(),
                    dish.getImage(),
                    PriceUtil.calculateFinalPrice(dish.getPrice(), cart.getDishTags()),
                    cart.getQuantity(),
                    dish.getStock(),
                    merchant.getShopName(),
                    cart.getDishTags()
            );
        }).filter(item -> item != null).collect(Collectors.toList());
    }

    @Override
    public void updateCartQuantity(String username, CartUpdateDTO dto) {
        User user = userHelper.getByUsername(username);

        Cart cart = cartMapper.selectById(dto.getId());
        if (cart == null) {
            throw new BusinessException("购物车商品不存在");
        }

        if (!cart.getUserId().equals(user.getId())) {
            throw new BusinessException("无权操作该购物车商品");
        }

        Dish dish = dishMapper.selectById(cart.getDishId());
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }

        if (dto.getQuantity() > dish.getStock()) {
            throw new BusinessException("数量不能超过库存");
        }

        cart.setQuantity(dto.getQuantity());
        cartMapper.updateById(cart);
    }

    @Override
    public void deleteCartItem(String username, Long id) {
        User user = userHelper.getByUsername(username);

        Cart cart = cartMapper.selectById(id);
        if (cart == null) {
            throw new BusinessException("购物车商品不存在");
        }

        if (!cart.getUserId().equals(user.getId())) {
            throw new BusinessException("无权删除该购物车商品");
        }

        cartMapper.deleteById(id);
    }

    @Override
    public void clearCart(String username) {
        User user = userHelper.getByUsername(username);

        cartMapper.delete(
                new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, user.getId())
        );
    }
}
