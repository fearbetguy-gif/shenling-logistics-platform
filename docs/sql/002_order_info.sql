CREATE DATABASE IF NOT EXISTS shenling_logistics DEFAULT CHARACTER SET utf8mb4;
USE shenling_logistics;

CREATE TABLE IF NOT EXISTS order_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    order_no VARCHAR(32) NOT NULL COMMENT '业务订单号',
    user_id BIGINT NOT NULL COMMENT '下单用户ID',
    sender_name VARCHAR(32) NOT NULL COMMENT '寄件人姓名',
    sender_phone VARCHAR(20) NOT NULL COMMENT '寄件人手机号',
    sender_address VARCHAR(255) NOT NULL COMMENT '寄件地址',
    receiver_name VARCHAR(32) NOT NULL COMMENT '收件人姓名',
    receiver_phone VARCHAR(20) NOT NULL COMMENT '收件人手机号',
    receiver_address VARCHAR(255) NOT NULL COMMENT '收件地址',
    goods_type VARCHAR(32) NOT NULL DEFAULT '普通物品' COMMENT '物品类型',
    freight_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '运费',
    status VARCHAR(32) NOT NULL COMMENT '订单状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user_status_created (user_id, status, created_at),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流订单表';
