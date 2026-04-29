package org.example.ordermanagement.controller.user;

import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.service.CouponService;
import org.example.ordermanagement.service.UserService;
import org.example.ordermanagement.vo.CouponVO;
import org.example.ordermanagement.common.result.Result;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户优惠券控制器 - 负责 C 端领券大厅与个人卡包管理。
 * 主要包含：浏览可领优惠券、一键领取、查看我在各个商家处拥有的卡包。
 */
@RestController
@RequestMapping("/api/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;
    private final UserService userService;

    /**
     * 获取当前用户可领取的公开优惠券列表（如领券中心）
     */
    @GetMapping("/available")
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<CouponVO>> getAvailableCoupons(Authentication authentication, org.example.ordermanagement.dto.UserCouponQueryDTO dto) {
        Long userId = userService.getIdByUsername(authentication.getName());
        return Result.success(couponService.pageAvailableCoupons(userId, dto));
    }

    /**
     * 用户点击领取指定的优惠券
     * @param id 领取的全局优惠券模板主键
     */
    @PostMapping("/receive/{id}")
    public Result<Void> receive(@PathVariable Long id, Authentication authentication) {
        Long userId = userService.getIdByUsername(authentication.getName());
        couponService.receiveCoupon(userId, id);
        return Result.success();
    }

    /**
     * 获取当前用户"我的卡包"里的优惠券列表（下单时也可用此接口筛选满足门槛的券）
     */
    @GetMapping("/my")
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<CouponVO>> getMyCoupons(
            Authentication authentication,
            org.example.ordermanagement.dto.UserCouponQueryDTO dto) {
        Long userId = userService.getIdByUsername(authentication.getName());
        return Result.success(couponService.pageMyCoupons(userId, dto));
    }
}
