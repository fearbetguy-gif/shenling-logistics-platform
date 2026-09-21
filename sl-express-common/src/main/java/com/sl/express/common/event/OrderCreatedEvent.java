package com.sl.express.common.event;

import java.time.LocalDateTime;

/** 订单服务发布、运输服务消费的订单创建事件。 */
public record OrderCreatedEvent(
        String orderNo,
        Long userId,
        String senderAddress,
        String receiverAddress,
        LocalDateTime createdAt
) {
}
