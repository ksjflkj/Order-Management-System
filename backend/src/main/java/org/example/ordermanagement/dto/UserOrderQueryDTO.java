package org.example.ordermanagement.dto;

import lombok.Data;

@Data
public class UserOrderQueryDTO {
    private String status;
    private Integer current = 1;
    private Integer size = 10;
}
