package org.example.ordermanagement.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class AdminOrderDetailVO {
    private Long id;
    private String orderNo;
    private String username;
    private String shopName;
    private BigDecimal totalAmount;
    private String status;
    private String remark;
    private String contactName;
    private String contactPhone;
    private String deliveryAddress;
    private String refundReason;
    private LocalDateTime refundTime;
    private String refundRejectReason;
    private LocalDateTime createTime;
    private List<OrderItemVO> items;
}