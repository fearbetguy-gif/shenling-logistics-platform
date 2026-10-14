USE wuliu;

CREATE TABLE IF NOT EXISTS order_status_sync_retry (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    order_no VARCHAR(32) NOT NULL COMMENT '订单号',
    transport_status VARCHAR(32) NOT NULL COMMENT '待同步运输状态',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    next_retry_at DATETIME NOT NULL COMMENT '下次重试时间',
    last_error VARCHAR(500) NULL COMMENT '最近一次错误',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_order_transport_status (order_no, transport_status),
    KEY idx_next_retry (next_retry_at, retry_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单状态同步补偿表';
