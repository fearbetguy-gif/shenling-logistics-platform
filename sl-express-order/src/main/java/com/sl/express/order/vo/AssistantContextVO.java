package com.sl.express.order.vo;

import java.util.List;

public record AssistantContextVO(
        OrderSummaryVO order,
        List<AssistantTrackVO> tracks
) {
}
