package org.example.ordermanagement.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CartVO {
    private Long id;
    private Long dishId;
    private Long merchantId;
    private String dishName;
    private String dishImage;
    private BigDecimal price;
    private Integer quantity;
    private Integer stock;
    private String shopName;
    private String dishTags;
}