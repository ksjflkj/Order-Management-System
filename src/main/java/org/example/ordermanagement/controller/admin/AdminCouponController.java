package org.example.ordermanagement.controller.admin;

import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.dto.CouponSaveDTO;
import org.example.ordermanagement.entity.Coupon;
import org.example.ordermanagement.service.CouponService;
import org.example.ordermanagement.common.result.Result;
import org.springframework.web.bind.annotation.*;



/**
 * 平台级优惠券模板管理 - 负责配置通用或指定条件可用的代金券。
 */
@RestController
@RequestMapping("/api/admin/coupon")
@RequiredArgsConstructor
public class AdminCouponController {

    private final CouponService couponService;

    /**
     * 发布新的优惠券模板
     */
    @PostMapping
    public Result<Void> create(@RequestBody CouponSaveDTO dto) {
        couponService.createCoupon(dto);
        return Result.success();
    }

    /**
     * 更新已有优惠券配置参数
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody CouponSaveDTO dto) {
        couponService.updateCoupon(id, dto);
        return Result.success();
    }

    /**
     * 下架或删除选定的优惠券模板
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return Result.success();
    }

    /**
     * 分页查询所有发放的优惠券模板列表
     */
    @GetMapping("/page")
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<Coupon>> page(org.example.ordermanagement.dto.AdminCouponQueryDTO dto) {
        return Result.success(couponService.pageAdminCoupons(dto));
    }
}
