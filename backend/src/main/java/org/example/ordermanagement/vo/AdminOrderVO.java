package org.example.ordermanagement.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AdminOrderVO {
    private Long id;
    private String orderNo;
    private String username;
    private String shopName;
    private BigDecimal totalAmount;
    private String status;
    private String remark;
    private LocalDateTime createTime;
}