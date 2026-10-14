package com.sl.express.order.vo;

import java.time.LocalDateTime;

public record AssistantTrackVO(
        String status,
        String description,
        LocalDateTime createdAt
) {
}
