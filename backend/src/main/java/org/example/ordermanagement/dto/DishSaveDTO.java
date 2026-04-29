package org.example.ordermanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DishSaveDTO {

    private Long id;

    private Long merchantId;
    
    private Long categoryId;

    @NotBlank(message = "菜品名称不能为空")
    private String name;

    private String image;

    @NotNull(message = "售价不能为空")
    private BigDecimal price;

    private BigDecimal originalPrice;

    private String description;

    @NotNull(message = "库存不能为空")
    private Integer stock;
}