package org.example.ordermanagement.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class OrderDetailVO {
    private Long id;
    private String orderNo;
    private Long merchantId;
    private String shopName;
    private BigDecimal totalAmount;
    private String status;
    private String remark;
    private String contactName;
    private String contactPhone;
    private String deliveryAddress;
    private LocalDateTime createTime;
    private String refundReason;
    private LocalDateTime refundTime;
    private LocalDateTime receivedTime;
    private String refundRejectReason;
    private List<OrderItemVO> items;
}
