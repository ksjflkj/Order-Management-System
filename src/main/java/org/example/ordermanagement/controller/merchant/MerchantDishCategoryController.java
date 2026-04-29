package org.example.ordermanagement.controller.merchant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.result.Result;
import org.example.ordermanagement.entity.DishCategory;
import org.example.ordermanagement.entity.Merchant;
import org.example.ordermanagement.service.DishCategoryService;
import org.example.ordermanagement.service.MerchantService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商家菜单分类控制器 - 负责管理商家拥有的菜品分类（如：主食、小吃、酒水等）。
 */
@RestController
@RequestMapping("/api/merchant/dish-category")
@RequiredArgsConstructor
public class MerchantDishCategoryController {

    private final DishCategoryService dishCategoryService;
    private final MerchantService merchantService;

    private boolean isOwner(String username, Long merchantId) {
        if (merchantId == null) return false;
        List<Merchant> merchants = merchantService.getMyMerchants(username);
        return merchants.stream().anyMatch(m -> m.getId().equals(merchantId));
    }

    /**
     * 获取指定商家的菜品分类列表（需要校验操作者是否为其店主）
     */
    @GetMapping("/list")
    public Result<List<DishCategory>> list(Authentication authentication, @RequestParam Long merchantId) {
        if (!isOwner(authentication.getName(), merchantId)) {
            return Result.fail("没有权限");
        }
        LambdaQueryWrapper<DishCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishCategory::getMerchantId, merchantId)
                .orderByDesc(DishCategory::getSortOrder)
                .orderByDesc(DishCategory::getCreateTime);
        return Result.success(dishCategoryService.list(queryWrapper));
    }

    /**
     * 新增或更新菜品分类名称、排序值
     */
    @PostMapping("/save")
    public Result<String> save(Authentication authentication, @RequestBody DishCategory category) {
        if (!isOwner(authentication.getName(), category.getMerchantId())) {
            return Result.fail("没有权限或未指定店铺");
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        dishCategoryService.saveOrUpdate(category);
        return Result.successMessage("操作成功");
    }

    /**
     * 删除指定的分类（如果有菜品绑定可先在此拦截，当前逻辑支持强删）
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(Authentication authentication, @PathVariable Long id) {
        DishCategory category = dishCategoryService.getById(id);
        if (category == null) {
            return Result.fail("分类不存在");
        }
        if (!isOwner(authentication.getName(), category.getMerchantId())) {
            return Result.fail("没有权限");
        }
        // 可加判断，如果该分类下有菜品则拒绝删除
        dishCategoryService.removeById(id);
        return Result.successMessage("删除成功");
    }
}
