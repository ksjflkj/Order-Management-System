package org.example.ordermanagement.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.result.Result;
import org.example.ordermanagement.dto.UserMerchantQueryDTO;

import org.example.ordermanagement.vo.UserDishListVO;
import org.example.ordermanagement.vo.UserMerchantDetailVO;
import org.example.ordermanagement.service.UserBrowseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端"大厅与浏览"控制器 - 负责展示平台商家的信息，属于 C 端非必须登录也可操作的模块（或部分必须登录）。
 * 包括：查询商家分页列表、展示指定商家的详情页、获取某一商家的全部菜品与分类列表。
 */
@RestController
@RequestMapping("/api/user/merchant")
@RequiredArgsConstructor
public class UserBrowseController {

    private final UserBrowseService userBrowseService;

    /**
     * 获取外卖大厅中的商家分页列表
     * @param queryDTO 包含搜索关键字（如店铺名称）及分页参数
     */
    @GetMapping("/page")
    public Result<IPage<org.example.ordermanagement.vo.UserMerchantVO>> page(UserMerchantQueryDTO queryDTO) {
        return Result.success(userBrowseService.pageMerchants(queryDTO));
    }

    /**
     * 进入特定店铺，查看该商家的详细资料（含店铺评分、环境介绍、位置、营业状态等）
     */
    @GetMapping("/{id}")
    public Result<UserMerchantDetailVO> detail(@PathVariable("id") Long id) {
        return Result.success(userBrowseService.getMerchantDetail(id));
    }

    /**
     * 在商家店铺页查询并展示他当前售卖的所有菜品（按菜品分类平铺展示）
     */
    @GetMapping("/{id}/dish/list")
    public Result<List<UserDishListVO>> dishList(@PathVariable("id") Long id) {
        return Result.success(userBrowseService.listMerchantDishes(id));
    }
}
