package com.sl.express.order.vo;

import java.util.List;

/** 规则引擎生成的配送异常建议，后续可作为大模型的可信上下文。 */
public record AssistantAdviceVO(
        String orderNo,
        boolean abnormal,
        List<DeliveryRuleVO> matchedRules,
        String summary,
        String generatedAnswer
) {
}
