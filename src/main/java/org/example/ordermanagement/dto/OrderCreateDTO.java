package org.example.ordermanagement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderCreateDTO {

    @NotNull(message = "地址ID不能为空")
    private Long addressId;

    @NotNull(message = "商家ID不能为空")
    private Long merchantId;

    private String remark;

    private Long couponId; // 使用的优惠券ID
}