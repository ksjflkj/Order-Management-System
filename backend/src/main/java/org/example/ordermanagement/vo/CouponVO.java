package org.example.ordermanagement.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponVO {
    private Long id;
    private String name;
    private String type;
    private BigDecimal value;
    private BigDecimal minAmount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer remainCount;
    private Integer eachLimit;
    private String status;
    private Boolean received; // 当前用户是否已领取
    private Long ownedCount; // 当前用户已领取数量
}
