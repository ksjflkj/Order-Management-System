-- =====================================================
-- V3: 订单表新增确认收货 / 退款相关字段
-- =====================================================

ALTER TABLE `orders`
    ADD COLUMN `refund_reason`        VARCHAR(500) NULL COMMENT '退款原因'       AFTER `delivery_address`,
    ADD COLUMN `refund_time`          DATETIME     NULL COMMENT '申请退款时间'    AFTER `refund_reason`,
    ADD COLUMN `received_time`        DATETIME     NULL COMMENT '用户确认收货时间' AFTER `refund_time`,
    ADD COLUMN `refund_reject_reason` VARCHAR(500) NULL COMMENT '商家拒绝退款原因' AFTER `received_time`;
