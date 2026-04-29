package org.example.ordermanagement.dto;

import lombok.Data;

@Data
public class MerchantQueryDTO {
    private String shopName;
    private Integer status;
    private Long current = 1L;
    private Long size = 10L;
}