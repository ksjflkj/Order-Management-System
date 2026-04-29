package org.example.ordermanagement.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponSaveDTO {
    private String name;
    private String type; // DISCOUNT, REDUCE, CASH
    private BigDecimal value;
    private BigDecimal minAmount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalCount;
    private Integer eachLimit = 1;
}
