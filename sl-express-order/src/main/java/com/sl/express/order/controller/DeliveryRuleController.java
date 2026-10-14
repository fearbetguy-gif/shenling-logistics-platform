package com.sl.express.order.controller;

import com.sl.express.common.result.Result;
import com.sl.express.order.service.DeliveryRuleService;
import com.sl.express.order.vo.DeliveryRuleVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/assistant/rules")
public class DeliveryRuleController {

    private final DeliveryRuleService deliveryRuleService;
    private DeliveryRuleController deliveryRuleController;

    public DeliveryRuleController(DeliveryRuleService deliveryRuleService) {
        this.deliveryRuleService = deliveryRuleService;
    }

    @GetMapping
    public Result<List<DeliveryRuleVO>> listRules(){
        return Result.success(deliveryRuleService.listEnabledRules());
    }
}
