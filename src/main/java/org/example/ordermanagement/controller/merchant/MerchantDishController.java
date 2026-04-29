package org.example.ordermanagement.controller.merchant;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.result.Result;
import org.example.ordermanagement.dto.DishQueryDTO;
import org.example.ordermanagement.dto.DishSaveDTO;
import org.example.ordermanagement.dto.DishStatusDTO;
import org.example.ordermanagement.entity.Dish;
import org.example.ordermanagement.entity.Merchant;
import org.example.ordermanagement.service.DishService;
import org.example.ordermanagement.service.MerchantService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商家核心菜品及门店营业控制器 - 控制商家的商品上架、下架、以及店铺的基本经营状态（营业/打烊）
 */
@RestController
@RequestMapping("/api/merchant/dish")
@RequiredArgsConstructor
public class MerchantDishController {

    private final DishService dishService;
    private final MerchantService merchantService;

    /**
     * 获取当前商家的店铺列表（只有审核通过的）
     */
    @GetMapping("/merchants")
    public Result<List<Merchant>> myMerchants(Authentication authentication) {
        return Result.success(merchantService.getMyMerchants(authentication.getName()));
    }

    /**
     * 获取当前商家的所有店铺（包括待审核的）
     */
    @GetMapping("/all-merchants")
    public Result<List<Merchant>> allMyMerchants(Authentication authentication) {
        return Result.success(merchantService.getAllMyMerchants(authentication.getName()));
    }

    /**
     * 商家管理后台：分页查看自己店铺下录入的菜品（支持名字、分类筛选）
     */
    @GetMapping("/page")
    public Result<IPage<Dish>> page(Authentication authentication, @ModelAttribute DishQueryDTO queryDTO) {
        return Result.success(dishService.pageMyDishes(authentication.getName(), queryDTO));
    }

    /**
     * 商家新增或编辑单个菜品信息（包含图片、价格、起售控制）
     */
    @PostMapping("/save")
    public Result<String> save(Authentication authentication, @Valid @RequestBody DishSaveDTO dto) {
        dishService.saveDish(authentication.getName(), dto);
        return Result.successMessage("操作成功");
    }

    /**
     * 快捷调整商品状态：上架(1) / 下架(0)
     */
    @PostMapping("/status")
    public Result<String> updateStatus(Authentication authentication, @Valid @RequestBody DishStatusDTO dto) {
        dishService.updateDishStatus(authentication.getName(), dto);
        return Result.successMessage("状态更新成功");
    }

    /**
     * 更新店铺信息
     */
    @PutMapping("/update-merchant")
    public Result<String> updateMerchant(Authentication authentication, @RequestBody Merchant merchant) {
        merchantService.updateMyMerchant(authentication.getName(), merchant);
        return Result.successMessage("更新成功");
    }

    /**
     * 切换营业状态：上班 / 打烊
     * 
     * @param open true=开始营业 false=暂停营业（打烊）
     */
    @PostMapping("/{merchantId}/toggle-open")
    public Result<String> toggleOpen(Authentication authentication,
            @PathVariable Long merchantId,
            @RequestParam boolean open) {
        merchantService.toggleShopOpen(authentication.getName(), merchantId, open);
        String msg = open ? "已开始营业，欢迎顾客光临！" : "已打烊，感谢今天的辛苦！";
        return Result.successMessage(msg);
    }
}
