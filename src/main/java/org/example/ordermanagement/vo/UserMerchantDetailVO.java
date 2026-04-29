package org.example.ordermanagement.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class UserMerchantDetailVO {
    private Long id;
    private String shopName;
    private String shopLogo;
    private String address;
    private String phone;
    private String description;
    private BigDecimal rating;
    private Integer isOpen;  // 1=营业中 0=打烊
}