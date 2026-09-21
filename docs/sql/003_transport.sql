CREATE DATABASE IF NOT EXISTS shenling_logistics DEFAULT CHARACTER SET utf8mb4;
USE shenling_logistics;

CREATE TABLE IF NOT EXISTS transport_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    order_no VARCHAR(32) NOT NULL COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    sender_address VARCHAR(255) NOT NULL COMMENT '寄件地址',
    receiver_address VARCHAR(255) NOT NULL COMMENT '收件地址',
    courier_id BIGINT NULL COMMENT '快递员ID',
    status VARCHAR(32) NOT NULL COMMENT '运输任务状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_transport_order_no (order_no),
    KEY idx_courier_status (courier_id, status),
    KEY idx_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运输任务表';

CREATE TABLE IF NOT EXISTS mq_consume_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    message_key VARCHAR(64) NOT NULL COMMENT '业务消息唯一键，此处使用订单号',
    message_type VARCHAR(64) NOT NULL COMMENT '消息类型',
    consumed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消费时间',
    UNIQUE KEY uk_message_type_key (message_type, message_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息消费记录表';
