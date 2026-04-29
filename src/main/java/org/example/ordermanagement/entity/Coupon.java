package org.example.ordermanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("coupon")
public class Coupon {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String type; // DISCOUNT-折扣, REDUCE-满减, CASH-代金券

    private BigDecimal value;

    @TableField("min_amount")
    private BigDecimal minAmount;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("total_count")
    private Integer totalCount;

    @TableField("remain_count")
    private Integer remainCount;

    @TableField("each_limit")
    private Integer eachLimit;

    private String status; // ACTIVE-可用, INACTIVE-禁用

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
