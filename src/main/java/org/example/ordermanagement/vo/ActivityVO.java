package org.example.ordermanagement.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityVO {
    private Long id;
    private String name;
    private String description;
    private String rules;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long merchantId;
    private String merchantName;
    private String status;
}
