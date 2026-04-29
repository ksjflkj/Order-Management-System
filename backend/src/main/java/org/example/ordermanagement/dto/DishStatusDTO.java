package org.example.ordermanagement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DishStatusDTO {

    @NotNull(message = "菜品ID不能为空")
    private Long id;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
