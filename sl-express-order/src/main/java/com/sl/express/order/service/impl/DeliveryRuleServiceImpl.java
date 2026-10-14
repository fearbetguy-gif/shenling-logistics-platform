package com.sl.express.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sl.express.order.entity.DeliveryExceptionRule;
import com.sl.express.order.mapper.DeliveryExceptionRuleMapper;
import com.sl.express.order.service.DeliveryRuleService;
import com.sl.express.order.vo.DeliveryRuleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeliveryRuleServiceImpl implements DeliveryRuleService {

    @Autowired
    private DeliveryExceptionRuleMapper ruleMapper;


    @Override
    public List<DeliveryRuleVO> listEnabledRules() {
        LambdaQueryWrapper<DeliveryExceptionRule> wrapper =
                new LambdaQueryWrapper<DeliveryExceptionRule>()
                        .eq(DeliveryExceptionRule::getEnabled,1)
                        .orderByAsc(DeliveryExceptionRule::getId);
        return ruleMapper.selectList(wrapper)
                .stream()
                .map(rule -> new DeliveryRuleVO(rule.getExceptionType(), rule.getRuleName(), rule.getConditionText(), rule.getSuggestionText()))
                .toList();
    }
}
