package com.sl.express.order.service;

import com.sl.express.order.vo.DeliveryRuleVO;

import java.util.List;

public interface DeliveryRuleService {
    List<DeliveryRuleVO> listEnabledRules();
}
