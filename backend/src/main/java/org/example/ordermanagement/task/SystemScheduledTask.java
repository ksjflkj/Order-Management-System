package org.example.ordermanagement.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ordermanagement.entity.*;
import org.example.ordermanagement.mapper.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemScheduledTask {

    private final OrdersMapper ordersMapper;
    private final OrderItemMapper orderItemMapper;
    private final DishMapper dishMapper;
    private final CouponUserMapper couponUserMapper;
    private final CouponMapper couponMapper;
    private final ActivityMapper activityMapper;

    /**
     * 1. 订单超时自动取消 (每分钟检查)
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void cancelTimeoutOrders() {
        LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(15);
        
        List<Orders> timeoutOrders = ordersMapper.selectList(
                new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getStatus, "PENDING_PAYMENT")
                        .le(Orders::getCreateTime, timeoutTime)
        );

        if (timeoutOrders == null || timeoutOrders.isEmpty()) {
            return;
        }

        log.info("发现 {} 个超时未支付订单，自动取消中...", timeoutOrders.size());

        for (Orders order : timeoutOrders) {
            order.setStatus("CANCELLED");
            order.setRemark((order.getRemark() == null ? "" : order.getRemark()) + " [系统：超时未付款自动取消]");
            ordersMapper.updateById(order);

            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId())
            );
            
            for (OrderItem item : items) {
                Dish dish = dishMapper.selectById(item.getDishId());
                if (dish != null) {
                    dish.setStock((dish.getStock() == null ? 0 : dish.getStock()) + item.getQuantity());
                    int sales = dish.getSales() == null ? 0 : dish.getSales();
                    dish.setSales(Math.max(0, sales - item.getQuantity()));
                    dishMapper.updateById(dish);
                }
            }

            List<CouponUser> couponUsers = couponUserMapper.selectList(
                    new LambdaQueryWrapper<CouponUser>()
                            .eq(CouponUser::getOrderId, order.getId())
                            .eq(CouponUser::getStatus, "USED")
            );
            
            for (CouponUser couponUser : couponUsers) {
                couponUser.setStatus("UNUSED");
                couponUser.setUseTime(null);
                couponUser.setOrderId(null);
                couponUserMapper.updateById(couponUser);
            }
            
            log.info("订单已自动取消，并在后台恢复了库存与优惠券: {}", order.getOrderNo());
        }
    }

    /**
     * 2. 优惠券过期处理 (每分钟检查)
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void expireCoupons() {
        LocalDateTime now = LocalDateTime.now();

        // 将已经过期的总优惠券库记录设为 INACTIVE/EXPIRED（这里以 INACTIVE 为例停止继续领取）
        List<Coupon> expiredCoupons = couponMapper.selectList(
                new LambdaQueryWrapper<Coupon>()
                        .eq(Coupon::getStatus, "ACTIVE")
                        .lt(Coupon::getEndTime, now)
        );

        if (expiredCoupons != null && !expiredCoupons.isEmpty()) {
            List<Long> expiredIds = expiredCoupons.stream().map(Coupon::getId).collect(Collectors.toList());
            
            // 更新优惠券库状态
            couponMapper.update(null, new LambdaUpdateWrapper<Coupon>()
                    .set(Coupon::getStatus, "INACTIVE")
                    .in(Coupon::getId, expiredIds));
            log.info("系统：自动下架了 {} 种过期的优惠券包", expiredIds.size());

            // 联动将用户手里领取但未使用、且对应这批 ID 的优惠券标记为已过期
            couponUserMapper.update(null, new LambdaUpdateWrapper<CouponUser>()
                    .set(CouponUser::getStatus, "EXPIRED")
                    .eq(CouponUser::getStatus, "UNUSED")
                    .in(CouponUser::getCouponId, expiredIds));
        }
    }

    /**
     * 3. 活动过期处理 (每分钟检查)
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void expireActivities() {
        LocalDateTime now = LocalDateTime.now();

        List<Activity> expiredActivities = activityMapper.selectList(
                new LambdaQueryWrapper<Activity>()
                        .eq(Activity::getStatus, "ACTIVE")
                        .lt(Activity::getEndTime, now)
        );

        if (expiredActivities != null && !expiredActivities.isEmpty()) {
            List<Long> expiredIds = expiredActivities.stream().map(Activity::getId).collect(Collectors.toList());
            
            activityMapper.update(null, new LambdaUpdateWrapper<Activity>()
                    .set(Activity::getStatus, "INACTIVE")
                    .in(Activity::getId, expiredIds));
                    
            log.info("系统：自动结束了 {} 个已过期的打折满减活动", expiredIds.size());
        }
    }
}
