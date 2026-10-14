CREATE DATABASE IF NOT EXISTS wuliu DEFAULT CHARACTER SET utf8mb4;
USE wuliu;

CREATE TABLE IF NOT EXISTS route_node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    node_code VARCHAR(64) NOT NULL COMMENT '节点编码',
    node_name VARCHAR(128) NOT NULL COMMENT '节点名称',
    node_type VARCHAR(32) NOT NULL COMMENT '节点类型：WAREHOUSE/HUB/STATION/CUSTOMER',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_route_node_code (node_code),
    KEY idx_route_node_type (node_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流路线节点表';

CREATE TABLE IF NOT EXISTS route_edge (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    from_node_code VARCHAR(64) NOT NULL COMMENT '起点节点编码',
    to_node_code VARCHAR(64) NOT NULL COMMENT '终点节点编码',
    distance_km DECIMAL(10,2) NOT NULL COMMENT '距离，单位：公里',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_route_edge (from_node_code, to_node_code),
    KEY idx_route_edge_from (from_node_code),
    KEY idx_route_edge_to (to_node_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流路线边表';

INSERT INTO route_node (node_code, node_name, node_type)
VALUES
    ('WAREHOUSE', '北京仓库', 'WAREHOUSE'),
    ('BEIJING_HUB', '北京分拨中心', 'HUB'),
    ('LANGFANG_HUB', '廊坊分拨中心', 'HUB'),
    ('LANGFANG_STATION', '廊坊配送站', 'STATION'),
    ('CUSTOMER', '客户地址', 'CUSTOMER')
ON DUPLICATE KEY UPDATE
    node_name = VALUES(node_name),
    node_type = VALUES(node_type);

INSERT INTO route_edge (from_node_code, to_node_code, distance_km)
VALUES
    ('WAREHOUSE', 'BEIJING_HUB', 10.00),
    ('WAREHOUSE', 'LANGFANG_HUB', 45.00),
    ('BEIJING_HUB', 'LANGFANG_HUB', 15.00),
    ('LANGFANG_HUB', 'LANGFANG_STATION', 8.00),
    ('LANGFANG_STATION', 'CUSTOMER', 5.00)
ON DUPLICATE KEY UPDATE
    distance_km = VALUES(distance_km);
