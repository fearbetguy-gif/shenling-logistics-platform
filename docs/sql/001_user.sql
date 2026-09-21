CREATE DATABASE IF NOT EXISTS shenling_logistics DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE shenling_logistics;

CREATE TABLE IF NOT EXISTS `user` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户主键',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    password VARCHAR(100) NOT NULL COMMENT 'BCrypt密码',
    nickname VARCHAR(32) NOT NULL COMMENT '昵称',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1正常 0禁用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_phone (phone),
    KEY idx_user_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
