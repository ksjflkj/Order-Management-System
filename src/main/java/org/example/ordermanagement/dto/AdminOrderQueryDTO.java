package org.example.ordermanagement.dto;

import lombok.Data;

@Data
public class AdminOrderQueryDTO {
    private String orderNo;
    private String status;
    private Long current = 1L;
    private Long size = 10L;
}