package org.example.ordermanagement.dto;

import lombok.Data;

@Data
public class DishQueryDTO {
    private Long merchantId;
    private String name;
    private Integer status;
    private Long current = 1L;
    private Long size = 10L;
}
