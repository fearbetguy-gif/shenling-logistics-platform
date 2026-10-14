package com.sl.express.order.service;

import com.sl.express.order.vo.AssistantContextVO;
import com.sl.express.order.vo.DeliveryRuleVO;

import java.util.List;

public interface AiAdviceService {

    /** 未启用、未配置密钥或调用失败时返回空字符串，由规则建议兜底。 */
    String generate(AssistantContextVO context, List<DeliveryRuleVO> matchedRules);
}
