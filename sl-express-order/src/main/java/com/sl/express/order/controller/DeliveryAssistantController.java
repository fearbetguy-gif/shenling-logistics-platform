package com.sl.express.order.controller;

import com.sl.express.common.result.Result;
import com.sl.express.order.service.DeliveryAssistantService;
import com.sl.express.order.vo.AssistantAdviceVO;
import com.sl.express.order.vo.AssistantContextVO;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/assistant")
public class DeliveryAssistantController {

    private final DeliveryAssistantService assistantService;

    public DeliveryAssistantController(DeliveryAssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @GetMapping("/orders/{orderNo}/context")
    public Result<AssistantContextVO> getContext(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable @NotBlank String orderNo) {
        return Result.success(assistantService.getOrderContext(userId, orderNo));
    }

    @GetMapping("/orders/{orderNo}/advice")
    public Result<AssistantAdviceVO> getAdvice(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable @NotBlank String orderNo) {
        return Result.success(assistantService.getAdvice(userId, orderNo));
    }
}
