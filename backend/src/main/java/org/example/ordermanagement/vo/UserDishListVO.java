package org.example.ordermanagement.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class UserDishListVO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String image;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String description;
    private Integer sales;
    private Integer stock;
}