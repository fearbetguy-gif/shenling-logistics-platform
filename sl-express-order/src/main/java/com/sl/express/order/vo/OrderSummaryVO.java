package com.sl.express.order.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryVO(
        String orderNo,
        String senderName,
        String receiverName,
        String senderAddress,
        String receiverAddress,
        String goodsType,
        BigDecimal freightAmount,
        String status,
        LocalDateTime createdAt
) {
}
