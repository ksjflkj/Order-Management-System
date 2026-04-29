package org.example.ordermanagement.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivitySaveDTO {
    private String name;
    private String description;
    private String rules; // JSON
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long merchantId;
}
