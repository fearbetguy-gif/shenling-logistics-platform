USE wuliu;

CREATE TABLE IF NOT EXISTS transport_track (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    order_no VARCHAR(32) NOT NULL COMMENT '订单号',
    status VARCHAR(32) NOT NULL COMMENT '物流状态',
    description VARCHAR(255) NOT NULL COMMENT '轨迹描述',
    operator_id BIGINT NULL COMMENT '操作人ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    KEY idx_order_created (order_no, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流轨迹表';
