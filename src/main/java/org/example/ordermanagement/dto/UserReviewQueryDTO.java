package org.example.ordermanagement.dto;

import lombok.Data;

@Data
public class UserReviewQueryDTO {
    private String shopName;
    private Integer current = 1;
    private Integer size = 10;
}
