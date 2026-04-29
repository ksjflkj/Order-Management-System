package org.example.ordermanagement.dto;

import lombok.Data;

@Data
public class AdminReviewQueryDTO {
    private String orderNo;
    private String shopName;
    private Long current = 1L;
    private Long size = 10L;
}
