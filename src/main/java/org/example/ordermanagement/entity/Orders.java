package org.example.ordermanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Orders {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;
    private Long userId;
    private Long merchantId;
    private BigDecimal totalAmount;
    private BigDecimal originalAmount; // 订单原价
    private BigDecimal discountAmount; // 优惠金额
    private String status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long addressId;
    private String contactName;
    private String contactPhone;
    private String deliveryAddress;
    private String refundReason;           // 退款原因
    private LocalDateTime refundTime;      // 申请退款时间
    private LocalDateTime receivedTime;    // 用户确认收货时间
    private String refundRejectReason;     // 商家拒绝退款原因
}