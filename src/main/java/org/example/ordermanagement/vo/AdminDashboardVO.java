package org.example.ordermanagement.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardVO {
    private Long userCount;
    private Long merchantCount;
    private Long dishCount;
    private Long orderCount;
    private Long paidOrderCount;
    private BigDecimal totalSalesAmount;
}