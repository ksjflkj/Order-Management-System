package org.example.ordermanagement.controller.user;

import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.service.ActivityService;
import org.example.ordermanagement.vo.ActivityVO;
import org.example.ordermanagement.common.result.Result;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户端活动控制器 - 负责 C 端营销活动的展示及应用计算。
 * 包含：浏览首页或商家可参与的活动（如满减、新客立减）、购物车结算时的自动减免金额试算。
 */
@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    /**
     * 获取当前生效中的活动列表，可通过 merchantId 指定拉取特定店家的店铺活动和平台共用活动
     * @param merchantId 商家主键（可选）
     */
    @GetMapping("/active")
    public Result<List<ActivityVO>> getActiveActivities(
            @RequestParam(required = false) Long merchantId) {
        return Result.success(activityService.getActiveActivities(merchantId));
    }

    /**
     * 计算并匹配满减条件后的实际折扣减免金额（结算时拦截器或前端试算用）
     * @param amount 订单原始金额
     * @param merchantId 对应的商家主键（可选）
     */
    @GetMapping("/calculate")
    public Result<BigDecimal> calculateDiscount(
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) Long merchantId) {
        return Result.success(activityService.calculateDiscount(amount, merchantId));
    }
}
