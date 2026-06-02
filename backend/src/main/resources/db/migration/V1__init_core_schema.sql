-- =====================================================
-- V1: 核心业务基础表
-- 说明：营销字段、退款字段、用户资料字段分别由 V2/V3/V4 追加。
-- =====================================================

CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码哈希',
    `phone` VARCHAR(32) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：USER/ADMIN/MERCHANT',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常，0禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_username` (`username`),
    UNIQUE KEY `uk_user_phone` (`phone`),
    KEY `idx_user_role_status` (`role`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `merchant` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '申请/归属用户ID',
    `shop_name` VARCHAR(100) NOT NULL COMMENT '店铺名称',
    `shop_logo` VARCHAR(500) DEFAULT NULL COMMENT '店铺Logo',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '店铺地址',
    `phone` VARCHAR(32) DEFAULT NULL COMMENT '联系电话',
    `description` VARCHAR(1000) DEFAULT NULL COMMENT '店铺描述',
    `business_license` VARCHAR(500) DEFAULT NULL COMMENT '营业执照图片',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态：0待审核，1通过，2拒绝/禁用',
    `is_open` TINYINT NOT NULL DEFAULT 1 COMMENT '营业开关：1营业，0打烊',
    `rating` DECIMAL(3,2) NOT NULL DEFAULT 5.00 COMMENT '店铺评分',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_merchant_user_id` (`user_id`),
    KEY `idx_merchant_status_open` (`status`, `is_open`),
    KEY `idx_merchant_shop_name` (`shop_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家表';

CREATE TABLE IF NOT EXISTS `dish_category` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
    `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_dish_category_merchant` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品分类表';

CREATE TABLE IF NOT EXISTS `dish` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
    `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
    `name` VARCHAR(100) NOT NULL COMMENT '菜品名称',
    `image` VARCHAR(500) DEFAULT NULL COMMENT '菜品图片',
    `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '售价',
    `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
    `description` VARCHAR(1000) DEFAULT NULL COMMENT '菜品描述',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1上架，0下架',
    `sales` INT NOT NULL DEFAULT 0 COMMENT '销量',
    `stock` INT NOT NULL DEFAULT 0 COMMENT '库存',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_dish_merchant_status` (`merchant_id`, `status`),
    KEY `idx_dish_category` (`category_id`),
    KEY `idx_dish_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品表';

CREATE TABLE IF NOT EXISTS `cart` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
    `dish_id` BIGINT NOT NULL COMMENT '菜品ID',
    `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
    `dish_tags` VARCHAR(500) DEFAULT NULL COMMENT '规格/标签',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_cart_user_merchant` (`user_id`, `merchant_id`),
    KEY `idx_cart_dish` (`dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

CREATE TABLE IF NOT EXISTS `address` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `contact_name` VARCHAR(64) NOT NULL COMMENT '联系人',
    `contact_phone` VARCHAR(32) NOT NULL COMMENT '联系电话',
    `province` VARCHAR(64) DEFAULT NULL COMMENT '省',
    `city` VARCHAR(64) DEFAULT NULL COMMENT '市',
    `district` VARCHAR(64) DEFAULT NULL COMMENT '区/县',
    `detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
    `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认：1是，0否',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_address_user_default` (`user_id`, `is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户地址表';

CREATE TABLE IF NOT EXISTS `orders` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
    `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单金额',
    `status` VARCHAR(32) NOT NULL DEFAULT 'PENDING_PAYMENT' COMMENT '订单状态',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '订单备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `address_id` BIGINT DEFAULT NULL COMMENT '地址ID',
    `contact_name` VARCHAR(64) DEFAULT NULL COMMENT '联系人快照',
    `contact_phone` VARCHAR(32) DEFAULT NULL COMMENT '联系电话快照',
    `delivery_address` VARCHAR(500) DEFAULT NULL COMMENT '配送地址快照',
    UNIQUE KEY `uk_orders_order_no` (`order_no`),
    KEY `idx_orders_user_status` (`user_id`, `status`),
    KEY `idx_orders_merchant_status` (`merchant_id`, `status`),
    KEY `idx_orders_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

CREATE TABLE IF NOT EXISTS `order_item` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `dish_id` BIGINT NOT NULL COMMENT '菜品ID',
    `dish_name` VARCHAR(100) NOT NULL COMMENT '菜品名称快照',
    `dish_image` VARCHAR(500) DEFAULT NULL COMMENT '菜品图片快照',
    `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '单价快照',
    `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
    `amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '小计金额',
    `dish_tags` VARCHAR(500) DEFAULT NULL COMMENT '规格/标签快照',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_order_item_order` (`order_id`),
    KEY `idx_order_item_dish` (`dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

CREATE TABLE IF NOT EXISTS `review` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
    `rating` TINYINT NOT NULL COMMENT '评分',
    `content` VARCHAR(1000) DEFAULT NULL COMMENT '评价内容',
    `merchant_reply` VARCHAR(1000) DEFAULT NULL COMMENT '商家回复',
    `merchant_reply_time` DATETIME DEFAULT NULL COMMENT '商家回复时间',
    `follow_up_content` VARCHAR(1000) DEFAULT NULL COMMENT '用户追评',
    `follow_up_time` DATETIME DEFAULT NULL COMMENT '追评时间',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常，0屏蔽',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_review_order` (`order_id`),
    KEY `idx_review_user` (`user_id`),
    KEY `idx_review_merchant_status` (`merchant_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';
