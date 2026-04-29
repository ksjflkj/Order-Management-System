package org.example.ordermanagement.dto;

import lombok.Data;

/**
 * 平台仲裁裁决 DTO
 * 管理员对退款纠纷作出最终裁决时使用
 */
@Data
public class ArbitrationDTO {

    /**
     * true  = 平台支持退款 → 订单状态流转至 REFUNDED
     * false = 平台驳回申诉 → 订单状态流转至 ARBITRATION_REJECTED
     */
    private boolean approve;

    /**
     * 管理员裁决说明（可选），会写入 refundRejectReason 字段供用户查看
     */
    private String note;
}
