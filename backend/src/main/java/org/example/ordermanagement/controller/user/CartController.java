package org.example.ordermanagement.controller.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.result.Result;
import org.example.ordermanagement.dto.CartAddDTO;
import org.example.ordermanagement.dto.CartUpdateDTO;
import org.example.ordermanagement.service.CartService;
import org.example.ordermanagement.vo.CartVO;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户购物车控制器 - 负责处理 C 端用户购物车的增删改查。
 * 包括加购、更新数量、删除商品、清空购物车等操作。
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * 将菜品加入购物车
     * @param authentication 当前登录用户的认证信息
     * @param dto 包含要加购的菜品ID、商家ID、数量、规格属性的请求体
     */
    @PostMapping("/add")
    public Result<String> add(Authentication authentication, @Valid @RequestBody CartAddDTO dto) {
        cartService.addToCart(authentication.getName(), dto);
        return Result.successMessage("加入购物车成功");
    }

    /**
     * 获取当前用户的购物车列表（按商家分组展示时使用）
     * @param authentication 当前登录用户的认证信息
     */
    @GetMapping("/list")
    public Result<List<CartVO>> list(Authentication authentication) {
        return Result.success(cartService.listCart(authentication.getName()));
    }

    /**
     * 修改购物车中某项菜品的数量（如页面加减号操作）
     * @param authentication 当前登录用户的认证信息
     * @param dto 更新数据，含购物车记录ID及新的数量
     */
    @PostMapping("/update")
    public Result<String> update(Authentication authentication, @Valid @RequestBody CartUpdateDTO dto) {
        cartService.updateCartQuantity(authentication.getName(), dto);
        return Result.successMessage("更新数量成功");
    }

    /**
     * 删除购物车中的单条商品记录
     * @param authentication 当前登录用户的认证信息
     * @param id 购物车记录ID
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(Authentication authentication, @PathVariable Long id) {
        cartService.deleteCartItem(authentication.getName(), id);
        return Result.successMessage("删除成功");
    }

    /**
     * 清空当前用户的所有购物车商品记录
     * @param authentication 当前登录用户的认证信息
     */
    @DeleteMapping("/clear")
    public Result<String> clear(Authentication authentication) {
        cartService.clearCart(authentication.getName());
        return Result.successMessage("清空购物车成功");
    }
}