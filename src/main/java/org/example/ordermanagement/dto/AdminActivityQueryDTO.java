package org.example.ordermanagement.dto;

import lombok.Data;

@Data
public class AdminActivityQueryDTO {
    private String name;
    private Long current = 1L;
    private Long size = 10L;
}
