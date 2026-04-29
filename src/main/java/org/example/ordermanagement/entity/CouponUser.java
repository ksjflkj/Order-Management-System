package org.example.ordermanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("coupon_user")
public class CouponUser {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("coupon_id")
    private Long couponId;

    private String status; // UNUSED-未使用, USED-已使用, EXPIRED-已过期

    @TableField("receive_time")
    private LocalDateTime receiveTime;

    @TableField("use_time")
    private LocalDateTime useTime;

    @TableField("order_id")
    private Long orderId;

    @TableField(exist = false)
    private Coupon coupon;
}
