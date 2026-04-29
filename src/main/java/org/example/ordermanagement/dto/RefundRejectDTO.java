package org.example.ordermanagement.dto;

import lombok.Data;

@Data
public class RefundRejectDTO {
    private String rejectReason; // 商家拒绝退款理由（可选）
}
