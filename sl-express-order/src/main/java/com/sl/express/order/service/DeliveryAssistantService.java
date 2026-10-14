package com.sl.express.order.service;

import com.sl.express.order.vo.AssistantContextVO;
import com.sl.express.order.vo.AssistantAdviceVO;

public interface DeliveryAssistantService {

    AssistantContextVO getOrderContext(Long userId, String orderNo);

    AssistantAdviceVO getAdvice(Long userId, String orderNo);
}
