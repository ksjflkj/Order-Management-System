package org.example.ordermanagement.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class OrderVO {
    private Long id;
    private String orderNo;
    private Long merchantId;
    private String shopName;
    private BigDecimal totalAmount;
    private BigDecimal originalAmount;
    private BigDecimal discountAmount;
    private String status;
    private String remark;
    private String contactName;
    private String contactPhone;
    private String deliveryAddress;
    private LocalDateTime createTime;
}