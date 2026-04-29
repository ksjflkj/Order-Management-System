package org.example.ordermanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.ordermanagement.dto.CouponSaveDTO;
import org.example.ordermanagement.entity.Coupon;
import org.example.ordermanagement.entity.CouponUser;
import org.example.ordermanagement.vo.CouponVO;

import java.util.List;

public interface CouponService extends IService<Coupon> {
    // 管理员
    void createCoupon(CouponSaveDTO dto);
    void updateCoupon(Long id, CouponSaveDTO dto);
    void deleteCoupon(Long id);
    com.baomidou.mybatisplus.core.metadata.IPage<Coupon> pageAdminCoupons(org.example.ordermanagement.dto.AdminCouponQueryDTO dto);

    // 用户
    com.baomidou.mybatisplus.core.metadata.IPage<CouponVO> pageAvailableCoupons(Long userId, org.example.ordermanagement.dto.UserCouponQueryDTO dto);
    void receiveCoupon(Long userId, Long couponId);
    com.baomidou.mybatisplus.core.metadata.IPage<CouponVO> pageMyCoupons(Long userId, org.example.ordermanagement.dto.UserCouponQueryDTO dto);
}
