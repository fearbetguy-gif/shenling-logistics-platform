package com.sl.express.order.controller;

import com.sl.express.common.result.Result;
import com.sl.express.order.service.OrderQueryService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/internal/orders")
public class InternalOrderController {
    private final OrderQueryService orderQueryService;

    public InternalOrderController(OrderQueryService orderQueryService) {
        this.orderQueryService = orderQueryService;
    }

    @PostMapping("/{orderNo}/status")
    public Result<Void> updateStatus(@PathVariable String orderNo,
                                     @RequestBody Map<String, String> request) {
        orderQueryService.updateStatusFromTransport(orderNo, request.get("status"));
        return Result.success();
    }
}
