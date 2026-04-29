-- 优惠券表
CREATE TABLE IF NOT EXISTS coupon (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '优惠券名称',
    type VARCHAR(20) NOT NULL COMMENT '类型：DISCOUNT-折扣, REDUCE-满减, CASH-代金券',
    value DECIMAL(10,2) NOT NULL COMMENT '折扣值/金额',
    min_amount DECIMAL(10,2) DEFAULT 0 COMMENT '最低消费金额',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    total_count INT DEFAULT 0 COMMENT '发放总数，0不限',
    remain_count INT DEFAULT 0 COMMENT '剩余数量',
    each_limit INT DEFAULT 1 COMMENT '每人限领数量',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-可用, INACTIVE-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 用户优惠券表
CREATE TABLE IF NOT EXISTS coupon_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    coupon_id BIGINT NOT NULL COMMENT '优惠券ID',
    status VARCHAR(20) DEFAULT 'UNUSED' COMMENT '状态：UNUSED-未使用, USED-已使用, EXPIRED-已过期',
    receive_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    use_time DATETIME COMMENT '使用时间',
    order_id BIGINT COMMENT '使用的订单ID',
    FOREIGN KEY (coupon_id) REFERENCES coupon(id)
);

-- 满减活动表
CREATE TABLE IF NOT EXISTS activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '活动名称',
    description VARCHAR(500) COMMENT '活动描述',
    rules JSON NOT NULL COMMENT '规则：满减阶梯 [{"threshold": 100, "reduce": 10}]',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    merchant_id BIGINT COMMENT '商户ID，NULL表示平台活动',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-可用, INACTIVE-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 订单表添加优惠相关字段
ALTER TABLE orders ADD COLUMN original_amount DECIMAL(10,2) DEFAULT 0 COMMENT '订单原价';
ALTER TABLE orders ADD COLUMN discount_amount DECIMAL(10,2) DEFAULT 0 COMMENT '优惠金额';
