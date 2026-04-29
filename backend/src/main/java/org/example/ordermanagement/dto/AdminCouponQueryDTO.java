package org.example.ordermanagement.dto;

import lombok.Data;

@Data
public class AdminCouponQueryDTO {
    private String name;
    private String type;
    private Long current = 1L;
    private Long size = 10L;
}
